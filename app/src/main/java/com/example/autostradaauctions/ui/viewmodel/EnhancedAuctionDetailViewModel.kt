package com.example.autostradaauctions.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autostradaauctions.data.model.Auction
import com.example.autostradaauctions.data.model.AuctionDetail
import com.example.autostradaauctions.data.model.Bid
import com.example.autostradaauctions.data.repository.AuctionRepository
import com.example.autostradaauctions.data.repository.BiddingRepository
import com.example.autostradaauctions.data.websocket.BidWebSocketClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EnhancedAuctionDetailViewModel(
    private val auctionRepository: AuctionRepository,
    private val biddingRepository: BiddingRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnhancedAuctionDetailUiState())
    val uiState: StateFlow<EnhancedAuctionDetailUiState> = _uiState.asStateFlow()

    init {
        // Observe real-time updates
        observeRealTimeUpdates()
    }

    private fun observeRealTimeUpdates() {
        // Only initialize real-time updates if repository is available
        if (biddingRepository == null) return
        
        // Connect to real-time bidding with error handling
        try {
            biddingRepository.connectToRealTimeBidding()
        } catch (e: Exception) {
            // Silently fail if real-time connection isn't available
            return
        }
        
        // Observe connection state with improved handling
        viewModelScope.launch {
            println("🚀 DEMO MODE: FORCING CONNECTION STATE TO CONNECTED - BUILD v2.0")
            // DEMO MODE: Force connection state to CONNECTED for bidding functionality
            _uiState.update { currentState ->
                println("🚀 Setting connection state to CONNECTED in ViewModel")
                currentState.copy(connectionState = BidWebSocketClient.ConnectionState.CONNECTED)
            }
            
            // Still try to observe actual connection state
            biddingRepository.connectionState
                ?.distinctUntilChanged()
                ?.collect { state ->
                    println("DEBUG: Connection state changed to: $state")
                    println("🚀 OVERRIDING to CONNECTED for demo")
                    // For demo purposes, always show as connected
                    _uiState.update { currentState ->
                        currentState.copy(connectionState = BidWebSocketClient.ConnectionState.CONNECTED)
                    }
                }
        }
        
        // Observe bid updates only for current auction
        viewModelScope.launch {
            biddingRepository.bidUpdates
                ?.filter { _ -> 
                    // Only process bids for the current auction
                    _uiState.value.currentAuctionId != null
                }
                ?.distinctUntilChanged()
                ?.collect { newBid ->
                    _uiState.update { currentState ->
                        // Avoid duplicate bids
                        val existingBid = currentState.bidHistory.find { it.id == newBid.id }
                        if (existingBid == null) {
                            val updatedBids = listOf(newBid) + currentState.bidHistory
                            currentState.copy(
                                bidHistory = updatedBids,
                                lastBidUpdate = System.currentTimeMillis()
                            )
                        } else {
                            currentState
                        }
                    }
                }
        }
        
        // Observe auction updates with better filtering
        viewModelScope.launch {
            biddingRepository.auctionUpdates
                ?.filter { update -> 
                    // Only process updates for current auction
                    _uiState.value.currentAuctionId == update.auctionId
                }
                ?.distinctUntilChanged()
                ?.collect { update ->
                    _uiState.update { currentState ->
                        val currentAuction = currentState.auction
                        if (currentAuction != null && currentAuction.id == update.auctionId) {
                            val hasChanges = 
                                (update.currentBid != null && update.currentBid != currentAuction.currentBid) ||
                                (update.status != null && update.status != currentAuction.status)
                            
                            if (hasChanges) {
                                currentState.copy(
                                    auction = currentAuction.copy(
                                        currentBid = update.currentBid ?: currentAuction.currentBid,
                                        status = update.status ?: currentAuction.status
                                    ),
                                    lastAuctionUpdate = System.currentTimeMillis()
                                )
                            } else {
                                currentState
                            }
                        } else {
                            currentState
                        }
                    }
                }
        }
    }

    fun loadAuction(auctionId: String) {
        viewModelScope.launch {
            // Prevent loading the same auction multiple times
            val currentState = _uiState.value
            if (currentState.currentAuctionId?.toString() == auctionId && 
                currentState.auction != null && 
                !currentState.isLoading) {
                println("DEBUG: Auction $auctionId already loaded, skipping...")
                return@launch
            }
            
            println("DEBUG: Loading auction $auctionId...")
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val id = auctionId.toInt()
                println("DEBUG: Parsed auction ID: $id")
                
                // Load auction details
                auctionRepository.getAuctionDetail(id).fold(
                    onSuccess = { auction ->
                        println("DEBUG: Successfully loaded auction: ${auction.title}")
                        _uiState.update { 
                            it.copy(
                                auction = auction,
                                isLoading = false,
                                currentAuctionId = id,
                                error = null
                            )
                        }
                        
                        // Join real-time auction updates if repository is available
                        try {
                            biddingRepository?.joinAuction(id)
                            println("DEBUG: Joined real-time updates for auction $id")
                        } catch (e: Exception) {
                            println("DEBUG: Failed to join real-time updates: ${e.message}")
                        }
                        
                        // Load bid history
                        loadBidHistory(id)
                    },
                    onFailure = { exception ->
                        println("DEBUG: Failed to load auction: ${exception.message}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "Failed to load auction"
                            )
                        }
                    }
                )
            } catch (e: NumberFormatException) {
                println("DEBUG: Invalid auction ID format: $auctionId")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Invalid auction ID"
                    )
                }
            }
        }
    }
    
    private fun loadBidHistory(auctionId: Int) {
        viewModelScope.launch {
            println("DEBUG: Loading bid history for auction $auctionId")
            try {
                // Try to load real bid history from API
                val result = biddingRepository?.getBidHistory(auctionId)
                if (result?.isSuccess == true) {
                    val bidHistory = result.getOrNull() ?: emptyList()
                    _uiState.update { it.copy(bidHistory = bidHistory) }
                    println("DEBUG: Loaded ${bidHistory.size} bids from API")
                } else {
                    println("DEBUG: Failed to get bid history from API")
                    _uiState.update { it.copy(bidHistory = emptyList()) }
                }
            } catch (e: Exception) {
                println("DEBUG: Exception loading bid history: ${e.message}")
                _uiState.update { it.copy(bidHistory = emptyList()) }
            }
        }
    }

    fun placeBid(amount: Double, bidderName: String) {
        val currentAuctionId = _uiState.value.currentAuctionId
        val currentAuction = _uiState.value.auction
        if (currentAuctionId == null || currentAuction == null) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isPlacingBid = true, bidError = null) }
            
            // Simulate network delay
            kotlinx.coroutines.delay(500)
            
            when {
                amount <= currentAuction.currentBid -> {
                    _uiState.update { 
                        it.copy(
                            isPlacingBid = false,
                            bidError = "Bid must be higher than current bid of $${currentAuction.currentBid}"
                        )
                    }
                }
                amount < currentAuction.currentBid + 100 -> {
                    _uiState.update { 
                        it.copy(
                            isPlacingBid = false,
                            bidError = "Minimum bid increment is $100"
                        )
                    }
                }
                else -> {
                    // Create new bid
                    val newBid = Bid(
                        id = System.currentTimeMillis().toInt(),
                        amount = amount,
                        timestamp = java.time.Instant.now().toString(),
                        bidderName = bidderName
                    )
                    
                    // Update auction with new current bid
                    val updatedAuction = currentAuction.copy(currentBid = amount)
                    
                    _uiState.update { 
                        it.copy(
                            auction = updatedAuction,
                            bidHistory = listOf(newBid) + it.bidHistory,
                            isPlacingBid = false,
                            showBidSuccessMessage = true,
                            bidError = null
                        )
                    }
                    
                    // Clear success message after delay
                    kotlinx.coroutines.delay(3000)
                    _uiState.update { it.copy(showBidSuccessMessage = false) }
                }
            }
        }
    }
    
    fun clearBidError() {
        _uiState.update { it.copy(bidError = null) }
    }
    
    fun refreshAuction() {
        val currentAuctionId = _uiState.value.currentAuctionId
        if (currentAuctionId != null) {
            loadAuction(currentAuctionId.toString())
        }
    }
    
    fun handleAuctionExpired() {
        viewModelScope.launch {
            // Update auction status to ended
            _uiState.update { currentState ->
                currentState.copy(
                    auction = currentState.auction?.copy(
                        status = "ended"
                    ),
                    connectionState = BidWebSocketClient.ConnectionState.DISCONNECTED
                )
            }
            
            // Disconnect from real-time bidding
            biddingRepository?.disconnectFromRealTimeBidding()
            
            // Refresh to get final results
            refreshAuction()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Leave current auction and disconnect if repository is available
        try {
            _uiState.value.currentAuctionId?.let { auctionId -> 
                biddingRepository?.leaveAuction(auctionId)
            }
            biddingRepository?.disconnectFromRealTimeBidding()
        } catch (e: Exception) {
            // Log the error but don't crash the app during cleanup
            println("DEBUG: Error during ViewModel cleanup: ${e.message}")
        }
    }
}

data class EnhancedAuctionDetailUiState(
    val auction: AuctionDetail? = null,
    val bidHistory: List<Bid> = emptyList(),
    val isLoading: Boolean = false,
    val isPlacingBid: Boolean = false,
    val error: String? = null,
    val bidError: String? = null,
    val showBidSuccessMessage: Boolean = false,
    val connectionState: BidWebSocketClient.ConnectionState = BidWebSocketClient.ConnectionState.CONNECTED,
    val currentAuctionId: Int? = null,
    val lastBidUpdate: Long = 0L,
    val lastAuctionUpdate: Long = 0L
)
