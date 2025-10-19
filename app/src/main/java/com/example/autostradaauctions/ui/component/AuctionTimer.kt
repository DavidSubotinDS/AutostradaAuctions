package com.example.autostradaauctions.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.time.LocalDateTime
import kotlin.math.min

@Composable
fun AuctionTimer(
    endTime: String,
    modifier: Modifier = Modifier,
    onAuctionExpired: () -> Unit = {}
) {
    var timeRemaining by remember { mutableStateOf(Duration.ZERO) }
    var isExpired by remember { mutableStateOf(false) }
    
    LaunchedEffect(endTime) {
        while (!isExpired) {
            try {
                // Parse the end time - you might need to adjust the format
                val endDateTime = LocalDateTime.parse(endTime.substringBefore('.'), 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                val endInstant = endDateTime.atZone(ZoneId.systemDefault()).toInstant()
                val now = Instant.now()
                
                val remaining = Duration.between(now, endInstant)
                
                if (remaining.isNegative || remaining.isZero) {
                    isExpired = true
                    timeRemaining = Duration.ZERO
                    onAuctionExpired()
                } else {
                    timeRemaining = remaining
                }
            } catch (e: Exception) {
                // If parsing fails, show expired
                println("DEBUG: AuctionTimer error parsing endTime '$endTime': ${e.message}")
                isExpired = true
                timeRemaining = Duration.ZERO
                onAuctionExpired()
            }
            
            delay(1000) // Update every second
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpired) {
                MaterialTheme.colorScheme.errorContainer
            } else if (timeRemaining.toHours() < 1) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timer icon and label
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Time remaining",
                        tint = if (isExpired) {
                            MaterialTheme.colorScheme.error
                        } else if (timeRemaining.toHours() < 1) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isExpired) "Auction Ended" else "Time Remaining",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Countdown display
                if (!isExpired) {
                    TimeDisplay(timeRemaining)
                } else {
                    Text(
                        text = "CLOSED",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Progress bar for urgency
            if (!isExpired && timeRemaining.toHours() < 24) {
                val totalMinutes = 24 * 60 // 24 hours in minutes
                val remainingMinutes = timeRemaining.toMinutes()
                val progress = (remainingMinutes.toFloat() / totalMinutes).coerceIn(0f, 1f)
                
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(2.dp)),
                    color = when {
                        timeRemaining.toHours() < 1 -> MaterialTheme.colorScheme.error
                        timeRemaining.toHours() < 6 -> Color(0xFFFF9800) // Orange
                        else -> MaterialTheme.colorScheme.primary
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TimeDisplay(duration: Duration) {
    val days = duration.toDays()
    val hours = duration.toHours() % 24
    val minutes = duration.toMinutes() % 60
    val seconds = duration.seconds % 60
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Always show all time units as requested by user
        TimeUnit(days.toString(), "d")
        TimeUnit(hours.toString().padStart(2, '0'), "h")
        TimeUnit(minutes.toString().padStart(2, '0'), "m")
        TimeUnit(seconds.toString().padStart(2, '0'), "s")
    }
}

@Composable
private fun TimeUnit(value: String, unit: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CircularAuctionTimer(
    endTime: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 120.dp
) {
    var timeRemaining by remember { mutableStateOf(Duration.ZERO) }
    var isExpired by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (isExpired) 0f else min(timeRemaining.toMinutes().toFloat() / (24 * 60), 1f),
        animationSpec = tween(1000),
        label = "timer_progress"
    )
    
    LaunchedEffect(endTime) {
        while (!isExpired) {
            try {
                val endDateTime = LocalDateTime.parse(endTime.substringBefore('.'), 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                val endInstant = endDateTime.atZone(ZoneId.systemDefault()).toInstant()
                val now = Instant.now()
                
                val remaining = Duration.between(now, endInstant)
                
                if (remaining.isNegative || remaining.isZero) {
                    isExpired = true
                    timeRemaining = Duration.ZERO
                } else {
                    timeRemaining = remaining
                }
            } catch (e: Exception) {
                println("DEBUG: CircularAuctionTimer error parsing endTime '$endTime': ${e.message}")
                isExpired = true
                timeRemaining = Duration.ZERO
            }
            
            delay(1000)
        }
    }
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircularProgress(
                progress = animatedProgress,
                isExpired = isExpired,
                timeRemaining = timeRemaining
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isExpired) {
                val days = timeRemaining.toDays()
                val hours = timeRemaining.toHours() % 24
                val minutes = timeRemaining.toMinutes() % 60
                val seconds = timeRemaining.seconds % 60
                
                Text(
                    text = "${days}d ${hours}h ${minutes}m ${seconds}s",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "ENDED",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun DrawScope.drawCircularProgress(
    progress: Float,
    isExpired: Boolean,
    timeRemaining: Duration
) {
    val strokeWidth = 8.dp.toPx()
    val diameter = size.minDimension
    val radius = (diameter - strokeWidth) / 2
    val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
    
    // Background circle
    drawArc(
        color = Color.Gray.copy(alpha = 0.3f),
        startAngle = -90f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = topLeft,
        size = Size(diameter, diameter),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
    
    if (!isExpired) {
        // Progress arc
        val color = when {
            timeRemaining.toHours() < 1 -> Color.Red
            timeRemaining.toHours() < 6 -> Color(0xFFFF9800) // Orange
            else -> Color.Green
        }
        
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}