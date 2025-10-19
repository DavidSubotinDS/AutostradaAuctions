package com.example.autostradaauctions.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class ImageRepository(
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val TAG = "ImageRepository"
        private const val BASE_URL = "http://10.0.2.2:5000/api"
    }

    suspend fun uploadImage(context: Context, imageUri: Uri, authToken: String): Result<ImageUploadResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Convert URI to File
                val file = uriToFile(context, imageUri)
                if (file == null) {
                    return@withContext Result.failure(Exception("Failed to convert URI to file"))
                }

                // Create request body
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                val multipartBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.name, requestBody)
                    .build()

                // Create request
                val request = Request.Builder()
                    .url("$BASE_URL/image/upload")
                    .post(multipartBody)
                    .addHeader("Authorization", "Bearer $authToken")
                    .build()

                // Execute request
                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string()

                Log.d(TAG, "Upload response: $responseBody")

                if (response.isSuccessful && responseBody != null) {
                    // Parse response (assuming it returns JSON with imageUrl and fileName)
                    val imageUrl = parseImageUrl(responseBody)
                    val fileName = parseFileName(responseBody)
                    
                    // Clean up temporary file
                    file.delete()
                    
                    Result.success(ImageUploadResponse(imageUrl, fileName))
                } else {
                    // Clean up temporary file
                    file.delete()
                    Result.failure(Exception("Upload failed: ${response.code} ${response.message}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading image", e)
                Result.failure(e)
            }
        }
    }

    suspend fun uploadMultipleImages(
        context: Context, 
        imageUris: List<Uri>, 
        authToken: String
    ): Result<List<ImageUploadResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val files = mutableListOf<File>()
                
                // Convert all URIs to files
                for (uri in imageUris) {
                    val file = uriToFile(context, uri)
                    if (file != null) {
                        files.add(file)
                    }
                }

                if (files.isEmpty()) {
                    return@withContext Result.failure(Exception("No valid images to upload"))
                }

                // Create multipart body
                val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
                
                for (file in files) {
                    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                    multipartBuilder.addFormDataPart("files", file.name, requestBody)
                }

                val multipartBody = multipartBuilder.build()

                // Create request
                val request = Request.Builder()
                    .url("$BASE_URL/image/upload-multiple")
                    .post(multipartBody)
                    .addHeader("Authorization", "Bearer $authToken")
                    .build()

                // Execute request
                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string()

                Log.d(TAG, "Multiple upload response: $responseBody")

                // Clean up temporary files
                files.forEach { it.delete() }

                if (response.isSuccessful && responseBody != null) {
                    val images = parseMultipleImageResponse(responseBody)
                    Result.success(images)
                } else {
                    Result.failure(Exception("Upload failed: ${response.code} ${response.message}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading multiple images", e)
                Result.failure(e)
            }
        }
    }

    suspend fun deleteImage(fileName: String, authToken: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/image/$fileName")
                    .delete()
                    .addHeader("Authorization", "Bearer $authToken")
                    .build()

                val response = okHttpClient.newCall(request).execute()

                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Delete failed: ${response.code} ${response.message}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting image", e)
                Result.failure(e)
            }
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
            
            inputStream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Error converting URI to file", e)
            null
        }
    }

    private fun parseImageUrl(responseBody: String): String {
        // Simple JSON parsing - in production, use a proper JSON parser
        val regex = "\"imageUrl\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        val match = regex.find(responseBody)
        return match?.groupValues?.get(1) ?: ""
    }

    private fun parseFileName(responseBody: String): String {
        // Simple JSON parsing - in production, use a proper JSON parser
        val regex = "\"fileName\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        val match = regex.find(responseBody)
        return match?.groupValues?.get(1) ?: ""
    }

    private fun parseMultipleImageResponse(responseBody: String): List<ImageUploadResponse> {
        // Simple JSON parsing - in production, use a proper JSON parser
        val images = mutableListOf<ImageUploadResponse>()
        
        val imageUrlRegex = "\"imageUrl\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        val fileNameRegex = "\"fileName\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        
        val imageUrls = imageUrlRegex.findAll(responseBody).map { it.groupValues[1] }.toList()
        val fileNames = fileNameRegex.findAll(responseBody).map { it.groupValues[1] }.toList()
        
        for (i in imageUrls.indices) {
            if (i < fileNames.size) {
                images.add(ImageUploadResponse(imageUrls[i], fileNames[i]))
            }
        }
        
        return images
    }
}

data class ImageUploadResponse(
    val imageUrl: String,
    val fileName: String
)