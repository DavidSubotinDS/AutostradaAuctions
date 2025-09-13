package com.example.autostradaauctions.config

import com.example.autostradaauctions.BuildConfig

/**
 * Application configuration and constants
 */
object AppConfig {
    
    // API Configuration
    const val BASE_URL = "http://10.0.2.2:5000/api/"  // Android emulator localhost
    const val SIGNALR_HUB_URL = "http://10.0.2.2:5001/biddingHub"
    
    // Network Configuration
    const val NETWORK_TIMEOUT_SECONDS = 30L
    const val RETRY_DELAY_SECONDS = 2L
    const val MAX_RETRY_ATTEMPTS = 3
    
    // Authentication
    const val TOKEN_REFRESH_THRESHOLD_MINUTES = 5L
    const val SESSION_TIMEOUT_MINUTES = 30L
    
    // UI Configuration
    const val ANIMATION_DURATION_MS = 300
    const val DEBOUNCE_DELAY_MS = 500L
    const val IMAGE_PLACEHOLDER_SIZE_DP = 200
    
    // Cache Configuration
    const val CACHE_MAX_SIZE_MB = 50L
    const val CACHE_RETENTION_DAYS = 7L
    
    // Real-time Updates
    const val BID_UPDATE_INTERVAL_MS = 1000L
    const val CONNECTION_RETRY_INTERVAL_MS = 5000L
    const val MAX_CONNECTION_RETRIES = 5
    
    // Production Configuration
    val isDebug: Boolean get() = BuildConfig.DEBUG
    val versionName: String get() = BuildConfig.VERSION_NAME
    val versionCode: Int get() = BuildConfig.VERSION_CODE
    
    // Feature Flags
    object Features {
        const val ENABLE_ANALYTICS = true
        const val ENABLE_CRASH_REPORTING = true
        const val ENABLE_PERFORMANCE_MONITORING = false
        const val ENABLE_BETA_FEATURES = false
    }
    
    // Security Configuration
    object Security {
        const val ENCRYPTION_KEY_ALIAS = "AutostradaAuctions_KeyStore"
        const val SHARED_PREFS_NAME = "secure_prefs"
        const val PIN_CODE_LENGTH = 4
        const val MAX_LOGIN_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MINUTES = 15L
    }
    
    // Error Messages
    object ErrorMessages {
        const val NETWORK_ERROR = "Network connection failed. Please check your internet connection."
        const val SERVER_ERROR = "Server error occurred. Please try again later."
        const val AUTHENTICATION_ERROR = "Authentication failed. Please login again."
        const val VALIDATION_ERROR = "Please check your input and try again."
        const val UNKNOWN_ERROR = "An unexpected error occurred. Please try again."
    }
    
    // Success Messages
    object SuccessMessages {
        const val LOGIN_SUCCESS = "Login successful"
        const val LOGOUT_SUCCESS = "Logged out successfully"
        const val BID_PLACED = "Bid placed successfully"
        const val PROFILE_UPDATED = "Profile updated successfully"
        const val FAVORITE_ADDED = "Added to favorites"
        const val FAVORITE_REMOVED = "Removed from favorites"
    }
}
