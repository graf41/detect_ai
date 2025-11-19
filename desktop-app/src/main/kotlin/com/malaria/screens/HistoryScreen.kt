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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.malaria.data.AnalysisRecord
import com.malaria.repository.AnalysisRepository

@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
) {
    var selectedFilter by remember { mutableStateOf<String?>(null) }
    var analyses by remember { mutableStateOf<List<AnalysisRecord>>(emptyList()) }
    val analysisRepository = remember { AnalysisRepository() }

    // Загружаем данные при открытии экрана и при изменении фильтра
    LaunchedEffect(selectedFilter) {
        analyses = if (selectedFilter != null) {
            val allAnalyses = analysisRepository.getAllAnalyses()
            allAnalyses.filter { analysis ->
                when (selectedFilter) {
                    "positive" -> analysis.diagnosis == "parasitized"
                    "negative" -> analysis.diagnosis == "uninfected"
                    else -> true
                }
            }
        } else {
            analysisRepository.getAllAnalyses()
        }
    }

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
                text = "Положительный 🦠",
                isSelected = selectedFilter == "positive",
                onClick = {
                    selectedFilter = if (selectedFilter == "positive") null else "positive"
                }
            )

            FilterCheckbox(
                text = "Отрицательный 😍",
                isSelected = selectedFilter == "negative",
                onClick = {
                    selectedFilter = if (selectedFilter == "negative") null else "negative"
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = when {
                selectedFilter == null -> "Все анализы (${analyses.size})"
                selectedFilter == "positive" -> "Положительные анализы (${analyses.size})"
                else -> "Отрицательные анализы (${analyses.size})"
            },
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (analyses.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(analyses) { analysis ->
                    AnalysisHistoryItem(analysis = analysis)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        } else {
            Text(
                text = "Нет сохраненных анализов",
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Вернуться на главную", color = Color.White)
        }
    }
}

@Composable
fun AnalysisHistoryItem(analysis: AnalysisRecord) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF7A7A7A))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = analysis.fileName,
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Результат: ${analysis.getRussianDiagnosis()}",
                fontSize = 14.sp,
                color = if (analysis.diagnosis == "parasitized") Color(0xFFFF6B6B) else Color(0xFF4CAF50),
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = "Уверенность: ${(analysis.confidence * 100).toInt()}%",
                fontSize = 12.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = "Дата: ${analysis.getFormattedDate()}",
                fontSize = 10.sp,
                color = Color.LightGray
            )
        }
    }
}