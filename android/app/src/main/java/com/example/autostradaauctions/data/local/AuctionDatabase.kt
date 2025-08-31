package com.example.autostradaauctions.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.autostradaauctions.data.model.*

@Database(
    entities = [User::class, Vehicle::class, Auction::class, Bid::class, Favorite::class, Comment::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AuctionDatabase : RoomDatabase() {
    abstract fun auctionDao(): AuctionDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AuctionDatabase? = null

        fun getDatabase(context: Context): AuctionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AuctionDatabase::class.java,
                    "auction_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
