using AutostradaAuctions.Api.Data;
using AutostradaAuctions.Api.Models;
using Microsoft.EntityFrameworkCore;

namespace AutostradaAuctions.Api.Services
{
    public interface INotificationService
    {
        Task CreateAuctionEndingNotificationsAsync(int auctionId);
        Task CheckAndSendPendingNotificationsAsync();
        Task<List<AuctionNotification>> GetUserNotificationsAsync(int userId, bool unreadOnly = false);
        Task MarkNotificationAsReadAsync(int notificationId);
        Task MarkAllNotificationsAsReadAsync(int userId);
    }

    public class NotificationService : INotificationService
    {
        private readonly AuctionDbContext _context;
        private readonly ILogger<NotificationService> _logger;

        public NotificationService(AuctionDbContext context, ILogger<NotificationService> logger)
        {
            _context = context;
            _logger = logger;
        }

        public async Task CreateAuctionEndingNotificationsAsync(int auctionId)
        {
            try
            {
                var auction = await _context.Auctions
                    .Include(a => a.Vehicle)
                    .FirstOrDefaultAsync(a => a.Id == auctionId);

                if (auction == null || auction.Status != AuctionStatus.Active)
                    return;

                // Get all users who have favorited this auction
                var userFavorites = await _context.UserFavorites
                    .Where(uf => uf.AuctionId == auctionId)
                    .ToListAsync();

                var notifications = new List<AuctionNotification>();
                var now = DateTime.UtcNow;

                foreach (var favorite in userFavorites)
                {
                    // Create notification schedule for different time intervals
                    var notificationTimes = new (NotificationType Type, double Hours)[]
                    {
                        (NotificationType.AuctionEndingIn24Hours, 24),
                        (NotificationType.AuctionEndingIn12Hours, 12),
                        (NotificationType.AuctionEndingIn6Hours, 6),
                        (NotificationType.AuctionEndingIn3Hours, 3),
                        (NotificationType.AuctionEndingIn1Hour, 1),
                        (NotificationType.AuctionEndingIn15Minutes, 0.25)
                    };

                    foreach (var notifTime in notificationTimes)
                    {
                        var triggerTime = auction.EndTime.AddHours(-notifTime.Hours);
                        
                        // Only create notification if trigger time is in the future
                        if (triggerTime > now)
                        {
                            var timeLeft = GetTimeLeftDescription(notifTime.Hours);
                            var notification = new AuctionNotification
                            {
                                UserId = favorite.UserId,
                                AuctionId = auctionId,
                                Type = notifTime.Type,
                                TriggerTime = triggerTime,
                                Title = $"🏁 Auction Ending Soon!",
                                Message = $"Your favorited {auction.Vehicle?.Year} {auction.Vehicle?.Make} {auction.Vehicle?.Model} auction ends in {timeLeft}. Current bid: ${auction.CurrentBid:N0}",
                                CreatedAt = now
                            };
                            notifications.Add(notification);
                        }
                    }
                }

                if (notifications.Any())
                {
                    await _context.AuctionNotifications.AddRangeAsync(notifications);
                    await _context.SaveChangesAsync();
                    _logger.LogInformation($"Created {notifications.Count} notifications for auction {auctionId}");
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, $"Error creating notifications for auction {auctionId}");
            }
        }

        public async Task CheckAndSendPendingNotificationsAsync()
        {
            try
            {
                var now = DateTime.UtcNow;
                var pendingNotifications = await _context.AuctionNotifications
                    .Include(n => n.Auction)
                    .ThenInclude(a => a.Vehicle)
                    .Include(n => n.User)
                    .Where(n => !n.IsSent && n.TriggerTime <= now)
                    .ToListAsync();

                foreach (var notification in pendingNotifications)
                {
                    // In a real app, you would send push notifications, emails, etc. here
                    // For now, we'll just mark them as sent and log
                    notification.IsSent = true;
                    notification.SentAt = now;
                    
                    _logger.LogInformation($"Sent notification to user {notification.UserId}: {notification.Message}");
                }

                if (pendingNotifications.Any())
                {
                    await _context.SaveChangesAsync();
                    _logger.LogInformation($"Processed {pendingNotifications.Count} pending notifications");
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error processing pending notifications");
            }
        }

        public async Task<List<AuctionNotification>> GetUserNotificationsAsync(int userId, bool unreadOnly = false)
        {
            var query = _context.AuctionNotifications
                .Include(n => n.Auction)
                .ThenInclude(a => a.Vehicle)
                .Where(n => n.UserId == userId && n.IsSent);

            if (unreadOnly)
                query = query.Where(n => !n.IsRead);

            return await query
                .OrderByDescending(n => n.CreatedAt)
                .Take(50) // Limit to 50 most recent
                .ToListAsync();
        }

        public async Task MarkNotificationAsReadAsync(int notificationId)
        {
            var notification = await _context.AuctionNotifications
                .FirstOrDefaultAsync(n => n.Id == notificationId);

            if (notification != null)
            {
                notification.IsRead = true;
                await _context.SaveChangesAsync();
            }
        }

        public async Task MarkAllNotificationsAsReadAsync(int userId)
        {
            var notifications = await _context.AuctionNotifications
                .Where(n => n.UserId == userId && !n.IsRead)
                .ToListAsync();

            foreach (var notification in notifications)
            {
                notification.IsRead = true;
            }

            await _context.SaveChangesAsync();
        }

        private string GetTimeLeftDescription(double hours)
        {
            if (hours >= 24)
                return $"{hours / 24:0} day(s)";
            else if (hours >= 1)
                return $"{hours:0} hour(s)";
            else
                return $"{hours * 60:0} minutes";
        }
    }
}