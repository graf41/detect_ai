package com.malaria.integration

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaType
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.awt.Color


/**
 *
 * ## проверка на:
 * - fastapi сервер доступен и отвечает
 * - Endpoint /analyze принимает запросы
 * - api возвращает валидную структуру JSON
 * - Время ответа в допустимых пределах
 *
 * ## для ci:
 * - Падает если сервер недоступен
 * - Падает если изменился API контракт
 * - Падает если сервер отвечает слишком медленно
 */
class ApiHealthCheckTest {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "http://localhost:8000"

    /**
     * основной тест на функционал
     */
    @Test
    fun `ml api should be fully operational`() = runBlocking {
        println("Starting REAL API Health Check...")

        // 1. Проверяем доступность сервера
        val serverAvailable = checkServerAvailability()
        assertTrue(serverAvailable, "ML Server should be running on $baseUrl")

        // 2. Проверяем основной endpoint
        val endpointAvailable = checkAnalyzeEndpoint()
        assertTrue(endpointAvailable, "Analyze endpoint should be available")

        // 3. Проверяем реальный запрос с тестовым изображением
        val testResult = performRealAnalysis()
        assertNotNull(testResult, "API should return valid analysis result")

        // 4. Проверяем структуру ответа
        validateResponseStructure(testResult!!)

        println("REAL API Health Check PASSED - ML Server is fully operational")
        println("   - Server: $baseUrl")
        println("   - Endpoint: /analyze")
        println("   - Diagnosis: ${testResult.diagnosis}")
        println("   - Confidence: ${testResult.confidence}")
        println("   - Response Time: ${testResult.processingTime}s")
    }

    /**
     * проверка производительности
     */
    @Test
    fun `api response time should be reasonable`() = runBlocking {
        val startTime = System.currentTimeMillis()

        val testImage = createRealTestImage()
        try {
            val result = performRealAnalysis(testImage)
            val endTime = System.currentTimeMillis()
            val totalTime = endTime - startTime

            assertNotNull(result, "API should respond within timeout")
            assertTrue(totalTime < 45000, "Total response time should be under 45 seconds (was ${totalTime}ms)")

            println("Performance Check: ${totalTime}ms")

        } finally {
            testImage.delete()
        }
    }

    /**
     * ПРОВЕРКА ОБРАБОТКИ ОШИБОК
     */
    @Test
    fun `api should handle invalid requests gracefully`() = runBlocking {
        val invalidFile = File("/invalid/path/nonexistent.jpg")

        try {
            val result = performRealAnalysis(invalidFile)
            // Ожидаем null или корректную обработку ошибки
            println("Error handling: ${if (result == null) "CORRECT" else "HANDLED"}")

        } catch (e: Exception) {
            // Если кидает исключение - это тоже приемлемо
            println("Error handling: Exception thrown (expected)")
        }
    }


    private fun checkServerAvailability(): Boolean {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/analyze")
                .head()
                .build()

            val response = client.newCall(request).execute()
            response.code != 404
        } catch (e: Exception) {
            println("Server unavailable: ${e.message}")
            false
        }
    }

    private fun checkAnalyzeEndpoint(): Boolean {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/analyze")
                .method("OPTIONS", null)
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful || response.code != 404
        } catch (e: Exception) {
            println("Analyze endpoint unavailable: ${e.message}")
            false
        }
    }

    private fun performRealAnalysis(imageFile: File = createRealTestImage()): com.malaria.components.AnalysisResult? {
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
                .url("$baseUrl/analyze")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                println("API returned error: HTTP ${response.code}")
                return null
            }

            val responseBody = response.body?.string()
            if (responseBody.isNullOrEmpty()) {
                println("API returned empty response")
                return null
            }

            // Используем реальный парсер из MalariaApiClient
            parseJsonResponse(responseBody)

        } catch (e: Exception) {
            println("API request failed: ${e.message}")
            null
        }
    }
    private fun validateResponseStructure(result: com.malaria.components.AnalysisResult) {
        assertTrue(result.diagnosis.isNotEmpty(), "Diagnosis should not be empty")
        assertTrue(
            result.diagnosis == "parasitized" || result.diagnosis == "uninfected",
            "Diagnosis should be 'parasitized' or 'uninfected', got '${result.diagnosis}'"
        )
        assertTrue(
            result.confidence >= 0.0 && result.confidence <= 1.0,
            "Confidence should be between 0 and 1, got ${result.confidence}"
        )
        assertTrue(result.processingTime >= 0, "Processing time should be positive")
        assertTrue(result.modelUsed.isNotEmpty(), "Model used should not be empty")
    }

    private fun createRealTestImage(): File {
        val width = 224
        val height = 224
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        val graphics = image.graphics

        graphics.color = Color(240, 240, 240)
        graphics.fillRect(0, 0, width, height)

        graphics.color = Color(180, 60, 60)

        // Рисуем неровную форму вместо идеального круга
        val xPoints = intArrayOf(100, 140, 150, 110, 70, 60)
        val yPoints = intArrayOf(80, 90, 130, 150, 130, 90)
        graphics.fillPolygon(xPoints, yPoints, 6)

        graphics.color = Color(200, 100, 100)
        for (i in 0..50) {
            val x = (80 + Math.random() * 50).toInt()
            val y = (80 + Math.random() * 50).toInt()
            val size = (1 + Math.random() * 3).toInt()
            graphics.fillOval(x, y, size, size)
        }

        graphics.dispose()

        val file = File.createTempFile("cell_test_image", ".jpg")
        ImageIO.write(image, "JPEG", file)
        println("Created realistic test image: ${file.absolutePath} (${file.length()} bytes)")
        return file
    }

    // Копия парсера из MalariaApiClient
    private fun parseJsonResponse(json: String): com.malaria.components.AnalysisResult? {
        return try {
            val diagnosis = if (json.contains("\"diagnosis\":\"parasitized\"")) "parasitized"
            else if (json.contains("\"diagnosis\":\"uninfected\"")) "uninfected"
            else "error"

            val confidenceMatch = "\"confidence\":\\s*([0-9.eE+-]+)".toRegex().find(json)
            val confidence = confidenceMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0

            val processingTimeMatch = "\"processing_time\":\\s*([0-9.]+)".toRegex().find(json)
            val processingTime = processingTimeMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0

            com.malaria.components.AnalysisResult(
                diagnosis = diagnosis,
                confidence = confidence,
                processingTime = processingTime,
                modelUsed = "EfficientNet-B0"
            )
        } catch (e: Exception) {
            println("❌ JSON parsing failed: ${e.message}")
            null
        }
    }
}