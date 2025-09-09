package com.example.autostradaauctions.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.autostradaauctions.ui.viewmodel.AuctionDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprehensiveAuctionDetailScreen(
    auctionId: String,
    onBackClick: () -> Unit,
    onPlaceBid: (Double) -> Unit = {},
    viewModel: AuctionDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showBidDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(auctionId) {
        viewModel.loadAuction(auctionId)
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
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
                val errorMessage = uiState.errorMessage
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
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
                    
                    Button(onClick = { viewModel.loadAuction(auctionId) }) {
                        Text("Retry")
                    }
                }
            }
            
            uiState.auction != null -> {
                val auction = uiState.auction
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        // Hero Image Section
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(auction.vehicle.imageUrl.ifEmpty { 
                                        when (auction.vehicle.make.lowercase()) {
                                            "tesla" -> "https://images.unsplash.com/photo-1560958089-b8a1929cea89?w=800&h=600&fit=crop"
                                            "bmw" -> "https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop"
                                            else -> "https://images.unsplash.com/photo-1494976688153-018c804d2886?w=800&h=600&fit=crop"
                                        }
                                    })
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "${auction.vehicle.year} ${auction.vehicle.make} ${auction.vehicle.model}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                error = painterResource(id = android.R.drawable.ic_menu_gallery),
                                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                            )
                            
                            // Gradient overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.7f)
                                            ),
                                            startY = 0f,
                                            endY = Float.POSITIVE_INFINITY
                                        )
                                    )
                            )
                            
                            // Back Button
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .align(Alignment.TopStart)
                                    .background(
                                        Color.Black.copy(alpha = 0.5f),
                                        RoundedCornerShape(50)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            
                            // Status Badge
                            Card(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .align(Alignment.TopEnd),
                                colors = CardDefaults.cardColors(
                                    containerColor = viewModel.getStatusColor(auction.status).copy(alpha = 0.9f)
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = auction.status,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // Vehicle Title & Current Bid
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "${auction.vehicle.year} ${auction.vehicle.make} ${auction.vehicle.model}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Text(
                                    text = auction.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Current Bid: ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = viewModel.formatPrice(auction.currentBid),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    
                    item {
                        // Main Content
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Time & Key Info Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                InfoCard(
                                    icon = Icons.Default.AccessTime,
                                    title = "Time Left",
                                    value = viewModel.calculateTimeLeft(auction.endTime),
                                    modifier = Modifier.weight(1f)
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                InfoCard(
                                    icon = Icons.Default.Speed,
                                    title = "Mileage",
                                    value = "${auction.vehicle.mileage} mi",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Vehicle Details
                            VehicleDetailsSection(auction = auction)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Description
                            DescriptionSection(description = auction.description)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Bidding History Preview
                            BiddingHistorySection(auction = auction, viewModel = viewModel)
                            
                            Spacer(modifier = Modifier.height(100.dp)) // Space for floating button
                        }
                    }
                }
                
                // Floating Action Button for Bidding
                if (auction.status.lowercase() == "active") {
                    FloatingActionButton(
                        onClick = { showBidDialog = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = "Place Bid"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Place Bid")
                        }
                    }
                }
            }
        }
    }
    
    // Bid Dialog
    if (showBidDialog) {
        BidPlacementDialog(
            currentBid = uiState.auction?.currentBid ?: 0.0,
            onBidPlaced = { bidAmount ->
                onPlaceBid(bidAmount)
                showBidDialog = false
            },
            onDismiss = { showBidDialog = false },
            formatPrice = viewModel::formatPrice
        )
    }
}

@Composable
private fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun VehicleDetailsSection(auction: com.example.autostradaauctions.data.model.Auction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Vehicle Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            DetailRow("Make", auction.vehicle.make)
            DetailRow("Model", auction.vehicle.model)
            DetailRow("Year", auction.vehicle.year.toString())
            auction.vehicle.vin?.let { DetailRow("VIN", it) }
            auction.vehicle.engine?.let { DetailRow("Engine", it) }
            auction.vehicle.transmission?.let { DetailRow("Transmission", it) }
            auction.vehicle.fuelType?.let { DetailRow("Fuel Type", it) }
            DetailRow("Color", auction.vehicle.color)
            DetailRow("Mileage", "${auction.vehicle.mileage} miles")
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String?) {
    if (value != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DescriptionSection(description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

@Composable
private fun BiddingHistorySection(
    auction: com.example.autostradaauctions.data.model.Auction,
    viewModel: AuctionDetailViewModel
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
                    text = "Bidding Activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${auction.bids.size} bids",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (auction.bids.isEmpty()) {
                Text(
                    text = "No bids yet. Be the first to bid!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                auction.bids.take(3).forEach { bid ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Bid #${bid.id}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = viewModel.formatPrice(bid.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                if (auction.bids.size > 3) {
                    Text(
                        text = "View all ${auction.bids.size} bids",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BidPlacementDialog(
    currentBid: Double,
    onBidPlaced: (Double) -> Unit,
    onDismiss: () -> Unit,
    formatPrice: (Double) -> String
) {
    var bidText by remember { mutableStateOf("") }
    val minimumBid = currentBid + 100
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Place Your Bid",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Current bid: ${formatPrice(currentBid)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Minimum bid: ${formatPrice(minimumBid)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = bidText,
                    onValueChange = { bidText = it },
                    label = { Text("Your bid amount") },
                    placeholder = { Text(formatPrice(minimumBid)) },
                    leadingIcon = { Text("$") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bidAmount = bidText.toDoubleOrNull()
                    if (bidAmount != null && bidAmount >= minimumBid) {
                        onBidPlaced(bidAmount)
                    }
                },
                enabled = bidText.toDoubleOrNull()?.let { it >= minimumBid } == true
            ) {
                Text("Place Bid")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
