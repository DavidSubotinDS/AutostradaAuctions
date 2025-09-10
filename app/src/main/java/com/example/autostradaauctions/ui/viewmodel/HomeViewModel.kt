package com.example.autostradaauctions.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autostradaauctions.data.model.Auction
import com.example.autostradaauctions.data.repository.MockAuctionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val allAuctions: List<Auction> = emptyList(),
    val filteredAuctions: List<Auction> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedMake: String = "All",
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val availableMakes: List<String> = emptyList()
)

class HomeViewModel : ViewModel() {
    private val repository = MockAuctionRepository()
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadAuctions()
    }
    
    fun loadAuctions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            repository.getAuctions()
                .onSuccess { auctions ->
                    val makes = listOf("All") + auctions.map { it.vehicle.make }.distinct().sorted()
                    
                    _uiState.value = _uiState.value.copy(
                        allAuctions = auctions,
                        filteredAuctions = auctions,
                        availableMakes = makes,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load auctions: ${exception.message}"
                    )
                }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }
    
    fun updateSelectedMake(make: String) {
        _uiState.value = _uiState.value.copy(selectedMake = make)
        applyFilters()
    }
    
    fun updatePriceRange(minPrice: Double?, maxPrice: Double?) {
        _uiState.value = _uiState.value.copy(minPrice = minPrice, maxPrice = maxPrice)
        applyFilters()
    }
    
    private fun applyFilters() {
        val currentState = _uiState.value
        var filtered = currentState.allAuctions
        
        // Apply search query
        if (currentState.searchQuery.isNotBlank()) {
            filtered = filtered.filter { auction ->
                auction.title.contains(currentState.searchQuery, ignoreCase = true) ||
                auction.vehicle.make.contains(currentState.searchQuery, ignoreCase = true) ||
                auction.vehicle.model.contains(currentState.searchQuery, ignoreCase = true) ||
                auction.description.contains(currentState.searchQuery, ignoreCase = true)
            }
        }
        
        // Apply make filter
        if (currentState.selectedMake != "All") {
            filtered = filtered.filter { it.vehicle.make == currentState.selectedMake }
        }
        
        // Apply price range filter
        currentState.minPrice?.let { min ->
            filtered = filtered.filter { it.currentBid >= min }
        }
        
        currentState.maxPrice?.let { max ->
            filtered = filtered.filter { it.currentBid <= max }
        }
        
        _uiState.value = currentState.copy(filteredAuctions = filtered)
    }
    
    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedMake = "All",
            minPrice = null,
            maxPrice = null,
            filteredAuctions = _uiState.value.allAuctions
        )
    }
    
    fun formatPrice(price: Double): String {
        return "$${String.format("%,.0f", price)}"
    }
    
    fun calculateTimeLeft(endTime: String): String {
        // For now, return a simple placeholder
        // You can implement proper time calculation later
        return "2h 30m"
    }
    
    fun getStatusColor(status: String): androidx.compose.ui.graphics.Color {
        return when (status.lowercase()) {
            "active" -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
            "scheduled" -> androidx.compose.ui.graphics.Color(0xFF2196F3) // Blue
            "ended" -> androidx.compose.ui.graphics.Color(0xFF9E9E9E) // Gray
            "cancelled" -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
            "sold" -> androidx.compose.ui.graphics.Color(0xFF9C27B0) // Purple
            else -> androidx.compose.ui.graphics.Color(0xFF9E9E9E) // Gray
        }
    }
}
