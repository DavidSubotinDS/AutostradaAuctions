package com.example.autostradaauctions.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.autostradaauctions.data.repository.ImageRepository
import com.example.autostradaauctions.data.auth.TokenManager
import com.example.autostradaauctions.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

data class AuctionSubmissionUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val selectedImages: List<Uri> = emptyList(),
    val uploadedImageUrls: List<String> = emptyList(),
    val uploadProgress: Float = 0f,
    val submissionSuccess: Boolean = false,
    val error: String? = null
)

data class VehicleDetails(
    val make: String = "",
    val model: String = "",
    val year: String = "",
    val mileage: String = "",
    val condition: String = "Good",
    val description: String = "",
    val startingBid: String = "",
    val reservePrice: String = "",
    val auctionDuration: String = "7" // days
)

class AuctionSubmissionViewModel(
    private val imageRepository: ImageRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuctionSubmissionUiState())
    val uiState: StateFlow<AuctionSubmissionUiState> = _uiState.asStateFlow()
    
    private val _vehicleDetails = MutableStateFlow(VehicleDetails())
    val vehicleDetails: StateFlow<VehicleDetails> = _vehicleDetails.asStateFlow()
    
    fun updateVehicleDetails(details: VehicleDetails) {
        _vehicleDetails.value = details
    }
    
    fun addImages(uris: List<Uri>) {
        val currentImages = _uiState.value.selectedImages
        _uiState.value = _uiState.value.copy(
            selectedImages = (currentImages + uris).take(10) // Max 10 images
        )
    }
    
    fun removeImage(uri: Uri) {
        val currentImages = _uiState.value.selectedImages.toMutableList()
        currentImages.remove(uri)
        _uiState.value = _uiState.value.copy(selectedImages = currentImages)
    }
    
    fun uploadImages(context: android.content.Context) {
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
            
            try {
                val imageUrls = mutableListOf<String>()
                val images = _uiState.value.selectedImages
                
                for ((index, uri) in images.withIndex()) {
                    _uiState.value = _uiState.value.copy(
                        uploadProgress = (index.toFloat() / images.size)
                    )
                    
                    // Upload image
                    imageRepository.uploadImage(context, uri, token).fold(
                        onSuccess = { response ->
                            imageUrls.add(response.imageUrl)
                        },
                        onFailure = { exception ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Failed to upload image: ${exception.message}"
                            )
                            return@launch
                        }
                    )
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    uploadedImageUrls = imageUrls,
                    uploadProgress = 1f
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Upload failed: ${e.message}"
                )
            }
        }
    }
    
    fun submitAuction() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            
            val token = tokenManager.getToken()
            if (token == null) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = "Authentication required"
                )
                return@launch
            }
            
            val details = _vehicleDetails.value
            val imageUrls = _uiState.value.uploadedImageUrls
            
            if (imageUrls.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = "Please upload at least one image"
                )
                return@launch
            }
            
            try {
                // Create auction submission JSON
                val auctionJson = JSONObject().apply {
                    put("make", details.make)
                    put("model", details.model)
                    put("year", details.year.toIntOrNull() ?: 0)
                    put("mileage", details.mileage.toIntOrNull() ?: 0)
                    put("condition", details.condition)
                    put("description", details.description)
                    put("startingBid", details.startingBid.toDoubleOrNull() ?: 0.0)
                    put("reservePrice", details.reservePrice.toDoubleOrNull() ?: 0.0)
                    put("auctionDuration", details.auctionDuration.toIntOrNull() ?: 7)
                    put("imageUrls", imageUrls.joinToString(","))
                }
                
                // Submit to backend API
                val requestBody = auctionJson.toString().toRequestBody("application/json".toMediaTypeOrNull())
                
                // For now, simulate success (you would implement actual API call here)
                kotlinx.coroutines.delay(2000)
                
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submissionSuccess = true
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = "Submission failed: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun resetForm() {
        _uiState.value = AuctionSubmissionUiState()
        _vehicleDetails.value = VehicleDetails()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionSubmissionScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuctionSubmissionViewModel = viewModel {
        AuctionSubmissionViewModel(
            AppContainer.imageRepository,
            AppContainer.tokenManager
        )
    }
) {
    val uiState by viewModel.uiState.collectAsState()
    val vehicleDetails by viewModel.vehicleDetails.collectAsState()
    val context = LocalContext.current
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addImages(uris)
        }
    }
    
    // Success Dialog
    if (uiState.submissionSuccess) {
        AlertDialog(
            onDismissRequest = { 
                viewModel.resetForm()
                onNavigateBack()
            },
            title = { Text("Auction Submitted!") },
            text = { 
                Text("Your auction has been submitted for review. You'll be notified once it's approved by our admin team.")
            },
            confirmButton = {
                Button(
                    onClick = { 
                        viewModel.resetForm()
                        onNavigateBack()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
    
    // Error Snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar for 3 seconds then clear
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
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
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Submit Auction",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Vehicle Details Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Vehicle Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Make and Model Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = vehicleDetails.make,
                            onValueChange = { 
                                viewModel.updateVehicleDetails(vehicleDetails.copy(make = it))
                            },
                            label = { Text("Make") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = vehicleDetails.model,
                            onValueChange = { 
                                viewModel.updateVehicleDetails(vehicleDetails.copy(model = it))
                            },
                            label = { Text("Model") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Year and Mileage Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = vehicleDetails.year,
                            onValueChange = { 
                                viewModel.updateVehicleDetails(vehicleDetails.copy(year = it))
                            },
                            label = { Text("Year") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = vehicleDetails.mileage,
                            onValueChange = { 
                                viewModel.updateVehicleDetails(vehicleDetails.copy(mileage = it))
                            },
                            label = { Text("Mileage") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Condition Dropdown
                    var conditionExpanded by remember { mutableStateOf(false) }
                    val conditions = listOf("Excellent", "Good", "Fair", "Poor")
                    
                    ExposedDropdownMenuBox(
                        expanded = conditionExpanded,
                        onExpandedChange = { conditionExpanded = !conditionExpanded }
                    ) {
                        OutlinedTextField(
                            value = vehicleDetails.condition,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Condition") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = conditionExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = conditionExpanded,
                            onDismissRequest = { conditionExpanded = false }
                        ) {
                            conditions.forEach { condition ->
                                DropdownMenuItem(
                                    text = { Text(condition) },
                                    onClick = {
                                        viewModel.updateVehicleDetails(vehicleDetails.copy(condition = condition))
                                        conditionExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Description
                    OutlinedTextField(
                        value = vehicleDetails.description,
                        onValueChange = { 
                            viewModel.updateVehicleDetails(vehicleDetails.copy(description = it))
                        },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Pricing Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Pricing & Duration",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = vehicleDetails.startingBid,
                            onValueChange = { 
                                viewModel.updateVehicleDetails(vehicleDetails.copy(startingBid = it))
                            },
                            label = { Text("Starting Bid ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = vehicleDetails.reservePrice,
                            onValueChange = { 
                                viewModel.updateVehicleDetails(vehicleDetails.copy(reservePrice = it))
                            },
                            label = { Text("Reserve Price ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = vehicleDetails.auctionDuration,
                        onValueChange = { 
                            viewModel.updateVehicleDetails(vehicleDetails.copy(auctionDuration = it))
                        },
                        label = { Text("Auction Duration (days)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Images Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vehicle Images",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            enabled = !uiState.isLoading && uiState.selectedImages.size < 10
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Photos")
                        }
                    }
                    
                    if (uiState.selectedImages.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.selectedImages) { uri ->
                                Card(
                                    modifier = Modifier.size(100.dp)
                                ) {
                                    Box {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        IconButton(
                                            onClick = { viewModel.removeImage(uri) },
                                            modifier = Modifier.align(Alignment.TopEnd)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        if (uiState.uploadedImageUrls.isEmpty() && uiState.selectedImages.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { viewModel.uploadImages(context) },
                                enabled = !uiState.isLoading,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Uploading... ${(uiState.uploadProgress * 100).toInt()}%")
                                } else {
                                    Text("Upload Images")
                                }
                            }
                        }
                        
                        if (uiState.uploadedImageUrls.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "✓ ${uiState.uploadedImageUrls.size} images uploaded successfully",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Add up to 10 high-quality images of your vehicle",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Submit Button
            Button(
                onClick = { viewModel.submitAuction() },
                enabled = !uiState.isSubmitting && 
                         uiState.uploadedImageUrls.isNotEmpty() &&
                         vehicleDetails.make.isNotBlank() &&
                         vehicleDetails.model.isNotBlank() &&
                         vehicleDetails.startingBid.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submitting...")
                } else {
                    Text("Submit for Review")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Error Snackbar
    uiState.error?.let { error ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}