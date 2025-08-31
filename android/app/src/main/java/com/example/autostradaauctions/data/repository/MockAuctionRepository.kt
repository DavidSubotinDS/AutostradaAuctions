package com.example.autostradaauctions.data.repository

import com.example.autostradaauctions.data.mock.MockData
import com.example.autostradaauctions.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockAuctionRepository @Inject constructor() {

    // Simulate network delay
    private suspend fun simulateNetworkDelay() {
        delay(500L)
    }

    fun getActiveAuctions(): Flow<List<Auction>> = flow {
        simulateNetworkDelay()
        emit(MockData.mockAuctions.filter { it.status == AuctionStatus.ACTIVE })
    }

    fun getAllAuctions(): Flow<List<Auction>> = flow {
        simulateNetworkDelay()
        emit(MockData.mockAuctions)
    }

    suspend fun getAuctionById(id: String): Auction? {
        simulateNetworkDelay()
        return MockData.mockAuctions.find { it.id == id }
    }

    suspend fun refreshAuctions() {
        simulateNetworkDelay()
        // In a real app, this would refresh from the server
    }

    fun getBidsForAuction(auctionId: String): Flow<List<Bid>> = flow {
        simulateNetworkDelay()
        emit(MockData.mockBids.filter { it.auctionId == auctionId }.sortedByDescending { it.timestamp })
    }

    fun getBidsByUser(userId: String): Flow<List<Bid>> = flow {
        simulateNetworkDelay()
        emit(MockData.mockBids.filter { it.bidderId == userId })
    }

    suspend fun placeBid(auctionId: String, bid: Bid): Result<Bid> {
        simulateNetworkDelay()
        return try {
            // Simulate successful bid placement
            val newBid = bid.copy(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                isWinning = true
            )
            Result.success(newBid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVehicleById(id: String): Vehicle? {
        simulateNetworkDelay()
        return MockData.mockVehicles.find { it.id == id }
    }

    fun getUserFavorites(userId: String): Flow<List<Favorite>> = flow {
        simulateNetworkDelay()
        emit(MockData.mockFavorites.filter { it.userId == userId })
    }

    suspend fun addToFavorites(favorite: Favorite): Result<Favorite> {
        simulateNetworkDelay()
        return Result.success(favorite)
    }

    suspend fun removeFromFavorites(userId: String, auctionId: String): Result<Unit> {
        simulateNetworkDelay()
        return Result.success(Unit)
    }

    fun getCommentsForAuction(auctionId: String): Flow<List<Comment>> = flow {
        simulateNetworkDelay()
        emit(MockData.mockComments.filter { it.auctionId == auctionId })
    }

    suspend fun postComment(auctionId: String, comment: Comment): Result<Comment> {
        simulateNetworkDelay()
        return try {
            val newComment = comment.copy(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis()
            )
            Result.success(newComment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        simulateNetworkDelay()
        return try {
            // Simple mock authentication - accept any email/password combination
            val user = MockData.mockUsers.find { it.email == email }
                ?: MockData.mockUsers.first().copy(email = email, name = email.substringBefore("@"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception("Login failed"))
        }
    }

    suspend fun registerUser(user: User): Result<User> {
        simulateNetworkDelay()
        return try {
            val newUser = user.copy(
                id = UUID.randomUUID().toString(),
                createdAt = System.currentTimeMillis()
            )
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(Exception("Registration failed"))
        }
    }

    // Helper method to get auctions from favorites
    suspend fun getAuctionsFromFavorites(favorites: List<Favorite>): List<Auction> {
        return favorites.mapNotNull { favorite ->
            MockData.mockAuctions.find { it.id == favorite.auctionId }
        }
    }

    // Helper method to get auctions from bids
    suspend fun getAuctionsFromBids(bids: List<Bid>): List<Auction> {
        val auctionIds = bids.map { it.auctionId }.distinct()
        return auctionIds.mapNotNull { auctionId ->
            MockData.mockAuctions.find { it.id == auctionId }
        }
    }
}
