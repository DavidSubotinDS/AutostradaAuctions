using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using AutostradaAuctions.Api.Data;
using AutostradaAuctions.Api.Models;
using System.ComponentModel.DataAnnotations;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;

namespace AutostradaAuctions.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly AuctionDbContext _context;
        private readonly IConfiguration _configuration;
        
        public AuthController(AuctionDbContext context, IConfiguration configuration)
        {
            _context = context;
            _configuration = configuration;
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody] LoginRequest request)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            Console.WriteLine($"Login attempt with email: '{request.Email}', password: '{request.Password}'");

            // Find user by username or email (using Email field from request)
            var user = await _context.Users
                .FirstOrDefaultAsync(u => u.Username == request.Email || u.Email == request.Email);

            Console.WriteLine($"User found: {user != null}");
            if (user != null)
            {
                Console.WriteLine($"User details: Username='{user.Username}', Email='{user.Email}', IsActive={user.IsActive}");
                Console.WriteLine($"Stored hash: '{user.PasswordHash}'");
                var passwordMatches = BCrypt.Net.BCrypt.Verify(request.Password, user.PasswordHash);
                Console.WriteLine($"Password verification result: {passwordMatches}");
            }

            if (user == null || !user.IsActive)
                return Unauthorized("Invalid credentials");

            // Verify password
            if (!BCrypt.Net.BCrypt.Verify(request.Password, user.PasswordHash))
                return Unauthorized("Invalid credentials");

            // Generate JWT token
            var accessToken = GenerateJwtToken(user);
            var refreshToken = GenerateRefreshToken();
            var expiresAt = DateTime.UtcNow.AddYears(100).ToString("yyyy-MM-ddTHH:mm:ssZ");

            // Return user info with token
            var userInfo = new LoginResponse
            {
                Token = accessToken,
                RefreshToken = refreshToken,
                ExpiresAt = expiresAt,
                User = new UserInfo
                {
                    Id = user.Id,
                    Username = user.Username,
                    Email = user.Email,
                    FirstName = user.FirstName,
                    LastName = user.LastName,
                    Role = user.Role.ToString(),
                    IsActive = user.IsActive
                }
            };

            return Ok(userInfo);
        }

        private string GenerateJwtToken(User user)
        {
            var jwtKey = _configuration["Jwt:Key"] ?? "your-super-secret-key-that-is-at-least-32-characters-long";
            var key = Encoding.ASCII.GetBytes(jwtKey);

            var tokenDescriptor = new SecurityTokenDescriptor
            {
                Subject = new ClaimsIdentity(new[]
                {
                    new Claim(ClaimTypes.NameIdentifier, user.Id.ToString()),
                    new Claim(ClaimTypes.Name, user.Username),
                    new Claim(ClaimTypes.Email, user.Email),
                    new Claim(ClaimTypes.Role, user.Role.ToString())
                }),
                Expires = DateTime.UtcNow.AddYears(100), // Effectively never expires
                Issuer = _configuration["Jwt:Issuer"] ?? "AutostradaAuctions",
                Audience = _configuration["Jwt:Audience"] ?? "AutostradaAuctions",
                SigningCredentials = new SigningCredentials(new SymmetricSecurityKey(key), SecurityAlgorithms.HmacSha256Signature)
            };

            var tokenHandler = new JwtSecurityTokenHandler();
            var token = tokenHandler.CreateToken(tokenDescriptor);
            return tokenHandler.WriteToken(token);
        }

        private string GenerateRefreshToken()
        {
            var randomNumber = new byte[32];
            using var rng = System.Security.Cryptography.RandomNumberGenerator.Create();
            rng.GetBytes(randomNumber);
            return Convert.ToBase64String(randomNumber);
        }

        [HttpPost("register")]
        public async Task<IActionResult> Register([FromBody] RegisterRequest request)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            // Check if username or email already exists
            var existingUser = await _context.Users
                .AnyAsync(u => u.Username == request.Username || u.Email == request.Email);

            if (existingUser)
                return BadRequest("Username or email already exists");

            // Hash password
            var hashedPassword = BCrypt.Net.BCrypt.HashPassword(request.Password);

            var user = new User
            {
                Username = request.Username,
                Email = request.Email,
                FirstName = request.FirstName,
                LastName = request.LastName,
                PasswordHash = hashedPassword,
                Role = UserRole.Buyer, // Default role
                IsActive = true,
                CreatedAt = DateTime.UtcNow
            };

            _context.Users.Add(user);
            await _context.SaveChangesAsync();

            // Generate JWT token for new user
            var accessToken = GenerateJwtToken(user);
            var refreshToken = GenerateRefreshToken();
            var expiresAt = DateTime.UtcNow.AddYears(100).ToString("yyyy-MM-ddTHH:mm:ssZ");

            var userInfo = new LoginResponse
            {
                Token = accessToken,
                RefreshToken = refreshToken,
                ExpiresAt = expiresAt,
                User = new UserInfo
                {
                    Id = user.Id,
                    Username = user.Username,
                    Email = user.Email,
                    FirstName = user.FirstName,
                    LastName = user.LastName,
                    Role = user.Role.ToString(),
                    IsActive = user.IsActive
                }
            };

            return Created("", userInfo);
        }
    }

    public class LoginRequest
    {
        [Required]
        public string Email { get; set; } = string.Empty;
        
        [Required]
        public string Password { get; set; } = string.Empty;
    }

    public class RegisterRequest
    {
        [Required]
        [StringLength(50)]
        public string Username { get; set; } = string.Empty;
        
        [Required]
        [EmailAddress]
        public string Email { get; set; } = string.Empty;
        
        [Required]
        public string FirstName { get; set; } = string.Empty;
        
        [Required]
        public string LastName { get; set; } = string.Empty;
        
        [Required]
        [MinLength(6)]
        public string Password { get; set; } = string.Empty;
    }

    public class LoginResponse
    {
        public string Token { get; set; } = string.Empty;
        public string RefreshToken { get; set; } = string.Empty;
        public UserInfo User { get; set; } = new UserInfo();
        public string ExpiresAt { get; set; } = string.Empty;
    }

    public class UserInfo
    {
        public int Id { get; set; }
        public string Username { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;
        public string FirstName { get; set; } = string.Empty;
        public string LastName { get; set; } = string.Empty;
        public string Role { get; set; } = string.Empty;
        public bool IsActive { get; set; }
    }
}
