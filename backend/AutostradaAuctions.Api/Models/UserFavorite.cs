using System.ComponentModel.DataAnnotations;

namespace AutostradaAuctions.Api.Models
{
    public class UserFavorite
    {
        public int Id { get; set; }

        [Required]
        public int UserId { get; set; }

        [Required]
        public int AuctionId { get; set; }

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        // Navigation properties
        public User User { get; set; } = null!;
        public Auction Auction { get; set; } = null!;
    }
}