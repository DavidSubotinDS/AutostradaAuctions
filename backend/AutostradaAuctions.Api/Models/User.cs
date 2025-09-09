using System.ComponentModel.DataAnnotations;

namespace AutostradaAuctions.Api.Models
{
    public class User
    {
        public int Id { get; set; }

        [Required]
        [StringLength(100)]
        public string FirstName { get; set; } = string.Empty;

        [Required]
        [StringLength(100)]
        public string LastName { get; set; } = string.Empty;

        [Required]
        [EmailAddress]
        [StringLength(255)]
        public string Email { get; set; } = string.Empty;

        [Required]
        public string PasswordHash { get; set; } = string.Empty;

        [StringLength(20)]
        public string PhoneNumber { get; set; } = string.Empty;

        public DateTime DateOfBirth { get; set; }

        [StringLength(200)]
        public string Address { get; set; } = string.Empty;

        [StringLength(100)]
        public string City { get; set; } = string.Empty;

        [StringLength(50)]
        public string State { get; set; } = string.Empty;

        [StringLength(20)]
        public string ZipCode { get; set; } = string.Empty;

        public bool IsEmailVerified { get; set; } = false;

        public bool IsActive { get; set; } = true;

        public UserRole Role { get; set; } = UserRole.Buyer;

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        // Navigation properties
        public List<Auction> CreatedAuctions { get; set; } = new List<Auction>();
        public List<Bid> Bids { get; set; } = new List<Bid>();
    }

    public enum UserRole
    {
        Buyer = 1,
        Seller = 2,
        Admin = 3
    }
}
