package com.example.autostradaauctions.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.autostradaauctions.data.repository.AdminRepository
import com.example.autostradaauctions.data.repository.AdminUser
import com.example.autostradaauctions.data.repository.UpdateUserRequest
import com.example.autostradaauctions.data.repository.CreateUserRequest
import com.example.autostradaauctions.data.auth.TokenManager
import com.example.autostradaauctions.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminUsersUiState(
    val isLoading: Boolean = false,
    val users: List<AdminUser> = emptyList(),
    val error: String? = null,
    val deleteConfirmationUser: AdminUser? = null,
    val editingUser: AdminUser? = null,
    val showUserForm: Boolean = false,
    val formMode: UserFormMode = UserFormMode.CREATE
)

enum class UserFormMode {
    CREATE, EDIT
}

class AdminUsersViewModel(
    private val adminRepository: AdminRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AdminUsersUiState())
    val uiState: StateFlow<AdminUsersUiState> = _uiState.asStateFlow()
    
    init {
        loadUsers()
    }
    
    fun loadUsers() {
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
            
            adminRepository.getAllUsers(token).fold(
                onSuccess = { users ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        users = users,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load users"
                    )
                }
            )
        }
    }
    
    fun showDeleteConfirmation(user: AdminUser) {
        _uiState.value = _uiState.value.copy(deleteConfirmationUser = user)
    }
    
    fun hideDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(deleteConfirmationUser = null)
    }
    
    fun deleteUser(user: AdminUser) {
        viewModelScope.launch {
            val token = tokenManager.getToken()
            if (token == null) {
                _uiState.value = _uiState.value.copy(error = "Authentication required")
                return@launch
            }
            
            adminRepository.deleteUser(user.id, token).fold(
                onSuccess = {
                    hideDeleteConfirmation()
                    loadUsers() // Refresh the list
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to delete user"
                    )
                    hideDeleteConfirmation()
                }
            )
        }
    }
    
    fun showEditUser(user: AdminUser) {
        _uiState.value = _uiState.value.copy(
            editingUser = user,
            showUserForm = true,
            formMode = UserFormMode.EDIT
        )
    }
    
    fun showCreateUser() {
        _uiState.value = _uiState.value.copy(
            editingUser = null,
            showUserForm = true,
            formMode = UserFormMode.CREATE
        )
    }
    
    fun hideUserForm() {
        _uiState.value = _uiState.value.copy(
            showUserForm = false,
            editingUser = null
        )
    }
    
    fun updateUser(userId: Int, updateRequest: UpdateUserRequest) {
        viewModelScope.launch {
            val token = tokenManager.getToken()
            if (token == null) {
                _uiState.value = _uiState.value.copy(error = "Authentication required")
                return@launch
            }
            
            adminRepository.updateUser(userId, updateRequest, token).fold(
                onSuccess = {
                    hideUserForm()
                    loadUsers() // Refresh the list
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to update user"
                    )
                }
            )
        }
    }
    
    fun createUser(createRequest: CreateUserRequest) {
        viewModelScope.launch {
            val token = tokenManager.getToken()
            if (token == null) {
                _uiState.value = _uiState.value.copy(error = "Authentication required")
                return@launch
            }
            
            adminRepository.createUser(createRequest, token).fold(
                onSuccess = {
                    hideUserForm()
                    loadUsers() // Refresh the list
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to create user"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

@Composable
fun AdminUsersScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminUsersViewModel = viewModel {
        AdminUsersViewModel(
            AppContainer.adminRepository,
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "User Management",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            FloatingActionButton(
                onClick = { viewModel.showCreateUser() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add User")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Users List
        if (uiState.users.isNotEmpty()) {
            Text(
                text = "Total Users: ${uiState.users.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.users) { user ->
                    UserCard(
                        user = user,
                        onEdit = { viewModel.showEditUser(user) },
                        onDelete = { viewModel.showDeleteConfirmation(user) }
                    )
                }
            }
        } else if (!uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No users found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadUsers() }) {
                        Text("Refresh")
                    }
                }
            }
        }
    }
    
    // User Form Dialog
    if (uiState.showUserForm) {
        UserFormDialog(
            user = uiState.editingUser,
            formMode = uiState.formMode,
            onDismiss = { viewModel.hideUserForm() },
            onSave = { updateRequest, createRequest ->
                when (uiState.formMode) {
                    UserFormMode.EDIT -> {
                        updateRequest?.let { request ->
                            uiState.editingUser?.let { user ->
                                viewModel.updateUser(user.id, request)
                            }
                        }
                    }
                    UserFormMode.CREATE -> {
                        createRequest?.let { request ->
                            viewModel.createUser(request)
                        }
                    }
                }
            }
        )
    }
    
    // Delete Confirmation Dialog
    uiState.deleteConfirmationUser?.let { user ->
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirmation() },
            title = { Text("Delete User") },
            text = {
                Text("Are you sure you want to delete ${user.firstName} ${user.lastName}? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteUser(user) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteConfirmation() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun UserCard(
    user: AdminUser,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
                verticalAlignment = Alignment.Top
            ) {
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit User",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete User",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Role Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (user.role.lowercase()) {
                        "admin" -> MaterialTheme.colorScheme.errorContainer
                        "seller" -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    }
                ) {
                    Text(
                        text = user.role,
                        style = MaterialTheme.typography.labelSmall,
                        color = when (user.role.lowercase()) {
                            "admin" -> MaterialTheme.colorScheme.onErrorContainer
                            "seller" -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                // Verification Badge
                if (user.isEmailVerified) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Verified",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "Unverified",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Joined: ${user.createdAt}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}