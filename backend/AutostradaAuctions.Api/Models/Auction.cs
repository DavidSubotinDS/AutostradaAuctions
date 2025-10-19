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
        
        // Approval System Fields
        public string? SubmittedByUserId { get; set; }
        public DateTime SubmittedAt { get; set; } = DateTime.UtcNow;
        public string? ApprovedByAdminId { get; set; }
        public DateTime? ApprovedAt { get; set; }
        public string? RejectionReason { get; set; }
        
        // Additional Fields
        public int ViewCount { get; set; }
        public int WatchCount { get; set; }
        public string ContactInfo { get; set; } = string.Empty;
        public decimal? BuyNowPrice { get; set; }
        public bool HasReserve { get; set; }

        // Navigation properties
        public Vehicle Vehicle { get; set; } = null!;
        public User Seller { get; set; } = null!;
        public List<Bid> Bids { get; set; } = new List<Bid>();
        public Bid? WinningBid { get; set; }
        public List<UserFavorite> UserFavorites { get; set; } = new List<UserFavorite>();
    }

    public enum AuctionStatus
    {
        Draft = 0,
        PendingApproval = 1,
        Scheduled = 2,
        Active = 3,
        Ended = 4,
        Cancelled = 5,
        Sold = 6,
        Rejected = 7
    }
}
