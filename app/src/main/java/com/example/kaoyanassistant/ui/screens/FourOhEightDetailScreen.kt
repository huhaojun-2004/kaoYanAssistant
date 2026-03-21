package com.example.kaoyanassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.kaoyanassistant.ui.components.ChartBarEntry
import com.example.kaoyanassistant.ui.components.HeroCard
import com.example.kaoyanassistant.ui.components.MetricChip
import com.example.kaoyanassistant.ui.components.StudyBarChart
import com.example.kaoyanassistant.ui.model.AppUiState
import com.example.kaoyanassistant.ui.model.buildFourOhEightDetailSummary
import com.example.kaoyanassistant.ui.model.buildWeekCategorySummary
import com.example.kaoyanassistant.ui.model.startOfCurrentWeek
import com.example.kaoyanassistant.util.DateTimeUtils
import com.example.kaoyanassistant.util.formatDurationText
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FourOhEightDetailScreen(
    weekStart: String,
    uiState: AppUiState,
    onOpenDailyDetail: (String) -> Unit,
) {
    val week = runCatching { LocalDate.parse(weekStart) }.getOrElse { startOfCurrentWeek() }
    val details = buildFourOhEightDetailSummary(uiState.subjects, uiState.sessions, week)
    val total = details.sumOf { it.durationMillis }
    val overall408 = buildWeekCategorySummary(uiState.subjects, uiState.sessions, week)
        .firstOrNull { it.subject.name == "408" }
        ?.durationMillis
        ?: 0L
    val unsplitTotal = (overall408 - total).coerceAtLeast(0L)

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HeroCard(
                title = "408 科目明细",
                subtitle = "${DateTimeUtils.dateLabel(week.toString())} - ${DateTimeUtils.dateLabel(week.plusDays(6).toString())}",
            ) {
                MetricChip(label = "周总时长", value = formatDurationText(total))
            }
        }

        item {
            StudyBarChart(
                entries = details.map {
                    ChartBarEntry(
                        label = it.subject.name,
                        value = it.durationMillis / 3_600_000f,
                        color = Color(it.subject.colorValue),
                    )
                },
            )
        }

        items(details, key = { it.subject.id }) { detail ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(detail.subject.colorValue).copy(alpha = 0.12f),
                ),
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(detail.subject.name, style = MaterialTheme.typography.titleMedium)
                    Text(formatDurationText(detail.durationMillis), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        item {
            Text(
                text = "如需查看某一天的具体时间段，可继续进入每日详情。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            )
        }

        if (unsplitTotal > 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Text(
                        text = "另有 ${formatDurationText(unsplitTotal)} 直接记在 408 总科目下，无法自动拆分到四门子科。",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                (0..6).forEach { offset ->
                    val date = week.plusDays(offset.toLong())
                    OutlinedButton(onClick = { onOpenDailyDetail(date.toString()) }) {
                        Text("${date.monthValue}/${date.dayOfMonth}")
                    }
                }
            }
        }
    }
}
