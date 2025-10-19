using AutostradaAuctions.Api.Services;

namespace AutostradaAuctions.Api.Services
{
    public class AuctionMonitoringService : BackgroundService
    {
        private readonly IServiceProvider _serviceProvider;
        private readonly ILogger<AuctionMonitoringService> _logger;
        private readonly TimeSpan _checkInterval = TimeSpan.FromMinutes(1); // Check every minute

        public AuctionMonitoringService(
            IServiceProvider serviceProvider,
            ILogger<AuctionMonitoringService> logger)
        {
            _serviceProvider = serviceProvider;
            _logger = logger;
        }

        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            _logger.LogInformation("🎯 Auction Monitoring Service started");

            // Initial demo reset when service starts
            try
            {
                using var scope = _serviceProvider.CreateScope();
                var demoService = scope.ServiceProvider.GetRequiredService<IDemoAuctionService>();
                await demoService.ResetAllAuctionsForDemoAsync();
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error during initial demo reset");
            }

            while (!stoppingToken.IsCancellationRequested)
            {
                try
                {
                    using var scope = _serviceProvider.CreateScope();
                    var demoService = scope.ServiceProvider.GetRequiredService<IDemoAuctionService>();
                    
                    await demoService.CheckAndEndExpiredAuctionsAsync();
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Error in auction monitoring service");
                }

                await Task.Delay(_checkInterval, stoppingToken);
            }

            _logger.LogInformation("🎯 Auction Monitoring Service stopped");
        }
    }
}