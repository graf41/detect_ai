package com.malaria.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.FileDialog
import java.awt.Frame

@Composable
fun AnalyzeScreen(onBackClick: () -> Unit) {
    var selectedFile by remember { mutableStateOf<String?>(null) }

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

        // Кнопка загрузки файла
        Button(
            onClick = {
                openFileDialog { filePath ->
                    selectedFile = filePath
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Загрузить изображение", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Отображение выбранного файла с кнопкой удаления
        if (selectedFile != null) {
            FileItem(
                filePath = selectedFile!!,
                onRemove = { selectedFile = null }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка анализа (активна только когда файл выбран)
        Button(
            onClick = {
                // TODO: Добавить логику анализа изображения
                println("Анализируем файл: $selectedFile")
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedFile != null
        ) {
            Text("Проанализировать изображение", color = Color.Black)
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

// Компонент для отображения файла с кнопкой удаления
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
        }

        Button(
            onClick = onRemove,
            modifier = Modifier.height(36.dp)
        ) {
            Text("✕", color = Color.Black, fontSize = 14.sp)
        }
    }
}

// Функция для получения имени файла из полного пути
private fun getFileName(filePath: String): String {
    return filePath.substringAfterLast("\\").substringAfterLast("/")
}

// Функция для открытия диалога выбора файла
private fun openFileDialog(onFileSelected: (String) -> Unit) {
    val fileDialog = FileDialog(null as Frame?, "Выберите изображение")
    fileDialog.mode = FileDialog.LOAD
    fileDialog.isMultipleMode = false

    // Устанавливаем фильтр для изображений
    fileDialog.setFilenameFilter { dir, name ->
        name.endsWith(".png", ignoreCase = true) ||
                name.endsWith(".jpg", ignoreCase = true) ||
                name.endsWith(".jpeg", ignoreCase = true) ||
                name.endsWith(".bmp", ignoreCase = true)
    }

    fileDialog.isVisible = true

    val file = fileDialog.file
    val directory = fileDialog.directory

    if (file != null && directory != null) {
        val filePath = "$directory$file"
        onFileSelected(filePath)
    }
}