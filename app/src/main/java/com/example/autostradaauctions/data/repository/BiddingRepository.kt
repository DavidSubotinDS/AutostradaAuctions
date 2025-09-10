package com.example.autostradaauctions.data.repository

import com.example.autostradaauctions.data.api.AuctionApiService
import com.example.autostradaauctions.data.model.Bid
import com.example.autostradaauctions.data.websocket.BidWebSocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class BiddingRepository(
    private val apiService: AuctionApiService,
    private val webSocketClient: BidWebSocketClient
) {
    
    val bidUpdates: SharedFlow<Bid> = webSocketClient.bidUpdates
    val connectionState: SharedFlow<BidWebSocketClient.ConnectionState> = webSocketClient.connectionState
    val auctionUpdates: SharedFlow<BidWebSocketClient.AuctionUpdate> = webSocketClient.auctionUpdates
    
    fun connectToRealTimeBidding() {
        webSocketClient.connect()
    }
    
    fun disconnectFromRealTimeBidding() {
        webSocketClient.disconnect()
    }
    
    fun joinAuction(auctionId: Int) {
        webSocketClient.joinAuction(auctionId)
    }
    
    fun leaveAuction(auctionId: Int) {
        webSocketClient.leaveAuction(auctionId)
    }
    
    suspend fun placeBid(auctionId: Int, amount: Double, bidderName: String): Result<Unit> {
        return try {
            // Place bid via REST API first
            apiService.placeBid(auctionId, PlaceBidRequest(amount, bidderName))
            
            // Also place bid via WebSocket for real-time update
            webSocketClient.placeBid(auctionId, amount, bidderName)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getBidHistory(auctionId: Int): Result<List<Bid>> {
        return try {
            val bids = apiService.getBidHistory(auctionId)
            Result.success(bids)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun isConnectedToRealTime(): Boolean {
        return webSocketClient.isConnected()
    }
}

data class PlaceBidRequest(
    val amount: Double,
    val bidderName: String
)
