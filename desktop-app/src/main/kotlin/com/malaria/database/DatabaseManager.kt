package com.malaria.database

import java.io.File
import java.sql.DriverManager
import java.util.*

object DatabaseManager {
    private var initialized = false
    private var dbPath: String = ""

    fun init() {
        if (!initialized) {
            try {
                // Определяем путь к БД в домашней директории пользователя
                val userHome = System.getProperty("user.home")
                val appDir = File(userHome, ".malaria-detection")
                if (!appDir.exists()) {
                    appDir.mkdirs()
                }

                dbPath = File(appDir, "malaria_analyses.db").absolutePath
                println("🔄 Инициализация БД по пути: $dbPath")

                // Явно загружаем драйвер SQLite
                Class.forName("org.sqlite.JDBC")

                // Создаем подключение
                val connection = DriverManager.getConnection("jdbc:sqlite:$dbPath")
                connection.close()

                createTables()
                initialized = true
                println("✅ БД успешно инициализирована")

            } catch (e: Exception) {
                println("❌ Ошибка инициализации БД: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun createTables() {
        val connection = DriverManager.getConnection("jdbc:sqlite:$dbPath")
        val statement = connection.createStatement()

        val createTableSQL = """
            CREATE TABLE IF NOT EXISTS analysis_records (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                image_path TEXT NOT NULL,
                file_name TEXT NOT NULL, 
                diagnosis TEXT NOT NULL,
                confidence REAL NOT NULL,
                processing_time REAL NOT NULL,
                model_used TEXT NOT NULL,
                analysis_date TEXT NOT NULL
            )
        """

        statement.execute(createTableSQL)
        statement.close()
        connection.close()
        println("✅ Таблица analysis_records создана/проверена")
    }

    fun getConnection() = DriverManager.getConnection("jdbc:sqlite:$dbPath")
}