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
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

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
                onEndDateClick = { showEndDatePicker = true },
                startDate = startDate,
                endDate = endDate
            )
            "about" -> AboutScreen(onBackClick = { currentScreen = "main" })
        }

        if (showStartDatePicker) {
            DatePickerDialog(
                currentDate = startDate,
                onDateSelected = { newDate ->
                    startDate = newDate
                    showStartDatePicker = false
                },
                onCancel = { showStartDatePicker = false },
                title = "Выбор начальной даты"
            )
        }

        if (showEndDatePicker) {
            DatePickerDialog(
                currentDate = endDate,
                onDateSelected = { newDate ->
                    endDate = newDate
                    showEndDatePicker = false
                },
                onCancel = { showEndDatePicker = false },
                title = "Выбор конечной даты",
                minDate = startDate
            )
        }
    }
}