package com.example.autostradaauctions.di

import com.example.autostradaauctions.data.api.AuctionApiService
import com.example.autostradaauctions.data.api.ApiConfig
import com.example.autostradaauctions.data.repository.AuctionRepository
import com.example.autostradaauctions.data.repository.BiddingRepository
import com.example.autostradaauctions.data.websocket.BidWebSocketClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AppContainer {
    
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val auctionApiService: AuctionApiService by lazy {
        retrofit.create(AuctionApiService::class.java)
    }
    
    val auctionRepository: AuctionRepository by lazy {
        AuctionRepository(auctionApiService)
    }
    
    val bidWebSocketClient: BidWebSocketClient by lazy {
        BidWebSocketClient()
    }
    
    val biddingRepository: BiddingRepository by lazy {
        BiddingRepository(auctionApiService, bidWebSocketClient)
    }
}
