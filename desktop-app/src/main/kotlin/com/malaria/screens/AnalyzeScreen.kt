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
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                openFileDialog { filePath ->
                    if (isSupportedFormat(filePath)) {
                        selectedFile = filePath
                    } else {
                        errorMessage = "Неподдерживаемый формат файла. Используйте PNG или JPEG."
                    }
                }
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
                }
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

@Composable
fun ErrorMessage(message: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFD32F2F)) // Красный фон для ошибки
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

    Spacer(modifier = Modifier.height(8.dp))
}

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
                text = "Формат: ${getFileExtension(filePath).uppercase()}",
                fontSize = 10.sp,
                color = Color(0xFF4CAF50), // Зеленый цвет для правильного формата
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

private fun getFileName(filePath: String): String {
    return filePath.substringAfterLast("\\").substringAfterLast("/")
}


private fun getFileExtension(filePath: String): String {
    return filePath.substringAfterLast(".", "").lowercase()
}


private fun isSupportedFormat(filePath: String): Boolean {
    val extension = getFileExtension(filePath)
    return extension == "png" || extension == "jpg" || extension == "jpeg"
}

private fun openFileDialog(onFileSelected: (String) -> Unit) {
    val fileDialog = FileDialog(null as Frame?, "Выберите изображение (PNG, JPEG)")
    fileDialog.mode = FileDialog.LOAD
    fileDialog.isMultipleMode = false

    fileDialog.isVisible = true

    val file = fileDialog.file
    val directory = fileDialog.directory

    if (file != null && directory != null) {
        val filePath = "$directory$file"
        onFileSelected(filePath)
    }
}