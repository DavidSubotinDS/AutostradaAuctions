using System.ComponentModel.DataAnnotations;

namespace AutostradaAuctions.Api.Models
{
    public class AuctionNotification
    {
        [Key]
        public int Id { get; set; }
        
        [Required]
        public int UserId { get; set; }
        
        [Required]
        public int AuctionId { get; set; }
        
        [Required]
        public NotificationType Type { get; set; }
        
        [Required]
        public DateTime TriggerTime { get; set; }
        
        [Required]
        public string Title { get; set; } = string.Empty;
        
        [Required]
        public string Message { get; set; } = string.Empty;
        
        public bool IsRead { get; set; } = false;
        
        public bool IsSent { get; set; } = false;
        
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
        
        public DateTime? SentAt { get; set; }
        
        // Navigation properties
        public User User { get; set; } = null!;
        public Auction Auction { get; set; } = null!;
    }
    
    public enum NotificationType
    {
        AuctionEndingIn24Hours,
        AuctionEndingIn12Hours,
        AuctionEndingIn6Hours,
        AuctionEndingIn3Hours,
        AuctionEndingIn1Hour,
        AuctionEndingIn15Minutes,
        AuctionEnded,
        OutbidNotification,
        AuctionWon,
        AuctionLost
    }
}