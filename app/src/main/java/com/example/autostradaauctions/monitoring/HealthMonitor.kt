package com.example.autostradaauctions.monitoring

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import com.example.autostradaauctions.config.AppConfig
import kotlin.math.max
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Application health monitoring and analytics
 */
object HealthMonitor {
    
    private const val TAG = "HealthMonitor"
    private val sessionStartTime = System.currentTimeMillis()
    private val crashCount = AtomicInteger(0)
    private val errorCount = AtomicInteger(0)
    private val apiCallCount = AtomicInteger(0)
    private val apiErrorCount = AtomicInteger(0)
    private val memoryPeakUsage = AtomicLong(0L)
    
    // Performance metrics
    private val screenLoadTimes = mutableMapOf<String, MutableList<Long>>()
    private val apiResponseTimes = mutableMapOf<String, MutableList<Long>>()
    
    fun initialize(application: Application) {
        if (AppConfig.Features.ENABLE_PERFORMANCE_MONITORING) {
            setupGlobalExceptionHandler()
            startPeriodicHealthCheck(application)
            logAppStart()
        }
    }
    
    /**
     * Set up global exception handler for crash reporting
     */
    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                logCrash(throwable)
                crashCount.incrementAndGet()
            } catch (e: Exception) {
                Log.e(TAG, "Error logging crash", e)
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
    
    /**
     * Log application startup
     */
    private fun logAppStart() {
        val deviceInfo = DeviceInfo(
            model = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            osVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            appVersion = "1.0.0", // Hardcoded version for now
            buildType = if (AppConfig.isDebug) "debug" else "release"
        )
        
        logEvent("app_start", mapOf(
            "device_info" to deviceInfo,
            "session_id" to generateSessionId()
        ))
    }
    
    /**
     * Start periodic health monitoring
     */
    private fun startPeriodicHealthCheck(context: Context) {
        CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
            while (true) {
                try {
                    delay(60_000) // Every minute
                    performHealthCheck(context)
                } catch (e: Exception) {
                    Log.w(TAG, "Health check failed", e)
                }
            }
        }
    }
    
    /**
     * Perform comprehensive health check
     */
    private suspend fun performHealthCheck(context: Context) = withContext(Dispatchers.IO) {
        val currentMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        memoryPeakUsage.updateAndGet { max(it, currentMemory) }
        
        val healthData = HealthData(
            timestamp = System.currentTimeMillis(),
            sessionDuration = System.currentTimeMillis() - sessionStartTime,
            memoryUsage = currentMemory,
            memoryPeak = memoryPeakUsage.get(),
            crashCount = crashCount.get(),
            errorCount = errorCount.get(),
            apiCallCount = apiCallCount.get(),
            apiErrorCount = apiErrorCount.get(),
            storageUsage = getStorageUsage(context),
            networkStatus = getNetworkStatus(context)
        )
        
        logHealthData(healthData)
        
        // Check for critical issues
        if (healthData.memoryUsage > Runtime.getRuntime().maxMemory() * 0.9) {
            logEvent("memory_warning", mapOf("usage" to healthData.memoryUsage))
        }
        
        if (healthData.apiErrorCount > healthData.apiCallCount * 0.5) {
            logEvent("api_error_threshold", mapOf(
                "error_rate" to (healthData.apiErrorCount.toFloat() / healthData.apiCallCount)
            ))
        }
    }
    
    /**
     * Log performance metrics for screen loads
     */
    fun logScreenLoad(screenName: String, loadTimeMs: Long) {
        screenLoadTimes.getOrPut(screenName) { mutableListOf() }.add(loadTimeMs)
        
        logEvent("screen_load", mapOf(
            "screen" to screenName,
            "load_time" to loadTimeMs
        ))
        
        if (loadTimeMs > 3000) { // Slow load warning
            logEvent("slow_screen_load", mapOf(
                "screen" to screenName,
                "load_time" to loadTimeMs
            ))
        }
    }
    
    /**
     * Log API call performance
     */
    fun logApiCall(endpoint: String, responseTimeMs: Long, success: Boolean) {
        apiCallCount.incrementAndGet()
        if (!success) {
            apiErrorCount.incrementAndGet()
        }
        
        apiResponseTimes.getOrPut(endpoint) { mutableListOf() }.add(responseTimeMs)
        
        logEvent("api_call", mapOf(
            "endpoint" to endpoint,
            "response_time" to responseTimeMs,
            "success" to success
        ))
    }
    
    /**
     * Log user interaction events
     */
    fun logUserEvent(eventName: String, properties: Map<String, Any> = emptyMap()) {
        if (AppConfig.Features.ENABLE_ANALYTICS) {
            logEvent("user_$eventName", properties + mapOf(
                "timestamp" to System.currentTimeMillis(),
                "session_duration" to (System.currentTimeMillis() - sessionStartTime)
            ))
        }
    }
    
    /**
     * Log application errors (non-fatal)
     */
    fun logError(error: Throwable, context: Map<String, Any> = emptyMap()) {
        errorCount.incrementAndGet()
        
        logEvent("error", mapOf(
            "message" to (error.message ?: "Unknown error"),
            "stack_trace" to error.stackTraceToString(),
            "error_type" to (error::class.simpleName ?: "Unknown"),
            "context" to context
        ))
    }
    
    /**
     * Log critical crashes
     */
    private fun logCrash(throwable: Throwable) {
        logEvent("crash", mapOf(
            "message" to (throwable.message ?: "Unknown crash"),
            "stack_trace" to throwable.stackTraceToString(),
            "crash_type" to (throwable::class.simpleName ?: "Unknown"),
            "session_duration" to (System.currentTimeMillis() - sessionStartTime)
        ))
    }
    
    /**
     * Get current storage usage
     */
    private fun getStorageUsage(context: Context): Long {
        return try {
            val cacheDir = context.cacheDir
            cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Get network status
     */
    private fun getNetworkStatus(context: Context): String {
        // Simplified network status check
        return "connected" // In real implementation, check ConnectivityManager
    }
    
    /**
     * Generate unique session ID
     */
    private fun generateSessionId(): String {
        return "${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    /**
     * Central event logging
     */
    private fun logEvent(eventName: String, properties: Map<String, Any>) {
        if (AppConfig.isDebug) {
            Log.d(TAG, "Event: $eventName, Properties: $properties")
        }
        
        // In production, send to analytics service
        // Firebase Analytics, Mixpanel, etc.
    }
    
    /**
     * Log health data for monitoring
     */
    private fun logHealthData(healthData: HealthData) {
        if (AppConfig.isDebug) {
            Log.d(TAG, "Health: $healthData")
        }
        
        // In production, send to monitoring service
        // Datadog, New Relic, etc.
    }
    
    /**
     * Get performance summary
     */
    fun getPerformanceSummary(): PerformanceSummary {
        return PerformanceSummary(
            sessionDuration = System.currentTimeMillis() - sessionStartTime,
            totalApiCalls = apiCallCount.get(),
            totalApiErrors = apiErrorCount.get(),
            totalErrors = errorCount.get(),
            totalCrashes = crashCount.get(),
            peakMemoryUsage = memoryPeakUsage.get(),
            averageScreenLoadTimes = screenLoadTimes.mapValues { (_, times) ->
                if (times.isNotEmpty()) times.average() else 0.0
            },
            averageApiResponseTimes = apiResponseTimes.mapValues { (_, times) ->
                if (times.isNotEmpty()) times.average() else 0.0
            }
        )
    }
}

/**
 * Device information data class
 */
data class DeviceInfo(
    val model: String,
    val manufacturer: String,
    val osVersion: String,
    val apiLevel: Int,
    val appVersion: String,
    val buildType: String
)

/**
 * Health monitoring data
 */
data class HealthData(
    val timestamp: Long,
    val sessionDuration: Long,
    val memoryUsage: Long,
    val memoryPeak: Long,
    val crashCount: Int,
    val errorCount: Int,
    val apiCallCount: Int,
    val apiErrorCount: Int,
    val storageUsage: Long,
    val networkStatus: String
)

/**
 * Performance summary
 */
data class PerformanceSummary(
    val sessionDuration: Long,
    val totalApiCalls: Int,
    val totalApiErrors: Int,
    val totalErrors: Int,
    val totalCrashes: Int,
    val peakMemoryUsage: Long,
    val averageScreenLoadTimes: Map<String, Double>,
    val averageApiResponseTimes: Map<String, Double>
)
