package com.example.autostradaauctions.data.model

data class PendingAuctionData(
    val id: Int,
    val title: String,
    val description: String,
    val startingPrice: Double,
    val reservePrice: Double?,
    val endTime: String,
    val imageUrls: List<String>,
    val make: String,
    val model: String,
    val year: Int,
    val mileage: String,
    val condition: String,
    val color: String,
    val vin: String,
    val transmissionType: String,
    val fuelType: String,
    val bodyType: String,
    val submittedBy: String,
    val submittedAt: String
)