package com.example.autostradaauctions.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey
    val id: String,
    val make: String,
    val model: String,
    val year: Int,
    val mileage: Int,
    val engineType: String,
    val transmission: String,
    val fuelType: String,
    val color: String,
    val vin: String,
    val description: String,
    val imageUrls: List<String>,
    val condition: VehicleCondition,
    val isVerified: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

enum class VehicleCondition {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    SALVAGE
}
