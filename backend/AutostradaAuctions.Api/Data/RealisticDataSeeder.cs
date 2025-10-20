using Microsoft.EntityFrameworkCore;
using AutostradaAuctions.Api.Models;
using BCrypt.Net;

namespace AutostradaAuctions.Api.Data
{
    public static class RealisticDataSeeder
    {
        public static async Task SeedRealisticData(IServiceProvider serviceProvider)
        {
            using var scope = serviceProvider.CreateScope();
            var context = scope.ServiceProvider.GetRequiredService<AuctionDbContext>();

            Console.WriteLine("🔄 Checking existing data...");
            
            // Always reset for demo - delete existing data
            if (await context.Users.AnyAsync() || await context.Vehicles.AnyAsync() || await context.Auctions.AnyAsync())
            {
                Console.WriteLine("🗑️ Clearing existing data for fresh demo...");
                
                // Delete in proper order to avoid foreign key issues
                context.UserFavorites.RemoveRange(context.UserFavorites);
                context.Bids.RemoveRange(context.Bids);
                context.Auctions.RemoveRange(context.Auctions);
                context.Vehicles.RemoveRange(context.Vehicles);
                context.Users.RemoveRange(context.Users);
                
                await context.SaveChangesAsync();
                Console.WriteLine("✅ Database cleared successfully!");
            }

            Console.WriteLine("🌱 Starting comprehensive data seeding...");

            // Create realistic users with proper IDs
            var users = CreateRealisticUsers();
            context.Users.AddRange(users);
            await context.SaveChangesAsync();
            Console.WriteLine($"👥 Created {users.Count} users");

            // Create realistic vehicles 
            var vehicles = CreateRealisticVehicles();
            context.Vehicles.AddRange(vehicles);
            await context.SaveChangesAsync();
            Console.WriteLine($"🚗 Created {vehicles.Count} vehicles");

            // Create realistic auctions with proper relationships
            var auctions = CreateRealisticAuctions(users, vehicles);
            context.Auctions.AddRange(auctions);
            await context.SaveChangesAsync();
            Console.WriteLine($"🔨 Created {auctions.Count} auctions");

            // Create realistic bids for auctions
            var bids = CreateRealisticBids(users, auctions);
            context.Bids.AddRange(bids);
            await context.SaveChangesAsync();
            Console.WriteLine($"💰 Created {bids.Count} bids");

            // Update current bids on auctions
            foreach (var auction in auctions.Where(a => bids.Any(b => b.AuctionId == a.Id)))
            {
                var highestBid = bids.Where(b => b.AuctionId == auction.Id).Max(b => b.Amount);
                auction.CurrentBid = highestBid;
                Console.WriteLine($"💵 Updated auction '{auction.Title}' current bid to ${highestBid:N0}");
            }

            // Create user favorites
            var favorites = CreateUserFavorites(users, auctions);
            context.UserFavorites.AddRange(favorites);
            Console.WriteLine($"⭐ Created {favorites.Count} user favorites");
            
            await context.SaveChangesAsync();

            // Update winning bids for ended auctions
            var endedAuctions = auctions.Where(a => a.Status == AuctionStatus.Ended).ToList();
            foreach (var auction in endedAuctions)
            {
                var winningBid = bids.Where(b => b.AuctionId == auction.Id).OrderByDescending(b => b.Amount).FirstOrDefault();
                if (winningBid != null)
                {
                    auction.WinningBidId = winningBid.Id;
                    winningBid.IsWinning = true;
                    Console.WriteLine($"🏆 Set winning bid for '{auction.Title}': ${winningBid.Amount:N0}");
                }
            }

            await context.SaveChangesAsync();

            // Print summary of created data
            var activeAuctions = auctions.Count(a => a.Status == AuctionStatus.Active);
            var totalBids = bids.Count;
            
            Console.WriteLine("✅ Comprehensive data seeding completed successfully!");
            Console.WriteLine($"📊 Summary: {users.Count} users, {vehicles.Count} vehicles, {auctions.Count} auctions ({activeAuctions} active), {totalBids} bids");
            Console.WriteLine("🚀 Ready for live bidding demo!");
        }

