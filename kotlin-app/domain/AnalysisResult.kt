// kotlin-app/domain/models/AnalysisResult.kt
package com.malaria.domain.models

/**
 * Доменная модель результата анализа малярии
 * Используется во всем приложении для представления результата диагностики
 */
data class AnalysisResult(
    val diagnosis: Diagnosis,
    val confidence: Double,
    val processingTime: Double,
    val modelUsed: String,
    val errorMessage: String? = null
) {
    enum class Diagnosis {
        PARASITIZED,    // Клетка заражена малярией
        UNINFECTED,     // Клетка здорова
        ERROR           // Ошибка при анализе
    }

    // Вычисляемое свойство - был ли анализ успешным
    val isSuccessful: Boolean
        get() = diagnosis != Diagnosis.ERROR && errorMessage == null

    // Вычисляемое свойство - уверенность в процентах
    val confidencePercentage: Int
        get() = (confidence * 100).toInt()

    // Текстовое представление диагноза для UI
    val diagnosisText: String
        get() = when (diagnosis) {
            Diagnosis.PARASITIZED -> "Заражена"
            Diagnosis.UNINFECTED -> "Здорова"
            Diagnosis.ERROR -> "Ошибка анализа"
        }

    // Цвет для отображения результата в UI
    val diagnosisColor: String
        get() = when (diagnosis) {
            Diagnosis.PARASITIZED -> "#FF4444"  // Красный
            Diagnosis.UNINFECTED -> "#44FF44"   // Зеленый
            Diagnosis.ERROR -> "#FFAA44"        // Оранжевый
        }
}