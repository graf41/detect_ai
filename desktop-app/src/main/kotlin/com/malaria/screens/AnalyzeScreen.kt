package com.malaria.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import com.malaria.components.AnalysisResult
import com.malaria.components.MalariaApiClient
/**
 * Главный экран анализа изображений клеток крови на малярию
 *
 * Предоставляет полный интерфейс для загрузки изображений, отправки на ML анализ
 * и отображения диагностических результатов. Реализует state-менеджмент для
 * управления процессом анализа и обработки ошибок.
 *
 * ## Основной workflow:
 * 1. Выбор файла через системный диалог
 * 2. Валидация формата изображения (PNG/JPEG)
 * 3. Асинхронная отправка на ML сервер
 * 4. Отображение результатов с цветовой кодировкой
 * 5. Обработка ошибок сети и анализа
 *
 * @param onBackClick callback для возврата на главный экран
 *
 * @see MalariaApiClient клиент для взаимодействия с ML бэкендом
 * @see AnalysisResult модель данных для отображения результатов
 */
@Composable
fun AnalyzeScreen(onBackClick: () -> Unit) {
    var selectedFile by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<AnalysisResult?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF929292))
            .padding(32.dp)
    ) {
        Text(
            text = "Анализ изображения",
            fontSize = 28.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                errorMessage = null
                analysisResult = null
                openFileDialog(
                    onFileSelected = { filePath ->
                        selectedFile = filePath
                    },
                    onError = { message ->
                        errorMessage = message
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Загрузить изображение", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            ErrorMessage(
                message = errorMessage!!,
                onDismiss = { errorMessage = null }
            )
        }

        if (selectedFile != null) {
            FileItem(
                filePath = selectedFile!!,
                onRemove = {
                    selectedFile = null
                    errorMessage = null
                    analysisResult = null
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        analysisResult?.let { result ->
            AnalysisResultView(result = result)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = {
                if (selectedFile != null && !isLoading) {
                    isLoading = true
                    analysisResult = null
                    errorMessage = null

                    coroutineScope.launch {
                        try {
                            val file = File(selectedFile!!)
                            println("Отправляем файл: ${file.name}")
                            val result = MalariaApiClient.analyzeMalariaImage(file)
                            println("Получен результат: $result")

                            if (result != null) {
                                analysisResult = result
                                println("Результат установлен в UI")
                            } else {
                                errorMessage = "Не удалось подключиться к серверу анализа"
                                println("Результат null")
                            }
                        } catch (e: Exception) {
                            println("Ошибка: ${e.message}")
                            errorMessage = "Ошибка: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedFile != null && !isLoading
        ) {
            Text(
                if (isLoading) "Анализ..." else "Проанализировать изображение",
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Вернуться на главную", color = Color.Black)
        }
    }
}

/**
 * Компонент для отображения результатов анализа малярии
 *
 * Визуализирует диагностическую информацию с цветовой кодировкой
 *
 * @param result объект AnalysisResult с данными диагностики
 *
 * @see AnalysisResult модель данных для отображения
 */
@Composable
fun AnalysisResultView(result: AnalysisResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF4A4A4A))
            .padding(16.dp)
    ) {
        Text(
            text = "Результат анализа:",
            fontSize = 18.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val diagnosisColor = if (result.diagnosis == "parasitized") Color(0xFFFF6B6B) else Color(0xFF4CAF50)
        Text(
            text = "Диагноз: ${getRussianDiagnosis(result.diagnosis)}",
            fontSize = 16.sp,
            color = diagnosisColor,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = "Уверенность: ${(result.confidence * 100).toInt()}%",
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = "Время обработки: ${result.processingTime} сек",
            fontSize = 12.sp,
            color = Color.LightGray,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = "Модель: ${result.modelUsed}",
            fontSize = 12.sp,
            color = Color.LightGray
        )
    }
}
/**
 * Компонент отображения ошибок с возможностью закрытия
 *
 * Отображает сообщения об ошибках в красном контейнере с кнопкой закрытия.
 * Используется для показа сетевых ошибок и ошибок валидации.
 *
 * @param message текст ошибки для отображения
 * @param onDismiss callback для скрытия компонента ошибки
 */
@Composable
fun ErrorMessage(message: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFD32F2F))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Ошибка:",
                fontSize = 14.sp,
                color = Color.White
            )
            Text(
                text = message,
                fontSize = 12.sp,
                color = Color.White,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Button(
            onClick = onDismiss,
            modifier = Modifier.height(36.dp),
            colors = androidx.compose.material.ButtonDefaults.buttonColors(
                backgroundColor = Color.White
            )
        ) {
            Text("✕", color = Color.Red, fontSize = 14.sp)
        }
    }
}
/**
 * Компонент отображения информации о выбранном файле
 *
 * Показывает имя файла, путь и формат с возможностью удаления выбора.
 *
 * @param filePath полный путь к выбранному файлу
 * @param onRemove callback для удаления выбранного файла
 */
@Composable
fun FileItem(filePath: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF7A7A7A))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Выбранный файл:",
                fontSize = 14.sp,
                color = Color.White
            )
            Text(
                text = getFileName(filePath),
                fontSize = 12.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Формат: ${filePath.substringAfterLast(".", "").uppercase()}",
                fontSize = 10.sp,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Button(
            onClick = onRemove,
            modifier = Modifier.height(36.dp)
        ) {
            Text("✕", color = Color.Black, fontSize = 14.sp)
        }
    }
}
/**
 * Преобразует диагноз в русскоязычный вариант для UI
 *
 * @param englishDiagnosis диагноз на английском от ML модели
 * @return локализованный диагноз для отображения пользователю
 */
private fun getRussianDiagnosis(englishDiagnosis: String): String {
    return when (englishDiagnosis) {
        "parasitized" -> "Заражено"
        "uninfected" -> "Не заражено"
        "error" -> "Ошибка"
        else -> englishDiagnosis
    }
}
/**
 * Открывает системный диалог выбора файла для загрузки изображений
 *
 * @param onFileSelected callback при успешном выборе файла
 * @param onError callback при ошибке выбора файла
 */
private fun openFileDialog(
    onFileSelected: (String) -> Unit,
    onError: (String) -> Unit
) {
    val fileDialog = FileDialog(null as Frame?, "Выберите изображение (PNG, JPEG)")
    fileDialog.mode = FileDialog.LOAD
    fileDialog.isMultipleMode = false

    fileDialog.isVisible = true

    val file = fileDialog.file
    val directory = fileDialog.directory
    fileDialog.dispose()
    if (file != null && directory != null) {
        val filePath = "$directory$file"
        if (isSupportedFormat(filePath)) {
            onFileSelected(filePath)
        } else {
            onError("Неподдерживаемый формат файла. Используйте PNG или JPEG.")
        }
    }
}
/**
 * Проверяет поддержку формата файла для анализа
 *
 * @param filePath путь к файлу для проверки
 * @return true если формат поддерживается (PNG, JPG, JPEG)
 */
private fun isSupportedFormat(filePath: String): Boolean {
    val extension = filePath.substringAfterLast(".", "").lowercase()
    return extension == "png" || extension == "jpg" || extension == "jpeg"
}
/**
 * Извлекает имя файла из полного пути
 *
 * @param filePath полный путь к файлу
 * @return только имя файла без пути
 */
private fun getFileName(filePath: String): String {
    return filePath.substringAfterLast("\\").substringAfterLast("/")
}