        private static List<User> CreateRealisticUsers()
        {
            var users = new List<User>
            {
                // Admin user
                new User
                {
                    FirstName = "Michael",
                    LastName = "Rodriguez",
                    Email = "admin@autostrada.com",
                    Username = "admin123",
                    PasswordHash = BCrypt.Net.BCrypt.HashPassword("password"),
                    PhoneNumber = "+1-555-0100",
                    DateOfBirth = new DateTime(1985, 3, 15),
                    Address = "123 Admin Street",
                    City = "Los Angeles",
                    State = "CA",
                    ZipCode = "90210",
                    IsEmailVerified = true,
                    IsActive = true,
                    Role = UserRole.Admin,
                    CreatedAt = DateTime.UtcNow.AddMonths(-6)
                },
                // Primary test user for app
                new User
                {
                    FirstName = "John",
                    LastName = "Smith",
                    Email = "user@test.com",
                    Username = "user123",
                    PasswordHash = BCrypt.Net.BCrypt.HashPassword("password"),
                    PhoneNumber = "+1-555-0199",
                    DateOfBirth = new DateTime(1990, 5, 20),
                    Address = "456 Test Drive",
                    City = "San Diego",
                    State = "CA",
                    ZipCode = "92101",
                    IsEmailVerified = true,
                    IsActive = true,
                    Role = UserRole.Buyer,
                    CreatedAt = DateTime.UtcNow.AddMonths(-2)
                },
                // Car dealer / frequent seller
                new User
                {
                    FirstName = "David",
                    LastName = "Thompson",
                    Email = "david.thompson@luxurycars.com",
                    Username = "luxurydealer",
                    PasswordHash = BCrypt.Net.BCrypt.HashPassword("password"),
                    PhoneNumber = "+1-555-0201",
                    DateOfBirth = new DateTime(1978, 8, 22),
                    Address = "456 Luxury Car Boulevard",
                    City = "Beverly Hills",
                    State = "CA",
                    ZipCode = "90210",
                    IsEmailVerified = true,
                    IsActive = true,
                    Role = UserRole.Seller,
                    CreatedAt = DateTime.UtcNow.AddMonths(-4)
                },
                // Enthusiast collector
                new User
                {
                    FirstName = "Sarah",
                    LastName = "Mitchell",
                    Email = "sarah.mitchell@email.com",
                    Username = "carCollector88",
                    PasswordHash = BCrypt.Net.BCrypt.HashPassword("password"),
                    PhoneNumber = "+1-555-0302",
                    DateOfBirth = new DateTime(1988, 12, 5),
                    Address = "789 Collector's Drive",
                    City = "Miami",
                    State = "FL",
                    ZipCode = "33101",
                    IsEmailVerified = true,
                    IsActive = true,
                    Role = UserRole.Buyer,
                    CreatedAt = DateTime.UtcNow.AddMonths(-3)
                },
                // Regular user / bidder
                new User
                {
                    FirstName = "James",
                    LastName = "Wilson",
                    Email = "james.wilson@email.com",
                    Username = "jwilson2024",
                    PasswordHash = BCrypt.Net.BCrypt.HashPassword("password"),
                    PhoneNumber = "+1-555-0403",
                    DateOfBirth = new DateTime(1992, 6, 18),
                    Address = "321 Suburban Lane",
                    City = "Austin",
                    State = "TX",
                    ZipCode = "73301",
                    IsEmailVerified = true,
                    IsActive = true,
                    Role = UserRole.Buyer,
                    CreatedAt = DateTime.UtcNow.AddMonths(-2)
                },
                // Another active bidder
                new User
                {
                    FirstName = "Emily",
                    LastName = "Chen",
                    Email = "emily.chen@techcorp.com",
                    Username = "emilychen",
                    PasswordHash = BCrypt.Net.BCrypt.HashPassword("password"),
                    PhoneNumber = "+1-555-0504",
                    DateOfBirth = new DateTime(1990, 9, 30),
                    Address = "654 Tech Valley Road",
                    City = "San Francisco",
                    State = "CA",
                    ZipCode = "94105",
                    IsEmailVerified = true,
                    IsActive = true,
                    Role = UserRole.Buyer,
                    CreatedAt = DateTime.UtcNow.AddMonths(-1)
                },
                // Sports car enthusiast
                new User
                {
                    FirstName = "Robert",
                    LastName = "Martinez",
                    Email = "rob.martinez@speedshop.com",
                    Username = "speedking",
                    PasswordHash = BCrypt.Net.BCrypt.HashPassword("password"),
                    PhoneNumber = "+1-555-0605",
                    DateOfBirth = new DateTime(1975, 4, 12),
                    Address = "987 Racing Circuit",
                    City = "Phoenix",
                    State = "AZ",
                    ZipCode = "85001",
                    IsEmailVerified = true,
                    IsActive = true,
                    Role = UserRole.Buyer,
                    CreatedAt = DateTime.UtcNow.AddDays(-21) // 3 weeks ago
                }
            };

            return users;
        }

