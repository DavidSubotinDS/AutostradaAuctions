using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

#pragma warning disable CA1814 // Prefer jagged arrays over multidimensional

namespace AutostradaAuctions.Api.Migrations
{
    /// <inheritdoc />
    public partial class InitialSQLiteCreate : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "Users",
                columns: table => new
                {
                    Id = table.Column<int>(type: "INTEGER", nullable: false)
                        .Annotation("Sqlite:Autoincrement", true),
                    FirstName = table.Column<string>(type: "TEXT", maxLength: 100, nullable: false),
                    LastName = table.Column<string>(type: "TEXT", maxLength: 100, nullable: false),
                    Email = table.Column<string>(type: "TEXT", maxLength: 255, nullable: false),
                    Username = table.Column<string>(type: "TEXT", maxLength: 50, nullable: false),
                    PasswordHash = table.Column<string>(type: "TEXT", nullable: false),
                    PhoneNumber = table.Column<string>(type: "TEXT", maxLength: 20, nullable: false),
                    DateOfBirth = table.Column<DateTime>(type: "TEXT", nullable: false),
                    Address = table.Column<string>(type: "TEXT", maxLength: 200, nullable: false),
                    City = table.Column<string>(type: "TEXT", maxLength: 100, nullable: false),
                    State = table.Column<string>(type: "TEXT", maxLength: 50, nullable: false),
                    ZipCode = table.Column<string>(type: "TEXT", maxLength: 20, nullable: false),
                    IsEmailVerified = table.Column<bool>(type: "INTEGER", nullable: false),
                    IsActive = table.Column<bool>(type: "INTEGER", nullable: false),
                    Role = table.Column<int>(type: "INTEGER", nullable: false),
                    CreatedAt = table.Column<DateTime>(type: "TEXT", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Users", x => x.Id);
                });

