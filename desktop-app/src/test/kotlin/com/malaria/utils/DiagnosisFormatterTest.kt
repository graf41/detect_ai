//Логика перевода диагнозов
package com.malaria.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class DiagnosisFormatterTest {

    private fun getRussianDiagnosis(englishDiagnosis: String): String {
        return when (englishDiagnosis) {
            "parasitized" -> "Заражено"
            "uninfected" -> "Не заражено"
            "error" -> "Ошибка"
            else -> englishDiagnosis
        }
    }

    @Test
    fun `should convert diagnosis to russian correctly`() {
        assertEquals("Заражено", getRussianDiagnosis("parasitized"))
        assertEquals("Не заражено", getRussianDiagnosis("uninfected"))
        assertEquals("Ошибка", getRussianDiagnosis("error"))
        assertEquals("unknown", getRussianDiagnosis("unknown"))
    }
}