using Microsoft.EntityFrameworkCore;
using AutostradaAuctions.Api.Data;
using AutostradaAuctions.Api.Models;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using System.Text;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Database configuration
builder.Services.AddDbContext<AuctionDbContext>(options =>
    options.UseSqlServer(builder.Configuration.GetConnectionString("DefaultConnection")));

// CORS configuration for Android app
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAndroidApp", policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

// SignalR for real-time bidding
builder.Services.AddSignalR();

// JWT Authentication
builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidateAudience = true,
            ValidateLifetime = true,
            ValidateIssuerSigningKey = true,
            ValidIssuer = builder.Configuration["Jwt:Issuer"],
            ValidAudience = builder.Configuration["Jwt:Audience"],
            IssuerSigningKey = new SymmetricSecurityKey(
                Encoding.UTF8.GetBytes(builder.Configuration["Jwt:Key"] ?? "default-key-for-development-only"))
        };
    });

builder.Services.AddAuthorization();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseCors("AllowAndroidApp");
app.UseAuthentication();
app.UseAuthorization();

app.MapControllers();

// Auto-migrate and seed database on startup
using (var scope = app.Services.CreateScope())
{
    var context = scope.ServiceProvider.GetRequiredService<AuctionDbContext>();
    try
    {
        await context.Database.MigrateAsync();
        Console.WriteLine("Database migration completed successfully.");
        
        // Seed additional sample data if needed
        await SeedSampleData(context);
    }
    catch (Exception ex)
    {
        Console.WriteLine($"Database migration failed: {ex.Message}");
        // Don't crash the app, just log the error
    }
}

app.Run();

