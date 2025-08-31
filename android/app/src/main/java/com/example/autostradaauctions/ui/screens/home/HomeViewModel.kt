package com.example.autostradaauctions.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autostradaauctions.data.model.Auction
import com.example.autostradaauctions.data.repository.AuctionRepository
import com.example.autostradaauctions.data.repository.MockAuctionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mockRepository: MockAuctionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadAuctions()
    }

    private fun loadAuctions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            mockRepository.getActiveAuctions().collect { auctions ->
                _uiState.value = _uiState.value.copy(
                    auctions = auctions,
                    isLoading = false
                )
            }
        }
    }

    fun refreshAuctions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            mockRepository.refreshAuctions()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    fun filterAuctions(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
}

data class HomeUiState(
    val auctions: List<Auction> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val errorMessage: String? = null
)
