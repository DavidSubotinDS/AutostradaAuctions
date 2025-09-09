package com.example.autostradaauctions.utils

import android.content.Context
import android.util.Log
import com.example.autostradaauctions.config.AppConfig
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Centralized error handling utility for consistent error processing throughout the app
 */
object ErrorHandler {
    
    private const val TAG = "ErrorHandler"
    
    /**
     * Process exceptions and return user-friendly error messages
     */
    fun handleError(
        throwable: Throwable,
        context: Context? = null,
        customMessage: String? = null
    ): ErrorResult {
        logError(throwable)
        
        val errorMessage = customMessage ?: when (throwable) {
            is UnknownHostException, is IOException -> {
                AppConfig.ErrorMessages.NETWORK_ERROR
            }
            is SocketTimeoutException -> {
                "Request timeout. Please check your connection and try again."
            }
            is HttpException -> {
                handleHttpException(throwable)
            }
            is AuthenticationException -> {
                AppConfig.ErrorMessages.AUTHENTICATION_ERROR
            }
            is ValidationException -> {
                throwable.message ?: AppConfig.ErrorMessages.VALIDATION_ERROR
            }
            else -> {
                AppConfig.ErrorMessages.UNKNOWN_ERROR
            }
        }
        
        return ErrorResult(
            message = errorMessage,
            exception = throwable,
            isRetryable = isRetryable(throwable)
        )
    }
    
    /**
     * Handle HTTP exceptions and return appropriate messages
     */
    private fun handleHttpException(httpException: HttpException): String {
        return when (httpException.code()) {
            400 -> "Bad request. Please check your input."
            401 -> "Authentication required. Please login."
            403 -> "Access forbidden. You don't have permission for this action."
            404 -> "Requested resource not found."
            408 -> "Request timeout. Please try again."
            429 -> "Too many requests. Please wait a moment and try again."
            500 -> "Server error. Please try again later."
            502 -> "Bad gateway. Server is temporarily unavailable."
            503 -> "Service unavailable. Please try again later."
            else -> "HTTP error (${httpException.code()}). Please try again."
        }
    }
    
    /**
     * Determine if an error is retryable
     */
    private fun isRetryable(throwable: Throwable): Boolean {
        return when (throwable) {
            is UnknownHostException,
            is SocketTimeoutException,
            is IOException -> true
            is HttpException -> {
                throwable.code() in listOf(408, 429, 500, 502, 503)
            }
            else -> false
        }
    }
    
    /**
     * Log error with appropriate level
     */
    private fun logError(throwable: Throwable) {
        if (AppConfig.isDebug) {
            Log.e(TAG, "Error occurred: ${throwable.message}", throwable)
        }
        
        // In production, send to crash reporting service
        if (AppConfig.Features.ENABLE_CRASH_REPORTING) {
            // TODO: Integrate with Firebase Crashlytics or similar service
            // Crashlytics.recordException(throwable)
        }
    }
    
    /**
     * Validate network connectivity
     */
    fun isNetworkError(throwable: Throwable): Boolean {
        return throwable is UnknownHostException || 
               throwable is IOException ||
               throwable is SocketTimeoutException
    }
    
    /**
     * Check if error requires authentication
     */
    fun requiresAuthentication(throwable: Throwable): Boolean {
        return throwable is AuthenticationException ||
               (throwable is HttpException && throwable.code() == 401)
    }
}

/**
 * Data class representing processed error information
 */
data class ErrorResult(
    val message: String,
    val exception: Throwable,
    val isRetryable: Boolean = false
)

/**
 * Custom exceptions for better error handling
 */
class AuthenticationException(message: String) : Exception(message)
class ValidationException(message: String) : Exception(message)
class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Extension function for easier error handling in ViewModels
 */
fun <T> Result<T>.handleError(
    onSuccess: (T) -> Unit,
    onError: (ErrorResult) -> Unit
) {
    fold(
        onSuccess = onSuccess,
        onFailure = { throwable ->
            onError(ErrorHandler.handleError(throwable))
        }
    )
}

/**
 * Safe API call wrapper with error handling
 */
suspend fun <T> safeApiCall(
    apiCall: suspend () -> T
): Result<T> {
    return try {
        Result.success(apiCall())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
