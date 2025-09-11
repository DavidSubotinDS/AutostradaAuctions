using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace AutostradaAuctions.Api.Migrations
{
    /// <inheritdoc />
    public partial class UpdatePasswordHashes : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.UpdateData(
                table: "Users",
                keyColumn: "Id",
                keyValue: 1,
                column: "PasswordHash",
                value: "$2a$11$wiAQrFy99wpavWH3alOxku8emSuSu4hJX6Zd8yOdnJAeXH5qr/bDe");

            migrationBuilder.UpdateData(
                table: "Users",
                keyColumn: "Id",
                keyValue: 2,
                column: "PasswordHash",
                value: "$2a$11$wiAQrFy99wpavWH3alOxku8emSuSu4hJX6Zd8yOdnJAeXH5qr/bDe");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.UpdateData(
                table: "Users",
                keyColumn: "Id",
                keyValue: 1,
                column: "PasswordHash",
                value: "$2a$11$7aS8K6MX/7nfTJC0Vw9Nku.DcLy0rJPkrW8rHoY8JfR9g3aWp.yMe");

            migrationBuilder.UpdateData(
                table: "Users",
                keyColumn: "Id",
                keyValue: 2,
                column: "PasswordHash",
                value: "$2a$11$7aS8K6MX/7nfTJC0Vw9Nku.DcLy0rJPkrW8rHoY8JfR9g3aWp.yMe");
        }
    }
}
