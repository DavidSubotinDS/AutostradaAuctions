package com.example.autostradaauctions.data.local

import androidx.room.*
import com.example.autostradaauctions.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AuctionDao {
    @Query("SELECT * FROM auctions ORDER BY endTime ASC")
    fun getAllAuctions(): Flow<List<Auction>>

    @Query("SELECT * FROM auctions WHERE status = 'ACTIVE' ORDER BY endTime ASC")
    fun getActiveAuctions(): Flow<List<Auction>>

    @Query("SELECT * FROM auctions WHERE id = :id")
    suspend fun getAuctionById(id: String): Auction?

    @Query("SELECT * FROM auctions WHERE sellerId = :sellerId")
    fun getAuctionsBySeller(sellerId: String): Flow<List<Auction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuction(auction: Auction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuctions(auctions: List<Auction>)

    @Update
    suspend fun updateAuction(auction: Auction)

    @Delete
    suspend fun deleteAuction(auction: Auction)

    @Query("SELECT * FROM bids WHERE auctionId = :auctionId ORDER BY timestamp DESC")
    fun getBidsForAuction(auctionId: String): Flow<List<Bid>>

    @Query("SELECT * FROM bids WHERE bidderId = :bidderId ORDER BY timestamp DESC")
    fun getBidsByUser(bidderId: String): Flow<List<Bid>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBid(bid: Bid)

    @Query("SELECT * FROM favorites WHERE userId = :userId")
    fun getFavoritesByUser(userId: String): Flow<List<Favorite>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    @Delete
    suspend fun deleteFavorite(favorite: Favorite)

    @Query("SELECT * FROM comments WHERE auctionId = :auctionId ORDER BY timestamp ASC")
    fun getCommentsForAuction(auctionId: String): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)
}

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles")
    fun getAllVehicles(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getVehicleById(id: String): Vehicle?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicles(vehicles: List<Vehicle>)

    @Update
    suspend fun updateVehicle(vehicle: Vehicle)

    @Delete
    suspend fun deleteVehicle(vehicle: Vehicle)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}