        private static List<Vehicle> CreateRealisticVehicles()
        {
            var vehicles = new List<Vehicle>
            {
                // Live auctions - ending soon
                new Vehicle
                {
                    Make = "Ferrari",
                    Model = "488 GTB",
                    Year = 2019,
                    Color = "Rosso Corsa Red",
                    VIN = "ZFF80AMA5K0235789",
                    Mileage = 8500,
                    FuelType = "Gasoline",
                    Transmission = "Automatic",
                    Description = "Pristine Ferrari 488 GTB with full service history. Carbon fiber racing package, premium leather interior, and track-ready performance.",
                    ImageUrls = new List<string>
                    {
                        "https://images.unsplash.com/photo-1583121274602-3e2820c69888?w=800",
                        "https://images.unsplash.com/photo-1558618047-3c8f8e92946d?w=800",
                        "https://images.unsplash.com/photo-1503736334956-4c8f8e92946d?w=800"
                    }
                },
                new Vehicle
                {
                    Make = "Porsche",
                    Model = "911 Turbo S",
                    Year = 2022,
                    Color = "Guards Red",
                    VIN = "WP0AC2A91NS123456",
                    Mileage = 2100,
                    FuelType = "Gasoline",
                    Transmission = "Automatic",
                    Description = "Like-new 911 Turbo S with only 2,100 miles. Factory warranty remaining, Sport Chrono package, and ceramic brakes.",
                    ImageUrls = new List<string>
                    {
                        "https://images.unsplash.com/photo-1544636331-e26879cd4d9b?w=800",
                        "https://images.unsplash.com/photo-1611016186353-9af58c69a533?w=800",
                        "https://images.unsplash.com/photo-1614162692292-7ac56d4df981?w=800"
                    }
                },
                // Live auctions - ending in hours
                new Vehicle
                {
                    Make = "Lamborghini",
                    Model = "Huracán EVO",
                    Year = 2021,
                    Color = "Arancio Borealis Orange",
                    VIN = "ZHWUC1ZF5MLA12345",
                    Mileage = 4200,
                    FuelType = "Gasoline",
                    Transmission = "Automatic",
                    Description = "Stunning Lamborghini Huracán EVO in rare Arancio Borealis. Full PPF protection, carbon fiber accents, and pristine condition.",
                    ImageUrls = new List<string>
                    {
                        "https://images.unsplash.com/photo-1544636331-e26879cd4d9b?w=800",
                        "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800",
                        "https://images.unsplash.com/photo-1571607388263-1044f9ea01dd?w=800"
                    }
                },
                new Vehicle
                {
                    Make = "McLaren",
                    Model = "720S",
                    Year = 2020,
                    Color = "McLaren Orange",
                    VIN = "SBM14DCA7LW123456",
                    Mileage = 6800,
                    FuelType = "Gasoline",
                    Transmission = "Automatic",
                    Description = "Exceptional McLaren 720S with carbon fiber body, luxury pack, and immaculate maintenance records.",
                    ImageUrls = new List<string>
                    {
                        "https://images.unsplash.com/photo-1544636331-e26879cd4d9b?w=800",
                        "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?w=800",
                        "https://images.unsplash.com/photo-1571607388263-1044f9ea01dd?w=800"
                    }
                },
                // Pending approval
                new Vehicle
                {
                    Make = "Aston Martin",
                    Model = "DB11",
                    Year = 2023,
                    Color = "Midnight Blue",
                    VIN = "SCFRMFAW5PGL12345",
                    Mileage = 1200,
                    FuelType = "Gasoline",
                    Transmission = "Automatic",
                    Description = "Brand new Aston Martin DB11 with full dealer warranty. Luxury interior package and premium paint finish.",
                    ImageUrls = new List<string>
                    {
                        "https://images.unsplash.com/photo-1571607388263-1044f9ea01dd?w=800",
                        "https://images.unsplash.com/photo-1525609004556-c46c7d6cf023?w=800",
                        "https://images.unsplash.com/photo-1520031441872-265e4ff70366?w=800"
                    }
                },
                new Vehicle
                {
                    Make = "BMW",
                    Model = "M4 Competition",
                    Year = 2023,
                    Color = "Alpine White",
                    VIN = "WBS3U9C07P5F12345",
                    Mileage = 3500,
                    FuelType = "Gasoline",
                    Transmission = "Automatic",
                    Description = "Latest BMW M4 Competition with M Performance exhaust, carbon package, and track-focused upgrades.",
                    ImageUrls = new List<string>
                    {
                        "https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800",
                        "https://images.unsplash.com/photo-1619976215249-95d6aa5b7d4f?w=800",
                        "https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800"
                    }
                },
                // Scheduled for future
                new Vehicle
                {
                    Make = "Tesla",
                    Model = "Model S Plaid",
                    Year = 2023,
                    Color = "Pearl White",
                    VIN = "5YJS3E1A7PF123456",
                    Mileage = 5200,
                    FuelType = "Electric",
                    Transmission = "Automatic",
                    Description = "Ultra-high performance Tesla Model S Plaid with tri-motor setup, carbon fiber spoiler, and premium interior.",
                    ImageUrls = new List<string>
                    {
                        "https://images.unsplash.com/photo-1617788138017-80ad40651399?w=800",
                        "https://images.unsplash.com/photo-1620891549027-942fdc95d7f5?w=800",
                        "https://images.unsplash.com/photo-1556875653-d3ca0d4dce37?w=800"
                    }
                },
                // Recently ended
                new Vehicle
                {
                    Make = "Mercedes-AMG",
                    Model = "GT 63 S",
                    Year = 2022,
                    Color = "Obsidian Black",
                    VIN = "WDDYK8AA0NA123456",
                    Mileage = 7800,
                    FuelType = "Gasoline",
                    Transmission = "Automatic",
                    Description = "Powerful Mercedes-AMG GT 63 S with handcrafted engine, premium interior, and exceptional road presence.",
                    ImageUrls = new List<string>
                    {
                        "https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=800",
                        "https://images.unsplash.com/photo-1553440569-bcc63803a83d?w=800",
                        "https://images.unsplash.com/photo-1594736797933-d0bd501ba2fe?w=800"
                    }
                }
            };

            return vehicles;
        }

