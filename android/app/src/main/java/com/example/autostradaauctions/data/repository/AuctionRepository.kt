package com.example.autostradaauctions.data.repository

import com.example.autostradaauctions.data.local.AuctionDao
import com.example.autostradaauctions.data.local.UserDao
import com.example.autostradaauctions.data.local.VehicleDao
import com.example.autostradaauctions.data.model.*
import com.example.autostradaauctions.data.remote.AuctionApiService
import com.example.autostradaauctions.data.remote.LoginRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuctionRepository @Inject constructor(
    private val auctionDao: AuctionDao,
    private val vehicleDao: VehicleDao,
    private val userDao: UserDao,
    private val apiService: AuctionApiService
) {

    fun getActiveAuctions(): Flow<List<Auction>> = auctionDao.getActiveAuctions()

    fun getAllAuctions(): Flow<List<Auction>> = auctionDao.getAllAuctions()

    suspend fun getAuctionById(id: String): Auction? {
        return try {
            val response = apiService.getAuctionById(id)
            if (response.isSuccessful) {
                response.body()?.let { auction ->
                    auctionDao.insertAuction(auction)
                    auction
                }
            } else {
                auctionDao.getAuctionById(id)
            }
        } catch (e: Exception) {
            auctionDao.getAuctionById(id)
        }
    }

    suspend fun refreshAuctions() {
        try {
            val response = apiService.getAuctions()
            if (response.isSuccessful) {
                response.body()?.let { auctions ->
                    auctionDao.insertAuctions(auctions)
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    fun getBidsForAuction(auctionId: String): Flow<List<Bid>> = auctionDao.getBidsForAuction(auctionId)

    fun getBidsByUser(userId: String): Flow<List<Bid>> = auctionDao.getBidsByUser(userId)

    suspend fun placeBid(auctionId: String, bid: Bid): Result<Bid> {
        return try {
            val response = apiService.placeBid(auctionId, bid)
            if (response.isSuccessful) {
                response.body()?.let { newBid ->
                    auctionDao.insertBid(newBid)
                    Result.success(newBid)
                } ?: Result.failure(Exception("No bid returned"))
            } else {
                Result.failure(Exception("Failed to place bid"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVehicleById(id: String): Vehicle? {
        return try {
            val response = apiService.getVehicleById(id)
            if (response.isSuccessful) {
                response.body()?.let { vehicle ->
                    vehicleDao.insertVehicle(vehicle)
                    vehicle
                }
            } else {
                vehicleDao.getVehicleById(id)
            }
        } catch (e: Exception) {
            vehicleDao.getVehicleById(id)
        }
    }

    fun getUserFavorites(userId: String): Flow<List<Favorite>> = auctionDao.getFavoritesByUser(userId)

    suspend fun addToFavorites(favorite: Favorite): Result<Favorite> {
        return try {
            val response = apiService.addToFavorites(favorite.userId, favorite)
            if (response.isSuccessful) {
                auctionDao.insertFavorite(favorite)
                Result.success(favorite)
            } else {
                Result.failure(Exception("Failed to add to favorites"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromFavorites(userId: String, auctionId: String): Result<Unit> {
        return try {
            val response = apiService.removeFromFavorites(userId, auctionId)
            if (response.isSuccessful) {
                // Remove from local database
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to remove from favorites"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCommentsForAuction(auctionId: String): Flow<List<Comment>> = auctionDao.getCommentsForAuction(auctionId)

    suspend fun postComment(auctionId: String, comment: Comment): Result<Comment> {
        return try {
            val response = apiService.postComment(auctionId, comment)
            if (response.isSuccessful) {
                response.body()?.let { newComment ->
                    auctionDao.insertComment(newComment)
                    Result.success(newComment)
                } ?: Result.failure(Exception("No comment returned"))
            } else {
                Result.failure(Exception("Failed to post comment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            val response = apiService.loginUser(LoginRequest(email, password))
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    userDao.insertUser(loginResponse.user)
                    Result.success(loginResponse.user)
                } ?: Result.failure(Exception("Login failed"))
            } else {
                Result.failure(Exception("Invalid credentials"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerUser(user: User): Result<User> {
        return try {
            val response = apiService.registerUser(user)
            if (response.isSuccessful) {
                response.body()?.let { newUser ->
                    userDao.insertUser(newUser)
                    Result.success(newUser)
                } ?: Result.failure(Exception("Registration failed"))
            } else {
                Result.failure(Exception("Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
