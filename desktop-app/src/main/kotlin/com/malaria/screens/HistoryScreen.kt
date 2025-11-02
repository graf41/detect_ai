package com.malaria.screens

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
import com.malaria.components.FilterCheckbox

@Composable
fun HistoryScreen(onBackClick: () -> Unit) {
    var selectedFilter by remember { mutableStateOf<String?>(null) }

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

        Spacer(modifier = Modifier.height(32.dp))

        // Кнопки фильтрации
        Text(
            text = "Фильтр по результатам:",
            fontSize = 18.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Поле "Положительно"
            FilterCheckbox(
                text = "Положительно",
                isSelected = selectedFilter == "positive",
                onClick = {
                    selectedFilter = if (selectedFilter == "positive") null else "positive"
                }
            )

            // Поле "Отрицательно"
            FilterCheckbox(
                text = "Отрицательно",
                isSelected = selectedFilter == "negative",
                onClick = {
                    selectedFilter = if (selectedFilter == "negative") null else "negative"
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Текст с информацией о выбранном фильтре
        Text(
            text = if (selectedFilter == null) {
                "Все анализы"
            } else if (selectedFilter == "positive") {
                "Показаны анализы с положительным результатом"
            } else {
                "Показаны анализы с отрицательным результатом"
            },
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
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