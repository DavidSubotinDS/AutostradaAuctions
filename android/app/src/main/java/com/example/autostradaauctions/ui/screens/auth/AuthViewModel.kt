package com.example.autostradaauctions.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autostradaauctions.data.model.User
import com.example.autostradaauctions.data.model.UserRole
import com.example.autostradaauctions.data.repository.AuctionRepository
import com.example.autostradaauctions.data.repository.MockAuctionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val mockRepository: MockAuctionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = mockRepository.loginUser(email, password)
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        currentUser = user
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Login failed"
                    )
                }
            )
        }
    }

    fun register(name: String, email: String, password: String, phoneNumber: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val user = User(
                id = UUID.randomUUID().toString(),
                email = email,
                name = name,
                phoneNumber = phoneNumber,
                createdAt = System.currentTimeMillis(),
                role = UserRole.USER
            )

            val result = mockRepository.registerUser(user)
            result.fold(
                onSuccess = { newUser ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        currentUser = newUser
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Registration failed"
                    )
                }
            )
        }
    }

    fun logout() {
        _uiState.value = AuthUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
