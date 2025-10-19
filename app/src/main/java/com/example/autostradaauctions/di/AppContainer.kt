package com.example.autostradaauctions.di

import android.content.Context
import com.example.autostradaauctions.data.api.AuctionApiService
import com.example.autostradaauctions.data.api.AuthApiService
import com.example.autostradaauctions.data.api.ApiConfig
import com.example.autostradaauctions.data.auth.TokenManager
import com.example.autostradaauctions.data.model.UserRole
import com.example.autostradaauctions.data.repository.AuctionRepository
import com.example.autostradaauctions.data.repository.MockAuctionRepository
import com.example.autostradaauctions.data.repository.AuthRepository
import com.example.autostradaauctions.data.repository.BiddingRepository
import com.example.autostradaauctions.data.repository.ImageRepository
import com.example.autostradaauctions.data.repository.AdminRepository
import com.example.autostradaauctions.data.serialization.UserRoleAdapter
import com.example.autostradaauctions.data.websocket.BidWebSocketClient
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AppContainer {
    
    private lateinit var applicationContext: Context
    private var isInitialized = false
    
    // Add instance property for singleton access
    val instance: AppContainer get() = this

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
        isInitialized = true
    }
    
    fun isAppContainerInitialized(): Boolean = isInitialized
    
    val tokenManager: TokenManager by lazy {
        TokenManager(applicationContext)
    }
    
    private val authInterceptor: Interceptor by lazy {
        Interceptor { chain ->
            val originalRequest = chain.request()
            val token = tokenManager.getAuthHeader()
            
            val newRequest = if (token != null) {
                originalRequest.newBuilder()
                    .header("Authorization", token)
                    .build()
            } else {
                originalRequest
            }
            
            chain.proceed(newRequest)
        }
    }
    
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
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
        val gson = GsonBuilder()
            .registerTypeAdapter(UserRole::class.java, UserRoleAdapter())
            .create()
            
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    val auctionApiService: AuctionApiService by lazy {
        retrofit.create(AuctionApiService::class.java)
    }
    
    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
    
    val authRepository: AuthRepository by lazy {
        AuthRepository(authApiService, tokenManager)
    }
    
    val auctionRepository: AuctionRepository by lazy {
        AuctionRepository(httpClient)
    }
    
    val mockAuctionRepository: MockAuctionRepository by lazy {
        MockAuctionRepository()
    }
    
    val bidWebSocketClient: BidWebSocketClient by lazy {
        BidWebSocketClient()
    }
    
    val biddingRepository: BiddingRepository by lazy {
        BiddingRepository(auctionApiService, bidWebSocketClient)
    }
    
    val imageRepository: ImageRepository by lazy {
        ImageRepository(httpClient)
    }
    
    val adminRepository: AdminRepository by lazy {
        AdminRepository(httpClient)
    }
}
