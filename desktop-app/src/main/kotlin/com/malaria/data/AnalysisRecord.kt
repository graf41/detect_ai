// desktop-app/src/main/kotlin/com/malaria/data/AnalysisRecord.kt
package com.malaria.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class AnalysisRecord(
    val id: Long = 0,
    val imagePath: String,
    val fileName: String,
    val diagnosis: String,
    val confidence: Double,
    val processingTime: Double,
    val modelUsed: String,

    // String для сериализации
    @Serializable(with = LocalDateTimeSerializer::class)
    val analysisDate: LocalDateTime = LocalDateTime.now()
) {
    fun getRussianDiagnosis(): String = when (diagnosis) {
        "parasitized" -> "Заражено"
        "uninfected" -> "Не заражено"
        else -> diagnosis
    }

    // Форматированная дата для UI
    fun getFormattedDate(): String {
        return analysisDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
    }

    // Для фильтрации по дате
    fun getDateForFilter(): String {
        return analysisDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }
}

// Сериализатор для LocalDateTime
object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), formatter)
    }
}