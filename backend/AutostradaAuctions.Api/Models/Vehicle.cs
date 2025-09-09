using System.ComponentModel.DataAnnotations;

namespace AutostradaAuctions.Api.Models
{
    public class Vehicle
    {
        public int Id { get; set; }

        [Required]
        [StringLength(100)]
        public string Make { get; set; } = string.Empty;

        [Required]
        [StringLength(100)]
        public string Model { get; set; } = string.Empty;

        public int Year { get; set; }

        [StringLength(50)]
        public string Color { get; set; } = string.Empty;

        [Required]
        [StringLength(17)]
        public string VIN { get; set; } = string.Empty;

        public int Mileage { get; set; }

        [StringLength(20)]
        public string FuelType { get; set; } = string.Empty;

        [StringLength(20)]
        public string Transmission { get; set; } = string.Empty;

        public string Description { get; set; } = string.Empty;

        public List<string> ImageUrls { get; set; } = new List<string>();

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        // Navigation properties
        public List<Auction> Auctions { get; set; } = new List<Auction>();
    }
}
