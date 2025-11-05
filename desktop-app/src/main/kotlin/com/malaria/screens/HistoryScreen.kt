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
fun HistoryScreen(
    onBackClick: () -> Unit,
    onStartDateClick: () -> Unit = {},
    onEndDateClick: () -> Unit = {}
) {
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

        Text(
            text = "Диапазон дат:",
            fontSize = 18.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Искать от",
                color = Color.White,
                fontSize = 16.sp
            )

            Button(
                onClick = onStartDateClick,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text("Выбрать", color = Color.Black, fontSize = 14.sp)
            }

            Text(
                text = "до",
                color = Color.White,
                fontSize = 16.sp
            )

            Button(
                onClick = onEndDateClick,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text("Выбрать", color = Color.Black, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

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
            FilterCheckbox(
                text = "Положительный",
                isSelected = selectedFilter == "positive",
                onClick = {
                    selectedFilter = if (selectedFilter == "positive") null else "positive"
                }
            )

            FilterCheckbox(
                text = "Отрицательный",
                isSelected = selectedFilter == "negative",
                onClick = {
                    selectedFilter = if (selectedFilter == "negative") null else "negative"
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

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

// Базовый DatePicker диалог (простая заглушка)
@Composable
fun DatePickerDialog(
    onDateSelected: (String) -> Unit,
    onCancel: () -> Unit,
    title: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Text(
                text = "",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onCancel) {
                    Text("Отмена")
                }

                Button(onClick = { onDateSelected("01.01.2024") }) {
                    Text("Подтвердить")
                }
            }
        }
    }
}