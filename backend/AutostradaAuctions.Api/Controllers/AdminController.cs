using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using AutostradaAuctions.Api.Data;
using AutostradaAuctions.Api.Models;
using Microsoft.AspNetCore.Authorization;
using System.ComponentModel.DataAnnotations;
using BCrypt.Net;

namespace AutostradaAuctions.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize(Roles = "Admin")]
    public class AdminController : ControllerBase
    {
        private readonly AuctionDbContext _context;
        private readonly ILogger<AdminController> _logger;

        public AdminController(AuctionDbContext context, ILogger<AdminController> logger)
        {
            _context = context;
            _logger = logger;
        }

        // GET: api/admin/users
        [HttpGet("users")]
        public async Task<ActionResult<IEnumerable<AdminUserDto>>> GetAllUsers()
        {
            var users = await _context.Users
                .Select(u => new AdminUserDto
                {
                    Id = u.Id,
                    FirstName = u.FirstName,
                    LastName = u.LastName,
                    Email = u.Email,
                    Username = u.Username,
                    Role = u.Role.ToString(),
                    IsEmailVerified = u.IsEmailVerified,
                    CreatedAt = u.CreatedAt
                })
                .OrderBy(u => u.Id)
                .ToListAsync();

            return Ok(users);
        }

        // GET: api/admin/users/5
        [HttpGet("users/{id}")]
        public async Task<ActionResult<AdminUserDetailDto>> GetUser(int id)
        {
            var user = await _context.Users
                .Include(u => u.CreatedAuctions)
                    .ThenInclude(a => a.Vehicle)
                .Include(u => u.Bids)
                    .ThenInclude(b => b.Auction)
                        .ThenInclude(a => a.Vehicle)
                .FirstOrDefaultAsync(u => u.Id == id);

            if (user == null)
            {
                return NotFound();
            }

            var userDetail = new AdminUserDetailDto
            {
                Id = user.Id,
                FirstName = user.FirstName,
                LastName = user.LastName,
                Email = user.Email,
                Username = user.Username,
                Role = user.Role.ToString(),
                IsEmailVerified = user.IsEmailVerified,
                CreatedAt = user.CreatedAt,
                CreatedAuctions = user.CreatedAuctions.Select(a => new AuctionSummaryDto
                {
                    Id = a.Id,
                    Title = a.Title,
                    CurrentBid = a.CurrentBid,
                    EndTime = a.EndTime,
                    Status = a.Status.ToString(),
                    VehicleMake = a.Vehicle.Make,
                    VehicleModel = a.Vehicle.Model,
                    VehicleYear = a.Vehicle.Year
                }).ToList(),
                Bids = user.Bids.Select(b => new UserBidSummaryDto
                {
                    Id = b.Id,
                    Amount = b.Amount,
                    Timestamp = b.Timestamp,
                    AuctionTitle = b.Auction.Title,
                    VehicleMake = b.Auction.Vehicle.Make,
                    VehicleModel = b.Auction.Vehicle.Model
                }).OrderByDescending(b => b.Timestamp).Take(10).ToList()
            };

            return Ok(userDetail);
        }

        // PUT: api/admin/users/5
        [HttpPut("users/{id}")]
        public async Task<IActionResult> UpdateUser(int id, UpdateUserRequest request)
        {
            var user = await _context.Users.FindAsync(id);
            if (user == null)
            {
                return NotFound();
            }

            // Update fields
            user.FirstName = request.FirstName;
            user.LastName = request.LastName;
            user.Email = request.Email;
            user.Username = request.Username;
            
            if (Enum.TryParse<UserRole>(request.Role, out var role))
            {
                user.Role = role;
            }
            
            user.IsEmailVerified = request.IsEmailVerified;

            // Update password if provided
            if (!string.IsNullOrEmpty(request.NewPassword))
            {
                user.PasswordHash = BCrypt.Net.BCrypt.HashPassword(request.NewPassword);
            }

            try
            {
                await _context.SaveChangesAsync();
                return Ok(new { message = "User updated successfully" });
            }
            catch (DbUpdateException ex)
            {
                _logger.LogError(ex, "Error updating user");
                return BadRequest("Error updating user. Email or username may already exist.");
            }
        }

        // DELETE: api/admin/users/5
        [HttpDelete("users/{id}")]
        public async Task<IActionResult> DeleteUser(int id)
        {
            var user = await _context.Users
                .Include(u => u.CreatedAuctions)
                .Include(u => u.Bids)
                .FirstOrDefaultAsync(u => u.Id == id);

            if (user == null)
            {
                return NotFound();
            }

            // Prevent deleting the last admin
            if (user.Role == UserRole.Admin)
            {
                var adminCount = await _context.Users.CountAsync(u => u.Role == UserRole.Admin);
                if (adminCount <= 1)
                {
                    return BadRequest("Cannot delete the last admin user");
                }
            }

            // Check if user has active auctions
            var hasActiveAuctions = user.CreatedAuctions.Any(a => 
                a.Status == AuctionStatus.Active || 
                a.Status == AuctionStatus.Scheduled || 
                a.Status == AuctionStatus.PendingApproval);

            if (hasActiveAuctions)
            {
                return BadRequest("Cannot delete user with active auctions. Please end or cancel their auctions first.");
            }

            // For ended auctions, we'll keep them but mark the seller as deleted
            foreach (var auction in user.CreatedAuctions.Where(a => a.Status == AuctionStatus.Ended || a.Status == AuctionStatus.Sold))
            {
                auction.SubmittedByUserId = null; // Keep auction but remove seller reference
            }

            _context.Users.Remove(user);
            await _context.SaveChangesAsync();

            return Ok(new { message = "User deleted successfully" });
        }

        // GET: api/admin/analytics
        [HttpGet("analytics")]
        public async Task<ActionResult<AdminAnalyticsDto>> GetAnalytics()
        {
            var totalUsers = await _context.Users.CountAsync();
            var totalAdmins = await _context.Users.CountAsync(u => u.Role == UserRole.Admin);
            var totalBuyers = await _context.Users.CountAsync(u => u.Role == UserRole.Buyer);
            var totalSellers = await _context.Users.CountAsync(u => u.Role == UserRole.Seller);

            var totalAuctions = await _context.Auctions.CountAsync();
            var activeAuctions = await _context.Auctions.CountAsync(a => a.Status == AuctionStatus.Active);
            var pendingAuctions = await _context.Auctions.CountAsync(a => a.Status == AuctionStatus.PendingApproval);
            var endedAuctions = await _context.Auctions.CountAsync(a => a.Status == AuctionStatus.Ended);

            var totalBids = await _context.Bids.CountAsync();
            var totalRevenue = await _context.Auctions
                .Where(a => a.Status == AuctionStatus.Sold)
                .SumAsync(a => a.CurrentBid);

            var recentUsers = await _context.Users
                .OrderByDescending(u => u.CreatedAt)
                .Take(5)
                .Select(u => new RecentUserDto
                {
                    Id = u.Id,
                    Name = $"{u.FirstName} {u.LastName}",
                    Email = u.Email,
                    Role = u.Role.ToString(),
                    CreatedAt = u.CreatedAt
                })
                .ToListAsync();

            var analytics = new AdminAnalyticsDto
            {
                TotalUsers = totalUsers,
                TotalAdmins = totalAdmins,
                TotalBuyers = totalBuyers,
                TotalSellers = totalSellers,
                TotalAuctions = totalAuctions,
                ActiveAuctions = activeAuctions,
                PendingAuctions = pendingAuctions,
                EndedAuctions = endedAuctions,
                TotalBids = totalBids,
                TotalRevenue = totalRevenue,
                RecentUsers = recentUsers
            };

            return Ok(analytics);
        }

        // POST: api/admin/users
        [HttpPost("users")]
        public async Task<ActionResult<AdminUserDto>> CreateUser(CreateUserRequest request)
        {
            try
            {
                // Check if email or username already exists
                var existingUser = await _context.Users
                    .FirstOrDefaultAsync(u => u.Email == request.Email || u.Username == request.Username);

                if (existingUser != null)
                {
                    return BadRequest("Email or username already exists");
                }

                var user = new User
                {
                    FirstName = request.FirstName,
                    LastName = request.LastName,
                    Email = request.Email,
                    Username = request.Username,
                    PasswordHash = BCrypt.Net.BCrypt.HashPassword(request.Password),
                    Role = Enum.TryParse<UserRole>(request.Role, out var role) ? role : UserRole.Buyer,
                    IsEmailVerified = request.IsEmailVerified,
                    CreatedAt = DateTime.UtcNow
                };

                _context.Users.Add(user);
                await _context.SaveChangesAsync();

                var userDto = new AdminUserDto
                {
                    Id = user.Id,
                    FirstName = user.FirstName,
                    LastName = user.LastName,
                    Email = user.Email,
                    Username = user.Username,
                    Role = user.Role.ToString(),
                    IsEmailVerified = user.IsEmailVerified,
                    CreatedAt = user.CreatedAt
                };

                return CreatedAtAction(nameof(GetUser), new { id = user.Id }, userDto);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error creating user");
                return StatusCode(500, "Error creating user");
            }
        }
    }

    // Request DTOs
    public class UpdateUserRequest
    {
        [Required]
        public string FirstName { get; set; } = string.Empty;
        
        [Required]
        public string LastName { get; set; } = string.Empty;
        
        [Required]
        [EmailAddress]
        public string Email { get; set; } = string.Empty;
        
        [Required]
        public string Username { get; set; } = string.Empty;
        
        [Required]
        public string Role { get; set; } = string.Empty;
        
        public bool IsEmailVerified { get; set; }
        
        public string? NewPassword { get; set; }
    }

    public class CreateUserRequest
    {
        [Required]
        public string FirstName { get; set; } = string.Empty;
        
        [Required]
        public string LastName { get; set; } = string.Empty;
        
        [Required]
        [EmailAddress]
        public string Email { get; set; } = string.Empty;
        
        [Required]
        public string Username { get; set; } = string.Empty;
        
        [Required]
        [MinLength(6)]
        public string Password { get; set; } = string.Empty;
        
        [Required]
        public string Role { get; set; } = string.Empty;
        
        public bool IsEmailVerified { get; set; }
    }

    // Response DTOs
    public class AdminUserDto
    {
        public int Id { get; set; }
        public string FirstName { get; set; } = string.Empty;
        public string LastName { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;
        public string Username { get; set; } = string.Empty;
        public string Role { get; set; } = string.Empty;
        public bool IsEmailVerified { get; set; }
        public DateTime CreatedAt { get; set; }
    }

    public class AdminUserDetailDto : AdminUserDto
    {
        public List<AuctionSummaryDto> CreatedAuctions { get; set; } = new List<AuctionSummaryDto>();
        public List<UserBidSummaryDto> Bids { get; set; } = new List<UserBidSummaryDto>();
    }

    public class UserBidSummaryDto
    {
        public int Id { get; set; }
        public decimal Amount { get; set; }
        public DateTime Timestamp { get; set; }
        public string AuctionTitle { get; set; } = string.Empty;
        public string VehicleMake { get; set; } = string.Empty;
        public string VehicleModel { get; set; } = string.Empty;
    }

    public class AdminAnalyticsDto
    {
        public int TotalUsers { get; set; }
        public int TotalAdmins { get; set; }
        public int TotalBuyers { get; set; }
        public int TotalSellers { get; set; }
        public int TotalAuctions { get; set; }
        public int ActiveAuctions { get; set; }
        public int PendingAuctions { get; set; }
        public int EndedAuctions { get; set; }
        public int TotalBids { get; set; }
        public decimal TotalRevenue { get; set; }
        public List<RecentUserDto> RecentUsers { get; set; } = new List<RecentUserDto>();
    }

    public class RecentUserDto
    {
        public int Id { get; set; }
        public string Name { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;
        public string Role { get; set; } = string.Empty;
        public DateTime CreatedAt { get; set; }
    }
}