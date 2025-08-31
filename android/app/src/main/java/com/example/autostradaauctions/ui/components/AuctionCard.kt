package com.example.autostradaauctions.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.autostradaauctions.data.model.Auction
import com.example.autostradaauctions.data.model.AuctionStatus
import java.text.NumberFormat
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionCard(
    auction: Auction,
    onClick: () -> Unit,
    isUserLoggedIn: Boolean,
    modifier: Modifier = Modifier
) {
    val timeRemaining = remember(auction.endTime) {
        (auction.endTime - System.currentTimeMillis()).milliseconds
    }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Vehicle Image
            AsyncImage(
                model = auction.vehicleId, // This would be replaced with actual image URL
                contentDescription = "Vehicle Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Title
                Text(
                    text = auction.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Current Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Current Bid",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = currencyFormat.format(auction.currentPrice),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Status Badge
                    StatusBadge(status = auction.status)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Time Remaining and Bid Count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = "Time",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatTimeRemaining(timeRemaining.inWholeMilliseconds),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Gavel,
                            contentDescription = "Bids",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${auction.totalBids} bids",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Action Button
                if (auction.status == AuctionStatus.ACTIVE) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isUserLoggedIn
                    ) {
                        if (isUserLoggedIn) {
                            Text("Place Bid")
                        } else {
                            Text("Login to Bid")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: AuctionStatus) {
    val (text, color) = when (status) {
        AuctionStatus.ACTIVE -> "Active" to MaterialTheme.colorScheme.primary
        AuctionStatus.COMPLETED -> "Completed" to MaterialTheme.colorScheme.outline
        AuctionStatus.CANCELLED -> "Cancelled" to MaterialTheme.colorScheme.error
        AuctionStatus.PENDING -> "Pending" to MaterialTheme.colorScheme.tertiary
        AuctionStatus.EXTENDED -> "Extended" to MaterialTheme.colorScheme.secondary
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

private fun formatTimeRemaining(milliseconds: Long): String {
    if (milliseconds <= 0) return "Ended"

    val days = milliseconds / (24 * 60 * 60 * 1000)
    val hours = (milliseconds % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
    val minutes = (milliseconds % (60 * 60 * 1000)) / (60 * 1000)

    return when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}
