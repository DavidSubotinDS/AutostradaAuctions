using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using AutostradaAuctions.Api.Services;
using System.Security.Claims;

namespace AutostradaAuctions.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class NotificationsController : ControllerBase
    {
        private readonly INotificationService _notificationService;

        public NotificationsController(INotificationService notificationService)
        {
            _notificationService = notificationService;
        }

        [HttpGet]
        public async Task<IActionResult> GetNotifications([FromQuery] bool unreadOnly = false)
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (!int.TryParse(userIdClaim, out int userId))
            {
                return Unauthorized();
            }

            var notifications = await _notificationService.GetUserNotificationsAsync(userId, unreadOnly);
            return Ok(notifications);
        }

        [HttpPost("{notificationId}/mark-read")]
        public async Task<IActionResult> MarkAsRead(int notificationId)
        {
            await _notificationService.MarkNotificationAsReadAsync(notificationId);
            return Ok();
        }

        [HttpPost("mark-all-read")]
        public async Task<IActionResult> MarkAllAsRead()
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (!int.TryParse(userIdClaim, out int userId))
            {
                return Unauthorized();
            }

            await _notificationService.MarkAllNotificationsAsReadAsync(userId);
            return Ok();
        }

        [HttpPost("create-for-auction/{auctionId}")]
        public async Task<IActionResult> CreateNotificationsForAuction(int auctionId)
        {
            await _notificationService.CreateAuctionEndingNotificationsAsync(auctionId);
            return Ok(new { message = "Notifications created successfully" });
        }
    }
}