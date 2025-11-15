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
/**
 *
 * Содержит полную диагностическую информацию, полученную от ML EfficientNet-B0-main.py по app.py.
 * Используется для передачи данных между api и ui
 *
 * ## Медицинская семантика:
 * - **parasitized**: клетка заражена малярийным плазмодием
 * - **uninfected**: клетка здорова
 *
 * @property diagnosis медицинский диагноз на основе классификации AI
 * @property confidence вероятность корректности диагнона в диапазоне 0.0 - 1.0
 * @property processingTime время выполнения анализа ML моделью в секундах
 * @property modelUsed идентификатор версии ML модели для трассируемости
 * @property error описание ошибки при неудачном анализе, null при успехе
 *
 * @see MalariaApiClient источник создания объектов данного класса
 */
data class AnalysisResult(
    val diagnosis: String,
    val confidence: Double,
    val processingTime: Double,
    val modelUsed: String,
    val error: String? = null
)
/**
 * HTTP клиент для взаимодействия с ML микросервисом анализа малярии
 *
 * Реализует паттерн API Client с использованием OkHttp для сетевых запросов
 * и Kotlin Coroutines для асинхронного выполнения. Обеспечивает отправку
 * изображений клеток крови на анализ и парсинг диагностических результатов.
 *
 * ## Особенности реализации:
 * - Таймауты 30 секунд для медицинского приложения
 * - Multipart/form-data для передачи бинарных изображений
 * - Regex-based парсинг JSON как временное решение
 * - Полная обработка ошибок с возвратом null
 *
 * @see AnalyzeScreen основной потребитель данного клиента
 */
object MalariaApiClient {
    private const val BASE_URL = "http://localhost:8000"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    /**
     * Выполняет анализ изображения клеток крови на наличие малярийных плазмодиев
     *
     * Отправляет multipart/form-data запрос к app.py, который связывается с best.pt
     * обрабатывает HTTP ответ и преобразует JSON в доменную модель.
     *
     * ## Процесс анализа:
     * 1. Валидация файла изображения
     * 2. Формирование multipart запроса
     * 3. Асинхронная отправка на ML сервер
     * 4. Парсинг JSON ответа
     * 5. Создание доменного объекта
     *
     * @param imageFile файл изображения микроскопии крови в форматах PNG/JPEG
     * @return результат анализа или null при сетевых/парсинг ошибках
     *
     * @throws IOException при проблемах сетевого соединения
     * @throws IllegalArgumentException при некорректном файле изображения
     */
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
    /**
     * Приватный парсер JSON ответа от ML сервиса
     *
     *
     * @param json сырой JSON ответ от API
     * @return сконструированный AnalysisResult или null при ошибках парсинга
     */
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