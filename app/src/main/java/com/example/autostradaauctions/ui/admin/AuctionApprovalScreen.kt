package com.example.autostradaauctions.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.autostradaauctions.data.auth.TokenManager
import com.example.autostradaauctions.data.repository.AuctionRepository
import com.example.autostradaauctions.data.model.PendingAuctionData
import com.example.autostradaauctions.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class ApprovalUiState(
    val isLoading: Boolean = false,
    val pendingAuctions: List<PendingAuctionData> = emptyList(),
    val selectedAuction: PendingAuctionData? = null,
    val showApprovalDialog: Boolean = false,
    val showRejectionDialog: Boolean = false,
    val rejectionReason: String = "",
    val isProcessing: Boolean = false,
    val error: String? = null
)

class AuctionApprovalViewModel(
    private val auctionRepository: AuctionRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ApprovalUiState())
    val uiState: StateFlow<ApprovalUiState> = _uiState.asStateFlow()
    
    companion object {
        private const val BASE_URL = "http://10.0.2.2:5000/api"
    }
    
    init {
        loadPendingAuctions()
    }
    
    fun loadPendingAuctions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val authToken = tokenManager.getAccessToken()
            if (authToken == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Not authenticated"
                )
                return@launch
            }
            
            auctionRepository.getPendingAuctions(authToken).fold(
                onSuccess = { auctions ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pendingAuctions = auctions
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load pending auctions"
                    )
                }
            )
        }
    }

    fun showApprovalDialog(auction: PendingAuctionData) {
        _uiState.value = _uiState.value.copy(
            selectedAuction = auction,
            showApprovalDialog = true
        )
    }
    
    fun showRejectionDialog(auction: PendingAuctionData) {
        _uiState.value = _uiState.value.copy(
            selectedAuction = auction,
            showRejectionDialog = true,
            rejectionReason = ""
        )
    }
    
    fun updateRejectionReason(reason: String) {
        _uiState.value = _uiState.value.copy(rejectionReason = reason)
    }
    
    fun approveAuction() {
        val auction = _uiState.value.selectedAuction ?: return
        processAuctionDecision(auction.id, true, "")
    }
    
    fun rejectAuction() {
        val auction = _uiState.value.selectedAuction ?: return
        val reason = _uiState.value.rejectionReason
        if (reason.isBlank()) return
        
        processAuctionDecision(auction.id, false, reason)
    }
    
    private fun processAuctionDecision(auctionId: Int, approved: Boolean, reason: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, error = null)
            
            val authToken = tokenManager.getAccessToken()
            if (authToken == null) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "Not authenticated"
                )
                return@launch
            }
            
            val rejectionReason = if (!approved && reason.isNotBlank()) reason else null
            
            auctionRepository.approveAuction(auctionId, approved, rejectionReason, authToken).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        showApprovalDialog = false,
                        showRejectionDialog = false,
                        selectedAuction = null
                    )
                    loadPendingAuctions() // Refresh the list
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        error = exception.message ?: "Failed to process approval"
                    )
                }
            )
        }
    }

    fun hideDialogs() {
        _uiState.value = _uiState.value.copy(
            showApprovalDialog = false,
            showRejectionDialog = false,
            selectedAuction = null,
            rejectionReason = "",
            isProcessing = false
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionApprovalScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuctionApprovalViewModel = viewModel {
        AuctionApprovalViewModel(
            AppContainer.auctionRepository,
            AppContainer.tokenManager
        )
    }
) {
    val uiState by viewModel.uiState.collectAsState()
    
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Auction Approvals",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
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
                            text = "Error",
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
                            onClick = { viewModel.loadPendingAuctions() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            
            uiState.pendingAuctions.isEmpty() -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Pending Auctions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "All auctions have been reviewed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.pendingAuctions) { auction ->
                        PendingAuctionCard(
                            auction = auction,
                            onApprove = { viewModel.showApprovalDialog(auction) },
                            onReject = { viewModel.showRejectionDialog(auction) }
                        )
                    }
                }
            }
        }
    }
    
    // Approval Dialog
    if (uiState.showApprovalDialog && uiState.selectedAuction != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDialogs() },
            title = { Text("Approve Auction") },
            text = { 
                Text("Are you sure you want to approve this ${uiState.selectedAuction!!.year} ${uiState.selectedAuction!!.make} ${uiState.selectedAuction!!.model} auction?")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.approveAuction() },
                    enabled = !uiState.isProcessing
                ) {
                    if (uiState.isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Approve")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.hideDialogs() },
                    enabled = !uiState.isProcessing
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Rejection Dialog
    if (uiState.showRejectionDialog && uiState.selectedAuction != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDialogs() },
            title = { Text("Reject Auction") },
            text = {
                Column {
                    Text("Please provide a reason for rejecting this auction:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.rejectionReason,
                        onValueChange = { viewModel.updateRejectionReason(it) },
                        label = { Text("Rejection Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.rejectAuction() },
                    enabled = !uiState.isProcessing && uiState.rejectionReason.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (uiState.isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Reject")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.hideDialogs() },
                    enabled = !uiState.isProcessing
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PendingAuctionCard(
    auction: PendingAuctionData,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${auction.year} ${auction.make} ${auction.model}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Seller: ${auction.submittedBy}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Submitted: ${auction.submittedAt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                AssistChip(
                    onClick = { },
                    label = { Text("Pending") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Images
            if (auction.imageUrls.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(auction.imageUrls.take(5)) { imageUrl ->
                        Card(
                            modifier = Modifier.size(80.dp)
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    if (auction.imageUrls.size > 5) {
                        item {
                            Card(
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+${auction.imageUrls.size - 5}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DetailChip(
                    label = "Mileage",
                    value = "${auction.mileage} mi"
                )
                DetailChip(
                    label = "Condition",
                    value = auction.condition
                )
                DetailChip(
                    label = "Starting Bid",
                    value = "$${String.format("%.0f", auction.startingPrice)}"
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Description
            Text(
                text = auction.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve")
                }
                
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject")
                }
            }
        }
    }
}

@Composable
fun DetailChip(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}