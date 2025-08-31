package com.example.autostradaauctions.di

import android.content.Context
import androidx.room.Room
import com.example.autostradaauctions.data.local.AuctionDao
import com.example.autostradaauctions.data.local.AuctionDatabase
import com.example.autostradaauctions.data.local.UserDao
import com.example.autostradaauctions.data.local.VehicleDao
import com.example.autostradaauctions.data.remote.AuctionApiService
import com.example.autostradaauctions.data.repository.MockAuctionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAuctionDatabase(@ApplicationContext context: Context): AuctionDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AuctionDatabase::class.java,
            "auction_database"
        ).build()
    }

    @Provides
    fun provideAuctionDao(database: AuctionDatabase): AuctionDao = database.auctionDao()

    @Provides
    fun provideVehicleDao(database: AuctionDatabase): VehicleDao = database.vehicleDao()

    @Provides
    fun provideUserDao(database: AuctionDatabase): UserDao = database.userDao()

    // For testing purposes, provide MockAuctionRepository
    // Replace with real AuctionRepository when backend is ready
    @Provides
    @Singleton
    fun provideMockAuctionRepository(): MockAuctionRepository {
        return MockAuctionRepository()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.autostrada-auctions.com/") // Replace with actual API URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuctionApiService(retrofit: Retrofit): AuctionApiService {
        return retrofit.create(AuctionApiService::class.java)
    }
}
