package com.example.autostradaauctions.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun VehicleImageGallery(
    imageUrls: List<String>,
    vehicleTitle: String,
    modifier: Modifier = Modifier
) {
    val displayUrls = if (imageUrls.isEmpty()) {
        // Default placeholder images based on vehicle type
        listOf(
            "https://images.unsplash.com/photo-1494976688153-018c804d2886?w=800&h=600&fit=crop",
            "https://images.unsplash.com/photo-1560958089-b8a1929cea89?w=800&h=600&fit=crop",
            "https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop"
        )
    } else {
        imageUrls
    }
    
    var selectedImageIndex by remember { mutableStateOf(0) }
    
    Column(modifier = modifier) {
        // Main image display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(displayUrls.getOrNull(selectedImageIndex) ?: displayUrls[0])
                    .crossfade(true)
                    .build(),
                contentDescription = vehicleTitle,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = painterResource(id = android.R.drawable.ic_menu_gallery),
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Thumbnail row
        if (displayUrls.size > 1) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(displayUrls.size) { index ->
                    Card(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        onClick = { selectedImageIndex = index },
                        colors = CardDefaults.cardColors(
                            containerColor = if (index == selectedImageIndex) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (index == selectedImageIndex) 
                            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
                        else null
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(displayUrls[index])
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = android.R.drawable.ic_menu_gallery)
                        )
                    }
                }
            }
        }
    }
}
