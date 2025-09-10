package com.example.autostradaauctions.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.autostradaauctions.data.model.Auction
import com.example.autostradaauctions.ui.viewmodel.HomeViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAuctionClick: (String) -> Unit = {},
    onLoginClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Row: Logo + Navigation icons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo - split into two words
            Column {
                Text(
                    text = "Autostrada",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Auctions",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Navigation icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout"
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Full-width Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            label = { Text("Search auctions...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Filters Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Make Filter with typing support
            Box(modifier = Modifier.weight(1f)) {
                var expanded by remember { mutableStateOf(false) }
                var makeSearchText by remember { mutableStateOf("") }
                
                // Filter makes based on search text
                val filteredMakes = remember(uiState.availableMakes, makeSearchText) {
                    if (makeSearchText.isEmpty()) {
                        uiState.availableMakes
                    } else {
                        uiState.availableMakes.filter { 
                            it.contains(makeSearchText, ignoreCase = true)
                        }
                    }
                }
                
                OutlinedTextField(
                    value = if (expanded) makeSearchText else uiState.selectedMake,
                    onValueChange = { value ->
                        if (expanded) {
                            makeSearchText = value
                        }
                    },
                    label = { Text("Make") },
                    readOnly = false,
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown, 
                            contentDescription = "Expand",
                            modifier = Modifier.clickable { 
                                expanded = !expanded
                                if (expanded) {
                                    makeSearchText = ""
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            expanded = true
                            makeSearchText = ""
                        }
                )
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { 
                        expanded = false
                        makeSearchText = ""
                    }
                ) {
                    filteredMakes.forEach { make ->
                        DropdownMenuItem(
                            text = { Text(make) },
                            onClick = {
                                viewModel.updateSelectedMake(make)
                                expanded = false
                                makeSearchText = ""
                            }
                        )
                    }
                }
            }
            
            // Refresh Button
            IconButton(
                onClick = { viewModel.loadAuctions() }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadAuctions() }) {
                        Text("Retry")
                    }
                }
            }
            
            uiState.filteredAuctions.isEmpty() -> {
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
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            else -> {
                // Results header
                Text(
                    text = "${uiState.filteredAuctions.size} auctions found",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Auction List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.filteredAuctions) { auction ->
                        FunctionalAuctionCard(
                            auction = auction,
                            onAuctionClick = { 
                                println("DEBUG: Card clicked for auction ${auction.id} - ${auction.title}")
                                onAuctionClick(auction.id.toString()) 
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FunctionalAuctionCard(
    auction: Auction,
    onAuctionClick: () -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    
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
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = auction.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "${auction.vehicle.year} ${auction.vehicle.make} ${auction.vehicle.model}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Bid: ",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = formatter.format(auction.currentBid),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Surface(
                    color = when (auction.status.lowercase()) {
                        "active" -> MaterialTheme.colorScheme.primaryContainer
                        "ended" -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = auction.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (auction.status.lowercase()) {
                            "active" -> MaterialTheme.colorScheme.onPrimaryContainer
                            "ended" -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Ends: ${auction.endTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
