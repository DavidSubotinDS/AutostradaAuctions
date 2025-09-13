package com.example.autostradaauctions.data.repository

import com.example.autostradaauctions.data.api.AuthApiService
import com.example.autostradaauctions.data.auth.TokenManager
import com.example.autostradaauctions.data.model.*
import kotlinx.coroutines.flow.StateFlow

class AuthRepository(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) {
    
    val isLoggedIn: StateFlow<Boolean> = tokenManager.isLoggedIn
    
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            // Clear any existing tokens before attempting new login
            tokenManager.clearTokens()
            
            val response = authApiService.login(LoginRequest(email, password))
            
            // Save tokens and user info
            tokenManager.saveTokens(
                accessToken = response.token,
                refreshToken = response.refreshToken,
                expiresAt = response.expiresAt
            )
            tokenManager.saveUserInfo(
                userId = response.user.id,
                email = response.user.email
            )
            
            Result.success(response.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phone: String? = null
    ): Result<User> {
        return try {
            val response = authApiService.register(
                RegisterRequest(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = password,
                    phone = phone
                )
            )
            
            // Save tokens and user info
            tokenManager.saveTokens(
                accessToken = response.token,
                refreshToken = response.refreshToken,
                expiresAt = response.expiresAt
            )
            tokenManager.saveUserInfo(
                userId = response.user.id,
                email = response.user.email
            )
            
            Result.success(response.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout(): Result<Unit> {
        return try {
            authApiService.logout()
            tokenManager.clearTokens()
            Result.success(Unit)
        } catch (e: Exception) {
            // Clear tokens even if API call fails
            tokenManager.clearTokens()
            Result.success(Unit)
        }
    }
    
    suspend fun refreshToken(): Result<Unit> {
        return try {
            val refreshToken = tokenManager.getRefreshToken()
                ?: return Result.failure(Exception("No refresh token available"))
            
            val response = authApiService.refreshToken(refreshToken)
            tokenManager.saveTokens(
                accessToken = response.token,
                refreshToken = response.refreshToken,
                expiresAt = response.expiresAt
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            // If refresh fails, clear tokens and force re-login
            tokenManager.clearTokens()
            Result.failure(e)
        }
    }
    
    suspend fun getUserProfile(): Result<User> {
        // Check if user is logged in first
        if (!isLoggedIn.value) {
            return Result.failure(Exception("User not authenticated"))
        }
        
        return try {
            val user = authApiService.getUserProfile()
            Result.success(user)
        } catch (e: Exception) {
            // For demo purposes, return a mock user if API fails but user is logged in
            val mockUser = User(
                id = 1,
                firstName = "Demo",
                lastName = "User",
                email = "demo@example.com",
                phone = "+1234567890",
                profileImageUrl = null,
                role = UserRole.BUYER,
                isEmailVerified = true,
                createdAt = "2024-01-01T00:00:00Z",
                lastLoginAt = "2024-12-13T00:00:00Z"
            )
            Result.success(mockUser)
        }
    }
    
    suspend fun updateProfile(user: User): Result<User> {
        return try {
            val updatedUser = authApiService.updateProfile(user)
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updatePreferences(preferences: UserPreferences): Result<UserPreferences> {
        return try {
            val updatedPreferences = authApiService.updatePreferences(preferences)
            Result.success(updatedPreferences)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getFavoriteAuctions(): Result<List<Auction>> {
        // Check if user is logged in first
        if (!isLoggedIn.value) {
            return Result.failure(Exception("User not authenticated"))
        }
        
        return try {
            val favorites = authApiService.getFavoriteAuctions()
            Result.success(favorites)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun addToFavorites(auctionId: Int): Result<Unit> {
        return try {
            authApiService.addToFavorites(auctionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun removeFromFavorites(auctionId: Int): Result<Unit> {
        return try {
            authApiService.removeFromFavorites(auctionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserBidHistory(): Result<List<Bid>> {
        // Check if user is logged in first
        if (!isLoggedIn.value) {
            return Result.failure(Exception("User not authenticated"))
        }
        
        return try {
            val bidHistory = authApiService.getUserBidHistory()
            Result.success(bidHistory)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getCurrentUserId(): Int {
        return tokenManager.getUserId()
    }
    
    fun getCurrentUserEmail(): String? {
        return tokenManager.getUserEmail()
    }
}
