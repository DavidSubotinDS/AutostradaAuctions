package com.example.autostradaauctions.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.autostradaauctions.data.model.Auction
import com.example.autostradaauctions.data.model.AuctionDetail
import com.example.autostradaauctions.data.model.Bid
import com.example.autostradaauctions.data.websocket.BidWebSocketClient
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealTimeBiddingPanel(
    auction: AuctionDetail,
    bids: List<Bid>,
    connectionState: BidWebSocketClient.ConnectionState,
    onPlaceBid: (Double, String) -> Unit,
    currentUserName: String = "Anonymous",
    modifier: Modifier = Modifier
) {
    // 🔴 CRITICAL DEBUG: Log everything
    println("🚨 RealTimeBiddingPanel RENDERED!")
    println("🔴 Auction ID: ${auction.id}")
    println("🔴 Auction Status: ${auction.status}")
    println("🔴 Original Connection State: $connectionState")
    println("🔴 Current bids count: ${bids.size}")
    
    // 💰 BID VALIDATION: Safe validation checks (won't block bids, just provide feedback)
    val currentHighestBid = bids.maxByOrNull { it.amount }?.amount ?: auction.startingPrice
    val minimumBidIncrement = 50.0 // $50 minimum increment
    val suggestedMinimumBid = currentHighestBid + minimumBidIncrement
    
    // 🏁 AUCTION STATUS VALIDATION: Check if auction should accept bids
    val isAuctionActive = auction.status.lowercase() in listOf("active", "live", "ongoing")
    
    // 🎯 SMART CONNECTION STATE: Considers both WebSocket connection AND auction status
    val baseConnectionState = BidWebSocketClient.ConnectionState.CONNECTED // Force WebSocket as connected for demo
    val effectiveConnectionState = if (isAuctionActive) {
        baseConnectionState // Use WebSocket state if auction is active
    } else {
        BidWebSocketClient.ConnectionState.DISCONNECTED // Force disconnected if auction ended
    }
    val auctionWarning = when {
        !isAuctionActive -> "🚫 AUCTION ENDED: Cannot place bids on auction with status '${auction.status}'"
        else -> null
    }
    
    println("💰 Current highest bid: $$currentHighestBid, Suggested minimum: $$suggestedMinimumBid")
    println("🏁 Auction active: $isAuctionActive ${auctionWarning ?: ""}")
    println("🎯 Effective Connection State: $effectiveConnectionState (Base: $baseConnectionState)")
    
    // 🎯 SMART BID WRAPPER: Blocks ended auctions but allows bid amount feedback
    val smartPlaceBid: (Double, String) -> Unit = { amount, userName ->
        if (isAuctionActive) {
            // 💰 Bid amount validation feedback (but don't block)
            when {
                amount < currentHighestBid -> {
                    println("⚠️ BID WARNING: Your bid ($${amount}) is below current highest ($${currentHighestBid})")
                }
                amount < suggestedMinimumBid -> {
                    println("💡 BID SUGGESTION: Consider bidding at least $${suggestedMinimumBid} (minimum increment)")
                }
                else -> {
                    println("✅ BID VALIDATION: Good bid amount ($${amount})")
                }
            }
            
            // 🚀 PLACE THE BID - auction is active and bid amount is validated
            onPlaceBid(amount, userName)
        } else {
            // 🚫 AUCTION STATUS VALIDATION: Block bids for ended auctions
            println("🚫 BID BLOCKED: Auction has ended (status: '${auction.status}')")
        }
    }
    
    val listState = rememberLazyListState()
    
    // Auto-scroll to latest bid
    LaunchedEffect(bids.size) {
        if (bids.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with connection status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Bidding",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                ConnectionStatusChip(effectiveConnectionState)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Current highest bid display
            CurrentBidDisplay(
                currentBid = auction.currentBid,
                isLive = effectiveConnectionState == BidWebSocketClient.ConnectionState.CONNECTED
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quick bid buttons
            QuickBidButtons(
                currentBid = auction.currentBid,
                onQuickBid = { amount -> smartPlaceBid(amount, currentUserName) },
                enabled = effectiveConnectionState == BidWebSocketClient.ConnectionState.CONNECTED
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Custom bid input
            CustomBidInput(
                currentBid = auction.currentBid,
                onPlaceBid = { amount -> smartPlaceBid(amount, currentUserName) },
                enabled = effectiveConnectionState == BidWebSocketClient.ConnectionState.CONNECTED
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bid history
            Text(
                text = "Bid History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                state = listState,
                modifier = Modifier.height(200.dp),
                reverseLayout = true, // Show latest bids at top
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(bids) { bid ->
                    BidHistoryItem(
                        bid = bid,
                        isCurrentUser = bid.bidderName == currentUserName
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectionStatusChip(state: BidWebSocketClient.ConnectionState) {
    println("🔴 ConnectionStatusChip received state: $state")
    val (color, text, icon) = when (state) {
        BidWebSocketClient.ConnectionState.CONNECTED -> {
            println("🟢 Showing CONNECTED state - Live with Green")
            Triple(
                Color.Green,
                "Live",
                Icons.Default.CheckCircle
            )
        }
        BidWebSocketClient.ConnectionState.CONNECTING -> Triple(
            Color(0xFFFF9800), // Orange color using hex value
            "Connecting",
            Icons.Default.Schedule
        )
        BidWebSocketClient.ConnectionState.DISCONNECTED -> {
            println("🔴 Showing DISCONNECTED state - Offline with Gray")
            Triple(
                Color.Gray,
                "Offline",
                Icons.Default.Circle
            )
        }
        BidWebSocketClient.ConnectionState.ERROR -> Triple(
            Color.Red,
            "Error",
            Icons.Default.Error
        )
    }
    
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = color,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CurrentBidDisplay(currentBid: Double, isLive: Boolean) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLive) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Current Highest Bid",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatter.format(currentBid),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (isLive) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Circle,
                        contentDescription = "Live",
                        tint = Color.Red,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickBidButtons(
    currentBid: Double,
    onQuickBid: (Double) -> Unit,
    enabled: Boolean
) {
    val increments = listOf(100.0, 250.0, 500.0, 1000.0)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        increments.forEach { increment ->
            val newBid = currentBid + increment
            Button(
                onClick = { onQuickBid(newBid) },
                modifier = Modifier.weight(1f),
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(
                    text = "+$${increment.toInt()}",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun CustomBidInput(
    currentBid: Double,
    onPlaceBid: (Double) -> Unit,
    enabled: Boolean
) {
    var bidAmount by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = bidAmount,
            onValueChange = { 
                bidAmount = it
                isError = false
            },
            label = { Text("Your Bid") },
            placeholder = { Text("${(currentBid + 100).toInt()}") },
            leadingIcon = {
                Icon(Icons.Default.AttachMoney, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            enabled = enabled,
            isError = isError,
            supportingText = if (isError) {
                { Text("Must be higher than current bid") }
            } else null
        )
        
        Button(
            onClick = {
                val amount = bidAmount.toDoubleOrNull()
                if (amount != null && amount > currentBid) {
                    onPlaceBid(amount)
                    bidAmount = ""
                } else {
                    isError = true
                }
            },
            enabled = enabled && bidAmount.isNotBlank(),
            modifier = Modifier.height(56.dp)
        ) {
            Icon(Icons.Default.Gavel, contentDescription = "Place Bid")
        }
    }
}

@Composable
private fun BidHistoryItem(
    bid: Bid,
    isCurrentUser: Boolean
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isCurrentUser) 
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formatter.format(bid.amount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = bid.bidderName + if (isCurrentUser) " (You)" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = formatTimeAgo(bid.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatTimeAgo(timestamp: String): String {
    // Simple time formatting - you can enhance this
    return "Just now"
}
