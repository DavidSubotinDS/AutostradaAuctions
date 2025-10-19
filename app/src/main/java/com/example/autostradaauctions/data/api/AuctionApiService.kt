package com.example.autostradaauctions.data.api

import com.example.autostradaauctions.config.AppConfig
import com.example.autostradaauctions.data.model.Auction
import com.example.autostradaauctions.data.model.AuctionDetail
import com.example.autostradaauctions.data.model.Bid
import com.example.autostradaauctions.data.repository.PlaceBidRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuctionApiService {
    @GET("api/auctions")
    suspend fun getAuctions(): List<Auction>
    
    @GET("api/auctions/{id}")
    suspend fun getAuctionDetail(@Path("id") id: Int): AuctionDetail
    
    @POST("api/auctions/{id}/bids")
    suspend fun placeBid(@Path("id") auctionId: Int, @Body request: PlaceBidRequest)
    
    @GET("api/auctions/{id}/bids")
    suspend fun getBidHistory(@Path("id") auctionId: Int): List<Bid>
}

object ApiConfig {
    // PERMANENT FIX: Use centralized AppConfig for consistent port management
    val BASE_URL = AppConfig.BASE_URL.removeSuffix("/api/") + "/"
}
