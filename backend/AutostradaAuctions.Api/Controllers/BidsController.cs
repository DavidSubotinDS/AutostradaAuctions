using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using AutostradaAuctions.Api.Data;
using AutostradaAuctions.Api.Models;
using Microsoft.AspNetCore.Authorization;
using System.Security.Claims;
using System.ComponentModel.DataAnnotations;
using Microsoft.AspNetCore.SignalR;
using AutostradaAuctions.Api.Hubs;

namespace AutostradaAuctions.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class BidsController : ControllerBase
    {
        private readonly AuctionDbContext _context;
        private readonly ILogger<BidsController> _logger;
        private readonly IHubContext<BiddingHub> _hubContext;

        public BidsController(AuctionDbContext context, ILogger<BidsController> logger, IHubContext<BiddingHub> hubContext)
        {
            _context = context;
            _logger = logger;
            _hubContext = hubContext;
        }

        // POST: api/bids
        [HttpPost]
        [Authorize]
        public async Task<ActionResult<BidDto>> PlaceBid(PlaceBidRequest request)
        {
            try
            {
                var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                if (string.IsNullOrEmpty(userId))
                {
                    return Unauthorized();
                }

                var auction = await _context.Auctions
                    .Include(a => a.Bids)
                    .FirstOrDefaultAsync(a => a.Id == request.AuctionId);

                if (auction == null)
                {
                    return NotFound("Auction not found");
                }

                // Validate auction status
                if (auction.Status != AuctionStatus.Active)
                {
                    return BadRequest("Auction is not active");
                }

                // Check if auction has ended
                if (DateTime.UtcNow > auction.EndTime)
                {
                    return BadRequest("Auction has ended");
                }

                // Validate bid amount
                var minimumBid = Math.Max(auction.CurrentBid + 1, auction.StartingPrice);
                if (request.Amount < minimumBid)
                {
                    return BadRequest($"Bid must be at least ${minimumBid:F2}");
                }

                // Check if user is not the seller
                if (auction.SellerId == int.Parse(userId))
                {
                    return BadRequest("Sellers cannot bid on their own auctions");
                }

                // Create bid
                var bid = new Bid
                {
                    AuctionId = request.AuctionId,
                    BidderId = int.Parse(userId),
                    Amount = request.Amount,
                    Timestamp = DateTime.UtcNow
                };

                _context.Bids.Add(bid);

                // Update auction current bid
                auction.CurrentBid = request.Amount;

                await _context.SaveChangesAsync();

                // Load bidder for response
                await _context.Entry(bid)
                    .Reference(b => b.Bidder)
                    .LoadAsync();

                var bidDto = new Hubs.BidDto
                {
                    Id = bid.Id,
                    Amount = bid.Amount,
                    Timestamp = bid.Timestamp,
                    BidderName = $"{bid.Bidder.FirstName} {bid.Bidder.LastName[0]}.",
                    AuctionId = bid.AuctionId,
                    IsWinning = bid.Amount == auction.CurrentBid
                };

                // Send SignalR notification to all users watching this auction
                var groupName = $"auction_{request.AuctionId}";
                await _hubContext.Clients.Group(groupName).SendAsync("ReceiveBidUpdate", request.AuctionId, bidDto);

                return CreatedAtAction(nameof(GetBid), new { id = bid.Id }, bidDto);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error placing bid");
                return StatusCode(500, "Error placing bid");
            }
        }

        // GET: api/bids/5
        [HttpGet("{id}")]
        public async Task<ActionResult<BidDto>> GetBid(int id)
        {
            var bid = await _context.Bids
                .Include(b => b.Bidder)
                .FirstOrDefaultAsync(b => b.Id == id);

            if (bid == null)
            {
                return NotFound();
            }

            var bidDto = new BidDto
            {
                Id = bid.Id,
                Amount = bid.Amount,
                Timestamp = bid.Timestamp,
                BidderName = $"{bid.Bidder.FirstName} {bid.Bidder.LastName[0]}.",
                AuctionId = bid.AuctionId,
                IsWinning = false // This will be determined by the client based on current auction state
            };

            return Ok(bidDto);
        }

        // GET: api/bids/auction/5
        [HttpGet("auction/{auctionId}")]
        public async Task<ActionResult<IEnumerable<BidDto>>> GetAuctionBids(int auctionId)
        {
            var bids = await _context.Bids
                .Include(b => b.Bidder)
                .Include(b => b.Auction)
                .Where(b => b.AuctionId == auctionId)
                .OrderByDescending(b => b.Timestamp)
                .Take(50) // Limit to last 50 bids
                .Select(b => new BidDto
                {
                    Id = b.Id,
                    Amount = b.Amount,
                    Timestamp = b.Timestamp,
                    BidderName = $"{b.Bidder.FirstName} {b.Bidder.LastName[0]}.",
                    AuctionId = b.AuctionId,
                    IsWinning = b.Amount == b.Auction.CurrentBid
                })
                .ToListAsync();

            return Ok(bids);
        }

        // GET: api/bids/my
        [HttpGet("my")]
        [Authorize]
        public async Task<ActionResult<IEnumerable<MyBidDto>>> GetMyBids()
        {
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (string.IsNullOrEmpty(userId))
            {
                return Unauthorized();
            }

            var bids = await _context.Bids
                .Include(b => b.Auction)
                    .ThenInclude(a => a.Vehicle)
                .Where(b => b.BidderId == int.Parse(userId))
                .OrderByDescending(b => b.Timestamp)
                .Select(b => new MyBidDto
                {
                    Id = b.Id,
                    Amount = b.Amount,
                    Timestamp = b.Timestamp,
                    IsWinning = b.Amount == b.Auction.CurrentBid,
                    Auction = new AuctionSummaryDto
                    {
                        Id = b.Auction.Id,
                        Title = b.Auction.Title,
                        CurrentBid = b.Auction.CurrentBid,
                        EndTime = b.Auction.EndTime,
                        Status = b.Auction.Status.ToString(),
                        VehicleMake = b.Auction.Vehicle.Make,
                        VehicleModel = b.Auction.Vehicle.Model,
                        VehicleYear = b.Auction.Vehicle.Year
                    }
                })
                .ToListAsync();

            return Ok(bids);
        }

        // POST: api/bids/5/watch - TODO: Implement after AuctionWatch model is properly configured
        /*
        [HttpPost("{auctionId}/watch")]
        [Authorize]
        public async Task<IActionResult> WatchAuction(int auctionId)
        {
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (string.IsNullOrEmpty(userId))
            {
                return Unauthorized();
            }

            var auction = await _context.Auctions.FindAsync(auctionId);
            if (auction == null)
            {
                return NotFound("Auction not found");
            }

            // Check if already watching
            var existingWatch = await _context.AuctionWatches
                .FirstOrDefaultAsync(w => w.AuctionId == auctionId && w.UserId == int.Parse(userId));

            if (existingWatch != null)
            {
                return BadRequest("Already watching this auction");
            }

            // Add watch
            var watch = new AuctionWatch
            {
                AuctionId = auctionId,
                UserId = int.Parse(userId),
                WatchedAt = DateTime.UtcNow
            };

            _context.AuctionWatches.Add(watch);
            auction.WatchCount++;
            
            await _context.SaveChangesAsync();

            return Ok(new { message = "Now watching auction" });
        }

        // DELETE: api/bids/5/watch
        [HttpDelete("{auctionId}/watch")]
        [Authorize]
        public async Task<IActionResult> UnwatchAuction(int auctionId)
        {
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (string.IsNullOrEmpty(userId))
            {
                return Unauthorized();
            }

            var watch = await _context.AuctionWatches
                .FirstOrDefaultAsync(w => w.AuctionId == auctionId && w.UserId == int.Parse(userId));

            if (watch == null)
            {
                return NotFound("Not watching this auction");
            }

            var auction = await _context.Auctions.FindAsync(auctionId);
            if (auction != null)
            {
                auction.WatchCount = Math.Max(0, auction.WatchCount - 1);
            }

            _context.AuctionWatches.Remove(watch);
            await _context.SaveChangesAsync();

            return Ok(new { message = "Stopped watching auction" });
        }
        */
    }

    // Request DTOs
    public class PlaceBidRequest
    {
        [Required]
        public int AuctionId { get; set; }
        
        [Required]
        [Range(0.01, double.MaxValue)]
        public decimal Amount { get; set; }
    }

    // Response DTOs
    public class MyBidDto
    {
        public int Id { get; set; }
        public decimal Amount { get; set; }
        public DateTime Timestamp { get; set; }
        public bool IsWinning { get; set; }
        public AuctionSummaryDto Auction { get; set; } = null!;
    }

    public class AuctionSummaryDto
    {
        public int Id { get; set; }
        public string Title { get; set; } = string.Empty;
        public decimal CurrentBid { get; set; }
        public DateTime EndTime { get; set; }
        public string Status { get; set; } = string.Empty;
        public string VehicleMake { get; set; } = string.Empty;
        public string VehicleModel { get; set; } = string.Empty;
        public int VehicleYear { get; set; }
    }
}