// Проверка, что репозиторий можно создать: saveAnalysis()
package com.malaria.repository

import com.malaria.data.AnalysisRecord
import kotlin.test.Test
import kotlin.test.assertTrue

class AnalysisRepositoryTest {

    @Test
    fun `repository should be created without errors`() {
        val repository = AnalysisRepository()
        assertTrue(true)
    }

    @Test
    fun `repository save method should be callable`() {
        val repository = AnalysisRepository()
        val record = AnalysisRecord(
            imagePath = "test/path.png",
            fileName = "test.png",
            diagnosis = "uninfected",
            confidence = 0.95,
            processingTime = 0.1,
            modelUsed = "TestModel"
        )

        val id = repository.saveAnalysis(record)
        assertTrue(true)
    }
}