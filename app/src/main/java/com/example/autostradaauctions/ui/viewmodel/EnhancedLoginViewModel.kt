package com.example.autostradaauctions.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.autostradaauctions.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EnhancedLoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    fun updateEmail(email: String) {
        _uiState.update { 
            it.copy(
                email = email,
                emailError = null
            ) 
        }
    }
    
    fun updatePassword(password: String) {
        _uiState.update { 
            it.copy(
                password = password,
                passwordError = null
            ) 
        }
    }
    
    fun login() {
        val currentState = _uiState.value
        
        // Validate inputs
        val emailError = validateEmail(currentState.email)
        val passwordError = validatePassword(currentState.password)
        
        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError
                )
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            authRepository.login(
                email = currentState.email,
                password = currentState.password
            ).fold(
                onSuccess = { user ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            error = null
                        ) 
                    }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = getErrorMessage(exception)
                        ) 
                    }
                }
            )
        }
    }
    
    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            else -> null
        }
    }
    
    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }
    
    private fun getErrorMessage(exception: Throwable): String {
        return when {
            exception.message?.contains("401") == true -> "Invalid email or password"
            exception.message?.contains("network") == true -> "Network error. Please check your connection"
            exception.message?.contains("timeout") == true -> "Request timeout. Please try again"
            else -> exception.message ?: "Login failed. Please try again"
        }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null
)

class EnhancedLoginViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EnhancedLoginViewModel::class.java)) {
            return EnhancedLoginViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
