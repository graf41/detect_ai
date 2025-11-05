package com.malaria.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
    onEndDateClick: () -> Unit = {},
    startDate: String = "",
    endDate: String = ""
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
                Text(
                    text = if (startDate.isEmpty()) "Выбрать" else startDate,
                    color = Color.Black,
                    fontSize = 14.sp
                )
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
                Text(
                    text = if (endDate.isEmpty()) "Выбрать" else endDate,
                    color = Color.Black,
                    fontSize = 14.sp
                )
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
                "Все анализы с ${if (startDate.isEmpty()) "..." else startDate} по ${if (endDate.isEmpty()) "..." else endDate}"
            } else if (selectedFilter == "positive") {
                "Положительные анализы с ${if (startDate.isEmpty()) "..." else startDate} по ${if (endDate.isEmpty()) "..." else endDate}"
            } else {
                "Отрицательные анализы с ${if (startDate.isEmpty()) "..." else startDate} по ${if (endDate.isEmpty()) "..." else endDate}"
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
    currentDate: String,
    onDateSelected: (String) -> Unit,
    onCancel: () -> Unit,
    title: String,
    minDate: String? = null
) {
    var selectedDay by remember { mutableStateOf(1) }
    var selectedMonth by remember { mutableStateOf(1) }
    var selectedYear by remember { mutableStateOf(2024) }

    val minDay = remember(minDate) {
        minDate?.split(".")?.get(0)?.toIntOrNull() ?: 1
    }
    val minMonth = remember(minDate) {
        minDate?.split(".")?.get(1)?.toIntOrNull() ?: 1
    }
    val minYear = remember(minDate) {
        minDate?.split(".")?.get(2)?.toIntOrNull() ?: 1900
    }

    fun canDecreaseDay(): Boolean {
        if (selectedYear > minYear) return true
        if (selectedYear == minYear && selectedMonth > minMonth) return true
        if (selectedYear == minYear && selectedMonth == minMonth && selectedDay > minDay) return true
        return false
    }

    fun getDaysInMonth(month: Int, year: Int): Int {
        return when (month) {
            1 -> 31
            2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
            3 -> 31
            4 -> 30
            5 -> 31
            6 -> 30
            7 -> 31
            8 -> 31
            9 -> 30
            10 -> 31
            11 -> 30
            12 -> 31
            else -> 31
        }
    }

    fun canIncreaseDay(): Boolean {
        return selectedDay < getDaysInMonth(selectedMonth, selectedYear)
    }

    fun canDecreaseMonth(): Boolean {
        if (selectedYear > minYear) return true
        if (selectedYear == minYear && selectedMonth > minMonth) return true
        return false
    }

    fun canIncreaseMonth(): Boolean = true

    fun canDecreaseYear(): Boolean {
        return selectedYear > minYear
    }

    fun canIncreaseYear(): Boolean = true

    LaunchedEffect(currentDate, minDate) {
        if (currentDate.isNotEmpty()) {
            val parts = currentDate.split(".")
            if (parts.size == 3) {
                selectedDay = parts[0].toIntOrNull() ?: 1
                selectedMonth = parts[1].toIntOrNull() ?: 1
                selectedYear = parts[2].toIntOrNull() ?: 2024
            }
        } else if (minDate != null) {
            val parts = minDate.split(".")
            if (parts.size == 3) {
                selectedDay = parts[0].toIntOrNull() ?: 1
                selectedMonth = parts[1].toIntOrNull() ?: 1
                selectedYear = parts[2].toIntOrNull() ?: 2024
            }
        } else {

            val today = Calendar.getInstance()
            selectedDay = today.get(Calendar.DAY_OF_MONTH)
            selectedMonth = today.get(Calendar.MONTH) + 1
            selectedYear = today.get(Calendar.YEAR)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onCancel() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .background(Color.White)
                .padding(24.dp)
                .clickable { /* Не закрывать при клике внутри */ },
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
                Button(
                    onClick = { if (canDecreaseDay()) selectedDay-- },
                    modifier = Modifier.width(60.dp),
                    enabled = canDecreaseDay()
                ) {
                    Text("<")
                }
                Text(
                    text = "$selectedDay",
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier
                        .width(80.dp)
                        .background(Color.LightGray)
                        .padding(8.dp),
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = { if (canIncreaseDay()) selectedDay++ },
                    modifier = Modifier.width(60.dp),
                    enabled = canIncreaseDay()
                ) {
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
                Button(
                    onClick = { if (canDecreaseMonth()) selectedMonth-- },
                    modifier = Modifier.width(60.dp),
                    enabled = canDecreaseMonth()
                ) {
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
                    color = Color.Black,
                    modifier = Modifier
                        .width(120.dp)
                        .background(Color.LightGray)
                        .padding(8.dp),
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = { if (canIncreaseMonth()) selectedMonth++ },
                    modifier = Modifier.width(60.dp),
                    enabled = canIncreaseMonth()
                ) {
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
                Button(
                    onClick = { if (canDecreaseYear()) selectedYear-- },
                    modifier = Modifier.width(60.dp),
                    enabled = canDecreaseYear()
                ) {
                    Text("<")
                }
                Text(
                    text = "$selectedYear",
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier
                        .width(80.dp)
                        .background(Color.LightGray)
                        .padding(8.dp),
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = { if (canIncreaseYear()) selectedYear++ },
                    modifier = Modifier.width(60.dp),
                    enabled = canIncreaseYear()
                ) {
                    Text(">")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Выбрано: ${String.format("%02d", selectedDay)}.${String.format("%02d", selectedMonth)}.$selectedYear",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier.width(140.dp)
                ) {
                    Text("Отмена", fontSize = 14.sp)
                }

                Button(
                    onClick = {
                        val newDate = "${String.format("%02d", selectedDay)}.${String.format("%02d", selectedMonth)}.$selectedYear"
                        onDateSelected(newDate)
                    },
                    modifier = Modifier.width(140.dp)
                ) {
                    Text("Подтвердить", fontSize = 14.sp)
                }
            }
        }
    }
}