        private static List<Auction> CreateRealisticAuctions(List<User> users, List<Vehicle> vehicles)
        {
            var now = DateTime.UtcNow;
            var admin = users.First(u => u.Role == UserRole.Admin);
            var dealer = users.First(u => u.Username == "luxurydealer");
            var collector = users.First(u => u.Username == "carCollector88");

            var auctions = new List<Auction>
            {
                // LIVE AUCTION - Ending in 15 minutes (Ferrari)
                new Auction
                {
                    Title = "2019 Ferrari 488 GTB - Track Ready Supercar",
                    Description = "Exceptional Ferrari 488 GTB with comprehensive service history and track-ready performance upgrades. This stunning supercar combines Italian craftsmanship with cutting-edge technology.",
                    VehicleId = vehicles[0].Id,
                    SellerId = dealer.Id,
                    StartingPrice = 220000m,
                    CurrentBid = 285000m,
                    ReservePrice = 270000m,
                    StartTime = now.AddDays(-3),
                    EndTime = now.AddMinutes(15),
                    Status = AuctionStatus.Active,
                    SubmittedByUserId = dealer.Id.ToString(),
                    SubmittedAt = now.AddDays(-5),
                    ApprovedByAdminId = admin.Id.ToString(),
                    ApprovedAt = now.AddDays(-4),
                    ContactInfo = "David Thompson - Luxury Car Specialist - (555) 0201",
                    HasReserve = true,
                    ViewCount = 247,
                    WatchCount = 23
                },
                // LIVE AUCTION - Ending in 1 hour (Porsche)
                new Auction
                {
                    Title = "2022 Porsche 911 Turbo S - Nearly New Condition",
                    Description = "Virtually brand new 911 Turbo S with factory warranty remaining. Sport Chrono package, ceramic brakes, and pristine maintenance.",
                    VehicleId = vehicles[1].Id,
                    SellerId = collector.Id,
                    StartingPrice = 180000m,
                    CurrentBid = 195500m,
                    ReservePrice = 190000m,
                    StartTime = now.AddDays(-2),
                    EndTime = now.AddHours(1),
                    Status = AuctionStatus.Active,
                    SubmittedByUserId = collector.Id.ToString(),
                    SubmittedAt = now.AddDays(-4),
                    ApprovedByAdminId = admin.Id.ToString(),
                    ApprovedAt = now.AddDays(-3),
                    ContactInfo = "Sarah Mitchell - Private Collector - (555) 0302",
                    HasReserve = true,
                    ViewCount = 189,
                    WatchCount = 18
                },
                // LIVE AUCTION - Ending in 6 hours (Lamborghini)
                new Auction
                {
                    Title = "2021 Lamborghini Huracán EVO - Rare Orange",
                    Description = "Stunning Huracán EVO in the rare Arancio Borealis color. Full paint protection film, carbon fiber accents, and immaculate condition throughout.",
                    VehicleId = vehicles[2].Id,
                    SellerId = dealer.Id,
                    StartingPrice = 240000m,
                    CurrentBid = 268750m,
                    ReservePrice = 260000m,
                    StartTime = now.AddDays(-1),
                    EndTime = now.AddHours(6),
                    Status = AuctionStatus.Active,
                    SubmittedByUserId = dealer.Id.ToString(),
                    SubmittedAt = now.AddDays(-3),
                    ApprovedByAdminId = admin.Id.ToString(),
                    ApprovedAt = now.AddDays(-2),
                    ContactInfo = "David Thompson - Luxury Car Specialist - (555) 0201",
                    HasReserve = true,
                    ViewCount = 156,
                    WatchCount = 15
                },
                // LIVE AUCTION - Ending in 24 hours (McLaren) - THE ONE IN YOUR SCREENSHOT
                new Auction
                {
                    Title = "2020 McLaren 720S - Signature Orange",
                    Description = "Exceptional McLaren 720S with carbon fiber body, luxury pack, and comprehensive maintenance records. A true driver's car.",
                    VehicleId = vehicles[3].Id,
                    SellerId = collector.Id,
                    StartingPrice = 50000m,
                    CurrentBid = 75000m,
                    ReservePrice = 85000m,
                    StartTime = now.AddHours(-12),
                    EndTime = now.AddHours(24),
                    Status = AuctionStatus.Active,
                    SubmittedByUserId = collector.Id.ToString(),
                    SubmittedAt = now.AddDays(-2),
                    ApprovedByAdminId = admin.Id.ToString(),
                    ApprovedAt = now.AddDays(-1),
                    ContactInfo = "Sarah Mitchell - Private Collector - (555) 0302",
                    HasReserve = true,
                    ViewCount = 98,
                    WatchCount = 12
                },
                // PENDING APPROVAL (Aston Martin)
                new Auction
                {
                    Title = "2023 Aston Martin DB11 - Factory Fresh",
                    Description = "Brand new Aston Martin DB11 with full dealer warranty. Luxury interior package and premium Midnight Blue paint finish.",
                    VehicleId = vehicles[4].Id,
                    SellerId = dealer.Id,
                    StartingPrice = 220000m,
                    CurrentBid = 220000m,
                    ReservePrice = 240000m,
                    StartTime = now.AddDays(2),
                    EndTime = now.AddDays(9),
                    Status = AuctionStatus.PendingApproval,
                    SubmittedByUserId = dealer.Id.ToString(),
                    SubmittedAt = now.AddHours(-6),
                    ContactInfo = "David Thompson - Luxury Car Specialist - (555) 0201",
                    HasReserve = true,
                    ViewCount = 45,
                    WatchCount = 8
                },
                // PENDING APPROVAL (BMW)
                new Auction
                {
                    Title = "2023 BMW M4 Competition - Track Package",
                    Description = "Latest BMW M4 Competition with M Performance exhaust, carbon package, and track-focused upgrades. Dealer maintained with full records.",
                    VehicleId = vehicles[5].Id,
                    SellerId = users.First(u => u.Username == "speedking").Id,
                    StartingPrice = 85000m,
                    CurrentBid = 85000m,
                    ReservePrice = 92000m,
                    StartTime = now.AddDays(1),
                    EndTime = now.AddDays(8),
                    Status = AuctionStatus.PendingApproval,
                    SubmittedByUserId = users.First(u => u.Username == "speedking").Id.ToString(),
                    SubmittedAt = now.AddHours(-3),
                    ContactInfo = "Robert Martinez - Performance Specialist - (555) 0605",
                    HasReserve = true,
                    ViewCount = 32,
                    WatchCount = 5
                },
                // SCHEDULED (Tesla)
                new Auction
                {
                    Title = "2023 Tesla Model S Plaid - Tri-Motor Beast",
                    Description = "Ultra-high performance Tesla Model S Plaid with tri-motor setup, carbon fiber spoiler, and premium interior. The future of performance.",
                    VehicleId = vehicles[6].Id,
                    SellerId = users.First(u => u.Username == "emilychen").Id,
                    StartingPrice = 120000m,
                    CurrentBid = 120000m,
                    ReservePrice = 130000m,
                    StartTime = now.AddDays(3),
                    EndTime = now.AddDays(10),
                    Status = AuctionStatus.Scheduled,
                    SubmittedByUserId = users.First(u => u.Username == "emilychen").Id.ToString(),
                    SubmittedAt = now.AddDays(-1),
                    ApprovedByAdminId = admin.Id.ToString(),
                    ApprovedAt = now.AddHours(-12),
                    ContactInfo = "Emily Chen - Tech Professional - (555) 0504",
                    HasReserve = true,
                    ViewCount = 67,
                    WatchCount = 11
                },
                // ENDED (Mercedes)
                new Auction
                {
                    Title = "2022 Mercedes-AMG GT 63 S - Handcrafted Power",
                    Description = "Powerful Mercedes-AMG GT 63 S with handcrafted engine, premium interior, and exceptional road presence. Recently serviced.",
                    VehicleId = vehicles[7].Id,
                    SellerId = dealer.Id,
                    StartingPrice = 150000m,
                    CurrentBid = 168500m,
                    ReservePrice = 160000m,
                    StartTime = now.AddDays(-5),
                    EndTime = now.AddHours(-2),
                    Status = AuctionStatus.Ended,
                    SubmittedByUserId = dealer.Id.ToString(),
                    SubmittedAt = now.AddDays(-7),
                    ApprovedByAdminId = admin.Id.ToString(),
                    ApprovedAt = now.AddDays(-6),
                    ContactInfo = "David Thompson - Luxury Car Specialist - (555) 0201",
                    HasReserve = true,
                    ViewCount = 203,
                    WatchCount = 19,
                    WinningBidId = null // Will be set after creating bids
                }
            };

            return auctions;
        }

