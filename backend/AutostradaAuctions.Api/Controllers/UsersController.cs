using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.Authorization;
using AutostradaAuctions.Api.Data;
using AutostradaAuctions.Api.Models;
using System.Security.Claims;

namespace AutostradaAuctions.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class UsersController : ControllerBase
    {
        private readonly AuctionDbContext _context;

        public UsersController(AuctionDbContext context)
        {
            _context = context;
        }

        private int GetCurrentUserId()
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (string.IsNullOrEmpty(userIdClaim) || !int.TryParse(userIdClaim, out int userId))
            {
                throw new UnauthorizedAccessException("Invalid user token");
            }
            return userId;
        }

        [HttpGet("profile")]
        public async Task<IActionResult> GetProfile()
        {
            try
            {
                var userId = GetCurrentUserId();
                
                var user = await _context.Users
                    .FirstOrDefaultAsync(u => u.Id == userId);

                if (user == null)
                    return NotFound("User not found");

                // Calculate user statistics
                var totalBids = await _context.Bids.CountAsync(b => b.BidderId == userId);
                var wonAuctions = await _context.Auctions
                    .Where(a => a.WinningBid != null && a.WinningBid.BidderId == userId)
                    .CountAsync();
                var activeBids = await _context.Bids
                    .Where(b => b.BidderId == userId && b.Auction.Status == AuctionStatus.Active)
                    .CountAsync();
                var favoriteCount = await _context.UserFavorites.CountAsync(f => f.UserId == userId);

                var profile = new UserProfileResponse
                {
                    Id = user.Id,
                    Username = user.Username,
                    Email = user.Email,
                    FirstName = user.FirstName,
                    LastName = user.LastName,
                    PhoneNumber = user.PhoneNumber,
                    DateOfBirth = user.DateOfBirth,
                    Address = user.Address,
                    City = user.City,
                    State = user.State,
                    ZipCode = user.ZipCode,
                    Role = user.Role.ToString(),
                    IsActive = user.IsActive,
                    IsEmailVerified = user.IsEmailVerified,
                    CreatedAt = user.CreatedAt,
                    Statistics = new UserStatistics
                    {
                        TotalBids = totalBids,
                        WonAuctions = wonAuctions,
                        ActiveBids = activeBids,
                        FavoriteAuctions = favoriteCount
                    }
                };

                return Ok(profile);
            }
            catch (UnauthorizedAccessException)
            {
                return Unauthorized();
            }
        }

        [HttpGet("favorites")]
        public async Task<IActionResult> GetFavorites()
        {
            try
            {
                var userId = GetCurrentUserId();

                var favorites = await _context.UserFavorites
                    .Where(f => f.UserId == userId)
                    .Include(f => f.Auction)
                        .ThenInclude(a => a.Vehicle)
                    .Include(f => f.Auction)
                        .ThenInclude(a => a.Seller)
                    .OrderByDescending(f => f.CreatedAt)
                    .Select(f => new FavoriteAuctionResponse
                    {
                        Id = f.Auction.Id,
                        Title = f.Auction.Title,
                        Description = f.Auction.Description,
                        StartingPrice = f.Auction.StartingPrice,
                        CurrentBid = f.Auction.CurrentBid,
                        StartTime = f.Auction.StartTime,
                        EndTime = f.Auction.EndTime,
                        Status = f.Auction.Status.ToString(),
                        Vehicle = new VehicleInfo
                        {
                            Id = f.Auction.Vehicle.Id,
                            Make = f.Auction.Vehicle.Make,
                            Model = f.Auction.Vehicle.Model,
                            Year = f.Auction.Vehicle.Year,
                            Color = f.Auction.Vehicle.Color,
                            Mileage = f.Auction.Vehicle.Mileage,
                            ImageUrls = f.Auction.Vehicle.ImageUrls
                        },
                        Seller = new SellerInfo
                        {
                            Id = f.Auction.Seller.Id,
                            Username = f.Auction.Seller.Username,
                            FirstName = f.Auction.Seller.FirstName,
                            LastName = f.Auction.Seller.LastName
                        },
                        AddedToFavoritesAt = f.CreatedAt
                    })
                    .ToListAsync();

                return Ok(favorites);
            }
            catch (UnauthorizedAccessException)
            {
                return Unauthorized();
            }
        }

        [HttpPost("favorites/{auctionId}")]
        public async Task<IActionResult> AddToFavorites(int auctionId)
        {
            try
            {
                var userId = GetCurrentUserId();

                // Check if auction exists
                var auctionExists = await _context.Auctions.AnyAsync(a => a.Id == auctionId);
                if (!auctionExists)
                    return NotFound("Auction not found");

                // Check if already in favorites
                var existingFavorite = await _context.UserFavorites
                    .FirstOrDefaultAsync(f => f.UserId == userId && f.AuctionId == auctionId);

                if (existingFavorite != null)
                    return BadRequest("Auction is already in favorites");

                var favorite = new UserFavorite
                {
                    UserId = userId,
                    AuctionId = auctionId,
                    CreatedAt = DateTime.UtcNow
                };

                _context.UserFavorites.Add(favorite);
                await _context.SaveChangesAsync();

                return Ok(new { message = "Auction added to favorites" });
            }
            catch (UnauthorizedAccessException)
            {
                return Unauthorized();
            }
        }

        [HttpDelete("favorites/{auctionId}")]
        public async Task<IActionResult> RemoveFromFavorites(int auctionId)
        {
            try
            {
                var userId = GetCurrentUserId();

                var favorite = await _context.UserFavorites
                    .FirstOrDefaultAsync(f => f.UserId == userId && f.AuctionId == auctionId);

                if (favorite == null)
                    return NotFound("Favorite not found");

                _context.UserFavorites.Remove(favorite);
                await _context.SaveChangesAsync();

                return Ok(new { message = "Auction removed from favorites" });
            }
            catch (UnauthorizedAccessException)
            {
                return Unauthorized();
            }
        }
    }

    public class UserProfileResponse
    {
        public int Id { get; set; }
        public string Username { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;
        public string FirstName { get; set; } = string.Empty;
        public string LastName { get; set; } = string.Empty;
        public string PhoneNumber { get; set; } = string.Empty;
        public DateTime DateOfBirth { get; set; }
        public string Address { get; set; } = string.Empty;
        public string City { get; set; } = string.Empty;
        public string State { get; set; } = string.Empty;
        public string ZipCode { get; set; } = string.Empty;
        public string Role { get; set; } = string.Empty;
        public bool IsActive { get; set; }
        public bool IsEmailVerified { get; set; }
        public DateTime CreatedAt { get; set; }
        public UserStatistics Statistics { get; set; } = new UserStatistics();
    }

    public class UserStatistics
    {
        public int TotalBids { get; set; }
        public int WonAuctions { get; set; }
        public int ActiveBids { get; set; }
        public int FavoriteAuctions { get; set; }
    }

    public class FavoriteAuctionResponse
    {
        public int Id { get; set; }
        public string Title { get; set; } = string.Empty;
        public string Description { get; set; } = string.Empty;
        public decimal StartingPrice { get; set; }
        public decimal CurrentBid { get; set; }
        public DateTime StartTime { get; set; }
        public DateTime EndTime { get; set; }
        public string Status { get; set; } = string.Empty;
        public VehicleInfo Vehicle { get; set; } = new VehicleInfo();
        public SellerInfo Seller { get; set; } = new SellerInfo();
        public DateTime AddedToFavoritesAt { get; set; }
    }

    public class VehicleInfo
    {
        public int Id { get; set; }
        public string Make { get; set; } = string.Empty;
        public string Model { get; set; } = string.Empty;
        public int Year { get; set; }
        public string Color { get; set; } = string.Empty;
        public int Mileage { get; set; }
        public List<string> ImageUrls { get; set; } = new List<string>();
    }

    public class SellerInfo
    {
        public int Id { get; set; }
        public string Username { get; set; } = string.Empty;
        public string FirstName { get; set; } = string.Empty;
        public string LastName { get; set; } = string.Empty;
    }
}