package com.example.autostradaauctions.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Performance optimization utilities
 */
object PerformanceUtils {
    
    private const val TAG = "PerformanceUtils"
    
    // Image cache for better performance
    private val imageCache = LruCache<String, Bitmap>(
        (Runtime.getRuntime().maxMemory() / 8).toInt()
    )
    
    /**
     * Debounce flow emissions for search and input fields
     */
    fun <T> Flow<T>.debounceAndDistinct(timeoutMillis: Long = 500L): Flow<T> {
        return this
            .debounce(timeoutMillis)
            .distinctUntilChanged()
    }
    
    /**
     * Create optimized bitmap from file path
     */
    fun decodeSampledBitmapFromFile(
        filePath: String,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        return try {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(filePath, options)
            
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            BitmapFactory.decodeFile(filePath, options)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Calculate sample size for bitmap scaling
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while (halfHeight / inSampleSize >= reqHeight && 
                   halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    /**
     * Cache bitmap with LRU cache
     */
    fun cacheBitmap(key: String, bitmap: Bitmap) {
        imageCache.put(key, bitmap)
    }
    
    /**
     * Retrieve bitmap from cache
     */
    fun getCachedBitmap(key: String): Bitmap? {
        return imageCache.get(key)
    }
    
    /**
     * Clear image cache
     */
    fun clearImageCache() {
        imageCache.evictAll()
    }
    
    /**
     * Compress and save bitmap to cache directory
     */
    suspend fun compressAndSaveBitmap(
        context: Context,
        bitmap: Bitmap,
        filename: String,
        quality: Int = 80
    ): String? = withContext(Dispatchers.IO) {
        try {
            val cacheDir = context.cacheDir
            val file = File(cacheDir, filename)
            
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }
            
            file.absolutePath
        } catch (e: IOException) {
            null
        }
    }
    
    /**
     * Clean up old cache files
     */
    suspend fun cleanupCache(
        context: Context,
        maxAgeMillis: Long = 7 * 24 * 60 * 60 * 1000L // 7 days
    ) = withContext(Dispatchers.IO) {
        try {
            val cacheDir = context.cacheDir
            val currentTime = System.currentTimeMillis()
            
            cacheDir.listFiles()?.forEach { file ->
                if (currentTime - file.lastModified() > maxAgeMillis) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            // Handle cleanup errors gracefully
        }
    }
    
    /**
     * Memory optimization for collections
     */
    inline fun <T> Collection<T>.optimizedForEach(action: (T) -> Unit) {
        val iterator = this.iterator()
        while (iterator.hasNext()) {
            action(iterator.next())
        }
    }
    
    /**
     * Check available memory
     */
    fun getAvailableMemoryPercentage(): Float {
        val runtime = Runtime.getRuntime()
        val used = runtime.totalMemory() - runtime.freeMemory()
        val max = runtime.maxMemory()
        return ((max - used) / max.toFloat()) * 100f
    }
    
    /**
     * Memory pressure detection
     */
    fun isMemoryLow(): Boolean {
        return getAvailableMemoryPercentage() < 20f
    }
    
    /**
     * Coroutine scope for background operations
     */
    val backgroundScope = CoroutineScope(
        Dispatchers.Default + SupervisorJob() + 
        CoroutineExceptionHandler { _, throwable ->
            // Log background operation errors
            if (com.example.autostradaauctions.config.AppConfig.isDebug) {
                println("Background operation failed: ${throwable.message}")
            }
        }
    )
    
    /**
     * Execute with timeout and fallback
     */
    suspend fun <T> executeWithTimeout(
        timeoutMillis: Long,
        fallback: T,
        block: suspend () -> T
    ): T {
        return try {
            withTimeout(timeoutMillis) {
                block()
            }
        } catch (e: TimeoutCancellationException) {
            fallback
        }
    }
}

/**
 * Extension functions for performance optimization
 */

/**
 * Chunked processing for large collections
 */
suspend fun <T, R> Collection<T>.processInChunks(
    chunkSize: Int = 100,
    processor: suspend (List<T>) -> List<R>
): List<R> = withContext(Dispatchers.Default) {
    chunked(chunkSize)
        .map { chunk -> async { processor(chunk) } }
        .awaitAll()
        .flatten()
}

/**
 * Lazy initialization with thread safety
 */
fun <T> lazyThreadSafe(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.SYNCHRONIZED, initializer)

/**
 * Measure execution time (debug only)
 */
inline fun <T> measureTimeIfDebug(
    tag: String = "Performance",
    block: () -> T
): T {
    return if (com.example.autostradaauctions.config.AppConfig.isDebug) {
        val startTime = System.currentTimeMillis()
        val result = block()
        val endTime = System.currentTimeMillis()
        println("$tag: ${endTime - startTime}ms")
        result
    } else {
        block()
    }
}
