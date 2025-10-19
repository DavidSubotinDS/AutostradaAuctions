namespace AutostradaAuctions.Api.Models
{
    public class AuctionWatch
    {
        public int Id { get; set; }
        public int AuctionId { get; set; }
        public int UserId { get; set; }
        public DateTime WatchedAt { get; set; } = DateTime.UtcNow;

        // Navigation properties
        public Auction Auction { get; set; } = null!;
        public User User { get; set; } = null!;
    }
}