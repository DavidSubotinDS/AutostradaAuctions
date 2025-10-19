package com.example.autostradaauctions.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import com.example.autostradaauctions.config.AppConfig

class AdminRepository(
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val TAG = "AdminRepository"
        // Port configuration now centralized in AppConfig
    }

    suspend fun getAllUsers(authToken: String): Result<List<AdminUser>> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("${AppConfig.BASE_URL}admin/users")
                    .get()
                    .addHeader("Authorization", "Bearer $authToken")
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string()

                Log.d(TAG, "Get users response: $responseBody")

                if (response.isSuccessful && responseBody != null) {
                    val users = parseUsersResponse(responseBody)
                    Result.success(users)
                } else {
                    Result.failure(Exception("Failed to get users: ${response.code} ${response.message}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting users", e)
                Result.failure(e)
            }
        }
    }

    suspend fun deleteUser(userId: Int, authToken: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("${AppConfig.BASE_URL}admin/users/$userId")
                    .delete()
                    .addHeader("Authorization", "Bearer $authToken")
                    .build()

                val response = okHttpClient.newCall(request).execute()

                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    val errorBody = response.body?.string()
                    Result.failure(Exception("Delete failed: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting user", e)
                Result.failure(e)
            }
        }
    }

    suspend fun updateUser(userId: Int, user: UpdateUserRequest, authToken: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("firstName", user.firstName)
                    put("lastName", user.lastName)
                    put("email", user.email)
                    put("username", user.username)
                    put("role", user.role)
                    put("isEmailVerified", user.isEmailVerified)
                    if (user.newPassword.isNotEmpty()) {
                        put("newPassword", user.newPassword)
                    }
                }

                val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("${AppConfig.BASE_URL}admin/users/$userId")
                    .put(requestBody)
                    .addHeader("Authorization", "Bearer $authToken")
                    .build()

                val response = okHttpClient.newCall(request).execute()

                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    val errorBody = response.body?.string()
                    Result.failure(Exception("Update failed: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating user", e)
                Result.failure(e)
            }
        }
    }

    suspend fun createUser(user: CreateUserRequest, authToken: String): Result<AdminUser> {
        return withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("firstName", user.firstName)
                    put("lastName", user.lastName)
                    put("email", user.email)
                    put("username", user.username)
                    put("password", user.password)
                    put("role", user.role)
                    put("isEmailVerified", user.isEmailVerified)
                }

                val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("${AppConfig.BASE_URL}admin/users")
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer $authToken")
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val newUser = parseUserResponse(responseBody)
                    Result.success(newUser)
                } else {
                    val errorBody = responseBody ?: "Unknown error"
                    Result.failure(Exception("Create failed: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating user", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getAnalytics(authToken: String): Result<AdminAnalytics> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("${AppConfig.BASE_URL}admin/analytics")
                    .get()
                    .addHeader("Authorization", "Bearer $authToken")
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val analytics = parseAnalyticsResponse(responseBody)
                    Result.success(analytics)
                } else {
                    Result.failure(Exception("Failed to get analytics: ${response.code} ${response.message}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting analytics", e)
                Result.failure(e)
            }
        }
    }

    private fun parseUsersResponse(responseBody: String): List<AdminUser> {
        val users = mutableListOf<AdminUser>()
        try {
            val jsonArray = JSONArray(responseBody)
            for (i in 0 until jsonArray.length()) {
                val userJson = jsonArray.getJSONObject(i)
                users.add(
                    AdminUser(
                        id = userJson.getInt("id"),
                        firstName = userJson.getString("firstName"),
                        lastName = userJson.getString("lastName"),
                        email = userJson.getString("email"),
                        username = userJson.getString("username"),
                        role = userJson.getString("role"),
                        isEmailVerified = userJson.getBoolean("isEmailVerified"),
                        createdAt = userJson.getString("createdAt")
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing users response", e)
        }
        return users
    }

    private fun parseUserResponse(responseBody: String): AdminUser {
        val userJson = JSONObject(responseBody)
        return AdminUser(
            id = userJson.getInt("id"),
            firstName = userJson.getString("firstName"),
            lastName = userJson.getString("lastName"),
            email = userJson.getString("email"),
            username = userJson.getString("username"),
            role = userJson.getString("role"),
            isEmailVerified = userJson.getBoolean("isEmailVerified"),
            createdAt = userJson.getString("createdAt")
        )
    }

    private fun parseAnalyticsResponse(responseBody: String): AdminAnalytics {
        val json = JSONObject(responseBody)
        return AdminAnalytics(
            totalUsers = json.getInt("totalUsers"),
            totalAdmins = json.getInt("totalAdmins"),
            totalBuyers = json.getInt("totalBuyers"),
            totalSellers = json.getInt("totalSellers"),
            totalAuctions = json.getInt("totalAuctions"),
            activeAuctions = json.getInt("activeAuctions"),
            pendingAuctions = json.getInt("pendingAuctions"),
            endedAuctions = json.getInt("endedAuctions"),
            totalBids = json.getInt("totalBids"),
            totalRevenue = json.getDouble("totalRevenue")
        )
    }
}

data class AdminUser(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val username: String,
    val role: String,
    val isEmailVerified: Boolean,
    val createdAt: String
)

data class UpdateUserRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val username: String,
    val role: String,
    val isEmailVerified: Boolean,
    val newPassword: String = ""
)

data class CreateUserRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val username: String,
    val password: String,
    val role: String,
    val isEmailVerified: Boolean
)

data class AdminAnalytics(
    val totalUsers: Int,
    val totalAdmins: Int,
    val totalBuyers: Int,
    val totalSellers: Int,
    val totalAuctions: Int,
    val activeAuctions: Int,
    val pendingAuctions: Int,
    val endedAuctions: Int,
    val totalBids: Int,
    val totalRevenue: Double
)