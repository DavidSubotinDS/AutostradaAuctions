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
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.Instant
import java.time.Duration
import java.time.format.DateTimeFormatter

@Composable
fun CountdownTimer(
    endTime: String,
    modifier: Modifier = Modifier
) {
    var timeRemaining by remember { mutableStateOf("") }
    var isExpired by remember { mutableStateOf(false) }
    
    LaunchedEffect(endTime) {
        while (!isExpired) {
            try {
                // Handle both formats: "2025-10-29T11:36:32.5329009" and "2025-10-29T11:36:32"
                val cleanEndTime = endTime.substringBefore('.')
                val endDateTime = LocalDateTime.parse(
                    cleanEndTime, 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                )
                val endInstant = endDateTime.atZone(ZoneId.systemDefault()).toInstant()
                val now = Instant.now()
                
                val remaining = Duration.between(now, endInstant)
                
                if (remaining.isNegative || remaining.isZero) {
                    timeRemaining = "Auction Ended"
                    isExpired = true
                } else {
                    val days = remaining.toDays()
                    val hours = remaining.toHours() % 24
                    val minutes = remaining.toMinutes() % 60
                    val seconds = remaining.seconds % 60
                    
                    // Always show days, hours, minutes, seconds format as requested
                    timeRemaining = "${days}d ${hours}h ${minutes}m ${seconds}s"
                }
                
                delay(1000) // Update every second
            } catch (e: Exception) {
                println("DEBUG: Error parsing endTime '$endTime': ${e.message}")
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
