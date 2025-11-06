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

        if (selectedFile != null) {
            Text(
                text = "Выбран файл: ${getFileName(selectedFile!!)}",
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
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

private fun getFileName(filePath: String): String {
    return filePath.substringAfterLast("\\").substringAfterLast("/")
}

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