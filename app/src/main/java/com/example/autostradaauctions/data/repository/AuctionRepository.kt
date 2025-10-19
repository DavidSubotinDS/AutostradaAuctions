package com.example.autostradaauctions.data.repository

import android.util.Log
import com.example.autostradaauctions.config.AppConfig
import com.example.autostradaauctions.data.api.AuctionApiService
import com.example.autostradaauctions.data.api.ApiConfig
import com.example.autostradaauctions.data.model.Auction
import com.example.autostradaauctions.data.model.AuctionDetail
import com.example.autostradaauctions.data.model.PendingAuctionData
import com.example.autostradaauctions.data.model.UserRole
import com.example.autostradaauctions.data.serialization.UserRoleAdapter
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

open class AuctionRepository(
    private val okHttpClient: OkHttpClient? = null
) {
    companion object {
        private const val TAG = "AuctionRepository"
        // Port configuration now centralized in AppConfig
    }
    
    private val gson = GsonBuilder()
        .registerTypeAdapter(UserRole::class.java, UserRoleAdapter())
        .create()
        
    private val api: AuctionApiService = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(AuctionApiService::class.java)
    
    open suspend fun getAuctions(): Result<List<Auction>> {
        return try {
            val auctions = api.getAuctions()
            Result.success(auctions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    open suspend fun getAuctionDetail(id: Int): Result<AuctionDetail> {
        return try {
            val auction = api.getAuctionDetail(id)
            Result.success(auction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Admin methods - require okHttpClient
    suspend fun getPendingAuctions(authToken: String): Result<List<PendingAuctionData>> {
        if (okHttpClient == null) return Result.failure(Exception("HTTP client not available"))
        
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("${AppConfig.BASE_URL}admin/auctions/pending")
                    .get()
                    .addHeader("Authorization", "Bearer $authToken")
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string()

                Log.d(TAG, "Get pending auctions response: $responseBody")

                if (response.isSuccessful && responseBody != null) {
                    val auctions = parsePendingAuctions(responseBody)
                    Result.success(auctions)
                } else {
                    Result.failure(Exception("Failed to get pending auctions: ${response.code} ${response.message}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting pending auctions", e)
                Result.failure(e)
            }
        }
    }

    suspend fun approveAuction(auctionId: Int, approved: Boolean, rejectionReason: String?, authToken: String): Result<Unit> {
        if (okHttpClient == null) return Result.failure(Exception("HTTP client not available"))
        
        return withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("approved", approved)
                    if (!approved && rejectionReason != null) {
                        put("rejectionReason", rejectionReason)
                    }
                }

                val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("${AppConfig.BASE_URL}admin/auctions/$auctionId/approve")
                    .put(requestBody)
                    .addHeader("Authorization", "Bearer $authToken")
                    .build()

                val response = okHttpClient.newCall(request).execute()

                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    val errorBody = response.body?.string()
                    Result.failure(Exception("Approval failed: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error approving auction", e)
                Result.failure(e)
            }
        }
    }

    suspend fun submitAuction(auctionData: SubmitAuctionRequest, authToken: String): Result<Unit> {
        if (okHttpClient == null) return Result.failure(Exception("HTTP client not available"))
        
        return withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("make", auctionData.make)
                    put("model", auctionData.model)
                    put("year", auctionData.year)
                    put("mileage", auctionData.mileage)
                    put("condition", auctionData.condition)
                    put("description", auctionData.description)
                    put("startingBid", auctionData.startingBid)
                    put("reservePrice", auctionData.reservePrice)
                    put("auctionDuration", auctionData.auctionDuration)
                    put("imageUrls", auctionData.imageUrls.joinToString(","))
                }

                val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("${AppConfig.BASE_URL}auctions")
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer $authToken")
                    .build()

                val response = okHttpClient.newCall(request).execute()

                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    val errorBody = response.body?.string()
                    Result.failure(Exception("Submission failed: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error submitting auction", e)
                Result.failure(e)
            }
        }
    }

    private fun parsePendingAuctions(responseBody: String): List<PendingAuctionData> {
        val auctions = mutableListOf<PendingAuctionData>()
        try {
            val jsonArray = JSONArray(responseBody)
            for (i in 0 until jsonArray.length()) {
                val auctionJson = jsonArray.getJSONObject(i)
                auctions.add(
                    PendingAuctionData(
                        id = auctionJson.getInt("id"),
                        title = auctionJson.optString("title", "Auction #${auctionJson.getInt("id")}"),
                        description = auctionJson.getString("description"),
                        startingPrice = auctionJson.getDouble("startingBid"),
                        reservePrice = auctionJson.optDouble("reservePrice", 0.0),
                        endTime = auctionJson.optString("endTime", ""),
                        imageUrls = auctionJson.optString("imageUrls", "").split(",").filter { it.isNotBlank() },
                        make = auctionJson.getString("make"),
                        model = auctionJson.getString("model"),
                        year = auctionJson.getInt("year"),
                        mileage = auctionJson.getInt("mileage").toString(),
                        condition = auctionJson.getString("condition"),
                        color = auctionJson.optString("color", "Unknown"),
                        vin = auctionJson.optString("vin", ""),
                        transmissionType = auctionJson.optString("transmissionType", "Unknown"),
                        fuelType = auctionJson.optString("fuelType", "Unknown"),
                        bodyType = auctionJson.optString("bodyType", "Unknown"),
                        submittedBy = auctionJson.getString("sellerName"),
                        submittedAt = auctionJson.getString("createdAt")
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing pending auctions response", e)
        }
        return auctions
    }
}

data class SubmitAuctionRequest(
    val make: String,
    val model: String,
    val year: Int,
    val mileage: Int,
    val condition: String,
    val description: String,
    val startingBid: Double,
    val reservePrice: Double,
    val auctionDuration: Int,
    val imageUrls: List<String>
)
