using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace AutostradaAuctions.Api.Migrations
{
    /// <inheritdoc />
    public partial class FixUser123PasswordHash : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            // Fix user123 password hash to match the working admin hash for "password"
            // This updates user123's PasswordHash to the same BCrypt hash that works for admin
            migrationBuilder.Sql(@"
                UPDATE Users 
                SET PasswordHash = '$2a$11$wiAQrFy99wpavWH3alOxku8emSuSu4hJX6Zd8yOdnJAeXH5qr/bDe'
                WHERE Email = 'user@autostrada.com';
            ");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            // Rollback: Restore original user123 password hash
            migrationBuilder.Sql(@"
                UPDATE Users 
                SET PasswordHash = '$2a$11$Ub0VIi.3punIWbYjaHsl/OXM7YG.TEV6kwjmZwVk7bYyx0zuIjuAa'
                WHERE Email = 'user@autostrada.com';
            ");
        }
    }
}
