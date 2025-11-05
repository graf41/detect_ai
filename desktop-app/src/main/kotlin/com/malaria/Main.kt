package com.malaria

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.malaria.screens.MainScreen
import com.malaria.screens.AnalyzeScreen
import com.malaria.screens.HistoryScreen
import com.malaria.screens.AboutScreen
import com.malaria.screens.DatePickerDialog

fun main() = application {
    var currentScreen by remember { mutableStateOf("main") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

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
            "history" -> HistoryScreen(
                onBackClick = { currentScreen = "main" },
                onStartDateClick = { showStartDatePicker = true },
                onEndDateClick = { showEndDatePicker = true }
            )
            "about" -> AboutScreen(onBackClick = { currentScreen = "main" })
        }

        if (showStartDatePicker) {
            DatePickerDialog(
                onDateSelected = {
                    showStartDatePicker = false
                },
                onCancel = { showStartDatePicker = false },
                title = "Выбор начальной даты"
            )
        }

        if (showEndDatePicker) {
            DatePickerDialog(
                onDateSelected = {
                    showEndDatePicker = false
                },
                onCancel = { showEndDatePicker = false },
                title = "Выбор конечной даты"
            )
        }
    }
}