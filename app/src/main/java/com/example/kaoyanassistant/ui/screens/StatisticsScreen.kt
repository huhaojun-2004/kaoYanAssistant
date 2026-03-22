package com.example.kaoyanassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kaoyanassistant.ui.components.PieSliceEntry
import com.example.kaoyanassistant.ui.components.StudyPieChart
import com.example.kaoyanassistant.ui.components.StudyTimeClockSummary
import com.example.kaoyanassistant.ui.components.TaskDateSelector
import com.example.kaoyanassistant.ui.components.TaskDateStepMode
import com.example.kaoyanassistant.ui.model.AppUiState
import com.example.kaoyanassistant.ui.model.buildDaySubjectSummary
import com.example.kaoyanassistant.ui.model.buildMonthCategorySummary
import com.example.kaoyanassistant.ui.model.buildTwentyFourHourClockStudySegments
import com.example.kaoyanassistant.ui.model.buildWeekCategorySummary
import com.example.kaoyanassistant.ui.theme.Pine
import com.example.kaoyanassistant.util.DateTimeUtils
import com.example.kaoyanassistant.util.formatDurationText
import java.time.YearMonth

private enum class StatisticsMode { DAY, WEEK, MONTH }

@Composable
fun StatisticsScreen(
    uiState: AppUiState,
    onDeleteStudyTime: (Long, Long, Long) -> Unit,
) {
    val todayKey = remember { DateTimeUtils.todayKey() }
    var selectedDate by remember { mutableStateOf(todayKey) }
    var mode by remember { mutableStateOf(StatisticsMode.DAY) }
    var deletingEntry by remember { mutableStateOf<PieSliceEntry?>(null) }
    val selectedLocalDate = remember(selectedDate) { DateTimeUtils.parseDate(selectedDate) }
    val month = remember(selectedDate) { YearMonth.from(selectedLocalDate) }

    val summaries = remember(uiState.sessions, uiState.subjects, selectedDate, mode) {
        when (mode) {
            StatisticsMode.DAY -> buildDaySubjectSummary(uiState.subjects, uiState.sessions, selectedDate)
            StatisticsMode.WEEK -> buildWeekCategorySummary(uiState.subjects, uiState.sessions, selectedLocalDate)
            StatisticsMode.MONTH -> buildMonthCategorySummary(uiState.subjects, uiState.sessions, month)
        }.filter { it.durationMillis > 0L }
    }
    val pieEntries = remember(summaries) {
        summaries.map { summary ->
            PieSliceEntry(
                subjectId = summary.subject.id,
                label = summary.subject.name,
                value = summary.durationMillis / 3_600_000f,
                color = Color(summary.subject.colorValue),
                supportingText = formatDurationText(summary.durationMillis),
                calloutText = formatPieCalloutDuration(summary.durationMillis),
            )
        }
    }

    val selectorLabel = when (mode) {
        StatisticsMode.DAY -> "统计日期"
        StatisticsMode.WEEK -> "周起点"
        StatisticsMode.MONTH -> "统计月份"
    }
    val selectorText = when (mode) {
        StatisticsMode.DAY -> DateTimeUtils.dateLabel(selectedDate)
        StatisticsMode.WEEK -> {
            val end = selectedLocalDate.plusDays(6)
            "${DateTimeUtils.dateLabel(selectedDate)} - ${DateTimeUtils.dateLabel(end.toString())}"
        }
        StatisticsMode.MONTH -> DateTimeUtils.monthLabel(month)
    }
    val stepMode = when (mode) {
        StatisticsMode.DAY -> TaskDateStepMode.DAY
        StatisticsMode.WEEK -> TaskDateStepMode.WEEK
        StatisticsMode.MONTH -> TaskDateStepMode.MONTH
    }
    val emptyText = when (mode) {
        StatisticsMode.DAY -> "${DateTimeUtils.dateLabel(selectedDate)} 还没有学习数据"
        StatisticsMode.WEEK -> "这一周还没有学习数据"
        StatisticsMode.MONTH -> "${DateTimeUtils.monthLabel(month)} 还没有学习数据"
    }
    val rangeStart = when (mode) {
        StatisticsMode.DAY -> DateTimeUtils.startOfDayMillis(selectedDate)
        StatisticsMode.WEEK -> selectedLocalDate.atStartOfDay(DateTimeUtils.zoneId).toInstant().toEpochMilli()
        StatisticsMode.MONTH -> month.atDay(1).atStartOfDay(DateTimeUtils.zoneId).toInstant().toEpochMilli()
    }
    val rangeEnd = when (mode) {
        StatisticsMode.DAY -> DateTimeUtils.endOfDayMillis(selectedDate)
        StatisticsMode.WEEK -> selectedLocalDate.plusDays(7).atStartOfDay(DateTimeUtils.zoneId).toInstant().toEpochMilli()
        StatisticsMode.MONTH -> month.plusMonths(1).atDay(1).atStartOfDay(DateTimeUtils.zoneId).toInstant().toEpochMilli()
    }
    val deleteScopeText = when (mode) {
        StatisticsMode.DAY -> DateTimeUtils.dateLabel(selectedDate)
        StatisticsMode.WEEK -> "当前这一周"
        StatisticsMode.MONTH -> DateTimeUtils.monthLabel(month)
    }
    val clockSegments = remember(uiState.sessions, rangeStart, rangeEnd) {
        buildTwentyFourHourClockStudySegments(
            subjects = uiState.subjects,
            sessions = uiState.sessions,
            rangeStart = rangeStart,
            rangeEnd = rangeEnd,
        )
    }
    val clockSubtitle = when (mode) {
        StatisticsMode.DAY -> "24 小时钟面；弧线表示你当天学过的时间段。"
        StatisticsMode.WEEK -> "24 小时钟面；会把本周出现过的学习时段折叠到同一圈里。"
        StatisticsMode.MONTH -> "24 小时钟面；会把本月出现过的学习时段折叠到同一圈里。"
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    StatisticsMode.DAY to "日统计",
                    StatisticsMode.WEEK to "周统计",
                    StatisticsMode.MONTH to "月统计",
                ).forEach { (targetMode, label) ->
                    Box(
                        modifier = Modifier.weight(1f),
                    ) {
                        FilterChip(
                            selected = mode == targetMode,
                            onClick = { mode = targetMode },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Pine.copy(alpha = 0.14f),
                                selectedLabelColor = Pine,
                            ),
                            label = {
                                Text(
                                    text = label,
                                    fontWeight = if (mode == targetMode) FontWeight.SemiBold else FontWeight.Medium,
                                )
                            },
                        )
                    }
                }
            }
        }

        item {
            TaskDateSelector(
                selectedDate = selectedDate,
                onDateChange = { selectedDate = it },
                label = selectorLabel,
                displayText = selectorText,
                stepMode = stepMode,
                pickerEnabled = mode != StatisticsMode.MONTH,
            )
        }

        item {
            StudyPieChart(
                entries = pieEntries,
                emptyText = emptyText,
                onDeleteEntry = { entry -> deletingEntry = entry },
                footer = {
                    StudyTimeClockSummary(
                        segments = clockSegments,
                        legendEntries = pieEntries,
                        title = "学习时段钟面",
                        subtitle = clockSubtitle,
                    )
                },
            )
        }
    }

    deletingEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { deletingEntry = null },
            title = { Text("删除学习时间") },
            text = {
                Text("确认删除 $deleteScopeText 内 ${entry.label} 的学习时间吗？这会直接修改统计对应的学习记录。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteStudyTime(entry.subjectId, rangeStart, rangeEnd)
                        deletingEntry = null
                    },
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingEntry = null }) {
                    Text("取消")
                }
            },
        )
    }
}

private fun formatPieCalloutDuration(durationMillis: Long): String {
    val totalMinutes = (durationMillis / 60_000).coerceAtLeast(0L)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h${minutes}m" else "${minutes}m"
}
