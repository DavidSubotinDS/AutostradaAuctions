package com.example.autostradaauctions.ui.screens.auction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.autostradaauctions.data.model.AuctionStatus
import com.example.autostradaauctions.data.model.Bid
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionDetailScreen(
    auctionId: String,
    userId: String?,
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuctionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    LaunchedEffect(auctionId) {
        viewModel.loadAuction(auctionId)
    }

    // Handle messages
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar or handle error
            viewModel.clearMessages()
        }
    }

    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            // Show success snackbar
            viewModel.clearMessages()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("Auction Details") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (userId != null) {
                    IconButton(onClick = { viewModel.toggleFavorite(userId) }) {
                        Icon(
                            if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (uiState.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.auction != null -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Vehicle Images
                        uiState.vehicle?.imageUrls?.let { images ->
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(images) { imageUrl ->
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = "Vehicle Image",
                                        modifier = Modifier
                                            .size(300.dp, 200.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }

                    item {
                        // Auction Info Card
                        Card {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = uiState.auction!!.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = uiState.auction!!.description,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Current Bid",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        Text(
                                            text = currencyFormat.format(uiState.auction!!.currentPrice),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "Total Bids",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        Text(
                                            text = "${uiState.auction!!.totalBids}",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Ends: ${dateFormat.format(Date(uiState.auction!!.endTime))}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    // Vehicle Details
                    uiState.vehicle?.let { vehicle ->
                        item {
                            Card {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Vehicle Details",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    VehicleDetailRow("Make", vehicle.make)
                                    VehicleDetailRow("Model", vehicle.model)
                                    VehicleDetailRow("Year", vehicle.year.toString())
                                    VehicleDetailRow("Mileage", "${NumberFormat.getInstance().format(vehicle.mileage)} miles")
                                    VehicleDetailRow("Engine", vehicle.engineType)
                                    VehicleDetailRow("Transmission", vehicle.transmission)
                                    VehicleDetailRow("Fuel Type", vehicle.fuelType)
                                    VehicleDetailRow("Color", vehicle.color)
                                    VehicleDetailRow("Condition", vehicle.condition.name)
                                }
                            }
                        }
                    }

                    // Bidding Section
                    if (uiState.auction!!.status == AuctionStatus.ACTIVE) {
                        item {
                            Card {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Place Your Bid",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    if (userId != null) {
                                        OutlinedTextField(
                                            value = uiState.bidAmount,
                                            onValueChange = viewModel::updateBidAmount,
                                            label = { Text("Bid Amount") },
                                            prefix = { Text("$") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = {
                                                uiState.bidAmount.toDoubleOrNull()?.let { amount ->
                                                    viewModel.placeBid(amount, userId)
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = !uiState.isPlacingBid && uiState.bidAmount.isNotEmpty()
                                        ) {
                                            if (uiState.isPlacingBid) {
                                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                            } else {
                                                Text("Place Bid")
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "You must be logged in to place a bid",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.outline
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = onLoginClick,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Login to Bid")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bid History
                    item {
                        Card {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Bid History",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                if (uiState.bids.isEmpty()) {
                                    Text(
                                        text = "No bids yet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                } else {
                                    uiState.bids.forEach { bid ->
                                        BidHistoryItem(bid = bid, currencyFormat = currencyFormat, dateFormat = dateFormat)
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Auction not found")
                }
            }
        }
    }
}

@Composable
private fun VehicleDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun BidHistoryItem(
    bid: Bid,
    currencyFormat: NumberFormat,
    dateFormat: SimpleDateFormat
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = currencyFormat.format(bid.amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = dateFormat.format(Date(bid.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        if (bid.isWinning) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Leading",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
