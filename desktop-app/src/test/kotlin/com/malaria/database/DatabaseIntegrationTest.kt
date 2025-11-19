//тест на то что DatabaseManager не падает
package com.malaria.database

import kotlin.test.Test
import kotlin.test.assertTrue

class DatabaseIntegrationTest {

    @Test
    fun `database should initialize without errors`() {
        DatabaseManager.init()
        assertTrue(true)
    }
}