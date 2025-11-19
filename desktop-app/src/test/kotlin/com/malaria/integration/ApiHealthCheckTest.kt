// проверка, что сервер живой
package com.malaria.integration

import kotlin.test.Test
import kotlin.test.assertTrue
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class ApiHealthCheckTest {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "http://localhost:8000"

    @Test
    fun `api health endpoint should respond`() {
        val request = okhttp3.Request.Builder()
            .url("$baseUrl/health")
            .build()

        val response = client.newCall(request).execute()
        assertTrue(response.isSuccessful)
    }
}