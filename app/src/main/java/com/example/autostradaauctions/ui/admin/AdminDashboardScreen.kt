package com.example.autostradaauctions.ui.admin

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.autostradaauctions.data.repository.AdminRepository
import com.example.autostradaauctions.data.repository.AdminAnalytics
import com.example.autostradaauctions.data.auth.TokenManager
import com.example.autostradaauctions.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminDashboardUiState(
    val isLoading: Boolean = false,
    val analytics: AdminAnalytics? = null,
    val error: String? = null
)

class AdminDashboardViewModel(
    private val adminRepository: AdminRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadAnalytics()
    }
    
    fun loadAnalytics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val token = tokenManager.getToken()
            if (token == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Authentication required"
                )
                return@launch
            }
            
            adminRepository.getAnalytics(token).fold(
                onSuccess = { analytics ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        analytics = analytics,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load analytics"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class AdminAction(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun AdminDashboardScreen(
    onNavigateToUsers: () -> Unit,
    onNavigateToAuctions: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AdminDashboardViewModel = viewModel {
        AdminDashboardViewModel(
            AppContainer.adminRepository,
            AppContainer.tokenManager
        )
    }
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val adminActions = listOf(
        AdminAction(
            title = "User Management",
            description = "View, edit, and delete users",
            icon = Icons.Default.Person,
            onClick = onNavigateToUsers
        ),
        AdminAction(
            title = "Auction Management",
            description = "Approve and manage auctions",
            icon = Icons.Default.Gavel,
            onClick = onNavigateToAuctions
        ),
        AdminAction(
            title = "System Settings",
            description = "Configure application settings",
            icon = Icons.Default.Settings,
            onClick = { /* TODO: Implement */ }
        ),
        AdminAction(
            title = "Reports",
            description = "Generate system reports",
            icon = Icons.Default.Assessment,
            onClick = { /* TODO: Implement */ }
        )
    )
    
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
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Admin Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Analytics Section
            item {
                Text(
                    text = "System Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                when {
                    uiState.isLoading -> {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    
                    uiState.error != null -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Error loading analytics",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = uiState.error!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.loadAnalytics() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    
                    uiState.analytics != null -> {
                        AnalyticsSection(analytics = uiState.analytics!!)
                    }
                }
            }
            
            // Quick Actions Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(adminActions.chunked(2)) { actionPair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    actionPair.forEach { action ->
                        AdminActionCard(
                            action = action,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill remaining space if odd number of actions
                    if (actionPair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsSection(analytics: AdminAnalytics) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Platform Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // User Stats
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    StatCard(
                        title = "Total Users",
                        value = analytics.totalUsers.toString(),
                        icon = Icons.Default.People,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    StatCard(
                        title = "Admins",
                        value = analytics.totalAdmins.toString(),
                        icon = Icons.Default.AdminPanelSettings,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                item {
                    StatCard(
                        title = "Sellers",
                        value = analytics.totalSellers.toString(),
                        icon = Icons.Default.Sell,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                item {
                    StatCard(
                        title = "Buyers",
                        value = analytics.totalBuyers.toString(),
                        icon = Icons.Default.ShoppingCart,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Auction Stats
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    StatCard(
                        title = "Total Auctions",
                        value = analytics.totalAuctions.toString(),
                        icon = Icons.Default.Gavel,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    StatCard(
                        title = "Active",
                        value = analytics.activeAuctions.toString(),
                        icon = Icons.Default.PlayArrow,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                item {
                    StatCard(
                        title = "Pending",
                        value = analytics.pendingAuctions.toString(),
                        icon = Icons.Default.HourglassEmpty,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                item {
                    StatCard(
                        title = "Ended",
                        value = analytics.endedAuctions.toString(),
                        icon = Icons.Default.Stop,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Revenue and Bids
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Bids",
                    value = analytics.totalBids.toString(),
                    icon = Icons.Default.TouchApp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Revenue",
                    value = "$${String.format("%.2f", analytics.totalRevenue)}",
                    icon = Icons.Default.AttachMoney,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AdminActionCard(
    action: AdminAction,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = action.onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = action.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = action.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}