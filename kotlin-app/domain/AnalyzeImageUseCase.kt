// kotlin-app/domain/usecases/AnalyzeImageUseCase.kt
package com.malaria.domain.usecases

import com.malaria.domain.models.AnalysisResult
import com.malaria.data.repository.AnalysisRepository
import java.io.File

/**
 * Use Case для анализа изображения на малярию
 * Содержит бизнес-логику процесса анализа
 */
class AnalyzeImageUseCase(
    private val analysisRepository: AnalysisRepository
) {

    /**
     * Выполняет анализ изображения
     * @param imageFile файл изображения для анализа
     * @return результат анализа
     */
    suspend operator fun invoke(imageFile: File): AnalysisResult {
        // Валидация файла
        if (!imageFile.exists()) {
            return AnalysisResult(
                diagnosis = AnalysisResult.Diagnosis.ERROR,
                confidence = 0.0,
                processingTime = 0.0,
                modelUsed = "",
                errorMessage = "Файл не существует"
            )
        }

        if (imageFile.length() == 0L) {
            return AnalysisResult(
                diagnosis = AnalysisResult.Diagnosis.ERROR,
                confidence = 0.0,
                processingTime = 0.0,
                modelUsed = "",
                errorMessage = "Файл пустой"
            )
        }

        // Проверяем что это изображение по расширению
        val allowedExtensions = setOf("jpg", "jpeg", "png", "bmp")
        val fileExtension = imageFile.extension.lowercase()
        if (!allowedExtensions.contains(fileExtension)) {
            return AnalysisResult(
                diagnosis = AnalysisResult.Diagnosis.ERROR,
                confidence = 0.0,
                processingTime = 0.0,
                modelUsed = "",
                errorMessage = "Неподдерживаемый формат файла. Используйте JPG, PNG или BMP"
            )
        }

        // Проверяем доступность ML API
        val isApiHealthy = analysisRepository.checkHealth()
        if (!isApiHealthy) {
            return AnalysisResult(
                diagnosis = AnalysisResult.Diagnosis.ERROR,
                confidence = 0.0,
                processingTime = 0.0,
                modelUsed = "",
                errorMessage = "ML API недоступен. Запустите сервер анализа"
            )
        }

        // Выполняем анализ через репозиторий
        return analysisRepository.analyzeImage(imageFile)
    }

    /**
     * Проверяет доступность ML API
     * @return true если API доступен, false если нет
     */
    suspend fun checkApiHealth(): Boolean {
        return analysisRepository.checkHealth()
    }

    /**
     * Получает информацию о ML модели
     * @return информация о модели или null если API недоступен
     */
    suspend fun getModelInfo(): String? {
        return analysisRepository.getModelInfo()?.let { modelInfo ->
            "Модель: ${modelInfo.model_name}\n" +
                    "Параметров: ${modelInfo.total_parameters}\n" +
                    "Размер входного изображения: ${modelInfo.input_size}x${modelInfo.input_size}\n" +
                    "Устройство: ${modelInfo.device}"
        }
    }
}