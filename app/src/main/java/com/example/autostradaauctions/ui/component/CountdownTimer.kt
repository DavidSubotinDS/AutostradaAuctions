package com.example.autostradaauctions.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun CountdownTimer(
    endTime: String,
    modifier: Modifier = Modifier
) {
    var timeRemaining by remember { mutableStateOf("") }
    var isExpired by remember { mutableStateOf(false) }
    
    LaunchedEffect(endTime) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        
        while (!isExpired) {
            try {
                val endDate = dateFormat.parse(endTime)
                val currentTime = System.currentTimeMillis()
                val endTimeMillis = endDate?.time ?: 0L
                
                val remainingMillis = endTimeMillis - currentTime
                
                if (remainingMillis <= 0) {
                    timeRemaining = "Auction Ended"
                    isExpired = true
                } else {
                    val days = TimeUnit.MILLISECONDS.toDays(remainingMillis)
                    val hours = TimeUnit.MILLISECONDS.toHours(remainingMillis) % 24
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis) % 60
                    
                    timeRemaining = when {
                        days > 0 -> "${days}d ${hours}h ${minutes}m"
                        hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
                        else -> "${minutes}m ${seconds}s"
                    }
                }
                
                delay(1000) // Update every second
            } catch (e: Exception) {
                timeRemaining = "Invalid date"
                isExpired = true
            }
        }
    }
    
    Surface(
        modifier = modifier,
        color = if (isExpired) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Time Remaining",
                style = MaterialTheme.typography.labelMedium,
                color = if (isExpired) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
            )
            
            Text(
                text = timeRemaining,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isExpired) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
            )
        }
    }
}
