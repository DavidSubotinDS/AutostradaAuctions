package com.example.autostradaauctions.ui.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.autostradaauctions.data.model.Auction
import com.example.autostradaauctions.data.repository.AuctionRepository
import com.example.autostradaauctions.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class FavoritesViewModel(
    private val authRepository: AuthRepository,
    private val auctionRepository: AuctionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()
    
    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            authRepository.getFavoriteAuctions().fold(
                onSuccess = { favorites ->
                    _uiState.update { 
                        it.copy(
                            favorites = favorites,
                            isLoading = false,
                            error = null
                        ) 
                    }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load favorites"
                        ) 
                    }
                }
            )
        }
    }
    
    fun toggleFavorite(auctionId: Int) {
        viewModelScope.launch {
            val currentFavorites = _uiState.value.favorites
            val isFavorited = currentFavorites.any { it.id == auctionId }
            
            if (isFavorited) {
                authRepository.removeFromFavorites(auctionId).fold(
                    onSuccess = {
                        _uiState.update { 
                            it.copy(favorites = currentFavorites.filter { auction -> auction.id != auctionId }) 
                        }
                    },
                    onFailure = { /* Handle error */ }
                )
            } else {
                authRepository.addToFavorites(auctionId).fold(
                    onSuccess = {
                        // Reload favorites to get the updated list
                        loadFavorites()
                    },
                    onFailure = { /* Handle error */ }
                )
            }
        }
    }
    
    fun formatPrice(price: Double): String {
        return NumberFormat.getCurrencyInstance(Locale.US).format(price)
    }
    
    fun calculateTimeLeft(endTime: String): String {
        // Simple time calculation - you can enhance this
        return "2d 5h left"
    }
    
    fun getStatusColor(status: String): Color {
        return when (status.lowercase()) {
            "active" -> Color.Green
            "ended" -> Color.Red
            "pending" -> Color(0xFFFF9800) // Orange
            else -> Color.Gray
        }
    }
}

data class FavoritesUiState(
    val favorites: List<Auction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FavoritesViewModelFactory(
    private val authRepository: AuthRepository,
    private val auctionRepository: AuctionRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            return FavoritesViewModel(authRepository, auctionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
