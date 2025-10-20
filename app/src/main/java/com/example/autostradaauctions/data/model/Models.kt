package com.example.autostradaauctions.data.model

data class Auction(
    val id: Int,
    val title: String,
    val description: String,
    val startingPrice: Double,
    val currentBid: Double,
    val reservePrice: Double?,
    val startTime: String,
    val endTime: String,
    val status: String,
    val viewCount: Int = 0,
    val watchCount: Int = 0,
    val vehicle: Vehicle,
    val sellerName: String
)

data class Vehicle(
    val id: Int,
    val make: String,
    val model: String,
    val year: Int,
    val color: String,
    val mileage: Int,
    val imageUrls: List<String>,
    val vin: String? = null,
    val fuelType: String? = null,
    val transmission: String? = null,
    val description: String? = null
) {
    val imageUrl: String?
        get() = imageUrls.firstOrNull()
    
    val engine: String?
        get() = fuelType
}

data class AuctionDetail(
    val id: Int,
    val title: String,
    val description: String,
    val startingPrice: Double,
    val currentBid: Double,
    val reservePrice: Double?,
    val startTime: String,
    val endTime: String,
    val status: String,
    val viewCount: Int = 0,
    val watchCount: Int = 0,
    val contactInfo: String? = null,
    val buyNowPrice: Double? = null,
    val hasReserve: Boolean = false,
    val vehicle: VehicleDetail,
    val sellerName: String,
    val bids: List<Bid>
)

data class VehicleDetail(
    val id: Int,
    val make: String,
    val model: String,
    val year: Int,
    val color: String,
    val vin: String,
    val mileage: Int,
    val fuelType: String,
    val transmission: String,
    val description: String,
    val imageUrls: List<String>
)

data class Bid(
    val id: Int,
    val amount: Double,
    val timestamp: String,
    val bidderName: String,
    val auctionId: Int? = null,
    val isWinning: Boolean = false
)
