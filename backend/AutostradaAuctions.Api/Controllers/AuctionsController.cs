using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using AutostradaAuctions.Api.Data;
using AutostradaAuctions.Api.Models;
using Microsoft.AspNetCore.Authorization;
using System.Security.Claims;
using System.ComponentModel.DataAnnotations;

namespace AutostradaAuctions.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuctionsController : ControllerBase
    {
        private readonly AuctionDbContext _context;
        private readonly ILogger<AuctionsController> _logger;

        public AuctionsController(AuctionDbContext context, ILogger<AuctionsController> logger)
        {
            _context = context;
            _logger = logger;
        }

        // GET: api/auctions
        [HttpGet]
        public async Task<ActionResult<IEnumerable<AuctionDto>>> GetAuctions()
        {
            var auctions = await _context.Auctions
                .Include(a => a.Vehicle)
                .Include(a => a.Seller)
                .Where(a => a.Status == AuctionStatus.Active || a.Status == AuctionStatus.Scheduled || a.Status == AuctionStatus.Ended)
                .Select(a => new AuctionDto
                {
                    Id = a.Id,
                    Title = a.Title,
                    Description = a.Description,
                    StartingPrice = a.StartingPrice,
                    CurrentBid = a.CurrentBid,
                    ReservePrice = a.ReservePrice,
                    StartTime = a.StartTime,
                    EndTime = a.EndTime,
                    Status = a.Status.ToString(),
                    ViewCount = a.ViewCount,
                    WatchCount = a.WatchCount,
                    Vehicle = new VehicleDto
                    {
                        Id = a.Vehicle.Id,
                        Make = a.Vehicle.Make,
                        Model = a.Vehicle.Model,
                        Year = a.Vehicle.Year,
                        Color = a.Vehicle.Color,
                        Mileage = a.Vehicle.Mileage,
                        ImageUrls = a.Vehicle.ImageUrls
                    },
                    SellerName = $"{a.Seller.FirstName} {a.Seller.LastName}"
                })
                .ToListAsync();

            return Ok(auctions);
        }

        // GET: api/auctions/pending
        [HttpGet("pending")]
        [Authorize(Roles = "Admin")]
        public async Task<ActionResult<IEnumerable<AuctionDto>>> GetPendingAuctions()
        {
            var auctions = await _context.Auctions
                .Include(a => a.Vehicle)
                .Include(a => a.Seller)
                .Where(a => a.Status == AuctionStatus.PendingApproval)
                .OrderBy(a => a.SubmittedAt)
                .Select(a => new AuctionDto
                {
                    Id = a.Id,
                    Title = a.Title,
                    Description = a.Description,
                    StartingPrice = a.StartingPrice,
                    CurrentBid = a.CurrentBid,
                    ReservePrice = a.ReservePrice,
                    StartTime = a.StartTime,
                    EndTime = a.EndTime,
                    Status = a.Status.ToString(),
                    SubmittedAt = a.SubmittedAt,
                    Vehicle = new VehicleDto
                    {
                        Id = a.Vehicle.Id,
                        Make = a.Vehicle.Make,
                        Model = a.Vehicle.Model,
                        Year = a.Vehicle.Year,
                        Color = a.Vehicle.Color,
                        Mileage = a.Vehicle.Mileage,
                        ImageUrls = a.Vehicle.ImageUrls
                    },
                    SellerName = $"{a.Seller.FirstName} {a.Seller.LastName}"
                })
                .ToListAsync();

            return Ok(auctions);
        }

        // GET: api/auctions/5
        [HttpGet("{id}")]
        public async Task<ActionResult<AuctionDetailDto>> GetAuction(int id)
        {
            var auction = await _context.Auctions
                .Include(a => a.Vehicle)
                .Include(a => a.Seller)
                .Include(a => a.Bids)
                    .ThenInclude(b => b.Bidder)
                .FirstOrDefaultAsync(a => a.Id == id);

            if (auction == null)
            {
                return NotFound();
            }

            // Increment view count
            auction.ViewCount++;
            await _context.SaveChangesAsync();

            var auctionDetail = new AuctionDetailDto
            {
                Id = auction.Id,
                Title = auction.Title,
                Description = auction.Description,
                StartingPrice = auction.StartingPrice,
                CurrentBid = auction.CurrentBid,
                ReservePrice = auction.ReservePrice,
                StartTime = auction.StartTime,
                EndTime = auction.EndTime,
                Status = auction.Status.ToString(),
                ViewCount = auction.ViewCount,
                WatchCount = auction.WatchCount,
                ContactInfo = auction.ContactInfo,
                BuyNowPrice = auction.BuyNowPrice,
                HasReserve = auction.HasReserve,
                Vehicle = new VehicleDetailDto
                {
                    Id = auction.Vehicle.Id,
                    Make = auction.Vehicle.Make,
                    Model = auction.Vehicle.Model,
                    Year = auction.Vehicle.Year,
                    Color = auction.Vehicle.Color,
                    VIN = auction.Vehicle.VIN,
                    Mileage = auction.Vehicle.Mileage,
                    FuelType = auction.Vehicle.FuelType,
                    Transmission = auction.Vehicle.Transmission,
                    Description = auction.Vehicle.Description,
                    ImageUrls = auction.Vehicle.ImageUrls
                },
                SellerName = $"{auction.Seller.FirstName} {auction.Seller.LastName}",
                Bids = auction.Bids.OrderByDescending(b => b.Amount).Take(10).Select(b => new BidDto
                {
                    Id = b.Id,
                    Amount = b.Amount,
                    Timestamp = b.Timestamp,
                    BidderName = $"{b.Bidder.FirstName} {b.Bidder.LastName[0]}." // Privacy: only show first name and last initial
                }).ToList()
            };

            return Ok(auctionDetail);
        }

        // POST: api/auctions
        [HttpPost]
        [Authorize]
        public async Task<ActionResult<AuctionDto>> CreateAuction(CreateAuctionRequest request)
        {
            try
            {
                var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                if (string.IsNullOrEmpty(userId))
                {
                    return Unauthorized();
                }

                // Create Vehicle first
                var vehicle = new Vehicle
                {
                    Make = request.Make,
                    Model = request.Model,
                    Year = request.Year,
                    Color = request.Color,
                    VIN = request.VIN,
                    Mileage = request.Mileage,
                    FuelType = request.FuelType,
                    Transmission = request.Transmission,
                    Description = request.VehicleDescription,
                    ImageUrls = request.ImageUrls
                };

                _context.Vehicles.Add(vehicle);
                await _context.SaveChangesAsync();

                // Create Auction
                var auction = new Auction
                {
                    Title = request.Title,
                    Description = request.Description,
                    VehicleId = vehicle.Id,
                    SellerId = int.Parse(userId),
                    StartingPrice = request.StartingPrice,
                    ReservePrice = request.ReservePrice,
                    StartTime = request.StartTime,
                    EndTime = request.EndTime,
                    Status = AuctionStatus.PendingApproval,
                    SubmittedByUserId = userId,
                    SubmittedAt = DateTime.UtcNow,
                    ContactInfo = request.ContactInfo,
                    BuyNowPrice = request.BuyNowPrice,
                    HasReserve = request.ReservePrice.HasValue && request.ReservePrice > 0
                };

                _context.Auctions.Add(auction);
                await _context.SaveChangesAsync();

                // Load related data for response
                await _context.Entry(auction)
                    .Reference(a => a.Vehicle)
                    .LoadAsync();
                
                await _context.Entry(auction)
                    .Reference(a => a.Seller)
                    .LoadAsync();

                var auctionDto = new AuctionDto
                {
                    Id = auction.Id,
                    Title = auction.Title,
                    Description = auction.Description,
                    StartingPrice = auction.StartingPrice,
                    CurrentBid = auction.CurrentBid,
                    ReservePrice = auction.ReservePrice,
                    StartTime = auction.StartTime,
                    EndTime = auction.EndTime,
                    Status = auction.Status.ToString(),
                    Vehicle = new VehicleDto
                    {
                        Id = vehicle.Id,
                        Make = vehicle.Make,
                        Model = vehicle.Model,
                        Year = vehicle.Year,
                        Color = vehicle.Color,
                        Mileage = vehicle.Mileage,
                        ImageUrls = vehicle.ImageUrls
                    },
                    SellerName = $"{auction.Seller.FirstName} {auction.Seller.LastName}"
                };

                return CreatedAtAction(nameof(GetAuction), new { id = auction.Id }, auctionDto);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error creating auction");
                return StatusCode(500, "Error creating auction");
            }
        }

        // POST: api/auctions/5/approve
        [HttpPost("{id}/approve")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> ApproveAuction(int id)
        {
            var auction = await _context.Auctions.FindAsync(id);
            if (auction == null)
            {
                return NotFound();
            }

            if (auction.Status != AuctionStatus.PendingApproval)
            {
                return BadRequest("Auction is not pending approval");
            }

            var adminId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            auction.Status = AuctionStatus.Scheduled;
            auction.ApprovedByAdminId = adminId;
            auction.ApprovedAt = DateTime.UtcNow;

            await _context.SaveChangesAsync();

            return Ok(new { message = "Auction approved successfully" });
        }

        // POST: api/auctions/5/reject
        [HttpPost("{id}/reject")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> RejectAuction(int id, [FromBody] RejectAuctionRequest request)
        {
            var auction = await _context.Auctions.FindAsync(id);
            if (auction == null)
            {
                return NotFound();
            }

            if (auction.Status != AuctionStatus.PendingApproval)
            {
                return BadRequest("Auction is not pending approval");
            }

            auction.Status = AuctionStatus.Rejected;
            auction.RejectionReason = request.Reason;

            await _context.SaveChangesAsync();

            return Ok(new { message = "Auction rejected successfully" });
        }

        // DELETE: api/auctions/5
        [HttpDelete("{id}")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> DeleteAuction(int id)
        {
            var auction = await _context.Auctions
                .Include(a => a.Vehicle)
                .FirstOrDefaultAsync(a => a.Id == id);
            
            if (auction == null)
            {
                return NotFound();
            }

            // Remove related vehicle if no other auctions reference it
            var vehicleHasOtherAuctions = await _context.Auctions
                .AnyAsync(a => a.VehicleId == auction.VehicleId && a.Id != id);
            
            if (!vehicleHasOtherAuctions)
            {
                _context.Vehicles.Remove(auction.Vehicle);
            }

            _context.Auctions.Remove(auction);
            await _context.SaveChangesAsync();

            return Ok(new { message = "Auction deleted successfully" });
        }

        // GET: api/auctions/my
        [HttpGet("my")]
        [Authorize]
        public async Task<ActionResult<IEnumerable<AuctionDto>>> GetMyAuctions()
        {
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (string.IsNullOrEmpty(userId))
            {
                return Unauthorized();
            }

            var auctions = await _context.Auctions
                .Include(a => a.Vehicle)
                .Include(a => a.Seller)
                .Where(a => a.SellerId == int.Parse(userId))
                .Select(a => new AuctionDto
                {
                    Id = a.Id,
                    Title = a.Title,
                    Description = a.Description,
                    StartingPrice = a.StartingPrice,
                    CurrentBid = a.CurrentBid,
                    ReservePrice = a.ReservePrice,
                    StartTime = a.StartTime,
                    EndTime = a.EndTime,
                    Status = a.Status.ToString(),
                    SubmittedAt = a.SubmittedAt,
                    ApprovedAt = a.ApprovedAt,
                    RejectionReason = a.RejectionReason,
                    Vehicle = new VehicleDto
                    {
                        Id = a.Vehicle.Id,
                        Make = a.Vehicle.Make,
                        Model = a.Vehicle.Model,
                        Year = a.Vehicle.Year,
                        Color = a.Vehicle.Color,
                        Mileage = a.Vehicle.Mileage,
                        ImageUrls = a.Vehicle.ImageUrls
                    },
                    SellerName = $"{a.Seller.FirstName} {a.Seller.LastName}"
                })
                .ToListAsync();

            return Ok(auctions);
        }
    }

    // DTOs for API requests and responses
    public class CreateAuctionRequest
    {
        [Required]
        public string Title { get; set; } = string.Empty;
        
        public string Description { get; set; } = string.Empty;
        
        [Required]
        public string Make { get; set; } = string.Empty;
        
        [Required]
        public string Model { get; set; } = string.Empty;
        
        [Required]
        public int Year { get; set; }
        
        public string Color { get; set; } = string.Empty;
        
        [Required]
        public string VIN { get; set; } = string.Empty;
        
        public int Mileage { get; set; }
        
        public string FuelType { get; set; } = string.Empty;
        
        public string Transmission { get; set; } = string.Empty;
        
        public string VehicleDescription { get; set; } = string.Empty;
        
        public List<string> ImageUrls { get; set; } = new List<string>();
        
        [Required]
        [Range(0, double.MaxValue)]
        public decimal StartingPrice { get; set; }
        
        public decimal? ReservePrice { get; set; }
        
        public decimal? BuyNowPrice { get; set; }
        
        [Required]
        public DateTime StartTime { get; set; }
        
        [Required]
        public DateTime EndTime { get; set; }
        
        public string ContactInfo { get; set; } = string.Empty;
    }

    public class RejectAuctionRequest
    {
        [Required]
        public string Reason { get; set; } = string.Empty;
    }

    public class AuctionDto
    {
        public int Id { get; set; }
        public string Title { get; set; } = string.Empty;
        public string Description { get; set; } = string.Empty;
        public decimal StartingPrice { get; set; }
        public decimal CurrentBid { get; set; }
        public decimal? ReservePrice { get; set; }
        public DateTime StartTime { get; set; }
        public DateTime EndTime { get; set; }
        public string Status { get; set; } = string.Empty;
        public int ViewCount { get; set; }
        public int WatchCount { get; set; }
        public DateTime SubmittedAt { get; set; }
        public DateTime? ApprovedAt { get; set; }
        public string? RejectionReason { get; set; }
        public VehicleDto Vehicle { get; set; } = null!;
        public string SellerName { get; set; } = string.Empty;
    }

    public class AuctionDetailDto : AuctionDto
    {
        public new VehicleDetailDto Vehicle { get; set; } = null!;
        public List<BidDto> Bids { get; set; } = new List<BidDto>();
        public string ContactInfo { get; set; } = string.Empty;
        public decimal? BuyNowPrice { get; set; }
        public bool HasReserve { get; set; }
    }

    public class VehicleDto
    {
        public int Id { get; set; }
        public string Make { get; set; } = string.Empty;
        public string Model { get; set; } = string.Empty;
        public int Year { get; set; }
        public string Color { get; set; } = string.Empty;
        public int Mileage { get; set; }
        public List<string> ImageUrls { get; set; } = new List<string>();
    }

    public class VehicleDetailDto : VehicleDto
    {
        public string VIN { get; set; } = string.Empty;
        public string FuelType { get; set; } = string.Empty;
        public string Transmission { get; set; } = string.Empty;
        public string Description { get; set; } = string.Empty;
    }

    public class BidDto
    {
        public int Id { get; set; }
        public decimal Amount { get; set; }
        public DateTime Timestamp { get; set; }
        public string BidderName { get; set; } = string.Empty;
        public int AuctionId { get; set; }
        public bool IsWinning { get; set; }
    }
}