// Method to seed comprehensive sample data
static async Task SeedSampleData(AuctionDbContext context)
{
    if (!await context.Auctions.AnyAsync())
    {
        // First, ensure we have sample users
        if (!await context.Users.AnyAsync())
        {
            var sampleUsers = new List<User>
            {
                new User
                {
                    FirstName = "BMW",
                    LastName = "Dealership",
                    Email = "dealer1@example.com",
                    PasswordHash = "hashed_password_1", // In real app, properly hash this
                    Role = UserRole.Seller,
                    IsActive = true,
                    CreatedAt = DateTime.UtcNow
                },
                new User
                {
                    FirstName = "Tesla",
                    LastName = "Center",
                    Email = "dealer2@example.com",
                    PasswordHash = "hashed_password_2",
                    Role = UserRole.Seller,
                    IsActive = true,
                    CreatedAt = DateTime.UtcNow
                }
            };
            context.Users.AddRange(sampleUsers);
            await context.SaveChangesAsync();
        }

        // Add sample vehicles first
        if (!await context.Vehicles.AnyAsync())
        {
            var sampleVehicles = new List<Vehicle>
            {
                new Vehicle
                {
                    Make = "BMW",
                    Model = "M3",
                    Year = 2020,
                    Color = "Alpine White",
                    VIN = "WBS8M9C55L5K01234",
                    Mileage = 15000,
                    FuelType = "Gasoline",
                    Transmission = "8-Speed Automatic",
                    Description = "Twin-turbo 3.0L inline-6 engine",
                    ImageUrls = new List<string> { "https://example.com/bmw1.jpg", "https://example.com/bmw2.jpg" }
                },
                new Vehicle
                {
                    Make = "Audi",
                    Model = "A4",
                    Year = 2019,
                    Color = "Brilliant Black",
                    VIN = "WAUENAF45KN012345",
                    Mileage = 25000,
                    FuelType = "Gasoline",
                    Transmission = "7-Speed S tronic",
                    Description = "Turbocharged 2.0L TFSI engine",
                    ImageUrls = new List<string> { "https://example.com/audi1.jpg", "https://example.com/audi2.jpg" }
                },
                new Vehicle
                {
                    Make = "Mercedes-Benz",
                    Model = "C-Class",
                    Year = 2021,
                    Color = "Obsidian Black",
                    VIN = "55SWF4KB5MU123456",
                    Mileage = 12000,
                    FuelType = "Gasoline",
                    Transmission = "9G-TRONIC Automatic",
                    Description = "Turbocharged 2.0L inline-4 engine",
                    ImageUrls = new List<string> { "https://example.com/mercedes1.jpg", "https://example.com/mercedes2.jpg" }
                },
                new Vehicle
                {
                    Make = "Tesla",
                    Model = "Model 3",
                    Year = 2018,
                    Color = "Pearl White",
                    VIN = "5YJ3E1EA7JF123456",
                    Mileage = 45000,
                    FuelType = "Electric",
                    Transmission = "Single-Speed Automatic",
                    Description = "Dual motor all-wheel drive",
                    ImageUrls = new List<string> { "https://example.com/tesla1.jpg", "https://example.com/tesla2.jpg" }
                },
                new Vehicle
                {
                    Make = "Porsche",
                    Model = "911",
                    Year = 2020,
                    Color = "Guards Red",
                    VIN = "WP0AB2A95LS123456",
                    Mileage = 8000,
                    FuelType = "Gasoline",
                    Transmission = "8-Speed PDK",
                    Description = "Twin-turbo 3.0L flat-6 engine",
                    ImageUrls = new List<string> { "https://example.com/porsche1.jpg", "https://example.com/porsche2.jpg" }
                },
                new Vehicle
                {
                    Make = "Ford",
                    Model = "Mustang",
                    Year = 2019,
                    Color = "Race Red",
                    VIN = "1FA6P8CF1K5123456",
                    Mileage = 18000,
                    FuelType = "Gasoline",
                    Transmission = "6-Speed Manual",
                    Description = "5.0L Coyote V8 engine",
                    ImageUrls = new List<string> { "https://example.com/mustang1.jpg", "https://example.com/mustang2.jpg" }
                }
            };
            context.Vehicles.AddRange(sampleVehicles);
            await context.SaveChangesAsync();
        }

        // Then add sample auctions
        var sampleAuctions = new List<Auction>
        {
            new Auction
            {
                Title = "2020 BMW M3 Competition",
                Description = "Perfect condition BMW M3 with low mileage",
                VehicleId = 1,
                SellerId = 1,
                StartingPrice = 45000m,
                ReservePrice = 50000m,
                CurrentBid = 52000m,
                StartTime = new DateTime(2025, 9, 8, 10, 0, 0, DateTimeKind.Utc),
                EndTime = new DateTime(2025, 9, 12, 18, 0, 0, DateTimeKind.Utc),
                Status = AuctionStatus.Active
            },
            new Auction
            {
                Title = "2019 Audi A4 Quattro",
                Description = "Excellent condition Audi A4 with all-wheel drive",
                VehicleId = 2,
                SellerId = 1,
                StartingPrice = 28000m,
                ReservePrice = 30000m,
                CurrentBid = 32500m,
                StartTime = new DateTime(2025, 9, 9, 14, 0, 0, DateTimeKind.Utc),
                EndTime = new DateTime(2025, 9, 11, 20, 0, 0, DateTimeKind.Utc),
                Status = AuctionStatus.Active
            },
            new Auction
            {
                Title = "2021 Mercedes-Benz C-Class",
                Description = "Luxury sedan with premium features",
                VehicleId = 3,
                SellerId = 1,
                StartingPrice = 35000m,
                ReservePrice = 37000m,
                CurrentBid = 38900m,
                StartTime = new DateTime(2025, 9, 10, 9, 0, 0, DateTimeKind.Utc),
                EndTime = new DateTime(2025, 9, 13, 17, 0, 0, DateTimeKind.Utc),
                Status = AuctionStatus.Active
            },
            new Auction
            {
                Title = "2018 Tesla Model 3 Performance",
                Description = "Electric performance sedan with autopilot",
                VehicleId = 4,
                SellerId = 2,
                StartingPrice = 32000m,
                ReservePrice = 34000m,
                CurrentBid = 35200m,
                StartTime = new DateTime(2025, 9, 7, 12, 0, 0, DateTimeKind.Utc),
                EndTime = new DateTime(2025, 9, 11, 15, 0, 0, DateTimeKind.Utc),
                Status = AuctionStatus.Active
            },
            new Auction
            {
                Title = "2020 Porsche 911 Carrera S",
                Description = "Iconic sports car in pristine condition",
                VehicleId = 5,
                SellerId = 1,
                StartingPrice = 75000m,
                ReservePrice = 80000m,
                CurrentBid = 85500m,
                StartTime = new DateTime(2025, 9, 9, 16, 0, 0, DateTimeKind.Utc),
                EndTime = new DateTime(2025, 9, 14, 19, 0, 0, DateTimeKind.Utc),
                Status = AuctionStatus.Active
            },
            new Auction
            {
                Title = "2019 Ford Mustang GT",
                Description = "American muscle car with V8 power",
                VehicleId = 6,
                SellerId = 1,
                StartingPrice = 25000m,
                ReservePrice = 27000m,
                CurrentBid = 28750m,
                StartTime = new DateTime(2025, 9, 8, 11, 0, 0, DateTimeKind.Utc),
                EndTime = new DateTime(2025, 9, 12, 14, 0, 0, DateTimeKind.Utc),
                Status = AuctionStatus.Active
            }
        };

        context.Auctions.AddRange(sampleAuctions);
        await context.SaveChangesAsync();
        Console.WriteLine("Comprehensive sample auction data seeded successfully.");
    }
}
