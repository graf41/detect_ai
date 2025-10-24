// android-app/src/main/kotlin/com/malaria/data/api/MLApiService.kt
package com.malaria.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.append
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.writeFully
import java.io.File

/**
 * HTTP клиент для взаимодействия с Python ML API
 * Отправляет изображения на анализ и получает результаты
 */
class MLApiService(
    private val baseUrl: String = "http://localhost:8000",
    private val client: HttpClient
) {

    /**
     * Анализирует изображение с помощью ML модели
     * @param imageBytes байты изображения для анализа
     * @return результат анализа или null в случае ошибки
     */
    suspend fun analyzeImage(imageBytes: ByteArray): AnalysisResponse? {
        return try {
            val response: AnalysisResponse = client.post("$baseUrl/analyze") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                "image",
                                imageBytes,
                                Headers.build {
                                    append(HttpHeaders.ContentType, "image/jpeg")
                                    append(HttpHeaders.ContentDisposition, "form-data; name=image; filename=image.jpg")
                                }
                            )
                        }
                    )
                )
            }.body()

            response
        } catch (e: Exception) {
            // В реальном приложении здесь должно быть логирование
            println("Ошибка при анализе изображения: ${e.message}")
            null
        }
    }

    /**
     * Проверяет доступность ML API сервера
     * @return true если сервер доступен, false в случае ошибки
     */
    suspend fun checkHealth(): Boolean {
        return try {
            val response: HealthResponse = client.get("$baseUrl/health").body()
            response.status == "healthy" && response.model_loaded == true
        } catch (e: Exception) {
            println("ML API недоступен: ${e.message}")
            false
        }
    }

    /**
     * Получает информацию о загруженной ML модели
     * @return информация о модели или null в случае ошибки
     */
    suspend fun getModelInfo(): ModelInfoResponse? {
        return try {
            client.get("$baseUrl/model-info").body()
        } catch (e: Exception) {
            println("Ошибка при получении информации о модели: ${e.message}")
            null
        }
    }
}

/**
 * Ответ от API анализа изображения
 */
data class AnalysisResponse(
    val diagnosis: String,           // "parasitized" или "uninfected"
    val confidence: Double,          // Уверенность предсказания (0.0 - 1.0)
    val processing_time: Double,     // Время обработки в секундах
    val model_used: String? = null,  // Использованная модель
    val error: String? = null        // Сообщение об ошибке (если есть)
)

/**
 * Ответ от health check endpoint
 */
data class HealthResponse(
    val status: String,              // "healthy" или "model_not_loaded"
    val service: String,             // Название сервиса
    val device: String,              // Устройство выполнения (CPU/GPU)
    val model_loaded: Boolean        // Загружена ли модель
)

/**
 * Информация о ML модели
 */
data class ModelInfoResponse(
    val model_name: String,          // Название модели
    val total_parameters: Long,      // Количество параметров
    val input_size: Int,             // Размер входного изображения
    val device: String               // Устройство выполнения
)

/**
 * Результат анализа для использования в UI
 */
data class AnalysisResult(
    val diagnosis: Diagnosis,
    val confidence: Double,
    val processingTime: Double,
    val modelUsed: String
) {
    enum class Diagnosis {
        PARASITIZED, UNINFECTED, ERROR
    }
}

/**
 * Расширение для конвертации API ответа в доменный объект
 */
fun AnalysisResponse.toDomainModel(): AnalysisResult {
    val diagnosis = when (this.diagnosis.lowercase()) {
        "parasitized" -> AnalysisResult.Diagnosis.PARASITIZED
        "uninfected" -> AnalysisResult.Diagnosis.UNINFECTED
        else -> AnalysisResult.Diagnosis.ERROR
    }

    return AnalysisResult(
        diagnosis = diagnosis,
        confidence = this.confidence,
        processingTime = this.processing_time,
        modelUsed = this.model_used ?: "EfficientNet-B0"
    )
}