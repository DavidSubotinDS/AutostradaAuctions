package com.example.autostradaauctions.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.autostradaauctions.data.model.Auction
import com.example.autostradaauctions.ui.component.AuctionSearchCard
import com.example.autostradaauctions.ui.viewmodel.SearchViewModel
import com.example.autostradaauctions.ui.viewmodel.SearchUiState
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSearchScreen(
    onBackClick: () -> Unit,
    onAuctionClick: (String) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilters by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Search Auctions") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(
                        imageVector = if (showFilters) Icons.Default.FilterListOff else Icons.Default.FilterList,
                        contentDescription = if (showFilters) "Hide Filters" else "Show Filters"
                    )
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                label = { Text("Search by make, model, year...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearSearch() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quick Filter Chips
            QuickFilterChips(
                selectedFilters = uiState.selectedQuickFilters,
                onFilterToggle = viewModel::toggleQuickFilter
            )
            
            if (showFilters) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Advanced Filters
                AdvancedFilters(
                    uiState = uiState,
                    onMakeChange = viewModel::updateMake,
                    onModelChange = viewModel::updateModel,
                    onYearRangeChange = viewModel::updateYearRange,
                    onPriceRangeChange = viewModel::updatePriceRange,
                    onStatusChange = viewModel::updateStatus,
                    onSortChange = viewModel::updateSortBy,
                    onClearFilters = viewModel::clearAllFilters
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Results header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.searchResults.size} results found",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                if (uiState.hasActiveFilters) {
                    TextButton(onClick = viewModel::clearAllFilters) {
                        Text("Clear All")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Search Results
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.error != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.error ?: "Unknown error",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = viewModel::retrySearch) {
                                Text("Retry")
                            }
                        }
                    }
                }
                
                uiState.searchResults.isEmpty() -> {
                    EmptySearchResults(
                        hasQuery = uiState.searchQuery.isNotEmpty(),
                        hasFilters = uiState.hasActiveFilters
                    )
                }
                
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.searchResults) { auction ->
                            AuctionSearchCard(
                                auction = auction,
                                searchQuery = uiState.searchQuery,
                                onClick = { onAuctionClick(auction.id.toString()) }
                            )
                        }
                        
                        // Add some bottom padding
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickFilterChips(
    selectedFilters: Set<String>,
    onFilterToggle: (String) -> Unit
) {
    val quickFilters = listOf(
        "Live Auctions" to "live",
        "Ending Soon" to "ending_soon",
        "Luxury Cars" to "luxury",
        "Classic Cars" to "classic",
        "Under $50k" to "under_50k",
        "No Reserve" to "no_reserve"
    )
    
    LazyColumn(
        modifier = Modifier.height(80.dp)
    ) {
        items(quickFilters.chunked(3)) { rowFilters ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowFilters.forEach { (label, key) ->
                    FilterChip(
                        onClick = { onFilterToggle(key) },
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                        selected = selectedFilters.contains(key),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Fill remaining space if needed
                repeat(3 - rowFilters.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AdvancedFilters(
    uiState: SearchUiState,
    onMakeChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onYearRangeChange: (Int, Int) -> Unit,
    onPriceRangeChange: (Double, Double) -> Unit,
    onStatusChange: (String) -> Unit,
    onSortChange: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Advanced Filters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                TextButton(onClick = onClearFilters) {
                    Text("Clear All")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Make and Model Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MakeDropdown(
                    selectedMake = uiState.selectedMake,
                    availableMakes = uiState.availableMakes,
                    onMakeChange = onMakeChange,
                    modifier = Modifier.weight(1f)
                )
                
                ModelDropdown(
                    selectedModel = uiState.selectedModel,
                    availableModels = uiState.availableModels,
                    onModelChange = onModelChange,
                    enabled = uiState.selectedMake.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Year Range
            Text(
                text = "Year Range: ${uiState.minYear} - ${uiState.maxYear}",
                style = MaterialTheme.typography.bodyMedium
            )
            RangeSlider(
                value = uiState.minYear.toFloat()..uiState.maxYear.toFloat(),
                onValueChange = { range ->
                    onYearRangeChange(range.start.toInt(), range.endInclusive.toInt())
                },
                valueRange = 1990f..2024f,
                steps = 33 // 2024 - 1990 - 1
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Price Range
            Text(
                text = "Price Range: ${NumberFormat.getCurrencyInstance(Locale.US).format(uiState.minPrice)} - " +
                      "${NumberFormat.getCurrencyInstance(Locale.US).format(uiState.maxPrice)}",
                style = MaterialTheme.typography.bodyMedium
            )
            RangeSlider(
                value = uiState.minPrice.toFloat()..uiState.maxPrice.toFloat(),
                onValueChange = { range ->
                    onPriceRangeChange(range.start.toDouble(), range.endInclusive.toDouble())
                },
                valueRange = 0f..200000f,
                steps = 19 // For nice increments
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status and Sort Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusDropdown(
                    selectedStatus = uiState.selectedStatus,
                    onStatusChange = onStatusChange,
                    modifier = Modifier.weight(1f)
                )
                
                SortDropdown(
                    selectedSort = uiState.sortBy,
                    onSortChange = onSortChange,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MakeDropdown(
    selectedMake: String,
    availableMakes: List<String>,
    onMakeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedMake,
            onValueChange = { },
            label = { Text("Make") },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickableWithoutRipple { expanded = true }
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All Makes") },
                onClick = {
                    onMakeChange("")
                    expanded = false
                }
            )
            availableMakes.forEach { make ->
                DropdownMenuItem(
                    text = { Text(make) },
                    onClick = {
                        onMakeChange(make)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ModelDropdown(
    selectedModel: String,
    availableModels: List<String>,
    onModelChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedModel,
            onValueChange = { },
            label = { Text("Model") },
            readOnly = true,
            enabled = enabled,
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickableWithoutRipple { if (enabled) expanded = true }
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All Models") },
                onClick = {
                    onModelChange("")
                    expanded = false
                }
            )
            availableModels.forEach { model ->
                DropdownMenuItem(
                    text = { Text(model) },
                    onClick = {
                        onModelChange(model)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun StatusDropdown(
    selectedStatus: String,
    onStatusChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val statuses = listOf(
        "All Statuses" to "",
        "Active" to "active",
        "Ending Soon" to "ending_soon",
        "Closed" to "closed"
    )
    
    Box(modifier = modifier) {
        OutlinedTextField(
            value = statuses.find { it.second == selectedStatus }?.first ?: "All Statuses",
            onValueChange = { },
            label = { Text("Status") },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickableWithoutRipple { expanded = true }
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            statuses.forEach { (label, value) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onStatusChange(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SortDropdown(
    selectedSort: String,
    onSortChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val sortOptions = listOf(
        "Relevance" to "relevance",
        "Price: Low to High" to "price_asc",
        "Price: High to Low" to "price_desc",
        "Time: Ending Soon" to "time_asc",
        "Time: Recently Added" to "time_desc",
        "Year: Newest First" to "year_desc",
        "Year: Oldest First" to "year_asc"
    )
    
    Box(modifier = modifier) {
        OutlinedTextField(
            value = sortOptions.find { it.second == selectedSort }?.first ?: "Relevance",
            onValueChange = { },
            label = { Text("Sort By") },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickableWithoutRipple { expanded = true }
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            sortOptions.forEach { (label, value) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSortChange(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptySearchResults(
    hasQuery: Boolean,
    hasFilters: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = when {
                    hasQuery && hasFilters -> "No auctions match your search and filters"
                    hasQuery -> "No auctions match your search"
                    hasFilters -> "No auctions match your filters"
                    else -> "Start searching for auctions"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = when {
                    hasQuery || hasFilters -> "Try adjusting your search criteria or clearing some filters"
                    else -> "Use the search bar above to find specific auctions or apply filters to browse by category"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Extension function for clickable without ripple effect
@Composable
private fun Modifier.clickableWithoutRipple(onClick: () -> Unit): Modifier {
    return this.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        onClick()
    }
}