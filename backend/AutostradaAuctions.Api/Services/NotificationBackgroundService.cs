using AutostradaAuctions.Api.Services;

namespace AutostradaAuctions.Api.Services
{
    public class NotificationBackgroundService : BackgroundService
    {
        private readonly IServiceProvider _serviceProvider;
        private readonly ILogger<NotificationBackgroundService> _logger;
        private readonly TimeSpan _checkInterval = TimeSpan.FromMinutes(1); // Check every minute

        public NotificationBackgroundService(
            IServiceProvider serviceProvider,
            ILogger<NotificationBackgroundService> logger)
        {
            _serviceProvider = serviceProvider;
            _logger = logger;
        }

        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            _logger.LogInformation("🔔 Notification Background Service started");

            while (!stoppingToken.IsCancellationRequested)
            {
                try
                {
                    using var scope = _serviceProvider.CreateScope();
                    var notificationService = scope.ServiceProvider.GetRequiredService<INotificationService>();
                    
                    await notificationService.CheckAndSendPendingNotificationsAsync();
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Error in notification background service");
                }

                await Task.Delay(_checkInterval, stoppingToken);
            }

            _logger.LogInformation("🔔 Notification Background Service stopped");
        }
    }
}