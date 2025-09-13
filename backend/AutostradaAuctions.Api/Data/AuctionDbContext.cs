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
        public DbSet<UserFavorite> UserFavorites { get; set; }

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
                    .OnDelete(DeleteBehavior.NoAction);
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

            // UserFavorite entity configuration
            modelBuilder.Entity<UserFavorite>(entity =>
            {
                entity.HasKey(e => e.Id);

                // Relationships
                entity.HasOne(e => e.User)
                    .WithMany()
                    .HasForeignKey(e => e.UserId)
                    .OnDelete(DeleteBehavior.Cascade);

                entity.HasOne(e => e.Auction)
                    .WithMany()
                    .HasForeignKey(e => e.AuctionId)
                    .OnDelete(DeleteBehavior.Cascade);

                // Unique constraint to prevent duplicate favorites
                entity.HasIndex(e => new { e.UserId, e.AuctionId }).IsUnique();
            });

            // Seed data
            SeedData(modelBuilder);
        }

        private void SeedData(ModelBuilder modelBuilder)
        {
            // Fixed datetime for seed data
            var seedDate = new DateTime(2024, 1, 1, 0, 0, 0, DateTimeKind.Utc);

            // Seed admin user
            modelBuilder.Entity<User>().HasData(
                new User
                {
                    Id = 1,
                    FirstName = "Admin",
                    LastName = "User",
                    Email = "admin@autostrada.com",
                    Username = "admin123",
                    PasswordHash = "$2a$11$wiAQrFy99wpavWH3alOxku8emSuSu4hJX6Zd8yOdnJAeXH5qr/bDe", // password: "password"
                    Role = UserRole.Admin,
                    IsEmailVerified = true,
                    CreatedAt = seedDate
                },
                new User
                {
                    Id = 2,
                    FirstName = "Regular",
                    LastName = "User",
                    Email = "user@autostrada.com",
                    Username = "user123",
                    PasswordHash = "$2a$11$wiAQrFy99wpavWH3alOxku8emSuSu4hJX6Zd8yOdnJAeXH5qr/bDe", // password: "password"
                    Role = UserRole.Buyer,
                    IsEmailVerified = true,
                    CreatedAt = seedDate
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
                    CreatedAt = seedDate
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
                    CreatedAt = seedDate
                },
                new Vehicle
                {
                    Id = 3,
                    Make = "Mercedes-Benz",
                    Model = "AMG GT",
                    Year = 2023,
                    Color = "Obsidian Black",
                    VIN = "WDDYK7HA2JA000003",
                    Mileage = 5200,
                    FuelType = "Gasoline",
                    Transmission = "Automatic",
                    Description = "Luxury sports car with handcrafted AMG engine and premium leather interior.",
                    CreatedAt = seedDate
                }
            );

            // Seed sample auctions (using higher IDs to avoid conflicts)
            modelBuilder.Entity<Auction>().HasData(
                new Auction
                {
                    Id = 10,
                    Title = "2023 Tesla Model S - Premium Electric Sedan",
                    Description = "Experience the future of driving with this pristine Tesla Model S featuring Autopilot, premium interior, and full self-driving capability.",
                    VehicleId = 1,
                    SellerId = 2,
                    StartingPrice = 75000,
                    ReservePrice = 85000,
                    CurrentBid = 75000,
                    StartTime = seedDate.AddDays(1),
                    EndTime = seedDate.AddDays(8),
                    Status = AuctionStatus.Scheduled,
                    CreatedAt = seedDate
                },
                new Auction
                {
                    Id = 11,
                    Title = "2022 BMW M3 - Track-Ready Performance",
                    Description = "High-performance BMW M3 with carbon fiber package, track-ready suspension, and manual transmission for the driving purist.",
                    VehicleId = 2,
                    SellerId = 2,
                    StartingPrice = 65000,
                    ReservePrice = 72000,
                    CurrentBid = 67500,
                    StartTime = seedDate,
                    EndTime = seedDate.AddDays(7),
                    Status = AuctionStatus.Active,
                    CreatedAt = seedDate
                },
                new Auction
                {
                    Id = 12,
                    Title = "2023 Mercedes-AMG GT - Luxury Sports Car",
                    Description = "Stunning luxury sports car with handcrafted AMG engine, premium leather interior, and cutting-edge technology.",
                    VehicleId = 3,
                    SellerId = 2,
                    StartingPrice = 95000,
                    ReservePrice = 105000,
                    CurrentBid = 95000,
                    StartTime = seedDate.AddDays(2),
                    EndTime = seedDate.AddDays(9),
                    Status = AuctionStatus.Scheduled,
                    CreatedAt = seedDate
                }
            );

            // Seed sample user favorites
            modelBuilder.Entity<UserFavorite>().HasData(
                new UserFavorite
                {
                    Id = 1,
                    UserId = 1, // Admin user
                    AuctionId = 11, // BMW M3
                    CreatedAt = seedDate
                },
                new UserFavorite
                {
                    Id = 2,
                    UserId = 1, // Admin user
                    AuctionId = 12, // Mercedes-AMG GT
                    CreatedAt = seedDate.AddHours(1)
                }
            );
        }
    }
}
