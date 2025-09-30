// kotlin-app/data/repository/AnalysisRepository.kt
package com.malaria.data.repository

import com.malaria.domain.models.AnalysisResult
import com.malaria.data.api.MLApiService
import com.malaria.data.api.HealthResponse
import com.malaria.data.api.ModelInfoResponse
import java.io.File

/**
 * Репозиторий для работы с анализом изображений
 * Управляет вызовами API и преобразует данные между слоями
 */
class AnalysisRepository(
    private val mlApiService: MLApiService
) {

    /**
     * Анализирует изображение через ML API
     * @param imageFile файл изображения для анализа
     * @return результат анализа
     */
    suspend fun analyzeImage(imageFile: File): AnalysisResult {
        return try {
            // Читаем байты файла
            val imageBytes = imageFile.readBytes()

            // Вызываем API
            val response = mlApiService.analyzeImage(imageBytes)

            // Преобразуем ответ API в доменную модель
            response?.toDomainModel() ?: AnalysisResult(
                diagnosis = AnalysisResult.Diagnosis.ERROR,
                confidence = 0.0,
                processingTime = 0.0,
                modelUsed = "",
                errorMessage = "Не удалось получить ответ от ML API"
            )

        } catch (e: Exception) {
            // Обработка ошибок чтения файла или сети
            AnalysisResult(
                diagnosis = AnalysisResult.Diagnosis.ERROR,
                confidence = 0.0,
                processingTime = 0.0,
                modelUsed = "",
                errorMessage = "Ошибка при анализе: ${e.message}"
            )
        }
    }

    /**
     * Анализирует изображение из байтов
     * @param imageBytes байты изображения
     * @return результат анализа
     */
    suspend fun analyzeImage(imageBytes: ByteArray): AnalysisResult {
        return try {
            val response = mlApiService.analyzeImage(imageBytes)
            response?.toDomainModel() ?: AnalysisResult(
                diagnosis = AnalysisResult.Diagnosis.ERROR,
                confidence = 0.0,
                processingTime = 0.0,
                modelUsed = "",
                errorMessage = "Пустой ответ от ML API"
            )
        } catch (e: Exception) {
            AnalysisResult(
                diagnosis = AnalysisResult.Diagnosis.ERROR,
                confidence = 0.0,
                processingTime = 0.0,
                modelUsed = "",
                errorMessage = "Сетевая ошибка: ${e.message}"
            )
        }
    }

    /**
     * Проверяет доступность ML API
     * @return true если API доступен и модель загружена
     */
    suspend fun checkHealth(): Boolean {
        return try {
            mlApiService.checkHealth()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Получает детальную информацию о здоровье API
     * @return информация о состоянии API или null при ошибке
     */
    suspend fun getHealthInfo(): HealthResponse? {
        return try {
            mlApiService.getHealthInfo()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Получает информацию о ML модели
     * @return информация о модели или null при ошибке
     */
    suspend fun getModelInfo(): ModelInfoResponse? {
        return try {
            mlApiService.getModelInfo()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Проверяет поддерживается ли файл для анализа
     * @param file файл для проверки
     * @return true если файл поддерживается
     */
    fun isFileSupported(file: File): Boolean {
        if (!file.exists() || file.length() == 0L) return false

        val supportedExtensions = setOf("jpg", "jpeg", "png", "bmp", "webp")
        val extension = file.extension.lowercase()

        return supportedExtensions.contains(extension)
    }

    /**
     * Получает размер файла в читаемом формате
     * @param file файл для проверки
     * @return строка с размером (например "2.5 MB")
     */
    fun getFileSizeReadable(file: File): String {
        val bytes = file.length()
        val kb = bytes / 1024.0
        val mb = kb / 1024.0

        return when {
            mb >= 1 -> "%.1f MB".format(mb)
            kb >= 1 -> "%.1f KB".format(kb)
            else -> "$bytes B"
        }
    }
}

/**
 * Расширение для конвертации API ответа в доменную модель
 */
private fun AnalysisResponse.toDomainModel(): AnalysisResult {
    val diagnosis = when (this.diagnosis.lowercase()) {
        "parasitized" -> AnalysisResult.Diagnosis.PARASITIZED
        "uninfected" -> AnalysisResult.Diagnosis.UNINFECTED
        else -> AnalysisResult.Diagnosis.ERROR
    }

    return AnalysisResult(
        diagnosis = diagnosis,
        confidence = this.confidence,
        processingTime = this.processing_time,
        modelUsed = this.model_used ?: "EfficientNet-B0",
        errorMessage = this.error
    )
}