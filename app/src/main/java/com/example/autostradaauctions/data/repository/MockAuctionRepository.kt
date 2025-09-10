package com.example.autostradaauctions.data.repository

import com.example.autostradaauctions.data.model.Auction
import com.example.autostradaauctions.data.model.AuctionDetail
import com.example.autostradaauctions.data.model.Vehicle
import com.example.autostradaauctions.data.model.VehicleDetail
import kotlinx.coroutines.delay

class MockAuctionRepository : AuctionRepository() {
    
    // Mock data for testing
    private val mockAuctions = listOf(
        Auction(
            id = 1,
            title = "2020 BMW M3 Competition",
            description = "Perfect condition BMW M3 with low mileage",
            startingPrice = 45000.0,
            currentBid = 52000.0,
            reservePrice = 50000.0,
            startTime = "2025-09-08T10:00:00Z",
            endTime = "2025-09-12T18:00:00Z",
            status = "Active",
            vehicle = Vehicle(
                id = 1,
                make = "BMW",
                model = "M3",
                year = 2020,
                color = "Alpine White",
                mileage = 15000,
                imageUrls = listOf("https://example.com/bmw1.jpg", "https://example.com/bmw2.jpg"),
                vin = "WBS8M9C55L5K01234",
                fuelType = "Gasoline",
                transmission = "8-Speed Automatic",
                description = "Twin-turbo 3.0L inline-6 engine"
            ),
            sellerName = "BMW Dealership"
        ),
        Auction(
            id = 2,
            title = "2019 Audi A4 Quattro",
            description = "Excellent condition Audi A4 with all-wheel drive",
            startingPrice = 28000.0,
            currentBid = 32500.0,
            reservePrice = 30000.0,
            startTime = "2025-09-09T14:00:00Z",
            endTime = "2025-09-11T20:00:00Z",
            status = "Active",
            vehicle = Vehicle(
                id = 2,
                make = "Audi",
                model = "A4",
                year = 2019,
                color = "Brilliant Black",
                mileage = 25000,
                imageUrls = listOf("https://example.com/audi1.jpg", "https://example.com/audi2.jpg"),
                vin = "WAUENAF45KN012345",
                fuelType = "Gasoline",
                transmission = "7-Speed S tronic",
                description = "Turbocharged 2.0L TFSI engine"
            ),
            sellerName = "Audi Center"
        ),
        Auction(
            id = 3,
            title = "2021 Mercedes-Benz C-Class",
            description = "Luxury sedan with premium features",
            startingPrice = 35000.0,
            currentBid = 38900.0,
            reservePrice = 37000.0,
            startTime = "2025-09-10T09:00:00Z",
            endTime = "2025-09-13T17:00:00Z",
            status = "Active",
            vehicle = Vehicle(
                id = 3,
                make = "Mercedes-Benz",
                model = "C-Class",
                year = 2021,
                color = "Obsidian Black",
                mileage = 12000,
                imageUrls = listOf("https://example.com/mercedes1.jpg", "https://example.com/mercedes2.jpg"),
                vin = "55SWF4KB5MU123456",
                fuelType = "Gasoline",
                transmission = "9G-TRONIC Automatic",
                description = "Turbocharged 2.0L inline-4 engine"
            ),
            sellerName = "Mercedes-Benz Dealer"
        ),
        Auction(
            id = 4,
            title = "2018 Tesla Model 3 Performance",
            description = "Electric performance sedan with autopilot",
            startingPrice = 32000.0,
            currentBid = 35200.0,
            reservePrice = 34000.0,
            startTime = "2025-09-07T12:00:00Z",
            endTime = "2025-09-11T15:00:00Z",
            status = "Active",
            vehicle = Vehicle(
                id = 4,
                make = "Tesla",
                model = "Model 3",
                year = 2018,
                color = "Pearl White",
                mileage = 45000,
                imageUrls = listOf("https://example.com/tesla1.jpg", "https://example.com/tesla2.jpg"),
                vin = "5YJ3E1EA7JF123456",
                fuelType = "Electric",
                transmission = "Single-Speed Automatic",
                description = "Dual motor all-wheel drive"
            ),
            sellerName = "Tesla Owner"
        ),
        Auction(
            id = 5,
            title = "2020 Porsche 911 Carrera S",
            description = "Iconic sports car in pristine condition",
            startingPrice = 75000.0,
            currentBid = 85500.0,
            reservePrice = 80000.0,
            startTime = "2025-09-09T16:00:00Z",
            endTime = "2025-09-14T19:00:00Z",
            status = "Active",
            vehicle = Vehicle(
                id = 5,
                make = "Porsche",
                model = "911",
                year = 2020,
                color = "Guards Red",
                mileage = 8000,
                imageUrls = listOf("https://example.com/porsche1.jpg", "https://example.com/porsche2.jpg"),
                vin = "WP0AB2A95LS123456",
                fuelType = "Gasoline",
                transmission = "8-Speed PDK",
                description = "Twin-turbo 3.0L flat-6 engine"
            ),
            sellerName = "Porsche Specialist"
        ),
        Auction(
            id = 6,
            title = "2019 Ford Mustang GT",
            description = "American muscle car with V8 power",
            startingPrice = 25000.0,
            currentBid = 28750.0,
            reservePrice = 27000.0,
            startTime = "2025-09-08T11:00:00Z",
            endTime = "2025-09-12T14:00:00Z",
            status = "Active",
            vehicle = Vehicle(
                id = 6,
                make = "Ford",
                model = "Mustang",
                year = 2019,
                color = "Race Red",
                mileage = 18000,
                imageUrls = listOf("https://example.com/mustang1.jpg", "https://example.com/mustang2.jpg"),
                vin = "1FA6P8CF1K5123456",
                fuelType = "Gasoline",
                transmission = "6-Speed Manual",
                description = "5.0L Coyote V8 engine"
            ),
            sellerName = "Ford Performance"
        )
    )
    
    override suspend fun getAuctions(): Result<List<Auction>> {
        // Simulate network delay
        delay(1000)
        return Result.success(mockAuctions)
    }
    
    override suspend fun getAuctionDetail(id: Int): Result<AuctionDetail> {
        delay(500)
        
        val auction = mockAuctions.find { it.id == id }
            ?: return Result.failure(Exception("Auction not found"))
        
        // Convert Auction to AuctionDetail
        val auctionDetail = AuctionDetail(
            id = auction.id,
            title = auction.title,
            description = auction.description,
            startingPrice = auction.startingPrice,
            currentBid = auction.currentBid,
            reservePrice = auction.reservePrice,
            startTime = auction.startTime,
            endTime = auction.endTime,
            status = auction.status,
            vehicle = VehicleDetail(
                id = auction.vehicle.id,
                make = auction.vehicle.make,
                model = auction.vehicle.model,
                year = auction.vehicle.year,
                color = auction.vehicle.color,
                vin = auction.vehicle.vin ?: "Unknown",
                mileage = auction.vehicle.mileage,
                fuelType = auction.vehicle.fuelType ?: "Unknown",
                transmission = auction.vehicle.transmission ?: "Unknown",
                description = auction.vehicle.description ?: "",
                imageUrls = auction.vehicle.imageUrls
            ),
            sellerName = auction.sellerName,
            bids = emptyList() // Mock empty bids for now
        )
        
        return Result.success(auctionDetail)
    }
}
