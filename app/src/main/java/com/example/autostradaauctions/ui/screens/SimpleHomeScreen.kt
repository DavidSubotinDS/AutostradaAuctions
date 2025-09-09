package com.example.autostradaauctions.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.autostradaauctions.ui.component.EnhancedAuctionCard
import com.example.autostradaauctions.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleHomeScreen(
    onAuctionClick: (String) -> Unit = {},
    onLoginClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilters by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AutostradaAuctions",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                IconButton(onClick = { viewModel.loadAuctions() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
                IconButton(onClick = onFavoritesClick) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorites"
                    )
                }
                IconButton(onClick = onProfileClick) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile"
                    )
                }
                IconButton(onClick = onLoginClick) {
                    Icon(
                        imageVector = Icons.Default.Login,
                        contentDescription = "Login"
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Enhanced Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            label = { Text("Search auctions...") },
            placeholder = { Text("Type to search...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                Row {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            Icons.Default.FilterList, 
                            contentDescription = "Filters",
                            tint = if (uiState.selectedMake != "All" || uiState.minPrice != null || uiState.maxPrice != null) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                imeAction = androidx.compose.ui.text.input.ImeAction.Search
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Filters Section
        if (showFilters) {
            FilterSection(
                availableMakes = uiState.availableMakes,
                selectedMake = uiState.selectedMake,
                minPrice = uiState.minPrice,
                maxPrice = uiState.maxPrice,
                onMakeSelected = viewModel::updateSelectedMake,
                onPriceRangeChanged = viewModel::updatePriceRange,
                onClearFilters = viewModel::clearFilters,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        // Results Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${uiState.filteredAuctions.size} auctions found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (uiState.searchQuery.isNotEmpty() || uiState.selectedMake != "All" || 
                uiState.minPrice != null || uiState.maxPrice != null) {
                TextButton(onClick = viewModel::clearFilters) {
                    Text("Clear all")
                }
            }
        }
        
        // Loading state
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // Error state
        else if (uiState.errorMessage != null) {
            val errorMessage = uiState.errorMessage
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(onClick = viewModel::loadAuctions) {
                    Text("Retry")
                }
            }
        }
        
        // Empty state
        else if (uiState.filteredAuctions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (uiState.allAuctions.isEmpty()) {
                        "No auctions available"
                    } else {
                        "No auctions match your filters"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Auction List
        else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.filteredAuctions) { auction ->
                    EnhancedAuctionCard(
                        auction = auction,
                        onClick = { onAuctionClick(auction.id.toString()) },
                        formatPrice = viewModel::formatPrice,
                        calculateTimeLeft = viewModel::calculateTimeLeft,
                        getStatusColor = viewModel::getStatusColor
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(
    availableMakes: List<String>,
    selectedMake: String,
    minPrice: Double?,
    maxPrice: Double?,
    onMakeSelected: (String) -> Unit,
    onPriceRangeChanged: (Double?, Double?) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    var minPriceText by remember(minPrice) { mutableStateOf(minPrice?.toInt()?.toString() ?: "") }
    var maxPriceText by remember(maxPrice) { mutableStateOf(maxPrice?.toInt()?.toString() ?: "") }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Makes Filter
            Text(
                text = "Vehicle Make",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(availableMakes) { make ->
                    FilterChip(
                        onClick = { onMakeSelected(make) },
                        label = { Text(make) },
                        selected = make == selectedMake
                    )
                }
            }
            
            // Price Range Filter
            Text(
                text = "Price Range",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = minPriceText,
                    onValueChange = { text ->
                        minPriceText = text
                        val min = text.toDoubleOrNull()
                        val max = maxPriceText.toDoubleOrNull()
                        onPriceRangeChanged(min, max)
                    },
                    label = { Text("Min Price") },
                    placeholder = { Text("$0") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = maxPriceText,
                    onValueChange = { text ->
                        maxPriceText = text
                        val min = minPriceText.toDoubleOrNull()
                        val max = text.toDoubleOrNull()
                        onPriceRangeChanged(min, max)
                    },
                    label = { Text("Max Price") },
                    placeholder = { Text("No limit") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealAuctionCard(
    auction: com.example.autostradaauctions.data.model.Auction,
    onAuctionClick: () -> Unit,
    viewModel: HomeViewModel
) {
    Card(
        onClick = onAuctionClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Car Icon as placeholder for image
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${auction.vehicle.year} ${auction.vehicle.make} ${auction.vehicle.model}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = auction.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Current Bid: ${viewModel.formatPrice(auction.currentBid)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Time Left: ${viewModel.calculateTimeLeft(auction.endTime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = auction.status,
                        style = MaterialTheme.typography.bodySmall,
                        color = viewModel.getStatusColor(auction.status),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// Keep the old components for backward compatibility
data class SimpleAuction(
    val id: String,
    val title: String,
    val currentBid: String,
    val timeLeft: String,
    val imageUrl: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionCard(
    auction: SimpleAuction,
    onAuctionClick: () -> Unit
) {
    Card(
        onClick = onAuctionClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Car Icon as placeholder for image
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = auction.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Current Bid: ${auction.currentBid}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Time Left: ${auction.timeLeft}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
