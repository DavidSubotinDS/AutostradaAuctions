package com.example.autostradaauctions.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autostradaauctions.data.model.AuctionDetail
import com.example.autostradaauctions.data.repository.AuctionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

data class AuctionDetailUiState(
    val auction: AuctionDetail? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuctionDetailViewModel : ViewModel() {
    private val repository = AuctionRepository()
    
    private val _uiState = MutableStateFlow(AuctionDetailUiState())
    val uiState: StateFlow<AuctionDetailUiState> = _uiState.asStateFlow()
    
    fun loadAuctionDetail(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            repository.getAuctionDetail(id)
                .onSuccess { auction ->
                    _uiState.value = _uiState.value.copy(
                        auction = auction,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load auction details: ${exception.message}"
                    )
                }
        }
    }
    
    fun formatPrice(price: Double): String {
        return "$${String.format("%,.0f", price)}"
    }
    
    fun formatMileage(mileage: Int): String {
        return "${String.format("%,d", mileage)} miles"
    }
    
    fun calculateTimeLeft(endTime: String): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val endDate = format.parse(endTime)
            val now = Date()
            
            if (endDate != null) {
                val diffInMillis = endDate.time - now.time
                
                if (diffInMillis <= 0) {
                    "Auction Ended"
                } else {
                    val days = diffInMillis / (1000 * 60 * 60 * 24)
                    val hours = (diffInMillis / (1000 * 60 * 60)) % 24
                    val minutes = (diffInMillis / (1000 * 60)) % 60
                    
                    when {
                        days > 0 -> "${days}d ${hours}h ${minutes}m"
                        hours > 0 -> "${hours}h ${minutes}m"
                        else -> "${minutes}m"
                    }
                }
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
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
