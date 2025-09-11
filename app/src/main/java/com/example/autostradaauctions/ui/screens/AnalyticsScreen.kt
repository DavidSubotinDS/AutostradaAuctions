package com.example.autostradaauctions.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBackClick: () -> Unit
) {
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
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Analytics Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Revenue Analytics
            item {
                Text(
                    text = "Revenue Analytics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        AnalyticsCard(
                            title = "Total Revenue",
                            value = "$1,234,567",
                            subtitle = "+12.5% from last month",
                            icon = Icons.Default.AttachMoney,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    item {
                        AnalyticsCard(
                            title = "This Month",
                            value = "$145,230",
                            subtitle = "23 auctions completed",
                            icon = Icons.Default.TrendingUp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    item {
                        AnalyticsCard(
                            title = "Avg. Sale Price",
                            value = "$52,430",
                            subtitle = "+5.2% vs last month",
                            icon = Icons.Default.Analytics,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
            
            // User Analytics
            item {
                Text(
                    text = "User Analytics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnalyticsCard(
                        title = "Active Users",
                        value = "1,847",
                        subtitle = "156 new this month",
                        icon = Icons.Default.People,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    AnalyticsCard(
                        title = "Total Bids",
                        value = "12,450",
                        subtitle = "2,340 this month",
                        icon = Icons.Default.Gavel,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Popular Categories
            item {
                Text(
                    text = "Popular Categories",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                PopularCategoryCard(
                    category = "BMW",
                    auctions = 23,
                    revenue = 850000.0,
                    percentage = 35.2f
                )
            }
            
            item {
                PopularCategoryCard(
                    category = "Mercedes-Benz",
                    auctions = 18,
                    revenue = 720000.0,
                    percentage = 28.7f
                )
            }
            
            item {
                PopularCategoryCard(
                    category = "Audi",
                    auctions = 15,
                    revenue = 520000.0,
                    percentage = 22.1f
                )
            }
            
            item {
                PopularCategoryCard(
                    category = "Tesla",
                    auctions = 12,
                    revenue = 480000.0,
                    percentage = 14.0f
                )
            }
            
            // Recent Activity
            item {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(5) { index ->
                ActivityCard(
                    activity = when (index) {
                        0 -> "New auction created: 2023 Porsche 911 GT3"
                        1 -> "Auction ended: 2020 BMW M3 sold for $85,000"
                        2 -> "New user registered: john_doe_123"
                        3 -> "High bid placed: $72,000 on Mercedes C63 AMG"
                        else -> "System maintenance completed successfully"
                    },
                    timestamp = "${index + 1} hour${if (index == 0) "" else "s"} ago",
                    icon = when (index) {
                        0 -> Icons.Default.Add
                        1 -> Icons.Default.Gavel
                        2 -> Icons.Default.PersonAdd
                        3 -> Icons.Default.TrendingUp
                        else -> Icons.Default.Settings
                    }
                )
            }
        }
    }
}

@Composable
fun AnalyticsCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun PopularCategoryCard(
    category: String,
    auctions: Int,
    revenue: Double,
    percentage: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Column {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$auctions auctions • ${NumberFormat.getCurrencyInstance(Locale.US).format(revenue)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = "${percentage}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = percentage / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun ActivityCard(
    activity: String,
    timestamp: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
