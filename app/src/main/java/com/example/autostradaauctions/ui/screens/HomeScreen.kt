package com.example.autostradaauctions.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.autostradaauctions.data.model.UserRole
import com.example.autostradaauctions.ui.viewmodel.HomeViewModel
import java.text.NumberFormat
import java.util.*
import java.time.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAuctionClick: (String) -> Unit = {},
    onLoginClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onSellClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    currentUserRole: UserRole? = null,
    viewModel: HomeViewModel = viewModel()
) {
    // 🚨 CRITICAL DEBUG: HomeScreen composable started
    println("🚨🚨🚨 HOMESCREEN COMPOSABLE STARTED")
    android.util.Log.d("AutostradaDebug", "🚨🚨🚨 HOMESCREEN COMPOSABLE STARTED")
    
    val uiState by viewModel.uiState.collectAsState()
    
    println("🚨 HOMESCREEN UI STATE: $uiState")
    android.util.Log.d("AutostradaDebug", "🚨 HOMESCREEN UI STATE: $uiState")
    
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
                // Analytics button for admin users only
                if (currentUserRole == UserRole.ADMIN) {
                    IconButton(onClick = onAnalyticsClick) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Analytics"
                        )
                    }
                }
                // Sell button for authenticated users (sellers)
                if (currentUserRole == UserRole.SELLER || currentUserRole == UserRole.ADMIN) {
                    IconButton(onClick = onSellClick) {
                        Icon(
                            imageVector = Icons.Default.Sell,
                            contentDescription = "Sell"
                        )
                    }
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
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout"
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Enhanced Search Bar (clickable)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSearchClick() },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Search luxury cars, classics, and more...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Quick Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickStatCard(
                title = "Live Auctions",
                value = uiState.liveAuctionsCount.toString(),
                icon = Icons.Default.Gavel
            )
            QuickStatCard(
                title = "Ending Soon", 
                value = uiState.endingSoonCount.toString(),
                icon = Icons.Default.Timer
            )
            QuickStatCard(
                title = "Total Bids",
                value = "${uiState.totalBidsCount}",
                icon = Icons.Default.TrendingUp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category Tabs
        var selectedTab by remember { mutableIntStateOf(0) }
        val tabs = listOf("Live Auctions", "Ending Soon", "Recently Added", "Closed")
        
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        
        // Filters Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Make Filter - Simple dropdown selection only
            Box(modifier = Modifier.weight(1f)) {
                var expanded by remember { mutableStateOf(false) }
                
                OutlinedTextField(
                    value = uiState.selectedMake,
                    onValueChange = { }, // No text input allowed
                    label = { Text("Make") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                            contentDescription = if (expanded) "Collapse" else "Expand"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                )
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    uiState.availableMakes.forEach { make ->
                        DropdownMenuItem(
                            text = { Text(make) },
                            onClick = {
                                viewModel.updateSelectedMake(make)
                                expanded = false
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
                // Get auctions based on selected tab
                val displayAuctions = when (selectedTab) {
                    0 -> uiState.filteredAuctions.filter { auction -> 
                        auction.status.equals("active", ignoreCase = true) ||
                        auction.status.equals("scheduled", ignoreCase = true)
                    }
                    1 -> uiState.filteredAuctions.filter { auction -> 
                        auction.status.equals("active", ignoreCase = true) ||
                        auction.status.equals("scheduled", ignoreCase = true)
                    }.sortedBy { calculateTimeRemaining(it.endTime) }.take(10) // Sort by time remaining, take top 10
                    2 -> uiState.filteredAuctions.filter { auction ->
                        // Recently Added = Pending Approval status
                        auction.status.equals("pendingapproval", ignoreCase = true) || 
                        auction.status.equals("pending_approval", ignoreCase = true)
                    }.sortedByDescending { it.id } // Show newest first
                    3 -> uiState.filteredAuctions.filter { auction -> 
                        auction.status.equals("closed", ignoreCase = true) || 
                        auction.status.equals("ended", ignoreCase = true) ||
                        auction.status.equals("sold", ignoreCase = true)
                    }
                    else -> uiState.filteredAuctions
                }
                
                // Results header
                Text(
                    text = "${displayAuctions.size} ${tabs[selectedTab].lowercase()} found",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Auction List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayAuctions) { auction ->
                        EnhancedAuctionCard(
                            auction = auction,
                            onAuctionClick = { 
                                println("🚨 CARD CLICKED: auction ${auction.id} - ${auction.title}")
                                println("🚨 CALLING onAuctionClick with: ${auction.id.toString()}")
                                onAuctionClick(auction.id.toString()) 
                            },
                            showTimer = selectedTab == 0 || selectedTab == 1 // Show timer for live and ending soon
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .height(80.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EnhancedAuctionCard(
    auction: Auction,
    onAuctionClick: () -> Unit,
    showTimer: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAuctionClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row with title and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = auction.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                // Status badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (auction.status.lowercase()) {
                            "active" -> MaterialTheme.colorScheme.primaryContainer
                            "scheduled" -> MaterialTheme.colorScheme.primaryContainer
                            "closed", "ended" -> MaterialTheme.colorScheme.surfaceVariant
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        }
                    )
                ) {
                    Text(
                        text = auction.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Vehicle info
            Text(
                text = "${auction.vehicle.year} ${auction.vehicle.make} ${auction.vehicle.model}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            // Description
            Text(
                text = auction.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Price and timer row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Current Bid",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale.US).format(auction.currentBid),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (showTimer && (auction.status.equals("active", ignoreCase = true) || auction.status.equals("scheduled", ignoreCase = true))) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Time Remaining",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Time remaining",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = calculateTimeRemaining(auction.endTime),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

// Utility function to calculate time remaining
private fun calculateTimeRemaining(endTime: String): String {
    return try {
        // Handle microseconds in timestamp: "2025-10-20T13:35:47.1553638"
        val cleanEndTime = if (endTime.contains('.')) {
            endTime.substringBefore('.')
        } else {
            endTime
        }
        val endDateTime = LocalDateTime.parse(
            cleanEndTime, 
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        )
        // DATABASE TIMES ARE IN UTC - interpret as UTC, not local timezone
        val endInstant = endDateTime.atZone(java.time.ZoneOffset.UTC).toInstant()
        val now = Instant.now()
        
        val remaining = Duration.between(now, endInstant)
        
        // DEBUG: Log the calculation with full details
        println("DEBUG HomeScreen: endTime='$endTime'")
        println("DEBUG HomeScreen: cleanEndTime='$cleanEndTime'")
        println("DEBUG HomeScreen: endDateTime=$endDateTime") 
        println("DEBUG HomeScreen: endInstant=$endInstant")
        println("DEBUG HomeScreen: now=$now")
        println("DEBUG HomeScreen: remaining=${remaining.toMinutes()}min, isNegative=${remaining.isNegative}")
        
        when {
            remaining.isNegative || remaining.isZero -> {
                println("DEBUG HomeScreen: Returning 'Ended' because remaining is negative/zero")
                "Ended"
            }
            else -> {
                val days = remaining.toDays()
                val hours = remaining.toHours() % 24
                val minutes = remaining.toMinutes() % 60
                
                when {
                    days > 0 -> "${days}d ${hours}h"
                    hours > 0 -> "${hours}h ${minutes}m"
                    else -> "${minutes}m"
                }
            }
        }
    } catch (e: Exception) {
        "Time N/A"
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
