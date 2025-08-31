package com.example.autostradaauctions.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auctions")
data class Auction(
    @PrimaryKey
    val id: String,
    val vehicleId: String,
    val sellerId: String,
    val title: String,
    val description: String,
    val startingPrice: Double,
    val currentPrice: Double,
    val reservePrice: Double? = null,
    val startTime: Long,
    val endTime: Long,
    val status: AuctionStatus,
    val totalBids: Int = 0,
    val highestBidderId: String? = null,
    val isReserveMet: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

enum class AuctionStatus {
    PENDING,
    ACTIVE,
    COMPLETED,
    CANCELLED,
    EXTENDED
}

@Entity(tableName = "bids")
data class Bid(
    @PrimaryKey
    val id: String,
    val auctionId: String,
    val bidderId: String,
    val amount: Double,
    val timestamp: Long,
    val isWinning: Boolean = false
)

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey
    val id: String,
    val userId: String,
    val auctionId: String,
    val createdAt: Long
)

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey
    val id: String,
    val auctionId: String,
    val userId: String,
    val content: String,
    val timestamp: Long,
    val parentCommentId: String? = null
)
