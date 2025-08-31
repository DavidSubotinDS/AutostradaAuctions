package com.example.autostradaauctions.data.remote

import com.example.autostradaauctions.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuctionApiService {
    @GET("auctions")
    suspend fun getAuctions(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("status") status: String? = null
    ): Response<List<Auction>>

    @GET("auctions/{id}")
    suspend fun getAuctionById(@Path("id") id: String): Response<Auction>

    @POST("auctions")
    suspend fun createAuction(@Body auction: Auction): Response<Auction>

    @PUT("auctions/{id}")
    suspend fun updateAuction(@Path("id") id: String, @Body auction: Auction): Response<Auction>

    @DELETE("auctions/{id}")
    suspend fun deleteAuction(@Path("id") id: String): Response<Unit>

    @GET("auctions/{id}/bids")
    suspend fun getBidsForAuction(@Path("id") auctionId: String): Response<List<Bid>>

    @POST("auctions/{id}/bids")
    suspend fun placeBid(@Path("id") auctionId: String, @Body bid: Bid): Response<Bid>

    @GET("vehicles")
    suspend fun getVehicles(): Response<List<Vehicle>>

    @GET("vehicles/{id}")
    suspend fun getVehicleById(@Path("id") id: String): Response<Vehicle>

    @POST("vehicles")
    suspend fun createVehicle(@Body vehicle: Vehicle): Response<Vehicle>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: String): Response<User>

    @POST("users/register")
    suspend fun registerUser(@Body user: User): Response<User>

    @POST("users/login")
    suspend fun loginUser(@Body credentials: LoginRequest): Response<LoginResponse>

    @GET("users/{id}/favorites")
    suspend fun getUserFavorites(@Path("id") userId: String): Response<List<Favorite>>

    @POST("users/{id}/favorites")
    suspend fun addToFavorites(@Path("id") userId: String, @Body favorite: Favorite): Response<Favorite>

    @DELETE("users/{userId}/favorites/{auctionId}")
    suspend fun removeFromFavorites(@Path("userId") userId: String, @Path("auctionId") auctionId: String): Response<Unit>

    @GET("auctions/{id}/comments")
    suspend fun getAuctionComments(@Path("id") auctionId: String): Response<List<Comment>>

    @POST("auctions/{id}/comments")
    suspend fun postComment(@Path("id") auctionId: String, @Body comment: Comment): Response<Comment>
}

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: User
)
