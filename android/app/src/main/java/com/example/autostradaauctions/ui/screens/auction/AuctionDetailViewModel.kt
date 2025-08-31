package com.example.autostradaauctions.ui.screens.auction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autostradaauctions.data.model.*
import com.example.autostradaauctions.data.repository.MockAuctionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AuctionDetailViewModel @Inject constructor(
    private val mockRepository: MockAuctionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuctionDetailUiState())
    val uiState: StateFlow<AuctionDetailUiState> = _uiState.asStateFlow()

    fun loadAuction(auctionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val auction = mockRepository.getAuctionById(auctionId)
            if (auction != null) {
                val vehicle = mockRepository.getVehicleById(auction.vehicleId)

                mockRepository.getBidsForAuction(auctionId).collect { bids ->
                    _uiState.value = _uiState.value.copy(
                        auction = auction,
                        vehicle = vehicle,
                        bids = bids,
                        isLoading = false
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Auction not found"
                )
            }
        }
    }

    fun placeBid(amount: Double, userId: String) {
        viewModelScope.launch {
            val auction = _uiState.value.auction ?: return@launch

            if (amount <= auction.currentPrice) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Bid must be higher than current price"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isPlacingBid = true)

            val bid = Bid(
                id = UUID.randomUUID().toString(),
                auctionId = auction.id,
                bidderId = userId,
                amount = amount,
                timestamp = System.currentTimeMillis()
            )

            val result = mockRepository.placeBid(auction.id, bid)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isPlacingBid = false,
                        bidAmount = "",
                        successMessage = "Bid placed successfully!"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isPlacingBid = false,
                        errorMessage = error.message ?: "Failed to place bid"
                    )
                }
            )
        }
    }

    fun updateBidAmount(amount: String) {
        _uiState.value = _uiState.value.copy(bidAmount = amount)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun toggleFavorite(userId: String) {
        viewModelScope.launch {
            val auction = _uiState.value.auction ?: return@launch

            if (_uiState.value.isFavorite) {
                mockRepository.removeFromFavorites(userId, auction.id)
                _uiState.value = _uiState.value.copy(isFavorite = false)
            } else {
                val favorite = Favorite(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    auctionId = auction.id,
                    createdAt = System.currentTimeMillis()
                )
                mockRepository.addToFavorites(favorite)
                _uiState.value = _uiState.value.copy(isFavorite = true)
            }
        }
    }
}

data class AuctionDetailUiState(
    val auction: Auction? = null,
    val vehicle: Vehicle? = null,
    val bids: List<Bid> = emptyList(),
    val isLoading: Boolean = false,
    val isPlacingBid: Boolean = false,
    val bidAmount: String = "",
    val isFavorite: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
