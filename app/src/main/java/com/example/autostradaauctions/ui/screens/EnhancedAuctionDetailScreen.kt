package com.example.autostradaauctions.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.autostradaauctions.di.AppContainer
import com.example.autostradaauctions.ui.component.RealTimeBiddingPanel
import com.example.autostradaauctions.ui.component.VehicleImageGallery
import com.example.autostradaauctions.ui.component.AuctionInfoCard
import com.example.autostradaauctions.ui.component.AuctionTimer
import com.example.autostradaauctions.ui.viewmodel.EnhancedAuctionDetailViewModel
import com.example.autostradaauctions.ui.viewmodel.EnhancedAuctionDetailViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedAuctionDetailScreen(
    auctionId: String,
    onBackClick: () -> Unit,
    viewModel: EnhancedAuctionDetailViewModel = run {
        println("🔥 CREATING VIEWMODEL: AppContainer.auctionRepository = ${AppContainer.auctionRepository}")
        println("🔥 CREATING VIEWMODEL: AppContainer.biddingRepository = ${AppContainer.biddingRepository}")
        viewModel(
            factory = EnhancedAuctionDetailViewModelFactory(
                AppContainer.auctionRepository,
                AppContainer.biddingRepository
            )
        )
    }
) {
    // 🚨 CRITICAL DEBUG: This MUST appear if the composable is executing
    println("🚨🚨🚨 COMPOSABLE EXECUTING - EnhancedAuctionDetailScreen started with ID: $auctionId")
    System.out.println("🚨🚨🚨 COMPOSABLE EXECUTING - EnhancedAuctionDetailScreen started with ID: $auctionId")
    
    println("🔥 ENHANCED AUCTION DETAIL SCREEN STARTED - auctionId=$auctionId")
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Get authentication state
    val isUserLoggedIn by AppContainer.authRepository.isLoggedIn.collectAsStateWithLifecycle()
    val currentUserId = if (isUserLoggedIn) AppContainer.authRepository.getCurrentUserId().toString() else null
    val currentUserEmail = if (isUserLoggedIn) AppContainer.authRepository.getCurrentUserEmail() else null
    
    LaunchedEffect(auctionId) {
        println("DEBUG: LaunchedEffect triggered for auction ID: $auctionId")
        viewModel.loadAuction(auctionId)
    }
    
    // Show bid error as snackbar
    uiState.bidError?.let { error ->
        LaunchedEffect(error) {
            // You can show a snackbar here
            viewModel.clearBidError()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // CRITICAL DEBUG: Check which condition is being hit
        println("🔍 UI STATE DEBUG: isLoading=${uiState.isLoading}, auction=${uiState.auction?.id ?: "NULL"}, error=${uiState.error ?: "NONE"}")
        
        when {
            uiState.isLoading && uiState.auction == null -> {
                println("📋 HIT CONDITION: Loading state with null auction")
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading auction details...")
                    }
                }
            }
            
            uiState.error != null -> {
                println("❌ HIT CONDITION: Error state")
                val errorMessage = uiState.error
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "🚗 Auction Not Found",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (errorMessage?.contains("404") == true) {
                                "This auction may have been removed or the link is incorrect."
                            } else {
                                errorMessage ?: "Unable to load auction details. Please check your connection."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(onClick = onBackClick) {
                                Text("← Go Back")
                            }
                            Button(onClick = { viewModel.refreshAuction() }) {
                                Text("🔄 Retry")
                            }
                        }
                    }
                }
            }
            
            uiState.auction != null -> {
                val auction = uiState.auction!!
                println("🚀 AUCTION LOADED SUCCESSFULLY: id=${auction.id}, status=${auction.status}, title=${auction.vehicle.year} ${auction.vehicle.make}")
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top App Bar
                    TopAppBar(
                        title = { Text("Auction Details") },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.refreshAuction() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                            }
                        }
                    )
                    
                    // 🚨 About to create LazyColumn
                    println("🚨 BEFORE LAZYCOLUMN CREATION")
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        println("🚨 INSIDE LAZYCOLUMN SCOPE")
                        item { 
                            println("🚨 FIRST LAZYCOLUMN ITEM EXECUTING")
                            println("🚨 GALLERY ITEM EXECUTING")
                            // Vehicle Image Gallery
                            VehicleImageGallery(
                                imageUrls = auction.vehicle.imageUrls,
                                vehicleTitle = "${auction.vehicle.year} ${auction.vehicle.make} ${auction.vehicle.model}"
                            )
                        }
                        
                        item {
                            println("🚨 TIMER ITEM EXECUTING")
                            // Auction Timer
                            AuctionTimer(
                                endTime = auction.endTime,
                                onAuctionExpired = {
                                    viewModel.handleAuctionExpired()
                                }
                            )
                        }
                        
                        item {
                            println("🚨 AUCTION INFO ITEM EXECUTING")
                            // Auction Info Card
                            AuctionInfoCard(auction = auction)
                        }
                        
                        item {
                            println("🚨 EXECUTING RealTimeBiddingPanel ITEM")
                            System.out.println("🚨 EXECUTING RealTimeBiddingPanel ITEM")
                            // Real-time Bidding Panel
                            println("🔥 ABOUT TO RENDER RealTimeBiddingPanel - auction=${auction.id}, status=${auction.status}")
                            println("🔥 AUTHENTICATION STATE - isLoggedIn=$isUserLoggedIn, currentUserId=$currentUserId")
                            RealTimeBiddingPanel(
                                auction = auction,
                                bids = uiState.bidHistory,
                                connectionState = uiState.connectionState,
                                onPlaceBid = { amount, bidderName ->
                                    viewModel.placeBid(amount, bidderName)
                                },
                                currentUserName = currentUserEmail ?: "You",


                            )
                        }
                        
                        item {
                            println("🚨 VEHICLE DETAILS ITEM EXECUTING")
                            // Vehicle Details (from your existing ComprehensiveAuctionDetailScreen)
                            VehicleDetailsSection(auction = auction)
                        }
                        
                        item {
                            // Description Section
                            if (auction.description.isNotBlank()) {
                                DescriptionSection(description = auction.description)
                            }
                        }
                        
                        // Add some bottom padding
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
        
        // Success message overlay
        if (uiState.showBidSuccessMessage) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "Bid placed successfully!",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        // Bidding in progress overlay
        if (uiState.isPlacingBid) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Placing bid...")
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleDetailsSection(auction: com.example.autostradaauctions.data.model.AuctionDetail) {
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
            
            DetailRowEnhanced("Make", auction.vehicle.make)
            DetailRowEnhanced("Model", auction.vehicle.model)
            DetailRowEnhanced("Year", auction.vehicle.year.toString())
            DetailRowEnhanced("VIN", auction.vehicle.vin)
            DetailRowEnhanced("Transmission", auction.vehicle.transmission)
            DetailRowEnhanced("Fuel Type", auction.vehicle.fuelType)
            DetailRowEnhanced("Color", auction.vehicle.color)
            DetailRowEnhanced("Mileage", "${auction.vehicle.mileage} miles")
        }
    }
}

@Composable
private fun DetailRowEnhanced(label: String, value: String?) {
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
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
