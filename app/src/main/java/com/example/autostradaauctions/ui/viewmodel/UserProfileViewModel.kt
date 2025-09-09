package com.example.autostradaauctions.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.autostradaauctions.data.model.User
import com.example.autostradaauctions.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()
    
    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            authRepository.getUserProfile().fold(
                onSuccess = { user ->
                    _uiState.update { 
                        it.copy(
                            user = user,
                            isLoading = false,
                            error = null
                        ) 
                    }
                    
                    // Load additional user statistics
                    loadUserStatistics()
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load profile"
                        ) 
                    }
                }
            )
        }
    }
    
    private fun loadUserStatistics() {
        viewModelScope.launch {
            // Load bid history to calculate statistics
            authRepository.getUserBidHistory().fold(
                onSuccess = { bidHistory ->
                    val activeBids = bidHistory.count { /* check if bid is still active */ true }
                    _uiState.update { 
                        it.copy(activeBids = activeBids) 
                    }
                },
                onFailure = { /* Handle error silently for statistics */ }
            )
            
            // Load favorites count
            authRepository.getFavoriteAuctions().fold(
                onSuccess = { favorites ->
                    _uiState.update { 
                        it.copy(favoriteCount = favorites.size) 
                    }
                },
                onFailure = { /* Handle error silently for statistics */ }
            )
        }
    }
    
    fun toggleEditMode() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }
    
    fun updateProfile(user: User) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            authRepository.updateProfile(user).fold(
                onSuccess = { updatedUser ->
                    _uiState.update { 
                        it.copy(
                            user = updatedUser,
                            isLoading = false,
                            isEditMode = false,
                            error = null
                        ) 
                    }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to update profile"
                        ) 
                    }
                }
            )
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}

data class UserProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val error: String? = null,
    val activeBids: Int = 0,
    val wonAuctions: Int = 0,
    val favoriteCount: Int = 0
)

class UserProfileViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            return UserProfileViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
