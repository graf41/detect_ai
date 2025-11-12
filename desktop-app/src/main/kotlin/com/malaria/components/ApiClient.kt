package com.malaria.components

import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AnalysisResult(
    val diagnosis: String,
    val confidence: Double,
    val processingTime: Double,
    val modelUsed: String,
    val error: String? = null
)

object MalariaApiClient {
    private const val BASE_URL = "http://localhost:8000"

    private val client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .build()

    suspend fun analyzeMalariaImage(imageFile: File): AnalysisResult? {
        return try {
            val boundary = "----WebKitFormBoundary" + System.currentTimeMillis()
            val imageBytes = Files.readAllBytes(imageFile.toPath())
            val requestBody = buildMultipartBody(boundary, imageBytes, imageFile.name)

            val request = HttpRequest.newBuilder()
                .uri(URI.create("$BASE_URL/analyze"))
                .header("Content-Type", "multipart/form-data; boundary=$boundary")
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                .build()

            val response = withContext(Dispatchers.IO) {
                client.send(request, HttpResponse.BodyHandlers.ofString())
            }
            println("=== RAW RESPONSE ===")
            println("Status: ${response.statusCode()}")
            println("Body: ${response.body()}")
            println("=== END RESPONSE ===")
            when (response.statusCode()) {
                200 -> parseJsonResponse(response.body())
                else -> {
                    println("API Error: ${response.statusCode()} - ${response.body()}")
                    null
                }
            }
        } catch (e: Exception) {
            println("Network error: ${e.message}")
            null
        }
    }

    private fun buildMultipartBody(boundary: String, imageBytes: ByteArray, fileName: String): ByteArray {
        val lineSeparator = "\r\n"
        val boundaryLine = "--$boundary$lineSeparator"
        val endBoundary = "--$boundary--$lineSeparator"

        val header = boundaryLine +
                "Content-Disposition: form-data; name=\"image\"; filename=\"$fileName\"$lineSeparator" +
                "Content-Type: image/jpeg$lineSeparator$lineSeparator"

        val footer = "$lineSeparator$endBoundary"

        return header.toByteArray() + imageBytes + footer.toByteArray()
    }

    private fun parseJsonResponse(json: String): AnalysisResult? {
        return try {
            println("PARSING JSON: $json")
            val diagnosis = if (json.contains("\"diagnosis\":\"parasitized\"")) "parasitized"
            else if (json.contains("\"diagnosis\":\"uninfected\"")) "uninfected"
            else "error"

            val confidenceMatch = "\"confidence\":\\s*([0-9.eE+-]+)".toRegex().find(json)
            val confidence = confidenceMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0

            val processingTimeMatch = "\"processing_time\":\\s*([0-9.]+)".toRegex().find(json)
            val processingTime = processingTimeMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
            println("PARSED: diagnosis=$diagnosis, confidence=$confidence, time=$processingTime")

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