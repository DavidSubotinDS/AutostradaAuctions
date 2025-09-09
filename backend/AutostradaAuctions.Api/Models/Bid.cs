using System.ComponentModel.DataAnnotations;

namespace AutostradaAuctions.Api.Models
{
    public class Bid
    {
        public int Id { get; set; }

        [Required]
        public int AuctionId { get; set; }

        [Required]
        public int BidderId { get; set; }

        [Required]
        [Range(0, double.MaxValue)]
        public decimal Amount { get; set; }

        public DateTime Timestamp { get; set; } = DateTime.UtcNow;

        public bool IsWinning { get; set; } = false;

        // Navigation properties
        public Auction Auction { get; set; } = null!;
        public User Bidder { get; set; } = null!;
    }
}
