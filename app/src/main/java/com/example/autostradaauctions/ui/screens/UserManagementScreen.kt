package com.example.autostradaauctions.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.autostradaauctions.data.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    onBackClick: () -> Unit,
    onCreateUser: () -> Unit = {},
    onEditUser: (String) -> Unit = {},
    onToggleUserStatus: (String) -> Unit = {}
) {
    // TODO: Replace with real user data from UserRepository/ViewModel
    val mockUsers = remember {
        emptyList<MockUser>()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Manage Users",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            FloatingActionButton(
                onClick = onCreateUser,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add User")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UserStatCard(
                title = "Total",
                value = if (mockUsers.isEmpty()) "N/A" else mockUsers.size.toString(),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            UserStatCard(
                title = "Active", 
                value = if (mockUsers.isEmpty()) "N/A" else mockUsers.count { it.isActive }.toString(),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            UserStatCard(
                title = "Admins",
                value = if (mockUsers.isEmpty()) "N/A" else mockUsers.count { it.role == UserRole.ADMIN }.toString(),
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Role Filter Tabs
        var selectedRole by remember { mutableStateOf<UserRole?>(null) }
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    onClick = { selectedRole = null },
                    label = { Text("All") },
                    selected = selectedRole == null
                )
            }
            item {
                FilterChip(
                    onClick = { selectedRole = UserRole.ADMIN },
                    label = { Text("Admins") },
                    selected = selectedRole == UserRole.ADMIN
                )
            }
            item {
                FilterChip(
                    onClick = { selectedRole = UserRole.BUYER },
                    label = { Text("Buyers") },
                    selected = selectedRole == UserRole.BUYER
                )
            }
            item {
                FilterChip(
                    onClick = { selectedRole = UserRole.SELLER },
                    label = { Text("Sellers") },
                    selected = selectedRole == UserRole.SELLER
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // User List
        val filteredUsers = if (selectedRole != null) {
            mockUsers.filter { it.role == selectedRole }
        } else {
            mockUsers
        }
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredUsers) { user ->
                AdminUserCard(
                    user = user,
                    onEdit = { onEditUser(user.id) },
                    onToggleStatus = { onToggleUserStatus(user.id) }
                )
            }
        }
    }
}

@Composable
fun UserStatCard(
    title: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AdminUserCard(
    user: MockUser,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar
            Card(
                modifier = Modifier.size(48.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (user.role) {
                        UserRole.ADMIN -> MaterialTheme.colorScheme.primary
                        UserRole.SELLER -> MaterialTheme.colorScheme.secondary
                        UserRole.BUYER -> MaterialTheme.colorScheme.tertiary
                    }
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (user.role) {
                            UserRole.ADMIN -> Icons.Default.Shield
                            UserRole.SELLER -> Icons.Default.Store
                            UserRole.BUYER -> Icons.Default.Person
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // User Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${user.firstName} ${user.lastName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Role Badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (user.role) {
                            UserRole.ADMIN -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            UserRole.SELLER -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                            UserRole.BUYER -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                        }
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = user.role.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (user.role) {
                            UserRole.ADMIN -> MaterialTheme.colorScheme.primary
                            UserRole.SELLER -> MaterialTheme.colorScheme.secondary
                            UserRole.BUYER -> MaterialTheme.colorScheme.tertiary
                        }
                    )
                }
            }
            
            // Status & Actions
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Status Toggle
                Switch(
                    checked = user.isActive,
                    onCheckedChange = { onToggleStatus() }
                )
                
                // Edit Button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit User",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// Mock data class - in real app this would come from your actual User model
data class MockUser(
    val id: String,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val isActive: Boolean
)
