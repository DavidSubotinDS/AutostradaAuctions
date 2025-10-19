package com.example.autostradaauctions.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.example.autostradaauctions.data.repository.ImageRepository
import com.example.autostradaauctions.data.repository.ImageUploadResponse
import com.example.autostradaauctions.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImageUploadViewModel(
    private val imageRepository: ImageRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ImageUploadUiState())
    val uiState: StateFlow<ImageUploadUiState> = _uiState.asStateFlow()
    
    fun addImage(uri: Uri) {
        val currentState = _uiState.value
        if (currentState.selectedImages.size < 10) { // Max 10 images
            _uiState.value = currentState.copy(
                selectedImages = currentState.selectedImages + uri
            )
        }
    }
    
    fun removeImage(index: Int) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            selectedImages = currentState.selectedImages.filterIndexed { i, _ -> i != index }
        )
    }
    
    fun uploadImages(authToken: String) {
        if (_uiState.value.selectedImages.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true, errorMessage = null)
            
            try {
                val context = _uiState.value.context
                if (context != null) {
                    val result = imageRepository.uploadMultipleImages(
                        context, 
                        _uiState.value.selectedImages, 
                        authToken
                    )
                    
                    result.fold(
                        onSuccess = { uploadedImages ->
                            _uiState.value = _uiState.value.copy(
                                isUploading = false,
                                uploadedImages = uploadedImages,
                                selectedImages = emptyList()
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isUploading = false,
                                errorMessage = error.message ?: "Upload failed"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    errorMessage = e.message ?: "Upload failed"
                )
            }
        }
    }
    
    fun setContext(context: android.content.Context) {
        _uiState.value = _uiState.value.copy(context = context)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class ImageUploadUiState(
    val selectedImages: List<Uri> = emptyList(),
    val uploadedImages: List<ImageUploadResponse> = emptyList(),
    val isUploading: Boolean = false,
    val errorMessage: String? = null,
    val context: android.content.Context? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageUploadScreen(
    modifier: Modifier = Modifier,
    authToken: String,
    onImagesUploaded: (List<String>) -> Unit = {},
    onBackPress: () -> Unit = {}
) {
    // Create ViewModel with manual dependency injection
    val viewModel = remember {
        ImageUploadViewModel(AppContainer.imageRepository)
    }
    
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Set context in view model
    LaunchedEffect(context) {
        viewModel.setContext(context)
    }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            viewModel.addImage(uri)
        }
    }
    
    // Handle successful upload
    LaunchedEffect(uiState.uploadedImages) {
        if (uiState.uploadedImages.isNotEmpty()) {
            val imageUrls = uiState.uploadedImages.map { it.imageUrl }
            onImagesUploaded(imageUrls)
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPress) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
            
            Text(
                text = "Upload Images",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            
            TextButton(
                onClick = { viewModel.uploadImages(authToken) },
                enabled = uiState.selectedImages.isNotEmpty() && !uiState.isUploading
            ) {
                Text("Upload")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Error message
        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    
                    IconButton(onClick = { viewModel.clearError() }) {
                        Icon(
                            Icons.Default.Close, 
                            contentDescription = "Close error",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        LazyColumn {
            // Add images button
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    onClick = { imagePickerLauncher.launch("image/*") },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add images",
                                modifier = Modifier.size(32.dp)
                            )
                            
                            Text(
                                text = "Add Images",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Text(
                                text = "Tap to select multiple images",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Selected images
            if (uiState.selectedImages.isNotEmpty()) {
                item {
                    Text(
                        text = "Selected Images (${uiState.selectedImages.size}/10)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                itemsIndexed(uiState.selectedImages) { index, uri ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Selected image ${index + 1}",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            
                            // Remove button
                            IconButton(
                                onClick = { viewModel.removeImage(index) },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remove image",
                                        tint = MaterialTheme.colorScheme.onError,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            // Uploaded images
            if (uiState.uploadedImages.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Uploaded Images",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                items(uiState.uploadedImages) { uploadedImage ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "✓ ${uploadedImage.fileName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            Text(
                                text = uploadedImage.imageUrl,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            // Loading indicator
            if (uiState.isUploading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Text(
                                text = "Uploading images...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}