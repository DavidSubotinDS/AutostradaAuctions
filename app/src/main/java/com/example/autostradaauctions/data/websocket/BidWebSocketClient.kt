package com.example.autostradaauctions.data.websocket

import android.util.Log
import com.example.autostradaauctions.data.model.Bid
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class BidWebSocketClient {
    companion object {
        private const val TAG = "BidWebSocketClient"
        private const val HUB_URL = "http://10.0.2.2:5000/biddingHub" // Android emulator localhost
    }

    private var hubConnection: HubConnection? = null
    private val _bidUpdates = MutableSharedFlow<Bid>()
    val bidUpdates: SharedFlow<Bid> = _bidUpdates.asSharedFlow()
    
    private val _connectionState = MutableSharedFlow<ConnectionState>()
    val connectionState: SharedFlow<ConnectionState> = _connectionState.asSharedFlow()
    
    private val _auctionUpdates = MutableSharedFlow<AuctionUpdate>()
    val auctionUpdates: SharedFlow<AuctionUpdate> = _auctionUpdates.asSharedFlow()

    fun connect() {
        if (hubConnection?.connectionState == HubConnectionState.CONNECTED) {
            Log.d(TAG, "Already connected")
            return
        }

        hubConnection = HubConnectionBuilder.create(HUB_URL)
            .build()

        // Handle incoming bid updates
        hubConnection?.on("ReceiveBidUpdate", { auctionId: Int, bid: BidDto ->
            Log.d(TAG, "Received bid update for auction $auctionId: ${bid.amount}")
            _bidUpdates.tryEmit(bid.toBid())
            _auctionUpdates.tryEmit(AuctionUpdate(auctionId, bid.amount))
        }, Int::class.java, BidDto::class.java)

        // Handle auction status updates
        hubConnection?.on("AuctionStatusChanged", { auctionId: Int, status: String ->
            Log.d(TAG, "Auction $auctionId status changed to: $status")
            _auctionUpdates.tryEmit(AuctionUpdate(auctionId, status = status))
        }, Int::class.java, String::class.java)

        // Handle connection events
        hubConnection?.onClosed { error ->
            Log.w(TAG, "Connection closed", error)
            _connectionState.tryEmit(ConnectionState.DISCONNECTED)
        }

        // Start connection
        hubConnection?.start()?.whenComplete { _, error ->
            if (error != null) {
                Log.e(TAG, "Failed to connect", error)
                _connectionState.tryEmit(ConnectionState.ERROR)
            } else {
                Log.i(TAG, "Connected successfully")
                _connectionState.tryEmit(ConnectionState.CONNECTED)
            }
        }
    }

    fun disconnect() {
        hubConnection?.stop()
        hubConnection = null
        _connectionState.tryEmit(ConnectionState.DISCONNECTED)
    }

    fun joinAuction(auctionId: Int) {
        hubConnection?.invoke("JoinAuction", auctionId)?.whenComplete { _, error ->
            if (error != null) {
                Log.e(TAG, "Failed to join auction $auctionId", error)
            } else {
                Log.d(TAG, "Joined auction $auctionId")
            }
        }
    }

    fun leaveAuction(auctionId: Int) {
        hubConnection?.invoke("LeaveAuction", auctionId)
    }

    fun placeBid(auctionId: Int, amount: Double, bidderName: String) {
        hubConnection?.invoke("PlaceBid", auctionId, amount, bidderName)?.whenComplete { _, error ->
            if (error != null) {
                Log.e(TAG, "Failed to place bid", error)
            } else {
                Log.d(TAG, "Bid placed successfully: $amount")
            }
        }
    }

    fun isConnected(): Boolean {
        return hubConnection?.connectionState == HubConnectionState.CONNECTED
    }

    data class BidDto(
        val id: Int,
        val amount: Double,
        val timestamp: String,
        val bidderName: String,
        val auctionId: Int
    ) {
        fun toBid(): Bid {
            return Bid(
                id = id,
                amount = amount,
                timestamp = timestamp,
                bidderName = bidderName
            )
        }
    }

    data class AuctionUpdate(
        val auctionId: Int,
        val currentBid: Double? = null,
        val status: String? = null
    )

    enum class ConnectionState {
        CONNECTING,
        CONNECTED,
        DISCONNECTED,
        ERROR
    }
}
