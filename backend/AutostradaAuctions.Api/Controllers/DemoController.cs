using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using AutostradaAuctions.Api.Services;

namespace AutostradaAuctions.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    // [Authorize(Roles = "Admin")] // Temporarily disabled for demo
    public class DemoController : ControllerBase
    {
        private readonly IDemoAuctionService _demoService;
        private readonly ILogger<DemoController> _logger;

        public DemoController(IDemoAuctionService demoService, ILogger<DemoController> logger)
        {
            _demoService = demoService;
            _logger = logger;
        }

        /// <summary>
        /// Reset all auctions for demo purposes with fresh timing
        /// </summary>
        [HttpPost("reset-auctions")]
        public async Task<IActionResult> ResetAuctionsForDemo()
        {
            try
            {
                _logger.LogInformation("Admin requested demo auction reset");
                await _demoService.ResetAllAuctionsForDemoAsync();
                
                return Ok(new { 
                    message = "Demo auctions have been reset with fresh timing",
                    timestamp = DateTime.UtcNow 
                });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error resetting demo auctions");
                return StatusCode(500, new { error = "Failed to reset auctions", details = ex.Message });
            }
        }

        /// <summary>
        /// Check and update expired auctions immediately
        /// </summary>
        [HttpPost("check-expired")]
        public async Task<IActionResult> CheckExpiredAuctions()
        {
            try
            {
                _logger.LogInformation("Admin requested expired auction check");
                await _demoService.CheckAndEndExpiredAuctionsAsync();
                
                return Ok(new { 
                    message = "Expired auctions have been checked and updated",
                    timestamp = DateTime.UtcNow 
                });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error checking expired auctions");
                return StatusCode(500, new { error = "Failed to check auctions", details = ex.Message });
            }
        }

        /// <summary>
        /// Get demo system status
        /// </summary>
        [HttpGet("status")]
        public IActionResult GetDemoStatus()
        {
            return Ok(new
            {
                message = "Demo auction system is active",
                features = new[]
                {
                    "Automatic auction timing reset on startup",
                    "Real-time auction status monitoring",
                    "Manual demo reset capability",
                    "Expired auction auto-ending"
                },
                timestamp = DateTime.UtcNow
            });
        }
    }
}