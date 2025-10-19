package com.example.autostradaauctions.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autostradaauctions.data.model.Auction
import com.example.autostradaauctions.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter
import androidx.compose.ui.graphics.Color

data class HomeUiState(
    val allAuctions: List<Auction> = emptyList(),
    val filteredAuctions: List<Auction> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedMake: String = "All",
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val availableMakes: List<String> = emptyList(),
    // New properties for enhanced home screen
    val liveAuctionsCount: Int = 0,
    val endingSoonCount: Int = 0,
    val totalBidsCount: Int = 0
)

class HomeViewModel : ViewModel() {
    private val repository = AppContainer.auctionRepository
    
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
                    
                    // Calculate stats
                    val liveAuctions = auctions.filter { 
                        it.status.equals("active", ignoreCase = true) ||
                        it.status.equals("scheduled", ignoreCase = true)
                    }
                    val endingSoon = liveAuctions.take(3) // For now, take first 3 as "ending soon"
                    val totalBids = auctions.sumOf { it.currentBid.toInt() / 1000 } // Estimate based on bid values
                    
                    _uiState.value = _uiState.value.copy(
                        allAuctions = auctions,
                        filteredAuctions = auctions,
                        availableMakes = makes,
                        isLoading = false,
                        liveAuctionsCount = liveAuctions.size,
                        endingSoonCount = endingSoon.size,
                        totalBidsCount = totalBids
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
        return try {
            // Handle both formats: "2025-10-29T11:36:32.5329009" and "2025-10-29T11:36:32"
            val cleanEndTime = endTime.substringBefore('.')
            val endDateTime = LocalDateTime.parse(
                cleanEndTime, 
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            )
            val endInstant = endDateTime.atZone(ZoneId.systemDefault()).toInstant()
            val now = Instant.now()
            
            val remaining = Duration.between(now, endInstant)
            
            when {
                remaining.isNegative || remaining.isZero -> "Ended"
                else -> {
                    val days = remaining.toDays()
                    val hours = remaining.toHours() % 24
                    val minutes = remaining.toMinutes() % 60
                    val seconds = remaining.seconds % 60
                    
                    // Always show full format: days, hours, minutes, seconds
                    "${days}d ${hours}h ${minutes}m ${seconds}s"
                }
            }
        } catch (e: Exception) {
            // Debug log to see what's causing the parsing issue
            println("DEBUG: Error parsing endTime '$endTime': ${e.message}")
            "Unknown"
        }
    }
    
    fun getStatusColor(status: String): Color {
        return when (status.lowercase()) {
            "active" -> Color(0xFF4CAF50) // Green
            "scheduled" -> Color(0xFF2196F3) // Blue
            "ended" -> Color(0xFF9E9E9E) // Gray
            "cancelled" -> Color(0xFFF44336) // Red
            "sold" -> Color(0xFF9C27B0) // Purple
            else -> Color(0xFF9E9E9E) // Gray
        }
    }
}
