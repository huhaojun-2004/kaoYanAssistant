package com.example.kaoyanassistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kaoyanassistant.ui.components.EmptyStateCard
import com.example.kaoyanassistant.ui.components.HeroCard
import com.example.kaoyanassistant.ui.components.MetricChip
import com.example.kaoyanassistant.ui.model.AppUiState
import com.example.kaoyanassistant.ui.model.buildDaySubjectSummary
import com.example.kaoyanassistant.ui.model.displaySubjectName
import com.example.kaoyanassistant.ui.model.totalStudyForDate
import com.example.kaoyanassistant.ui.theme.Cloud
import com.example.kaoyanassistant.ui.theme.Paper
import com.example.kaoyanassistant.ui.theme.Pine
import com.example.kaoyanassistant.util.DateTimeUtils
import java.time.format.DateTimeFormatter

private val fullDateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日")

@Composable
fun DailyDetailScreen(
    date: String,
    uiState: AppUiState,
) {
    val summaries = buildDaySubjectSummary(uiState.subjects, uiState.sessions, date)
    val dayRecord = uiState.dayRecordMap[date]
    val total = totalStudyForDate(uiState.sessions, date)

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            HeroCard(
                title = DateTimeUtils.parseDate(date).format(fullDateFormatter),
                subtitle = "",
                accent = Pine,
                compact = true,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MetricChip(
                        label = "学习总量",
                        value = formatDailyDetailDuration(total),
                        modifier = Modifier.weight(1f),
                        borderColor = Pine.copy(alpha = 0.16f),
                    )
                    MetricChip(
                        label = "当天状态",
                        value = when {
                            dayRecord?.isRestDay == true -> "休息日"
                            dayRecord?.isCheckIn == true -> "已打卡"
                            else -> "未打卡"
                        },
                        modifier = Modifier.weight(1f),
                        borderColor = Pine.copy(alpha = 0.16f),
                    )
                }
            }
        }

        item {
            Text(
                text = "当日学习分布",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        if (summaries.isEmpty()) {
            item {
                EmptyStateCard(
                    text = when {
                        dayRecord?.isRestDay == true -> "这一天已设为休息日。"
                        else -> "这一天还没有学习记录。"
                    },
                )
            }
        } else {
            item {
                DailyHorizontalBarChart(
                    items = summaries.map { summary ->
                        DailyBarItem(
                            label = displaySubjectName(summary.subject, uiState.subjectMap),
                            value = summary.durationMillis,
                            color = Color(summary.subject.colorValue),
                        )
                    },
                )
            }
        }
    }
}

private data class DailyBarItem(
    val label: String,
    val value: Long,
    val color: Color,
)

@Composable
private fun DailyHorizontalBarChart(
    items: List<DailyBarItem>,
) {
    val maxValue = items.maxOfOrNull { it.value }?.coerceAtLeast(1L) ?: 1L

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Paper),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items.forEach { item ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = formatDailyDetailDuration(item.value),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .background(Cloud, RoundedCornerShape(999.dp)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((item.value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .background(item.color, RoundedCornerShape(999.dp)),
                        )
                    }
                }
            }
        }
    }
}

private fun formatDailyDetailDuration(durationMillis: Long): String {
    val totalMinutes = (durationMillis / 60_000).coerceAtLeast(0L)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) {
        "${hours}h ${minutes}m"
    } else {
        "${minutes}m"
    }
}
