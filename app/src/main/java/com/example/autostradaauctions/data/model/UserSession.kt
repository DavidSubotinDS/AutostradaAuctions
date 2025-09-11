package com.example.autostradaauctions.data.model

data class UserSession(
    val id: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val isActive: Boolean
)

data class LoginResponse(
    val id: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val isActive: Boolean
)
