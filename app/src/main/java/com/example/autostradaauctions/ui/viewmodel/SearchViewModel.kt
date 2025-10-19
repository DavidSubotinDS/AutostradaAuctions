package com.example.autostradaauctions.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autostradaauctions.data.model.Auction
import com.example.autostradaauctions.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class SearchUiState(
    val searchQuery: String = "",
    val selectedQuickFilters: Set<String> = emptySet(),
    val selectedMake: String = "",
    val selectedModel: String = "",
    val minYear: Int = 1990,
    val maxYear: Int = 2024,
    val minPrice: Double = 0.0,
    val maxPrice: Double = 200000.0,
    val selectedStatus: String = "",
    val sortBy: String = "relevance",
    val searchResults: List<Auction> = emptyList(),
    val availableMakes: List<String> = emptyList(),
    val availableModels: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val hasActiveFilters: Boolean
        get() = selectedQuickFilters.isNotEmpty() ||
                selectedMake.isNotEmpty() ||
                selectedModel.isNotEmpty() ||
                minYear != 1990 ||
                maxYear != 2024 ||
                minPrice != 0.0 ||
                maxPrice != 200000.0 ||
                selectedStatus.isNotEmpty() ||
                sortBy != "relevance"
}

class SearchViewModel(
    private val appContainer: AppContainer
) : ViewModel() {
    private val repository = appContainer.auctionRepository
    
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    private var allAuctions: List<Auction> = emptyList()
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            repository.getAuctions()
                .onSuccess { auctions ->
                    allAuctions = auctions
                    val makes = auctions.map { it.vehicle.make }.distinct().sorted()
                    
                    _uiState.value = _uiState.value.copy(
                        searchResults = auctions,
                        availableMakes = makes,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load auctions: ${exception.message}"
                    )
                }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        performSearch()
    }
    
    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "")
        performSearch()
    }
    
    fun toggleQuickFilter(filter: String) {
        val currentFilters = _uiState.value.selectedQuickFilters.toMutableSet()
        if (currentFilters.contains(filter)) {
            currentFilters.remove(filter)
        } else {
            currentFilters.add(filter)
        }
        _uiState.value = _uiState.value.copy(selectedQuickFilters = currentFilters)
        performSearch()
    }
    
    fun updateMake(make: String) {
        val models = if (make.isEmpty()) {
            emptyList()
        } else {
            allAuctions.filter { it.vehicle.make == make }
                .map { it.vehicle.model }
                .distinct()
                .sorted()
        }
        
        _uiState.value = _uiState.value.copy(
            selectedMake = make,
            selectedModel = "", // Reset model when make changes
            availableModels = models
        )
        performSearch()
    }
    
    fun updateModel(model: String) {
        _uiState.value = _uiState.value.copy(selectedModel = model)
        performSearch()
    }
    
    fun updateYearRange(minYear: Int, maxYear: Int) {
        _uiState.value = _uiState.value.copy(minYear = minYear, maxYear = maxYear)
        performSearch()
    }
    
    fun updatePriceRange(minPrice: Double, maxPrice: Double) {
        _uiState.value = _uiState.value.copy(minPrice = minPrice, maxPrice = maxPrice)
        performSearch()
    }
    
    fun updateStatus(status: String) {
        _uiState.value = _uiState.value.copy(selectedStatus = status)
        performSearch()
    }
    
    fun updateSortBy(sortBy: String) {
        _uiState.value = _uiState.value.copy(sortBy = sortBy)
        performSearch()
    }
    
    fun clearAllFilters() {
        _uiState.value = _uiState.value.copy(
            selectedQuickFilters = emptySet(),
            selectedMake = "",
            selectedModel = "",
            minYear = 1990,
            maxYear = 2024,
            minPrice = 0.0,
            maxPrice = 200000.0,
            selectedStatus = "",
            sortBy = "relevance",
            availableModels = emptyList()
        )
        performSearch()
    }
    
    fun retrySearch() {
        _uiState.value = _uiState.value.copy(error = null)
        loadInitialData()
    }
    
    private fun performSearch() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Add a small delay to debounce search
            delay(300)
            
            try {
                var filteredAuctions = allAuctions
                
                // Apply text search
                val query = _uiState.value.searchQuery.trim()
                if (query.isNotEmpty()) {
                    filteredAuctions = filteredAuctions.filter { auction ->
                        auction.title.contains(query, ignoreCase = true) ||
                        auction.description.contains(query, ignoreCase = true) ||
                        auction.vehicle.make.contains(query, ignoreCase = true) ||
                        auction.vehicle.model.contains(query, ignoreCase = true) ||
                        auction.vehicle.year.toString().contains(query)
                    }
                }
                
                // Apply quick filters
                _uiState.value.selectedQuickFilters.forEach { filter ->
                    filteredAuctions = when (filter) {
                        "live" -> filteredAuctions.filter { it.status == "active" }
                        "ending_soon" -> filteredAuctions.filter { 
                            it.status == "active" // Mock ending soon logic
                        }.take(5)
                        "luxury" -> filteredAuctions.filter { it.currentBid >= 100000 }
                        "classic" -> filteredAuctions.filter { it.vehicle.year <= 1995 }
                        "under_50k" -> filteredAuctions.filter { it.currentBid < 50000 }
                        "no_reserve" -> filteredAuctions.filter { it.reservePrice == null || it.reservePrice == 0.0 }
                        else -> filteredAuctions
                    }
                }
                
                // Apply advanced filters
                val state = _uiState.value
                
                if (state.selectedMake.isNotEmpty()) {
                    filteredAuctions = filteredAuctions.filter { it.vehicle.make == state.selectedMake }
                }
                
                if (state.selectedModel.isNotEmpty()) {
                    filteredAuctions = filteredAuctions.filter { it.vehicle.model == state.selectedModel }
                }
                
                filteredAuctions = filteredAuctions.filter { auction ->
                    auction.vehicle.year in state.minYear..state.maxYear &&
                    auction.currentBid in state.minPrice..state.maxPrice
                }
                
                if (state.selectedStatus.isNotEmpty()) {
                    filteredAuctions = when (state.selectedStatus) {
                        "ending_soon" -> filteredAuctions.filter { it.status == "active" }.take(5)
                        else -> filteredAuctions.filter { it.status == state.selectedStatus }
                    }
                }
                
                // Apply sorting
                filteredAuctions = when (state.sortBy) {
                    "price_asc" -> filteredAuctions.sortedBy { it.currentBid }
                    "price_desc" -> filteredAuctions.sortedByDescending { it.currentBid }
                    "time_asc" -> filteredAuctions.sortedBy { it.endTime }
                    "time_desc" -> filteredAuctions.sortedByDescending { it.startTime }
                    "year_asc" -> filteredAuctions.sortedBy { it.vehicle.year }
                    "year_desc" -> filteredAuctions.sortedByDescending { it.vehicle.year }
                    else -> filteredAuctions // relevance - keep original order
                }
                
                _uiState.value = _uiState.value.copy(
                    searchResults = filteredAuctions,
                    isLoading = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Search failed: ${e.message}"
                )
            }
        }
    }
}