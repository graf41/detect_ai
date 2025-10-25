package com.malaria

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    var currentScreen by remember { mutableStateOf("main") }

    Window(
        onCloseRequest = ::exitApplication,
        title = "AI Malaria Detection"
    ) {
        when (currentScreen) {
            "main" -> MainScreen(
                onAnalyzeClick = { currentScreen = "analyze" },
                onHistoryClick = { currentScreen = "history" },
                onAboutClick = { currentScreen = "about" }
            )
            "analyze" -> AnalyzeScreen(onBackClick = { currentScreen = "main" })
            "history" -> HistoryScreen(onBackClick = { currentScreen = "main" })
            "about" -> AboutScreen(onBackClick = { currentScreen = "main" })
        }
    }
}

@Composable
fun MainScreen(
    onAnalyzeClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF929292))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "AI Malaria Detection 🦠",
            fontSize = 40.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(60.dp))

        Button(
            onClick = onAnalyzeClick,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(60.dp)
        ) {
            Text("Анализировать изображение", color = Color.Green)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onHistoryClick,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(60.dp)
        ) {
            Text("История анализов", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAboutClick,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(60.dp)
        ) {
            Text("О приложении", color = Color.Black)
        }
    }
}

@Composable
fun AnalyzeScreen(onBackClick: () -> Unit) {
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
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Выбрать файл PNG", color = Color.Black)
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
fun HistoryScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF929292))
            .padding(32.dp)
    ) {
        Text(
            text = "История анализов",
            fontSize = 28.sp,
            color = Color.White
        )

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
fun AboutScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF929292))
            .padding(32.dp)
    ) {
        Text(
            text = "О приложении",
            fontSize = 28.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Вернуться на главную", color = Color.Black)
        }
    }
}