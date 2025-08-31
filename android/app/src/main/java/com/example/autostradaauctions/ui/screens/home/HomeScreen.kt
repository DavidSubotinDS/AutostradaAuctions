package com.example.autostradaauctions.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autostradaauctions.ui.components.AuctionCard
import com.example.autostradaauctions.ui.components.SearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAuctionClick: (String) -> Unit,
    onLoginClick: () -> Unit,
    isUserLoggedIn: Boolean,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Autostrada Auctions") },
            actions = {
                if (!isUserLoggedIn) {
                    TextButton(onClick = onLoginClick) {
                        Text("Login")
                    }
                } else {
                    IconButton(onClick = { /* Navigate to profile */ }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            }
        )

        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = {
                searchQuery = it
                viewModel.filterAuctions(it)
            },
            placeholder = "Search auctions...",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Auction List
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.auctions.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No auctions found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.auctions.filter { auction ->
                            searchQuery.isEmpty() ||
                            auction.title.contains(searchQuery, ignoreCase = true) ||
                            auction.description.contains(searchQuery, ignoreCase = true)
                        }
                    ) { auction ->
                        AuctionCard(
                            auction = auction,
                            onClick = { onAuctionClick(auction.id) },
                            isUserLoggedIn = isUserLoggedIn
                        )
                    }
                }
            }
        }
    }
}
