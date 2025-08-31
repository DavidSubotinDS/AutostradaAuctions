package com.example.autostradaauctions.data.mock

import com.example.autostradaauctions.data.model.*
import java.util.*

object MockData {

    // Mock Users
    val mockUsers = listOf(
        User(
            id = "user1",
            email = "john.doe@example.com",
            name = "John Doe",
            phoneNumber = "+1234567890",
            createdAt = System.currentTimeMillis() - 86400000,
            role = UserRole.USER
        ),
        User(
            id = "user2",
            email = "admin@autostrada.com",
            name = "Admin User",
            phoneNumber = "+1987654321",
            createdAt = System.currentTimeMillis() - 172800000,
            role = UserRole.ADMIN
        )
    )

    // Mock Vehicles
    val mockVehicles = listOf(
        Vehicle(
            id = "vehicle1",
            make = "BMW",
            model = "M3",
            year = 2021,
            mileage = 15000,
            engineType = "3.0L Twin Turbo I6",
            transmission = "8-Speed Automatic",
            fuelType = "Gasoline",
            color = "Alpine White",
            vin = "WBS8M9C52M5J12345",
            description = "Pristine BMW M3 with low mileage. One owner vehicle with full service history.",
            imageUrls = listOf(
                "https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800",
                "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800"
            ),
            condition = VehicleCondition.EXCELLENT,
            isVerified = true,
            createdAt = System.currentTimeMillis() - 432000000,
            updatedAt = System.currentTimeMillis() - 86400000
        ),
        Vehicle(
            id = "vehicle2",
            make = "Porsche",
            model = "911 Carrera",
            year = 2020,
            mileage = 8500,
            engineType = "3.0L Twin Turbo H6",
            transmission = "7-Speed Manual",
            fuelType = "Gasoline",
            color = "Guards Red",
            vin = "WP0AA2A96LS123456",
            description = "Beautiful Porsche 911 Carrera in Guards Red. Manual transmission, perfect condition.",
            imageUrls = listOf(
                "https://images.unsplash.com/photo-1544636331-e26879cd4d9b?w=800",
                "https://images.unsplash.com/photo-1583121274602-3e2820c69888?w=800"
            ),
            condition = VehicleCondition.EXCELLENT,
            isVerified = true,
            createdAt = System.currentTimeMillis() - 259200000,
            updatedAt = System.currentTimeMillis() - 172800000
        ),
        Vehicle(
            id = "vehicle3",
            make = "Mercedes-Benz",
            model = "C63 AMG",
            year = 2019,
            mileage = 22000,
            engineType = "4.0L Twin Turbo V8",
            transmission = "9-Speed Automatic",
            fuelType = "Gasoline",
            color = "Obsidian Black",
            vin = "55SWF8DB5KU123456",
            description = "Mercedes-Benz C63 AMG with AMG Performance Package. Excellent condition.",
            imageUrls = listOf(
                "https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=800"
            ),
            condition = VehicleCondition.GOOD,
            isVerified = true,
            createdAt = System.currentTimeMillis() - 345600000,
            updatedAt = System.currentTimeMillis() - 259200000
        )
    )

    // Mock Auctions
    val mockAuctions = listOf(
        Auction(
            id = "auction1",
            vehicleId = "vehicle1",
            sellerId = "user1",
            title = "2021 BMW M3 - Low Mileage",
            description = "Pristine BMW M3 with only 15,000 miles. Perfect condition, full service history.",
            startingPrice = 55000.0,
            currentPrice = 68500.0,
            reservePrice = 70000.0,
            startTime = System.currentTimeMillis() - 86400000,
            endTime = System.currentTimeMillis() + 172800000, // 2 days from now
            status = AuctionStatus.ACTIVE,
            totalBids = 12,
            highestBidderId = "user2",
            isReserveMet = false,
            createdAt = System.currentTimeMillis() - 86400000,
            updatedAt = System.currentTimeMillis() - 3600000
        ),
        Auction(
            id = "auction2",
            vehicleId = "vehicle2",
            sellerId = "user2",
            title = "2020 Porsche 911 Carrera - Manual",
            description = "Beautiful Guards Red Porsche 911 with manual transmission. Enthusiast owned.",
            startingPrice = 85000.0,
            currentPrice = 92000.0,
            reservePrice = 95000.0,
            startTime = System.currentTimeMillis() - 172800000,
            endTime = System.currentTimeMillis() + 86400000, // 1 day from now
            status = AuctionStatus.ACTIVE,
            totalBids = 8,
            highestBidderId = "user1",
            isReserveMet = false,
            createdAt = System.currentTimeMillis() - 172800000,
            updatedAt = System.currentTimeMillis() - 1800000
        ),
        Auction(
            id = "auction3",
            vehicleId = "vehicle3",
            sellerId = "user1",
            title = "2019 Mercedes-Benz C63 AMG",
            description = "Powerful C63 AMG with performance package. Well maintained and serviced.",
            startingPrice = 45000.0,
            currentPrice = 52000.0,
            reservePrice = null,
            startTime = System.currentTimeMillis() - 259200000,
            endTime = System.currentTimeMillis() + 432000000, // 5 days from now
            status = AuctionStatus.ACTIVE,
            totalBids = 15,
            highestBidderId = "user2",
            isReserveMet = true,
            createdAt = System.currentTimeMillis() - 259200000,
            updatedAt = System.currentTimeMillis() - 7200000
        )
    )

    // Mock Bids
    val mockBids = listOf(
        Bid(
            id = "bid1",
            auctionId = "auction1",
            bidderId = "user2",
            amount = 68500.0,
            timestamp = System.currentTimeMillis() - 3600000,
            isWinning = true
        ),
        Bid(
            id = "bid2",
            auctionId = "auction1",
            bidderId = "user1",
            amount = 67000.0,
            timestamp = System.currentTimeMillis() - 7200000,
            isWinning = false
        ),
        Bid(
            id = "bid3",
            auctionId = "auction2",
            bidderId = "user1",
            amount = 92000.0,
            timestamp = System.currentTimeMillis() - 1800000,
            isWinning = true
        )
    )

    // Mock Favorites
    val mockFavorites = listOf(
        Favorite(
            id = "fav1",
            userId = "user1",
            auctionId = "auction2",
            createdAt = System.currentTimeMillis() - 86400000
        ),
        Favorite(
            id = "fav2",
            userId = "user1",
            auctionId = "auction3",
            createdAt = System.currentTimeMillis() - 172800000
        )
    )

    // Mock Comments
    val mockComments = listOf(
        Comment(
            id = "comment1",
            auctionId = "auction1",
            userId = "user2",
            content = "Beautiful car! Any service records available?",
            timestamp = System.currentTimeMillis() - 14400000
        ),
        Comment(
            id = "comment2",
            auctionId = "auction1",
            userId = "user1",
            content = "Yes, full BMW service history. All maintenance up to date.",
            timestamp = System.currentTimeMillis() - 10800000,
            parentCommentId = "comment1"
        )
    )
}
