package com.example.autostradaauctions.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TokenManager(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val USER_ID_KEY = "user_id"
        private const val USER_EMAIL_KEY = "user_email"
        private const val TOKEN_EXPIRES_AT_KEY = "token_expires_at"
    }
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    init {
        // Check if user is already logged in
        _isLoggedIn.value = getAccessToken() != null && !isTokenExpired()
    }
    
    fun saveTokens(accessToken: String, refreshToken: String, expiresAt: String) {
        encryptedPrefs.edit().apply {
            putString(ACCESS_TOKEN_KEY, accessToken)
            putString(REFRESH_TOKEN_KEY, refreshToken)
            putString(TOKEN_EXPIRES_AT_KEY, expiresAt)
            apply()
        }
        _isLoggedIn.value = true
    }
    
    fun saveUserInfo(userId: Int, email: String) {
        encryptedPrefs.edit().apply {
            putInt(USER_ID_KEY, userId)
            putString(USER_EMAIL_KEY, email)
            apply()
        }
    }
    
    fun getAccessToken(): String? {
        return encryptedPrefs.getString(ACCESS_TOKEN_KEY, null)
    }
    
    fun getRefreshToken(): String? {
        return encryptedPrefs.getString(REFRESH_TOKEN_KEY, null)
    }
    
    fun getUserId(): Int {
        return encryptedPrefs.getInt(USER_ID_KEY, -1)
    }
    
    fun getUserEmail(): String? {
        return encryptedPrefs.getString(USER_EMAIL_KEY, null)
    }
    
    fun isTokenExpired(): Boolean {
        val expiresAt = encryptedPrefs.getString(TOKEN_EXPIRES_AT_KEY, null) ?: return true
        // Simple check - you can implement proper date parsing
        return System.currentTimeMillis() > expiresAt.toLongOrNull() ?: 0
    }
    
    fun clearTokens() {
        encryptedPrefs.edit().clear().apply()
        _isLoggedIn.value = false
    }
    
    fun getAuthHeader(): String? {
        val token = getAccessToken()
        return if (token != null) "Bearer $token" else null
    }

    // Add missing getToken() method for compatibility
    fun getToken(): String? {
        return getAccessToken()
    }
}