        private static List<Bid> CreateRealisticBids(List<User> users, List<Auction> auctions)
        {
            var bids = new List<Bid>();
            var now = DateTime.UtcNow;
            
            var bidders = users.Where(u => u.Role == UserRole.Buyer).ToList();
            var user123 = users.First(u => u.Username == "user123");

            // Ferrari bids (ending soon - very active)
            var ferrariAuction = auctions.First(a => a.Title.Contains("Ferrari"));
            bids.AddRange(new[]
            {
                new Bid { AuctionId = ferrariAuction.Id, BidderId = bidders[1].Id, Amount = 220000m, Timestamp = now.AddDays(-3).AddMinutes(15) },
                new Bid { AuctionId = ferrariAuction.Id, BidderId = bidders[2].Id, Amount = 235000m, Timestamp = now.AddDays(-3).AddHours(2) },
                new Bid { AuctionId = ferrariAuction.Id, BidderId = bidders[3].Id, Amount = 245000m, Timestamp = now.AddDays(-2).AddHours(1) },
                new Bid { AuctionId = ferrariAuction.Id, BidderId = user123.Id, Amount = 255000m, Timestamp = now.AddDays(-2).AddHours(3) },
                new Bid { AuctionId = ferrariAuction.Id, BidderId = bidders[1].Id, Amount = 265000m, Timestamp = now.AddDays(-1).AddHours(2) },
                new Bid { AuctionId = ferrariAuction.Id, BidderId = bidders[4].Id, Amount = 275000m, Timestamp = now.AddHours(-8) },
                new Bid { AuctionId = ferrariAuction.Id, BidderId = bidders[2].Id, Amount = 285000m, Timestamp = now.AddHours(-2) }
            });

            // Porsche bids (ending in 1 hour)
            var porscheAuction = auctions.First(a => a.Title.Contains("Porsche"));
            bids.AddRange(new[]
            {
                new Bid { AuctionId = porscheAuction.Id, BidderId = bidders[2].Id, Amount = 180000m, Timestamp = now.AddDays(-2).AddMinutes(30) },
                new Bid { AuctionId = porscheAuction.Id, BidderId = user123.Id, Amount = 185000m, Timestamp = now.AddDays(-2).AddHours(4) },
                new Bid { AuctionId = porscheAuction.Id, BidderId = bidders[3].Id, Amount = 190000m, Timestamp = now.AddDays(-1).AddHours(6) },
                new Bid { AuctionId = porscheAuction.Id, BidderId = bidders[4].Id, Amount = 195500m, Timestamp = now.AddHours(-4) }
            });

            // Lamborghini bids (ending in 6 hours)
            var lamboAuction = auctions.First(a => a.Title.Contains("Lamborghini"));
            bids.AddRange(new[]
            {
                new Bid { AuctionId = lamboAuction.Id, BidderId = bidders[1].Id, Amount = 240000m, Timestamp = now.AddDays(-1).AddMinutes(20) },
                new Bid { AuctionId = lamboAuction.Id, BidderId = bidders[3].Id, Amount = 250000m, Timestamp = now.AddHours(-18) },
                new Bid { AuctionId = lamboAuction.Id, BidderId = user123.Id, Amount = 260000m, Timestamp = now.AddHours(-12) },
                new Bid { AuctionId = lamboAuction.Id, BidderId = bidders[4].Id, Amount = 268750m, Timestamp = now.AddHours(-3) }
            });

            // McLaren bids (ending in 24 hours) - THIS MATCHES YOUR SCREENSHOT
            var mclarenAuction = auctions.First(a => a.Title.Contains("McLaren"));
            bids.AddRange(new[]
            {
                new Bid { AuctionId = mclarenAuction.Id, BidderId = bidders[2].Id, Amount = 60000m, Timestamp = now.AddHours(-10) },
                new Bid { AuctionId = mclarenAuction.Id, BidderId = user123.Id, Amount = 65000m, Timestamp = now.AddHours(-8) },
                new Bid { AuctionId = mclarenAuction.Id, BidderId = bidders[1].Id, Amount = 70000m, Timestamp = now.AddHours(-6) },
                new Bid { AuctionId = mclarenAuction.Id, BidderId = bidders[3].Id, Amount = 75000m, Timestamp = now.AddHours(-2) }
            });

            // Mercedes bids (ended auction)
            var mercedesAuction = auctions.First(a => a.Title.Contains("Mercedes"));
            var endedBids = new[]
            {
                new Bid { AuctionId = mercedesAuction.Id, BidderId = bidders[1].Id, Amount = 150000m, Timestamp = now.AddDays(-5).AddMinutes(30) },
                new Bid { AuctionId = mercedesAuction.Id, BidderId = bidders[2].Id, Amount = 155000m, Timestamp = now.AddDays(-4).AddHours(2) },
                new Bid { AuctionId = mercedesAuction.Id, BidderId = user123.Id, Amount = 160000m, Timestamp = now.AddDays(-3).AddHours(4) },
                new Bid { AuctionId = mercedesAuction.Id, BidderId = bidders[4].Id, Amount = 165000m, Timestamp = now.AddDays(-2).AddHours(1) },
                new Bid { AuctionId = mercedesAuction.Id, BidderId = bidders[1].Id, Amount = 168500m, Timestamp = now.AddHours(-3) }
            };
            
            bids.AddRange(endedBids);
            
            return bids;
        }

