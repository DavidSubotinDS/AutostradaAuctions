using System.ComponentModel.DataAnnotations;

namespace AutostradaAuctions.Api.Models
{
    public class Auction
    {
        public int Id { get; set; }

        [Required]
        [StringLength(200)]
        public string Title { get; set; } = string.Empty;

        public string Description { get; set; } = string.Empty;

        [Required]
        public int VehicleId { get; set; }

        [Required]
        public int SellerId { get; set; }

        [Range(0, double.MaxValue)]
        public decimal StartingPrice { get; set; }

        [Range(0, double.MaxValue)]
        public decimal? ReservePrice { get; set; }

        [Range(0, double.MaxValue)]
        public decimal CurrentBid { get; set; }

        public DateTime StartTime { get; set; }

        public DateTime EndTime { get; set; }

        public AuctionStatus Status { get; set; } = AuctionStatus.Scheduled;

        public int? WinningBidId { get; set; }

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        // Navigation properties
        public Vehicle Vehicle { get; set; } = null!;
        public User Seller { get; set; } = null!;
        public List<Bid> Bids { get; set; } = new List<Bid>();
        public Bid? WinningBid { get; set; }
    }

    public enum AuctionStatus
    {
        Scheduled = 1,
        Active = 2,
        Ended = 3,
        Cancelled = 4,
        Sold = 5
    }
}
