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

// Method to seed additional sample data
static async Task SeedSampleData(AuctionDbContext context)
{
    if (!await context.Auctions.AnyAsync())
    {
        // Add sample auctions
        var sampleAuctions = new List<Auction>
        {
            new Auction
            {
                Title = "2023 Tesla Model S - Low Mileage",
                Description = "Pristine condition Tesla Model S with all premium features.",
                VehicleId = 1,
                SellerId = 1,
                StartingPrice = 50000m,
                ReservePrice = 75000m,
                CurrentBid = 50000m,
                StartTime = DateTime.UtcNow.AddHours(1),
                EndTime = DateTime.UtcNow.AddDays(7),
                Status = AuctionStatus.Scheduled
            },
            new Auction
            {
                Title = "2022 BMW M3 - Performance Package",
                Description = "Track-ready BMW M3 with carbon fiber upgrades.",
                VehicleId = 2,
                SellerId = 1,
                StartingPrice = 35000m,
                ReservePrice = 45000m,
                CurrentBid = 35000m,
                StartTime = DateTime.UtcNow.AddHours(-2),
                EndTime = DateTime.UtcNow.AddDays(3),
                Status = AuctionStatus.Active
            }
        };

        context.Auctions.AddRange(sampleAuctions);
        await context.SaveChangesAsync();
        Console.WriteLine("Sample auction data seeded successfully.");
    }
}
