package com.malaria.components

import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit

data class AnalysisResult(
    val diagnosis: String,
    val confidence: Double,
    val processingTime: Double,
    val modelUsed: String,
    val error: String? = null
)

object MalariaApiClient {
    private const val BASE_URL = "http://localhost:8000"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeMalariaImage(imageFile: File): AnalysisResult? {
        return try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "image",
                    imageFile.name,
                    RequestBody.create("image/*".toMediaType(), imageFile)
                )
                .build()

            val request = Request.Builder()
                .url("$BASE_URL/analyze")
                .post(requestBody)
                .build()

            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            val responseBody = response.body?.string()

            when (response.code) {
                200 -> responseBody?.let { parseJsonResponse(it) }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseJsonResponse(json: String): AnalysisResult? {
        return try {
            val diagnosis = if (json.contains("\"diagnosis\":\"parasitized\"")) "parasitized"
            else if (json.contains("\"diagnosis\":\"uninfected\"")) "uninfected"
            else "error"

            val confidenceMatch = "\"confidence\":\\s*([0-9.eE+-]+)".toRegex().find(json)
            val confidence = confidenceMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0

            val processingTimeMatch = "\"processing_time\":\\s*([0-9.]+)".toRegex().find(json)
            val processingTime = processingTimeMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0

            AnalysisResult(
                diagnosis = diagnosis,
                confidence = confidence,
                processingTime = processingTime,
                modelUsed = "EfficientNet-B0"
            )
        } catch (e: Exception) {
            null
        }
    }
}