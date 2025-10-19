package com.example.autostradaauctions.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autostradaauctions.data.model.AuctionDetail
import com.example.autostradaauctions.data.repository.AuctionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.Instant
import java.time.Duration
import java.time.format.DateTimeFormatter
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
