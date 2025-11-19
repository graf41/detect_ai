package com.malaria.repository

import com.malaria.data.AnalysisRecord
import com.malaria.database.DatabaseManager
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AnalysisRepository {

    fun saveAnalysis(record: AnalysisRecord): Long {
        return try {
            val connection = DatabaseManager.getConnection()
            val sql = """
                INSERT INTO analysis_records 
                (image_path, file_name, diagnosis, confidence, processing_time, model_used, analysis_date)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """

            val statement = connection.prepareStatement(sql, arrayOf("id"))
            statement.setString(1, record.imagePath)
            statement.setString(2, record.fileName)
            statement.setString(3, record.diagnosis)
            statement.setDouble(4, record.confidence)
            statement.setDouble(5, record.processingTime)
            statement.setString(6, record.modelUsed)
            statement.setString(7, record.analysisDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))

            statement.executeUpdate()
            val generatedKeys = statement.generatedKeys
            generatedKeys.next()
            val id = generatedKeys.getLong(1)

            statement.close()
            connection.close()

            println("✅ Результат сохранен в БД с ID: $id")
            id
        } catch (e: Exception) {
            println("❌ Ошибка сохранения в БД: ${e.message}")
            -1L
        }
    }

    fun getAllAnalyses(): List<AnalysisRecord> {
        return try {
            val connection = DatabaseManager.getConnection()
            val statement = connection.createStatement()
            val resultSet: ResultSet = statement.executeQuery("SELECT * FROM analysis_records ORDER BY id DESC")

            val analyses = mutableListOf<AnalysisRecord>()
            while (resultSet.next()) {
                analyses.add(
                    AnalysisRecord(
                        id = resultSet.getLong("id"),
                        imagePath = resultSet.getString("image_path"),
                        fileName = resultSet.getString("file_name"),
                        diagnosis = resultSet.getString("diagnosis"),
                        confidence = resultSet.getDouble("confidence"),
                        processingTime = resultSet.getDouble("processing_time"),
                        modelUsed = resultSet.getString("model_used"),
                        analysisDate = LocalDateTime.parse(resultSet.getString("analysis_date"), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    )
                )
            }

            resultSet.close()
            statement.close()
            connection.close()

            analyses
        } catch (e: Exception) {
            println("❌ Ошибка чтения из БД: ${e.message}")
            emptyList()
        }
    }

    // Остальные методы пока оставь пустыми
    fun getAnalysesByDiagnosis(diagnosis: String): List<AnalysisRecord> {
        return emptyList()
    }

    fun getFilteredAnalyses(
        diagnosis: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): List<AnalysisRecord> {
        return emptyList()
    }
}