package com.example.autostradaauctions.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBidsScreen(
    onBackClick: () -> Unit,
    onViewAuction: (String) -> Unit = {}
) {
    // Mock bid data - in real app this would come from ViewModel
    val mockBids = remember {
        listOf(
            MockBid(
                "1", 
                "2020 BMW M3 Competition", 
                52000.0, 
                55000.0,
                "2025-09-15T18:00:00Z",
                BidStatus.WINNING,
                true
            ),
            MockBid(
                "2", 
                "2019 Audi A4 Quattro", 
                33500.0, 
                35000.0,
                "2025-09-12T15:30:00Z",
                BidStatus.OUTBID,
                true
            ),
            MockBid(
                "3", 
                "2021 Mercedes-Benz C-Class", 
                38900.0, 
                38900.0,
                "2025-09-08T12:00:00Z",
                BidStatus.WON,
                false
            ),
            MockBid(
                "4", 
                "2018 Tesla Model 3", 
                25200.0, 
                27500.0,
                "2025-09-05T20:00:00Z",
                BidStatus.LOST,
                false
            )
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "My Bids",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BidStatCard(
                title = "Total Bids",
                value = mockBids.size.toString(),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            BidStatCard(
                title = "Winning",
                value = mockBids.count { it.status == BidStatus.WINNING }.toString(),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            BidStatCard(
                title = "Won",
                value = mockBids.count { it.status == BidStatus.WON }.toString(),
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Filter Tabs
        var selectedFilter by remember { mutableStateOf(BidFilter.ALL) }
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(BidFilter.entries) { filter ->
                FilterChip(
                    onClick = { selectedFilter = filter },
                    label = { Text(filter.displayName) },
                    selected = selectedFilter == filter
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Bids List
        val filteredBids = when (selectedFilter) {
            BidFilter.ALL -> mockBids
            BidFilter.ACTIVE -> mockBids.filter { it.isActive }
            BidFilter.WON -> mockBids.filter { it.status == BidStatus.WON }
            BidFilter.LOST -> mockBids.filter { it.status == BidStatus.LOST }
        }
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredBids) { bid ->
                BidCard(
                    bid = bid,
                    onViewAuction = { onViewAuction(bid.auctionId) }
                )
            }
        }
    }
}

@Composable
fun BidStatCard(
    title: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BidCard(
    bid: MockBid,
    onViewAuction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onViewAuction
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bid.vehicleName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "My Bid",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = NumberFormat.getCurrencyInstance(Locale.US).format(bid.myBidAmount),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Current Bid",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = NumberFormat.getCurrencyInstance(Locale.US).format(bid.currentHighestBid),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (bid.myBidAmount >= bid.currentHighestBid) 
                                    MaterialTheme.colorScheme.secondary 
                                else 
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Ends: ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date())}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Status Badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (bid.status) {
                            BidStatus.WINNING -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                            BidStatus.OUTBID -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            BidStatus.WON -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                            BidStatus.LOST -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (bid.status) {
                                BidStatus.WINNING -> Icons.Default.TrendingUp
                                BidStatus.OUTBID -> Icons.Default.TrendingDown
                                BidStatus.WON -> Icons.Default.EmojiEvents
                                BidStatus.LOST -> Icons.Default.Close
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = when (bid.status) {
                                BidStatus.WINNING -> MaterialTheme.colorScheme.secondary
                                BidStatus.OUTBID -> MaterialTheme.colorScheme.error
                                BidStatus.WON -> MaterialTheme.colorScheme.tertiary
                                BidStatus.LOST -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = bid.status.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = when (bid.status) {
                                BidStatus.WINNING -> MaterialTheme.colorScheme.secondary
                                BidStatus.OUTBID -> MaterialTheme.colorScheme.error
                                BidStatus.WON -> MaterialTheme.colorScheme.tertiary
                                BidStatus.LOST -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

// Mock data classes
data class MockBid(
    val auctionId: String,
    val vehicleName: String,
    val myBidAmount: Double,
    val currentHighestBid: Double,
    val auctionEndTime: String,
    val status: BidStatus,
    val isActive: Boolean
)

enum class BidStatus(val displayName: String) {
    WINNING("Winning"),
    OUTBID("Outbid"),
    WON("Won"),
    LOST("Lost")
}

enum class BidFilter(val displayName: String) {
    ALL("All"),
    ACTIVE("Active"),
    WON("Won"),
    LOST("Lost")
}
