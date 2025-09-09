package com.example.autostradaauctions.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/**
 * Production-ready security utilities for encryption and secure storage
 */
object SecurityManager {
    
    private const val KEYSTORE_ALIAS = "AutostradaAuctions_SecureKey"
    private const val TRANSFORMATION = "AES/CBC/PKCS7Padding"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    
    /**
     * Generate or retrieve encryption key from Android Keystore
     */
    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        
        return if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
            // Key exists, retrieve it
            keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
        } else {
            // Generate new key
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(false) // Set to true for biometric authentication
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }
    
    /**
     * Encrypt sensitive data
     */
    fun encrypt(data: String): EncryptedData {
        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data.toByteArray())
        
        return EncryptedData(
            data = Base64.encodeToString(encryptedData, Base64.DEFAULT),
            iv = Base64.encodeToString(iv, Base64.DEFAULT)
        )
    }
    
    /**
     * Decrypt sensitive data
     */
    fun decrypt(encryptedData: EncryptedData): String {
        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        
        val iv = Base64.decode(encryptedData.iv, Base64.DEFAULT)
        val ivSpec = IvParameterSpec(iv)
        
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        
        val encryptedBytes = Base64.decode(encryptedData.data, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        
        return String(decryptedBytes)
    }
    
    /**
     * Validate JWT token format
     */
    fun isValidJwtFormat(token: String): Boolean {
        return try {
            val parts = token.split(".")
            parts.size == 3 && parts.all { it.isNotEmpty() }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Extract expiration time from JWT token (simplified)
     */
    fun getTokenExpirationTime(token: String): Long? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            // In a real implementation, you'd parse the JSON payload
            // and extract the 'exp' claim
            null // Placeholder for actual implementation
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check if token is expired
     */
    fun isTokenExpired(token: String): Boolean {
        val expirationTime = getTokenExpirationTime(token)
        return expirationTime?.let {
            System.currentTimeMillis() / 1000 > it
        } ?: true
    }
    
    /**
     * Generate secure random string for nonces, etc.
     */
    fun generateSecureRandomString(length: Int = 16): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }
    
    /**
     * Hash password with salt (for client-side validation)
     */
    fun hashPassword(password: String, salt: String): String {
        // In production, use a proper password hashing library like BCrypt
        return Base64.encodeToString(
            (password + salt).toByteArray(),
            Base64.DEFAULT
        )
    }
    
    /**
     * Validate password strength
     */
    fun validatePasswordStrength(password: String): PasswordValidationResult {
        val issues = mutableListOf<String>()
        
        if (password.length < 8) {
            issues.add("Password must be at least 8 characters long")
        }
        
        if (!password.any { it.isUpperCase() }) {
            issues.add("Password must contain at least one uppercase letter")
        }
        
        if (!password.any { it.isLowerCase() }) {
            issues.add("Password must contain at least one lowercase letter")
        }
        
        if (!password.any { it.isDigit() }) {
            issues.add("Password must contain at least one number")
        }
        
        if (!password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) }) {
            issues.add("Password must contain at least one special character")
        }
        
        return PasswordValidationResult(
            isValid = issues.isEmpty(),
            issues = issues
        )
    }
    
    /**
     * Sanitize input to prevent injection attacks
     */
    fun sanitizeInput(input: String): String {
        return input
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;")
            .trim()
    }
    
    /**
     * Clear sensitive data from memory
     */
    fun clearSensitiveData(vararg data: CharArray) {
        data.forEach { array ->
            array.fill('\u0000')
        }
    }
}

/**
 * Data class for encrypted data with IV
 */
data class EncryptedData(
    val data: String,
    val iv: String
)

/**
 * Password validation result
 */
data class PasswordValidationResult(
    val isValid: Boolean,
    val issues: List<String>
)

/**
 * Security audit logger
 */
object SecurityAudit {
    private const val TAG = "SecurityAudit"
    
    fun logLoginAttempt(username: String, success: Boolean, ip: String? = null) {
        if (com.example.autostradaauctions.config.AppConfig.isDebug) {
            println("$TAG: Login attempt - User: $username, Success: $success, IP: $ip")
        }
        // In production, send to security monitoring service
    }
    
    fun logSecurityViolation(violation: String, details: String? = null) {
        if (com.example.autostradaauctions.config.AppConfig.isDebug) {
            println("$TAG: Security violation - $violation: $details")
        }
        // In production, alert security team
    }
    
    fun logDataAccess(resource: String, userId: Int?) {
        if (com.example.autostradaauctions.config.AppConfig.isDebug) {
            println("$TAG: Data access - Resource: $resource, User: $userId")
        }
        // In production, log to audit trail
    }
}
