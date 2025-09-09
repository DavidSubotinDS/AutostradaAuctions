using Microsoft.EntityFrameworkCore;
using AutostradaAuctions.Api.Models;
using System.Text.Json;

namespace AutostradaAuctions.Api.Data
{
    public class AuctionDbContext : DbContext
    {
        public AuctionDbContext(DbContextOptions<AuctionDbContext> options) : base(options) { }

        public DbSet<User> Users { get; set; }
        public DbSet<Vehicle> Vehicles { get; set; }
        public DbSet<Auction> Auctions { get; set; }
        public DbSet<Bid> Bids { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            // User entity configuration
            modelBuilder.Entity<User>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.HasIndex(e => e.Email).IsUnique();
                entity.Property(e => e.Email).IsRequired().HasMaxLength(255);
                entity.Property(e => e.PasswordHash).IsRequired();
                entity.Property(e => e.Role).HasConversion<int>();
            });

            // Vehicle entity configuration
            modelBuilder.Entity<Vehicle>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.HasIndex(e => e.VIN).IsUnique();
                entity.Property(e => e.VIN).IsRequired().HasMaxLength(17);
                entity.Property(e => e.Make).IsRequired().HasMaxLength(100);
                entity.Property(e => e.Model).IsRequired().HasMaxLength(100);
                
                // Configure ImageUrls as JSON
                entity.Property(e => e.ImageUrls)
                    .HasConversion(
                        v => JsonSerializer.Serialize(v, (JsonSerializerOptions?)null),
                        v => JsonSerializer.Deserialize<List<string>>(v, (JsonSerializerOptions?)null) ?? new List<string>()
                    );
            });

            // Auction entity configuration
            modelBuilder.Entity<Auction>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Title).IsRequired().HasMaxLength(200);
                entity.Property(e => e.StartingPrice).HasColumnType("decimal(18,2)");
                entity.Property(e => e.ReservePrice).HasColumnType("decimal(18,2)");
                entity.Property(e => e.CurrentBid).HasColumnType("decimal(18,2)");
                entity.Property(e => e.Status).HasConversion<int>();

                // Relationships
                entity.HasOne(e => e.Vehicle)
                    .WithMany(v => v.Auctions)
                    .HasForeignKey(e => e.VehicleId)
                    .OnDelete(DeleteBehavior.Restrict);

                entity.HasOne(e => e.Seller)
                    .WithMany(u => u.CreatedAuctions)
                    .HasForeignKey(e => e.SellerId)
                    .OnDelete(DeleteBehavior.Restrict);

                entity.HasOne(e => e.WinningBid)
                    .WithMany()
                    .HasForeignKey(e => e.WinningBidId)
                    .OnDelete(DeleteBehavior.SetNull);
            });

            // Bid entity configuration
            modelBuilder.Entity<Bid>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Amount).HasColumnType("decimal(18,2)");

                // Relationships
                entity.HasOne(e => e.Auction)
                    .WithMany(a => a.Bids)
                    .HasForeignKey(e => e.AuctionId)
                    .OnDelete(DeleteBehavior.Cascade);

                entity.HasOne(e => e.Bidder)
                    .WithMany(u => u.Bids)
                    .HasForeignKey(e => e.BidderId)
                    .OnDelete(DeleteBehavior.Restrict);

                // Index for performance
                entity.HasIndex(e => new { e.AuctionId, e.Amount });
            });

            // Seed data
            SeedData(modelBuilder);
        }

        private void SeedData(ModelBuilder modelBuilder)
        {
            // Seed admin user
            modelBuilder.Entity<User>().HasData(
                new User
                {
                    Id = 1,
                    FirstName = "Admin",
                    LastName = "User",
                    Email = "admin@autostrada.com",
                    PasswordHash = "$2a$11$Xk1wLU8FaP6nWaHbYFDHYeWW4dHQoZ8CzOtK5.KzYYzLZdKy9gWxy", // password: admin123
                    Role = UserRole.Admin,
                    IsEmailVerified = true,
                    CreatedAt = DateTime.UtcNow
                }
            );

            // Seed sample vehicles
            modelBuilder.Entity<Vehicle>().HasData(
                new Vehicle
                {
                    Id = 1,
                    Make = "Tesla",
                    Model = "Model S",
                    Year = 2023,
                    Color = "Pearl White",
                    VIN = "5YJSA1E20MF000001",
                    Mileage = 15000,
                    FuelType = "Electric",
                    Transmission = "Automatic",
                    Description = "Pristine Tesla Model S with Autopilot, premium interior, and full self-driving capability.",
                    CreatedAt = DateTime.UtcNow
                },
                new Vehicle
                {
                    Id = 2,
                    Make = "BMW",
                    Model = "M3",
                    Year = 2022,
                    Color = "Alpine White",
                    VIN = "WBS8M9C59N5000002",
                    Mileage = 8500,
                    FuelType = "Gasoline",
                    Transmission = "Manual",
                    Description = "High-performance BMW M3 with carbon fiber package and track-ready suspension.",
                    CreatedAt = DateTime.UtcNow
                }
            );
        }
    }
}
