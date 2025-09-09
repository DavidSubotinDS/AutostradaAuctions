package com.example.autostradaauctions.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autostradaauctions.data.model.Auction
import com.example.autostradaauctions.data.model.Bid
import com.example.autostradaauctions.data.repository.AuctionRepository
import com.example.autostradaauctions.data.repository.BiddingRepository
import com.example.autostradaauctions.data.websocket.BidWebSocketClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EnhancedAuctionDetailViewModel(
    private val auctionRepository: AuctionRepository,
    private val biddingRepository: BiddingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnhancedAuctionDetailUiState())
    val uiState: StateFlow<EnhancedAuctionDetailUiState> = _uiState.asStateFlow()

    init {
        // Observe real-time updates
        observeRealTimeUpdates()
    }

    private fun observeRealTimeUpdates() {
        // Connect to real-time bidding
        biddingRepository.connectToRealTimeBidding()
        
        // Observe connection state
        viewModelScope.launch {
            biddingRepository.connectionState.collect { state ->
                _uiState.update { it.copy(connectionState = state) }
            }
        }
        
        // Observe bid updates
        viewModelScope.launch {
            biddingRepository.bidUpdates.collect { newBid ->
                _uiState.update { currentState ->
                    val updatedBids = listOf(newBid) + currentState.bidHistory
                    currentState.copy(
                        bidHistory = updatedBids,
                        lastBidUpdate = System.currentTimeMillis()
                    )
                }
            }
        }
        
        // Observe auction updates (current bid, status changes)
        viewModelScope.launch {
            biddingRepository.auctionUpdates.collect { update ->
                _uiState.update { currentState ->
                    if (currentState.auction?.id == update.auctionId) {
                        currentState.copy(
                            auction = currentState.auction?.copy(
                                currentBid = update.currentBid ?: currentState.auction.currentBid,
                                status = update.status ?: currentState.auction.status
                            ),
                            lastAuctionUpdate = System.currentTimeMillis()
                        )
                    } else {
                        currentState
                    }
                }
            }
        }
    }

    fun loadAuction(auctionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val id = auctionId.toInt()
                
                // Load auction details
                auctionRepository.getAuctionDetail(id).fold(
                    onSuccess = { auction ->
                        _uiState.update { 
                            it.copy(
                                auction = auction,
                                isLoading = false,
                                currentAuctionId = id
                            )
                        }
                        
                        // Join real-time auction updates
                        biddingRepository.joinAuction(id)
                        
                        // Load bid history
                        loadBidHistory(id)
                    },
                    onFailure = { exception ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "Failed to load auction"
                            )
                        }
                    }
                )
            } catch (e: NumberFormatException) {
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
            biddingRepository.getBidHistory(auctionId).fold(
                onSuccess = { bids ->
                    _uiState.update { it.copy(bidHistory = bids) }
                },
                onFailure = { exception ->
                    // Don't show error for bid history failure, just log it
                    println("Failed to load bid history: ${exception.message}")
                }
            )
        }
    }

    fun placeBid(amount: Double, bidderName: String) {
        val currentAuctionId = _uiState.value.currentAuctionId
        if (currentAuctionId == null) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isPlacingBid = true, bidError = null) }
            
            biddingRepository.placeBid(currentAuctionId, amount, bidderName).fold(
                onSuccess = {
                    _uiState.update { 
                        it.copy(
                            isPlacingBid = false,
                            showBidSuccessMessage = true
                        )
                    }
                    
                    // Clear success message after delay
                    kotlinx.coroutines.delay(3000)
                    _uiState.update { it.copy(showBidSuccessMessage = false) }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isPlacingBid = false,
                            bidError = exception.message ?: "Failed to place bid"
                        )
                    }
                }
            )
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
    
    override fun onCleared() {
        super.onCleared()
        // Leave current auction and disconnect
        _uiState.value.currentAuctionId?.let { 
            biddingRepository.leaveAuction(it) 
        }
        biddingRepository.disconnectFromRealTimeBidding()
    }
}

data class EnhancedAuctionDetailUiState(
    val auction: Auction? = null,
    val bidHistory: List<Bid> = emptyList(),
    val isLoading: Boolean = false,
    val isPlacingBid: Boolean = false,
    val error: String? = null,
    val bidError: String? = null,
    val showBidSuccessMessage: Boolean = false,
    val connectionState: BidWebSocketClient.ConnectionState = BidWebSocketClient.ConnectionState.DISCONNECTED,
    val currentAuctionId: Int? = null,
    val lastBidUpdate: Long = 0L,
    val lastAuctionUpdate: Long = 0L
)
