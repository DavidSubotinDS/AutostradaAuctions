package com.example.autostradaauctions

import android.app.Application
import com.example.autostradaauctions.di.AppContainer
import com.example.autostradaauctions.monitoring.HealthMonitor
import com.example.autostradaauctions.config.AppConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AutostradaAuctionsApplication : Application() {
    
    // Application-wide coroutine scope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize core dependencies
        AppContainer.initialize(this)
        
        // Initialize monitoring and health systems
        initializeMonitoring()
        
        // Initialize security systems
        initializeSecurity()
        
        // Perform background setup tasks
        performBackgroundSetup()
    }
    
    /**
     * Initialize monitoring and analytics
     */
    private fun initializeMonitoring() {
        if (AppConfig.Features.ENABLE_PERFORMANCE_MONITORING) {
            HealthMonitor.initialize(this)
        }
        
        // Log application start
        HealthMonitor.logUserEvent("app_launch", mapOf(
            "version" to AppConfig.versionName,
            "build_type" to if (AppConfig.isDebug) "debug" else "release"
        ))
    }
    
    /**
     * Initialize security systems
     */
    private fun initializeSecurity() {
        // Initialize encrypted preferences
        try {
            AppContainer.instance.tokenManager.getToken()
        } catch (e: Exception) {
            // Token retrieval might fail on first run, this is expected
        }
    }
    
    /**
     * Perform background initialization tasks
     */
    private fun performBackgroundSetup() {
        applicationScope.launch {
            try {
                // Clean up old cache files
                com.example.autostradaauctions.utils.PerformanceUtils.cleanupCache(
                    this@AutostradaAuctionsApplication
                )
                
            } catch (e: Exception) {
                HealthMonitor.logError(e, mapOf("context" to "background_setup"))
            }
        }
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        
        // Handle memory pressure
        HealthMonitor.logUserEvent("memory_pressure", mapOf(
            "available_memory" to com.example.autostradaauctions.utils.PerformanceUtils.getAvailableMemoryPercentage()
        ))
        
        // Clear caches to free memory
        com.example.autostradaauctions.utils.PerformanceUtils.clearImageCache()
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        
        HealthMonitor.logUserEvent("memory_trim", mapOf(
            "trim_level" to level
        ))
        
        // Adjust memory usage based on trim level
        when (level) {
            TRIM_MEMORY_UI_HIDDEN,
            TRIM_MEMORY_BACKGROUND -> {
                // App moved to background, release some resources
                com.example.autostradaauctions.utils.PerformanceUtils.clearImageCache()
            }
            TRIM_MEMORY_MODERATE,
            TRIM_MEMORY_COMPLETE -> {
                // System under memory pressure, release more resources
                com.example.autostradaauctions.utils.PerformanceUtils.clearImageCache()
            }
        }
    }
}
