using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using AutostradaAuctions.Api.Data;
using AutostradaAuctions.Api.Models;

namespace AutostradaAuctions.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuctionsController : ControllerBase
    {
        private readonly AuctionDbContext _context;

        public AuctionsController(AuctionDbContext context)
        {
            _context = context;
        }

        // GET: api/auctions
        [HttpGet]
        public async Task<ActionResult<IEnumerable<AuctionDto>>> GetAuctions()
        {
            var auctions = await _context.Auctions
                .Include(a => a.Vehicle)
                .Include(a => a.Seller)
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
    }

    // DTOs for API responses
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
        public VehicleDto Vehicle { get; set; } = null!;
        public string SellerName { get; set; } = string.Empty;
    }

    public class AuctionDetailDto : AuctionDto
    {
        public new VehicleDetailDto Vehicle { get; set; } = null!;
        public List<BidDto> Bids { get; set; } = new List<BidDto>();
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
    }
}
