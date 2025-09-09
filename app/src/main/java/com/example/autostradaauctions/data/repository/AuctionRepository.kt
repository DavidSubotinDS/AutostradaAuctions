package com.example.autostradaauctions.data.repository

import com.example.autostradaauctions.data.api.AuctionApiService
import com.example.autostradaauctions.data.api.ApiConfig
import com.example.autostradaauctions.data.model.Auction
import com.example.autostradaauctions.data.model.AuctionDetail
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuctionRepository {
    private val api: AuctionApiService = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AuctionApiService::class.java)
    
    suspend fun getAuctions(): Result<List<Auction>> {
        return try {
            val auctions = api.getAuctions()
            Result.success(auctions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAuctionDetail(id: Int): Result<AuctionDetail> {
        return try {
            val auction = api.getAuctionDetail(id)
            Result.success(auction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
