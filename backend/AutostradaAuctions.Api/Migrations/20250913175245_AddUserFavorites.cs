using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

#pragma warning disable CA1814 // Prefer jagged arrays over multidimensional

namespace AutostradaAuctions.Api.Migrations
{
    /// <inheritdoc />
    public partial class AddUserFavorites : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "UserFavorites",
                columns: table => new
                {
                    Id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    UserId = table.Column<int>(type: "int", nullable: false),
                    AuctionId = table.Column<int>(type: "int", nullable: false),
                    CreatedAt = table.Column<DateTime>(type: "datetime2", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_UserFavorites", x => x.Id);
                    table.ForeignKey(
                        name: "FK_UserFavorites_Auctions_AuctionId",
                        column: x => x.AuctionId,
                        principalTable: "Auctions",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "FK_UserFavorites_Users_UserId",
                        column: x => x.UserId,
                        principalTable: "Users",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.InsertData(
                table: "Auctions",
                columns: new[] { "Id", "CreatedAt", "CurrentBid", "Description", "EndTime", "ReservePrice", "SellerId", "StartTime", "StartingPrice", "Status", "Title", "VehicleId", "WinningBidId" },
                values: new object[,]
                {
                    { 10, new DateTime(2024, 1, 1, 0, 0, 0, 0, DateTimeKind.Utc), 75000m, "Experience the future of driving with this pristine Tesla Model S featuring Autopilot, premium interior, and full self-driving capability.", new DateTime(2024, 1, 9, 0, 0, 0, 0, DateTimeKind.Utc), 85000m, 2, new DateTime(2024, 1, 2, 0, 0, 0, 0, DateTimeKind.Utc), 75000m, 1, "2023 Tesla Model S - Premium Electric Sedan", 1, null },
                    { 11, new DateTime(2024, 1, 1, 0, 0, 0, 0, DateTimeKind.Utc), 67500m, "High-performance BMW M3 with carbon fiber package, track-ready suspension, and manual transmission for the driving purist.", new DateTime(2024, 1, 8, 0, 0, 0, 0, DateTimeKind.Utc), 72000m, 2, new DateTime(2024, 1, 1, 0, 0, 0, 0, DateTimeKind.Utc), 65000m, 2, "2022 BMW M3 - Track-Ready Performance", 2, null }
                });

            migrationBuilder.InsertData(
                table: "Vehicles",
                columns: new[] { "Id", "Color", "CreatedAt", "Description", "FuelType", "ImageUrls", "Make", "Mileage", "Model", "Transmission", "VIN", "Year" },
                values: new object[] { 3, "Obsidian Black", new DateTime(2024, 1, 1, 0, 0, 0, 0, DateTimeKind.Utc), "Luxury sports car with handcrafted AMG engine and premium leather interior.", "Gasoline", "[]", "Mercedes-Benz", 5200, "AMG GT", "Automatic", "WDDYK7HA2JA000003", 2023 });

            migrationBuilder.InsertData(
                table: "Auctions",
                columns: new[] { "Id", "CreatedAt", "CurrentBid", "Description", "EndTime", "ReservePrice", "SellerId", "StartTime", "StartingPrice", "Status", "Title", "VehicleId", "WinningBidId" },
                values: new object[] { 12, new DateTime(2024, 1, 1, 0, 0, 0, 0, DateTimeKind.Utc), 95000m, "Stunning luxury sports car with handcrafted AMG engine, premium leather interior, and cutting-edge technology.", new DateTime(2024, 1, 10, 0, 0, 0, 0, DateTimeKind.Utc), 105000m, 2, new DateTime(2024, 1, 3, 0, 0, 0, 0, DateTimeKind.Utc), 95000m, 1, "2023 Mercedes-AMG GT - Luxury Sports Car", 3, null });

            migrationBuilder.InsertData(
                table: "UserFavorites",
                columns: new[] { "Id", "AuctionId", "CreatedAt", "UserId" },
                values: new object[,]
                {
                    { 1, 11, new DateTime(2024, 1, 1, 0, 0, 0, 0, DateTimeKind.Utc), 1 },
                    { 2, 12, new DateTime(2024, 1, 1, 1, 0, 0, 0, DateTimeKind.Utc), 1 }
                });

            migrationBuilder.CreateIndex(
                name: "IX_UserFavorites_AuctionId",
                table: "UserFavorites",
                column: "AuctionId");

            migrationBuilder.CreateIndex(
                name: "IX_UserFavorites_UserId_AuctionId",
                table: "UserFavorites",
                columns: new[] { "UserId", "AuctionId" },
                unique: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "UserFavorites");

            migrationBuilder.DeleteData(
                table: "Auctions",
                keyColumn: "Id",
                keyValue: 10);

            migrationBuilder.DeleteData(
                table: "Auctions",
                keyColumn: "Id",
                keyValue: 11);

            migrationBuilder.DeleteData(
                table: "Auctions",
                keyColumn: "Id",
                keyValue: 12);

            migrationBuilder.DeleteData(
                table: "Vehicles",
                keyColumn: "Id",
                keyValue: 3);
        }
    }
}
