using Microsoft.EntityFrameworkCore;
using AutostradaAuctions.Api.Models;

namespace AutostradaAuctions.Api.Data
{
    public static class SeedImageUrls
    {
        public static async Task SeedAuctionImages(IServiceProvider serviceProvider)
        {
            using var scope = serviceProvider.CreateScope();
            var context = scope.ServiceProvider.GetRequiredService<AuctionDbContext>();

            // Sample car images from publicly available sources
            var teslaImages = new List<string>
            {
                "https://images.unsplash.com/photo-1617788138017-80ad40651399?w=800",
                "https://images.unsplash.com/photo-1620891549027-942fdc95d7f5?w=800",
                "https://images.unsplash.com/photo-1556875653-d3ca0d4dce37?w=800"
            };

            var bmwImages = new List<string>
            {
                "https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800",
                "https://images.unsplash.com/photo-1619976215249-95d6aa5b7d4f?w=800",
                "https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800"
            };

            var mercedesImages = new List<string>
            {
                "https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=800",
                "https://images.unsplash.com/photo-1553440569-bcc63803a83d?w=800",
                "https://images.unsplash.com/photo-1594736797933-d0bd501ba2fe?w=800"
            };

            // Update vehicles with images
            var vehicles = await context.Vehicles.ToListAsync();

            foreach (var vehicle in vehicles)
            {
                if (vehicle.ImageUrls == null || !vehicle.ImageUrls.Any())
                {
                    switch (vehicle.Make.ToLower())
                    {
                        case "tesla":
                            vehicle.ImageUrls = teslaImages;
                            break;
                        case "bmw":
                            vehicle.ImageUrls = bmwImages;
                            break;
                        case "mercedes-benz":
                            vehicle.ImageUrls = mercedesImages;
                            break;
                        default:
                            vehicle.ImageUrls = new List<string>
                            {
                                "https://images.unsplash.com/photo-1494976688954-90b803689993?w=800",
                                "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800"
                            };
                            break;
                    }
                }
            }

            await context.SaveChangesAsync();
        }

        public static async Task SeedAdditionalAuctions(IServiceProvider serviceProvider)
        {
            using var scope = serviceProvider.CreateScope();
            var context = scope.ServiceProvider.GetRequiredService<AuctionDbContext>();

            // Check if we already have enough auctions
            var existingAuctions = await context.Auctions.CountAsync();
            if (existingAuctions >= 10) return;

            // Get admin user
            var adminUser = await context.Users.FirstOrDefaultAsync(u => u.Role == UserRole.Admin);
            if (adminUser == null) return;

            // Add more luxury car auctions
            var additionalAuctions = new[]
            {
                new { Make = "Porsche", Model = "911 Turbo S", Year = 2023, Color = "Guards Red", Price = 180000m, Reserve = 195000m, Mileage = 2500 },
                new { Make = "Ferrari", Model = "F8 Tributo", Year = 2022, Color = "Rosso Corsa", Price = 280000m, Reserve = 300000m, Mileage = 1200 },
                new { Make = "Lamborghini", Model = "Huracán EVO", Year = 2023, Color = "Arancio Borealis", Price = 230000m, Reserve = 250000m, Mileage = 800 },
                new { Make = "Aston Martin", Model = "DB11", Year = 2022, Color = "Midnight Blue", Price = 200000m, Reserve = 220000m, Mileage = 3500 },
                new { Make = "McLaren", Model = "570S", Year = 2021, Color = "McLaren Orange", Price = 170000m, Reserve = 185000m, Mileage = 4200 }
            };

            foreach (var carData in additionalAuctions)
            {
                // Create vehicle with unique VIN
                var vehicle = new Vehicle
                {
                    Make = carData.Make,
                    Model = carData.Model,
                    Year = carData.Year,
                    Color = carData.Color,
                    VIN = GenerateUniqueVIN(carData.Make, carData.Year),
                    Mileage = carData.Mileage,
                    FuelType = "Gasoline",
                    Transmission = "Automatic",
                    Description = $"Exceptional {carData.Make} {carData.Model} in pristine condition.",
                    ImageUrls = GetCarImages(carData.Make)
                };

                context.Vehicles.Add(vehicle);
                await context.SaveChangesAsync();

                // Create auction
                var auction = new Auction
                {
                    Title = $"{carData.Year} {carData.Make} {carData.Model} - Luxury Sports Car",
                    Description = $"Stunning {carData.Make} {carData.Model} with premium features and exceptional performance. One owner, meticulously maintained.",
                    VehicleId = vehicle.Id,
                    SellerId = adminUser.Id,
                    StartingPrice = carData.Price,
                    CurrentBid = carData.Price,
                    ReservePrice = carData.Reserve,
                    StartTime = DateTime.UtcNow.AddDays(1),
                    EndTime = DateTime.UtcNow.AddDays(8),
                    Status = AuctionStatus.Scheduled,
                    SubmittedAt = DateTime.UtcNow
                };

                context.Auctions.Add(auction);
            }

            await context.SaveChangesAsync();
        }

        private static List<string> GetCarImages(string make)
        {
            return make.ToLower() switch
            {
                "porsche" => new List<string>
                {
                    "https://images.unsplash.com/photo-1544636331-e26879cd4d9b?w=800",
                    "https://images.unsplash.com/photo-1611016186353-9af58c69a533?w=800",
                    "https://images.unsplash.com/photo-1614162692292-7ac56d4df981?w=800"
                },
                "ferrari" => new List<string>
                {
                    "https://images.unsplash.com/photo-1583121274602-3e2820c69888?w=800",
                    "https://images.unsplash.com/photo-1558618047-3c8c76ca7d13?w=800",
                    "https://images.unsplash.com/photo-1503736334956-4c8f8e92946d?w=800"
                },
                "lamborghini" => new List<string>
                {
                    "https://images.unsplash.com/photo-1544636331-e26879cd4d9b?w=800",
                    "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800",
                    "https://images.unsplash.com/photo-1571607388263-1044f9ea01dd?w=800"
                },
                "aston martin" => new List<string>
                {
                    "https://images.unsplash.com/photo-1571607388263-1044f9ea01dd?w=800",
                    "https://images.unsplash.com/photo-1525609004556-c46c7d6cf023?w=800",
                    "https://images.unsplash.com/photo-1520031441872-265e4ff70366?w=800"
                },
                "mclaren" => new List<string>
                {
                    "https://images.unsplash.com/photo-1544636331-e26879cd4d9b?w=800",
                    "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?w=800",
                    "https://images.unsplash.com/photo-1571607388263-1044f9ea01dd?w=800"
                },
                _ => new List<string>
                {
                    "https://images.unsplash.com/photo-1494976688954-90b803689993?w=800",
                    "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800"
                }
            };
        }

        private static string GenerateUniqueVIN(string make, int year)
        {
            // Generate a unique VIN by combining make prefix, year, and timestamp
            var makePrefix = make.ToUpper().Substring(0, Math.Min(3, make.Length)).PadRight(3, 'X');
            var timestamp = DateTimeOffset.UtcNow.ToUnixTimeSeconds().ToString();
            var random = new Random().Next(100, 999);
            
            // VIN format: [3-char make][year][timestamp][random] truncated to 17 characters
            var vin = $"{makePrefix}{year}{timestamp}{random}";
            return vin.Length > 17 ? vin.Substring(0, 17) : vin.PadRight(17, '0');
        }
    }
}