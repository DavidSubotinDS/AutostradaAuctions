package com.example.autostradaauctions.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val email: String,
    val name: String,
    val phoneNumber: String? = null,
    val profileImageUrl: String? = null,
    val isEmailVerified: Boolean = false,
    val createdAt: Long,
    val role: UserRole = UserRole.USER
)

enum class UserRole {
    USER,
    ADMIN
}
