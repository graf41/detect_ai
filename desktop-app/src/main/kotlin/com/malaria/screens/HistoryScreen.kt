package com.malaria.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malaria.components.FilterCheckbox
import java.text.SimpleDateFormat
import java.util.*

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

@Composable
fun DatePickerDialog(
    onDateSelected: (String) -> Unit,
    onCancel: () -> Unit,
    title: String
) {
    var selectedDay by remember { mutableStateOf(1) }
    var selectedMonth by remember { mutableStateOf(1) }
    var selectedYear by remember { mutableStateOf(2024) }

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

            // Выбор дня
            Text("День:", color = Color.Black, fontSize = 16.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { if (selectedDay > 1) selectedDay-- }) {
                    Text("<")
                }
                Text("$selectedDay", fontSize = 18.sp, color = Color.Black)
                Button(onClick = { if (selectedDay < 31) selectedDay++ }) {
                    Text(">")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Выбор месяца
            Text("Месяц:", color = Color.Black, fontSize = 16.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { if (selectedMonth > 1) selectedMonth-- }) {
                    Text("<")
                }
                Text(
                    text = when (selectedMonth) {
                        1 -> "Январь"
                        2 -> "Февраль"
                        3 -> "Март"
                        4 -> "Апрель"
                        5 -> "Май"
                        6 -> "Июнь"
                        7 -> "Июль"
                        8 -> "Август"
                        9 -> "Сентябрь"
                        10 -> "Октябрь"
                        11 -> "Ноябрь"
                        12 -> "Декабрь"
                        else -> "Январь"
                    },
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Button(onClick = { if (selectedMonth < 12) selectedMonth++ }) {
                    Text(">")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Выбор года
            Text("Год:", color = Color.Black, fontSize = 16.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { selectedYear-- }) {
                    Text("<")
                }
                Text("$selectedYear", fontSize = 18.sp, color = Color.Black)
                Button(onClick = { selectedYear++ }) {
                    Text(">")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Выбрано: ${String.format("%02d", selectedDay)}.${String.format("%02d", selectedMonth)}.$selectedYear",
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onCancel) {
                    Text("Отмена")
                }

                Button(onClick = {
                    val newDate = "${String.format("%02d", selectedDay)}.${String.format("%02d", selectedMonth)}.$selectedYear"
                    onDateSelected(newDate)
                }) {
                    Text("Подтвердить")
                }
            }
        }
    }
}