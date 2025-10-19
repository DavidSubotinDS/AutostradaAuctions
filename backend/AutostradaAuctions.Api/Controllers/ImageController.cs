using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using System.ComponentModel.DataAnnotations;

namespace AutostradaAuctions.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class ImageController : ControllerBase
    {
        private readonly IWebHostEnvironment _environment;
        private readonly ILogger<ImageController> _logger;
        
        public ImageController(IWebHostEnvironment environment, ILogger<ImageController> logger)
        {
            _environment = environment;
            _logger = logger;
        }

        [HttpPost("upload")]
        [Authorize]
        public async Task<IActionResult> UploadImage(IFormFile file)
        {
            try
            {
                if (file == null || file.Length == 0)
                    return BadRequest("No file uploaded");

                // Validate file type
                var allowedExtensions = new[] { ".jpg", ".jpeg", ".png", ".gif" };
                var fileExtension = Path.GetExtension(file.FileName).ToLowerInvariant();
                
                if (!allowedExtensions.Contains(fileExtension))
                    return BadRequest("Only image files (jpg, jpeg, png, gif) are allowed");

                // Validate file size (max 5MB)
                if (file.Length > 5 * 1024 * 1024)
                    return BadRequest("File size must be less than 5MB");

                // Create uploads directory if it doesn't exist
                var uploadsDir = Path.Combine(_environment.WebRootPath ?? _environment.ContentRootPath, "uploads");
                if (!Directory.Exists(uploadsDir))
                    Directory.CreateDirectory(uploadsDir);

                // Generate unique filename
                var fileName = $"{Guid.NewGuid()}{fileExtension}";
                var filePath = Path.Combine(uploadsDir, fileName);

                // Save file
                using (var stream = new FileStream(filePath, FileMode.Create))
                {
                    await file.CopyToAsync(stream);
                }

                // Return the relative URL
                var imageUrl = $"/uploads/{fileName}";
                
                return Ok(new { imageUrl, fileName });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error uploading image");
                return StatusCode(500, "Error uploading image");
            }
        }

        [HttpPost("upload-multiple")]
        [Authorize]
        public async Task<IActionResult> UploadMultipleImages(List<IFormFile> files)
        {
            try
            {
                if (files == null || files.Count == 0)
                    return BadRequest("No files uploaded");

                if (files.Count > 10)
                    return BadRequest("Maximum 10 files allowed");

                var results = new List<object>();
                var uploadsDir = Path.Combine(_environment.WebRootPath ?? _environment.ContentRootPath, "uploads");
                
                if (!Directory.Exists(uploadsDir))
                    Directory.CreateDirectory(uploadsDir);

                foreach (var file in files)
                {
                    if (file.Length == 0) continue;

                    // Validate file type
                    var allowedExtensions = new[] { ".jpg", ".jpeg", ".png", ".gif" };
                    var fileExtension = Path.GetExtension(file.FileName).ToLowerInvariant();
                    
                    if (!allowedExtensions.Contains(fileExtension))
                        continue;

                    // Validate file size (max 5MB)
                    if (file.Length > 5 * 1024 * 1024)
                        continue;

                    // Generate unique filename
                    var fileName = $"{Guid.NewGuid()}{fileExtension}";
                    var filePath = Path.Combine(uploadsDir, fileName);

                    // Save file
                    using (var stream = new FileStream(filePath, FileMode.Create))
                    {
                        await file.CopyToAsync(stream);
                    }

                    // Add to results
                    var imageUrl = $"/uploads/{fileName}";
                    results.Add(new { imageUrl, fileName });
                }

                return Ok(new { images = results });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error uploading multiple images");
                return StatusCode(500, "Error uploading images");
            }
        }

        [HttpDelete("{fileName}")]
        [Authorize]
        public IActionResult DeleteImage(string fileName)
        {
            try
            {
                var uploadsDir = Path.Combine(_environment.WebRootPath ?? _environment.ContentRootPath, "uploads");
                var filePath = Path.Combine(uploadsDir, fileName);

                if (System.IO.File.Exists(filePath))
                {
                    System.IO.File.Delete(filePath);
                    return Ok(new { message = "Image deleted successfully" });
                }

                return NotFound("Image not found");
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error deleting image");
                return StatusCode(500, "Error deleting image");
            }
        }
    }
}