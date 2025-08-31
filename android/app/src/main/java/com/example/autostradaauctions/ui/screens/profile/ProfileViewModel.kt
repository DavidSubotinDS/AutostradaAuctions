package com.example.autostradaauctions.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autostradaauctions.data.model.Auction
import com.example.autostradaauctions.data.model.User
import com.example.autostradaauctions.data.repository.MockAuctionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val mockRepository: MockAuctionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Load user bids and convert to auctions
                mockRepository.getBidsByUser(userId).collect { bids ->
                    val userBidAuctions = mockRepository.getAuctionsFromBids(bids)
                    _uiState.value = _uiState.value.copy(
                        userBids = userBidAuctions,
                        isLoading = false
                    )
                }

                // Load favorites and convert to auctions
                mockRepository.getUserFavorites(userId).collect { favorites ->
                    val favoriteAuctions = mockRepository.getAuctionsFromFavorites(favorites)
                    _uiState.value = _uiState.value.copy(
                        favoriteAuctions = favoriteAuctions
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load profile"
                )
            }
        }
    }

    fun setUser(user: User) {
        _uiState.value = _uiState.value.copy(user = user)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class ProfileUiState(
    val user: User? = null,
    val userBids: List<Auction> = emptyList(),
    val favoriteAuctions: List<Auction> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
