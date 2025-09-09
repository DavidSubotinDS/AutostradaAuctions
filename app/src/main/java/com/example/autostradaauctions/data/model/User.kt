package com.example.autostradaauctions.data.model

data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String? = null,
    val profileImageUrl: String? = null,
    val role: UserRole,
    val isEmailVerified: Boolean = false,
    val createdAt: String,
    val lastLoginAt: String? = null,
    val preferences: UserPreferences = UserPreferences()
)

data class UserPreferences(
    val notificationsEnabled: Boolean = true,
    val emailNotifications: Boolean = true,
    val bidReminders: Boolean = true,
    val favoriteCategories: List<String> = emptyList(),
    val maxBidAmount: Double? = null,
    val autoIncrementAmount: Double = 100.0,
    val darkMode: Boolean = false
)

enum class UserRole {
    BUYER,
    SELLER,
    ADMIN
}

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val role: UserRole = UserRole.BUYER
)

data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val user: User,
    val expiresAt: String
)

data class FavoriteAuction(
    val id: Int,
    val userId: Int,
    val auctionId: Int,
    val createdAt: String
)