        private static List<UserFavorite> CreateUserFavorites(List<User> users, List<Auction> auctions)
        {
            var favorites = new List<UserFavorite>();
            var now = DateTime.UtcNow;
            
            var buyers = users.Where(u => u.Role == UserRole.Buyer).ToList();
            var activeAuctions = auctions.Where(a => a.Status == AuctionStatus.Active).ToList();

            // Create some realistic favorites
            favorites.AddRange(new[]
            {
                new UserFavorite { UserId = buyers[0].Id, AuctionId = activeAuctions[0].Id, CreatedAt = now.AddDays(-2) },
                new UserFavorite { UserId = buyers[0].Id, AuctionId = activeAuctions[1].Id, CreatedAt = now.AddDays(-1) },
                new UserFavorite { UserId = buyers[1].Id, AuctionId = activeAuctions[0].Id, CreatedAt = now.AddHours(-12) },
                new UserFavorite { UserId = buyers[1].Id, AuctionId = activeAuctions[2].Id, CreatedAt = now.AddHours(-8) },
                new UserFavorite { UserId = buyers[2].Id, AuctionId = activeAuctions[1].Id, CreatedAt = now.AddHours(-6) },
                new UserFavorite { UserId = buyers[2].Id, AuctionId = activeAuctions[3].Id, CreatedAt = now.AddHours(-4) }
            });

            return favorites;
        }
    }
}