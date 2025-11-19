//Логика валидации файлов
package com.malaria.utils

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileValidationTest {

    private fun isSupportedFormat(filePath: String): Boolean {
        val extension = filePath.substringAfterLast(".", "").lowercase()
        return extension == "png" || extension == "jpg" || extension == "jpeg"
    }

    @Test
    fun `should validate supported image formats`() {
        assertTrue(isSupportedFormat("image.png"))
        assertTrue(isSupportedFormat("photo.jpg"))
        assertTrue(isSupportedFormat("picture.jpeg"))
        assertFalse(isSupportedFormat("document.pdf"))
        assertFalse(isSupportedFormat("text.txt"))
    }
}