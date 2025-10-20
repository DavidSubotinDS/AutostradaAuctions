package com.example.autostradaauctions.data.api

import com.example.autostradaauctions.data.model.*
import retrofit2.http.*

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse
    
    @POST("auth/refresh")
    suspend fun refreshToken(@Body refreshToken: String): AuthResponse
    
    @POST("auth/logout")
    suspend fun logout()
    
    @GET("users/profile")
    suspend fun getUserProfile(): User
    
    @PUT("users/profile")
    suspend fun updateProfile(@Body user: User): User
    
    @PUT("users/preferences")
    suspend fun updatePreferences(@Body preferences: UserPreferences): UserPreferences
    
    @GET("users/favorites")
    suspend fun getFavoriteAuctions(): List<Auction>
    
    @POST("users/favorites/{auctionId}")
    suspend fun addToFavorites(@Path("auctionId") auctionId: Int)
    
    @DELETE("users/favorites/{auctionId}")
    suspend fun removeFromFavorites(@Path("auctionId") auctionId: Int)
    
    @GET("users/bid-history")
    suspend fun getUserBidHistory(): List<Bid>
}
