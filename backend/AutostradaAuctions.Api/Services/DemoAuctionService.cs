using AutostradaAuctions.Api.Data;
using AutostradaAuctions.Api.Models;
using Microsoft.EntityFrameworkCore;

namespace AutostradaAuctions.Api.Services
{
    public interface IDemoAuctionService
    {
        Task ResetAllAuctionsForDemoAsync();
        Task CheckAndEndExpiredAuctionsAsync();
    }

    public class DemoAuctionService : IDemoAuctionService
    {
        private readonly AuctionDbContext _context;
        private readonly ILogger<DemoAuctionService> _logger;

        public DemoAuctionService(AuctionDbContext context, ILogger<DemoAuctionService> logger)
        {
            _context = context;
            _logger = logger;
        }

        public async Task ResetAllAuctionsForDemoAsync()
        {
            _logger.LogInformation("🔄 Starting demo auction reset...");
            
            var now = DateTime.UtcNow;
            
            // Get all auctions
            var auctions = await _context.Auctions.ToListAsync();
            
            foreach (var auction in auctions)
            {
                // Reset auction timings based on original demo intent
                switch (auction.Title)
                {
                    case var title when title.Contains("Ferrari"):
                        // Ending soon - 30 minutes
                        auction.StartTime = now.AddHours(-2);
                        auction.EndTime = now.AddMinutes(30);
                        auction.Status = AuctionStatus.Active;
                        break;
                        
                    case var title when title.Contains("Porsche"):
                        // Ending in 2 hours
                        auction.StartTime = now.AddHours(-1);
                        auction.EndTime = now.AddHours(2);
                        auction.Status = AuctionStatus.Active;
                        break;
                        
                    case var title when title.Contains("Lamborghini"):
                        // Ending in 6 hours
                        auction.StartTime = now.AddMinutes(-30);
                        auction.EndTime = now.AddHours(6);
                        auction.Status = AuctionStatus.Active;
                        break;
                        
                    case var title when title.Contains("McLaren"):
                        // Ending in 1 day
                        auction.StartTime = now.AddHours(-4);
                        auction.EndTime = now.AddHours(24);
                        auction.Status = AuctionStatus.Active;
                        break;
                        
                    case var title when title.Contains("Aston Martin"):
                        // Starting in 2 hours, ending in 3 days
                        auction.StartTime = now.AddHours(2);
                        auction.EndTime = now.AddDays(3);
                        auction.Status = AuctionStatus.Scheduled;
                        break;
                        
                    case var title when title.Contains("BMW"):
                        // Pending approval
                        auction.StartTime = now.AddDays(1);
                        auction.EndTime = now.AddDays(8);
                        auction.Status = AuctionStatus.PendingApproval;
                        break;
                        
                    case var title when title.Contains("Tesla"):
                        // Scheduled for future
                        auction.StartTime = now.AddDays(3);
                        auction.EndTime = now.AddDays(10);
                        auction.Status = AuctionStatus.Scheduled;
                        break;
                        
                    case var title when title.Contains("Mercedes"):
                        // Recently ended
                        auction.StartTime = now.AddDays(-7);
                        auction.EndTime = now.AddHours(-2);
                        auction.Status = AuctionStatus.Ended;
                        break;
                        
                    default:
                        // Default: active auction ending in 4 hours
                        auction.StartTime = now.AddHours(-1);
                        auction.EndTime = now.AddHours(4);
                        auction.Status = AuctionStatus.Active;
                        break;
                }
                
                _logger.LogInformation($"🔄 Reset auction '{auction.Title}' - Status: {auction.Status}, Ends: {auction.EndTime:yyyy-MM-dd HH:mm:ss}");
            }
            
            await _context.SaveChangesAsync();
            _logger.LogInformation("✅ Demo auction reset completed!");
        }

        public async Task CheckAndEndExpiredAuctionsAsync()
        {
            var now = DateTime.UtcNow;
            
            // Find active auctions that have expired
            var expiredAuctions = await _context.Auctions
                .Where(a => a.Status == AuctionStatus.Active && a.EndTime <= now)
                .ToListAsync();
            
            foreach (var auction in expiredAuctions)
            {
                auction.Status = AuctionStatus.Ended;
                _logger.LogInformation($"🏁 Auction '{auction.Title}' has ended");
            }
            
            // Find scheduled auctions that should start
            var auctionsToStart = await _context.Auctions
                .Where(a => a.Status == AuctionStatus.Scheduled && a.StartTime <= now)
                .ToListAsync();
            
            foreach (var auction in auctionsToStart)
            {
                auction.Status = AuctionStatus.Active;
                _logger.LogInformation($"🚀 Auction '{auction.Title}' has started");
            }
            
            if (expiredAuctions.Any() || auctionsToStart.Any())
            {
                await _context.SaveChangesAsync();
            }
        }
    }
}