            migrationBuilder.CreateTable(
                name: "Vehicles",
                columns: table => new
                {
                    Id = table.Column<int>(type: "INTEGER", nullable: false)
                        .Annotation("Sqlite:Autoincrement", true),
                    Make = table.Column<string>(type: "TEXT", maxLength: 100, nullable: false),
                    Model = table.Column<string>(type: "TEXT", maxLength: 100, nullable: false),
                    Year = table.Column<int>(type: "INTEGER", nullable: false),
                    Color = table.Column<string>(type: "TEXT", maxLength: 50, nullable: false),
                    VIN = table.Column<string>(type: "TEXT", maxLength: 17, nullable: false),
                    Mileage = table.Column<int>(type: "INTEGER", nullable: false),
                    FuelType = table.Column<string>(type: "TEXT", maxLength: 20, nullable: false),
                    Transmission = table.Column<string>(type: "TEXT", maxLength: 20, nullable: false),
                    Description = table.Column<string>(type: "TEXT", nullable: false),
                    ImageUrls = table.Column<string>(type: "TEXT", nullable: false),
                    CreatedAt = table.Column<DateTime>(type: "TEXT", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Vehicles", x => x.Id);
                });

            migrationBuilder.CreateTable(
                name: "AuctionNotifications",
                columns: table => new
                {
                    Id = table.Column<int>(type: "INTEGER", nullable: false)
                        .Annotation("Sqlite:Autoincrement", true),
                    UserId = table.Column<int>(type: "INTEGER", nullable: false),
                    AuctionId = table.Column<int>(type: "INTEGER", nullable: false),
                    Type = table.Column<int>(type: "INTEGER", nullable: false),
                    TriggerTime = table.Column<DateTime>(type: "TEXT", nullable: false),
                    Title = table.Column<string>(type: "TEXT", maxLength: 200, nullable: false),
                    Message = table.Column<string>(type: "TEXT", maxLength: 1000, nullable: false),
                    IsRead = table.Column<bool>(type: "INTEGER", nullable: false),
                    IsSent = table.Column<bool>(type: "INTEGER", nullable: false),
                    CreatedAt = table.Column<DateTime>(type: "TEXT", nullable: false),
                    SentAt = table.Column<DateTime>(type: "TEXT", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_AuctionNotifications", x => x.Id);
                    table.ForeignKey(
                        name: "FK_AuctionNotifications_Users_UserId",
                        column: x => x.UserId,
                        principalTable: "Users",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "Auctions",
                columns: table => new
                {
                    Id = table.Column<int>(type: "INTEGER", nullable: false)
                        .Annotation("Sqlite:Autoincrement", true),
                    Title = table.Column<string>(type: "TEXT", maxLength: 200, nullable: false),
                    Description = table.Column<string>(type: "TEXT", nullable: false),
                    VehicleId = table.Column<int>(type: "INTEGER", nullable: false),
                    SellerId = table.Column<int>(type: "INTEGER", nullable: false),
                    StartingPrice = table.Column<decimal>(type: "decimal(18,2)", nullable: false),
                    ReservePrice = table.Column<decimal>(type: "decimal(18,2)", nullable: true),
                    CurrentBid = table.Column<decimal>(type: "decimal(18,2)", nullable: false),
                    StartTime = table.Column<DateTime>(type: "TEXT", nullable: false),
                    EndTime = table.Column<DateTime>(type: "TEXT", nullable: false),
                    Status = table.Column<int>(type: "INTEGER", nullable: false),
                    WinningBidId = table.Column<int>(type: "INTEGER", nullable: true),
                    CreatedAt = table.Column<DateTime>(type: "TEXT", nullable: false),
                    SubmittedByUserId = table.Column<string>(type: "TEXT", nullable: true),
                    SubmittedAt = table.Column<DateTime>(type: "TEXT", nullable: false),
                    ApprovedByAdminId = table.Column<string>(type: "TEXT", nullable: true),
                    ApprovedAt = table.Column<DateTime>(type: "TEXT", nullable: true),
                    RejectionReason = table.Column<string>(type: "TEXT", nullable: true),
                    ViewCount = table.Column<int>(type: "INTEGER", nullable: false),
                    WatchCount = table.Column<int>(type: "INTEGER", nullable: false),
                    ContactInfo = table.Column<string>(type: "TEXT", nullable: false),
                    BuyNowPrice = table.Column<decimal>(type: "decimal(18,2)", nullable: true),
                    HasReserve = table.Column<bool>(type: "INTEGER", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Auctions", x => x.Id);
                    table.ForeignKey(
                        name: "FK_Auctions_Users_SellerId",
                        column: x => x.SellerId,
                        principalTable: "Users",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Restrict);
                    table.ForeignKey(
                        name: "FK_Auctions_Vehicles_VehicleId",
                        column: x => x.VehicleId,
                        principalTable: "Vehicles",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Restrict);
                });

            migrationBuilder.CreateTable(
                name: "Bids",
                columns: table => new
                {
                    Id = table.Column<int>(type: "INTEGER", nullable: false)
                        .Annotation("Sqlite:Autoincrement", true),
                    AuctionId = table.Column<int>(type: "INTEGER", nullable: false),
                    BidderId = table.Column<int>(type: "INTEGER", nullable: false),
                    Amount = table.Column<decimal>(type: "decimal(18,2)", nullable: false),
                    Timestamp = table.Column<DateTime>(type: "TEXT", nullable: false),
                    IsWinning = table.Column<bool>(type: "INTEGER", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Bids", x => x.Id);
                    table.ForeignKey(
                        name: "FK_Bids_Auctions_AuctionId",
                        column: x => x.AuctionId,
                        principalTable: "Auctions",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "FK_Bids_Users_BidderId",
                        column: x => x.BidderId,
                        principalTable: "Users",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Restrict);
                });

            migrationBuilder.CreateTable(
                name: "UserFavorites",
                columns: table => new
                {
                    Id = table.Column<int>(type: "INTEGER", nullable: false)
                        .Annotation("Sqlite:Autoincrement", true),
                    UserId = table.Column<int>(type: "INTEGER", nullable: false),
                    AuctionId = table.Column<int>(type: "INTEGER", nullable: false),
                    CreatedAt = table.Column<DateTime>(type: "TEXT", nullable: false)
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
                table: "Users",
                columns: new[] { "Id", "Address", "City", "CreatedAt", "DateOfBirth", "Email", "FirstName", "IsActive", "IsEmailVerified", "LastName", "PasswordHash", "PhoneNumber", "Role", "State", "Username", "ZipCode" },
                values: new object[,]
                {
                    { 1, "", "", new DateTime(2024, 1, 1, 0, 0, 0, 0, DateTimeKind.Utc), new DateTime(1990, 1, 1, 0, 0, 0, 0, DateTimeKind.Utc), "admin@autostrada.com", "Admin", true, true, "User", "$2a$11$wiAQrFy99wpavWH3alOxku8emSuSu4hJX6Zd8yOdnJAeXH5qr/bDe", "", 3, "", "admin123", "" },
                    { 2, "", "", new DateTime(2024, 1, 1, 0, 0, 0, 0, DateTimeKind.Utc), new DateTime(1995, 1, 1, 0, 0, 0, 0, DateTimeKind.Utc), "user@autostrada.com", "Regular", true, true, "User", "$2a$11$wiAQrFy99wpavWH3alOxku8emSuSu4hJX6Zd8yOdnJAeXH5qr/bDe", "", 1, "", "user123", "" }
                });

            migrationBuilder.InsertData(
                table: "Vehicles",
                columns: new[] { "Id", "Color", "CreatedAt", "Description", "FuelType", "ImageUrls", "Make", "Mileage", "Model", "Transmission", "VIN", "Year" },
                values: new object[,]
                {
                    { 1, "Pearl White", new DateTime(2024, 1, 1, 0, 0, 0, 0, DateTimeKind.Utc), "Pristine Tesla Model S with Autopilot, premium interior, and full self-driving capability.", "Electric", "[]", "Tesla", 15000, "Model S", "Automatic", "5YJSA1E20MF000001", 2023 },
                    { 2, "Alpine White", new DateTime(2024, 1, 1, 0, 0, 0, 0, DateTimeKind.Utc), "High-performance BMW M3 with carbon fiber package and track-ready suspension.", "Gasoline", "[]", "BMW", 8500, "M3", "Manual", "WBS8M9C59N5000002", 2022 },
                    { 3, "Obsidian Black", new DateTime(2024, 1, 1, 0, 0, 0, 0, DateTimeKind.Utc), "Luxury sports car with handcrafted AMG engine and premium leather interior.", "Gasoline", "[]", "Mercedes-Benz", 5200, "AMG GT", "Automatic", "WDDYK7HA2JA000003", 2023 }
                });

            migrationBuilder.InsertData(
                table: "Auctions",
                columns: new[] { "Id", "ApprovedAt", "ApprovedByAdminId", "BuyNowPrice", "ContactInfo", "CreatedAt", "CurrentBid", "Description", "EndTime", "HasReserve", "RejectionReason", "ReservePrice", "SellerId", "StartTime", "StartingPrice", "Status", "SubmittedAt", "SubmittedByUserId", "Title", "VehicleId", "ViewCount", "WatchCount", "WinningBidId" },
                values: new object[,]
                {
                    { 10, null, null, null, "", new DateTime(2025, 9, 7, 12, 0, 0, 0, DateTimeKind.Utc), 67500m, "High-performance BMW M3 with carbon fiber package, track-ready suspension, and manual transmission for the driving purist.", new DateTime(2025, 9, 14, 12, 15, 0, 0, DateTimeKind.Utc), true, null, 72000m, 2, new DateTime(2025, 9, 8, 12, 0, 0, 0, DateTimeKind.Utc), 65000m, 3, new DateTime(2025, 9, 7, 12, 0, 0, 0, DateTimeKind.Utc), "2", "2022 BMW M3 - Track-Ready Performance (ENDING SOON!)", 2, 45, 12, null },
                    { 11, null, null, null, "", new DateTime(2025, 9, 8, 12, 0, 0, 0, DateTimeKind.Utc), 78500m, "Experience the future of driving with this pristine Tesla Model S featuring Autopilot, premium interior, and full self-driving capability.", new DateTime(2025, 9, 14, 13, 0, 0, 0, DateTimeKind.Utc), true, null, 85000m, 2, new DateTime(2025, 9, 9, 12, 0, 0, 0, DateTimeKind.Utc), 75000m, 3, new DateTime(2025, 9, 8, 12, 0, 0, 0, DateTimeKind.Utc), "2", "2023 Tesla Model S - Premium Electric Sedan", 1, 62, 18, null },
                    { 12, null, null, null, "", new DateTime(2025, 9, 11, 12, 0, 0, 0, DateTimeKind.Utc), 98000m, "Stunning luxury sports car with handcrafted AMG engine, premium leather interior, and cutting-edge technology.", new DateTime(2025, 9, 14, 18, 0, 0, 0, DateTimeKind.Utc), true, null, 105000m, 2, new DateTime(2025, 9, 12, 12, 0, 0, 0, DateTimeKind.Utc), 95000m, 3, new DateTime(2025, 9, 11, 12, 0, 0, 0, DateTimeKind.Utc), "2", "2023 Mercedes-AMG GT - Luxury Sports Car", 3, 34, 8, null },
                    { 13, null, null, null, "", new DateTime(2025, 9, 14, 10, 0, 0, 0, DateTimeKind.Utc), 45000m, "Powerful V8 engine, premium interior, and classic American muscle car styling. Perfect for enthusiasts.", new DateTime(2025, 9, 21, 12, 0, 0, 0, DateTimeKind.Utc), true, null, 52000m, 2, new DateTime(2025, 9, 14, 12, 0, 0, 0, DateTimeKind.Utc), 45000m, 1, new DateTime(2025, 9, 14, 10, 0, 0, 0, DateTimeKind.Utc), "2", "2019 Ford Mustang GT - American Muscle", 1, 5, 2, null },
                    { 14, null, null, null, "", new DateTime(2025, 9, 7, 12, 0, 0, 0, DateTimeKind.Utc), 185000m, "Ultimate performance machine with twin-turbo flat-six engine and PDK transmission. Track-tested perfection.", new DateTime(2025, 9, 15, 11, 0, 0, 0, DateTimeKind.Utc), true, null, 195000m, 1, new DateTime(2025, 9, 8, 12, 0, 0, 0, DateTimeKind.Utc), 180000m, 3, new DateTime(2025, 9, 7, 12, 0, 0, 0, DateTimeKind.Utc), "1", "2021 Porsche 911 Turbo S - Track Beast", 1, 89, 25, null }
                });

            migrationBuilder.InsertData(
                table: "UserFavorites",
                columns: new[] { "Id", "AuctionId", "CreatedAt", "UserId" },
                values: new object[,]
                {
                    { 1, 11, new DateTime(2024, 1, 1, 0, 0, 0, 0, DateTimeKind.Utc), 1 },
                    { 2, 12, new DateTime(2024, 1, 1, 1, 0, 0, 0, DateTimeKind.Utc), 1 }
                });

            migrationBuilder.CreateIndex(
                name: "IX_AuctionNotifications_AuctionId",
                table: "AuctionNotifications",
                column: "AuctionId");

            migrationBuilder.CreateIndex(
                name: "IX_AuctionNotifications_UserId_IsSent_TriggerTime",
                table: "AuctionNotifications",
                columns: new[] { "UserId", "IsSent", "TriggerTime" });

            migrationBuilder.CreateIndex(
                name: "IX_Auctions_SellerId",
                table: "Auctions",
                column: "SellerId");

            migrationBuilder.CreateIndex(
                name: "IX_Auctions_VehicleId",
                table: "Auctions",
                column: "VehicleId");

            migrationBuilder.CreateIndex(
                name: "IX_Auctions_WinningBidId",
                table: "Auctions",
                column: "WinningBidId");

            migrationBuilder.CreateIndex(
                name: "IX_Bids_AuctionId_Amount",
                table: "Bids",
                columns: new[] { "AuctionId", "Amount" });

            migrationBuilder.CreateIndex(
                name: "IX_Bids_BidderId",
                table: "Bids",
                column: "BidderId");

            migrationBuilder.CreateIndex(
                name: "IX_UserFavorites_AuctionId",
                table: "UserFavorites",
                column: "AuctionId");

            migrationBuilder.CreateIndex(
                name: "IX_UserFavorites_UserId_AuctionId",
                table: "UserFavorites",
                columns: new[] { "UserId", "AuctionId" },
                unique: true);

            migrationBuilder.CreateIndex(
                name: "IX_Users_Email",
                table: "Users",
                column: "Email",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "IX_Vehicles_VIN",
                table: "Vehicles",
                column: "VIN",
                unique: true);

            migrationBuilder.AddForeignKey(
                name: "FK_AuctionNotifications_Auctions_AuctionId",
                table: "AuctionNotifications",
                column: "AuctionId",
                principalTable: "Auctions",
                principalColumn: "Id",
                onDelete: ReferentialAction.Cascade);

            migrationBuilder.AddForeignKey(
                name: "FK_Auctions_Bids_WinningBidId",
                table: "Auctions",
                column: "WinningBidId",
                principalTable: "Bids",
                principalColumn: "Id");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_Bids_Auctions_AuctionId",
                table: "Bids");

            migrationBuilder.DropTable(
                name: "AuctionNotifications");

            migrationBuilder.DropTable(
                name: "UserFavorites");

            migrationBuilder.DropTable(
                name: "Auctions");

            migrationBuilder.DropTable(
                name: "Bids");

            migrationBuilder.DropTable(
                name: "Vehicles");

            migrationBuilder.DropTable(
                name: "Users");
        }
    }
}
