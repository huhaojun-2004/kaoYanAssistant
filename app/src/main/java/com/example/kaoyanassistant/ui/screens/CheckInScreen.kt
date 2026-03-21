package com.example.kaoyanassistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kaoyanassistant.ui.components.HeroCard
import com.example.kaoyanassistant.ui.components.MetricChip
import com.example.kaoyanassistant.ui.model.AppUiState
import com.example.kaoyanassistant.ui.model.buildCalendarMonth
import com.example.kaoyanassistant.ui.model.currentStreak
import com.example.kaoyanassistant.ui.model.totalStudyForDate
import com.example.kaoyanassistant.ui.model.totalStudyForMonth
import com.example.kaoyanassistant.ui.theme.Apricot
import com.example.kaoyanassistant.ui.theme.Danger
import com.example.kaoyanassistant.ui.theme.Paper
import com.example.kaoyanassistant.ui.theme.Pine
import com.example.kaoyanassistant.ui.theme.Sky
import com.example.kaoyanassistant.ui.theme.Wine
import com.example.kaoyanassistant.util.DateTimeUtils
import com.example.kaoyanassistant.util.formatDurationText
import java.time.YearMonth

@Composable
fun CheckInScreen(
    uiState: AppUiState,
    onToggleCheckInToday: (Boolean) -> Unit,
    onOpenDailyDetail: (String) -> Unit,
    onSetRestDay: (String, Boolean) -> Unit,
) {
    val today = DateTimeUtils.today()
    val todayKey = today.toString()
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val monthDays = buildCalendarMonth(currentMonth, uiState.sessions, uiState.dayRecords)
    val todayRecord = uiState.dayRecordMap[todayKey]
    val isTodayCheckedIn = todayRecord?.isCheckIn == true
    val isTodayRestDay = todayRecord?.isRestDay == true
    val todayStudy = totalStudyForDate(uiState.sessions, todayKey)
    val checkedAccent = Pine.copy(red = Pine.red * 0.92f, green = Pine.green * 0.92f, blue = Pine.blue * 0.92f)
    val missedAccent = Wine
    val accentColor = when {
        isTodayRestDay -> Sky
        isTodayCheckedIn -> checkedAccent
        else -> missedAccent
    }
    var showCancelConfirm by remember { mutableStateOf(false) }
    var selectedDateKey by remember { mutableStateOf<String?>(null) }
    val selectedDateRecord = selectedDateKey?.let(uiState.dayRecordMap::get)

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            HeroCard(
                title = "今日打卡",
                subtitle = "",
                accent = accentColor,
                compact = true,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MetricChip(
                        label = "连续天数",
                        value = "${currentStreak(uiState.dayRecords)}天",
                        modifier = Modifier.weight(1f),
                        borderColor = accentColor.copy(alpha = 0.16f),
                    )
                    MetricChip(
                        label = "本月学时",
                        value = formatDurationText(totalStudyForMonth(uiState.sessions, currentMonth)),
                        modifier = Modifier.weight(1f),
                        borderColor = accentColor.copy(alpha = 0.16f),
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Paper),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(172.dp)
                            .clip(CircleShape)
                            .background(color = accentColor.copy(alpha = 0.1f), shape = CircleShape)
                            .border(width = 10.dp, color = accentColor.copy(alpha = 0.2f), shape = CircleShape)
                            .let { baseModifier ->
                                if (isTodayRestDay) {
                                    baseModifier
                                } else {
                                    baseModifier.clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) {
                                        if (isTodayCheckedIn) {
                                            showCancelConfirm = true
                                        } else {
                                            onToggleCheckInToday(true)
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                                .background(
                                    color = if (isTodayCheckedIn) accentColor else Paper,
                                    shape = CircleShape,
                                )
                                .border(
                                    width = 1.dp,
                                    color = accentColor.copy(alpha = 0.26f),
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = when {
                                        isTodayRestDay -> "休息日"
                                        isTodayCheckedIn -> "已打卡"
                                        else -> "点击打卡"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    color = if (isTodayCheckedIn) MaterialTheme.colorScheme.onPrimary else accentColor,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = when {
                                        isTodayRestDay -> "今天无需打卡"
                                        isTodayCheckedIn -> "再次点击可取消"
                                        else -> DateTimeUtils.dateLabel(todayKey)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isTodayCheckedIn) {
                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.86f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    },
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MetricChip(
                            label = "今日学习",
                            value = formatDurationText(todayStudy),
                            modifier = Modifier.weight(1f),
                        )
                        MetricChip(
                            label = "今日状态",
                            value = when {
                                isTodayRestDay -> "休息日"
                                isTodayCheckedIn -> "已完成"
                                else -> "未打卡"
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    OutlinedButton(onClick = { onOpenDailyDetail(todayKey) }) {
                        Text("查看今日详情")
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Paper),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "本月打卡",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                            Icon(Icons.Outlined.ChevronLeft, contentDescription = "上个月")
                        }
                        Text(
                            text = DateTimeUtils.monthLabel(currentMonth),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                            Icon(Icons.Outlined.ChevronRight, contentDescription = "下个月")
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("一", "二", "三", "四", "五", "六", "日").forEach { label ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 1.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                                )
                            }
                        }
                    }

                    monthDays.chunked(7).forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            week.forEach { cell ->
                                val isToday = cell.date == today
                                val isChecked = cell.record?.isCheckIn == true
                                val isRestDay = cell.record?.isRestDay == true
                                val baseColor = when {
                                    !cell.inCurrentMonth -> Apricot.copy(alpha = 0.08f)
                                    isRestDay -> Sky.copy(alpha = if (isToday) 0.24f else 0.18f)
                                    isChecked -> checkedAccent.copy(alpha = if (isToday) 0.34f else 0.28f)
                                    isToday -> missedAccent.copy(alpha = 0.30f)
                                    else -> Apricot.copy(alpha = 0.18f)
                                }
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(0.92f)
                                        .clickable(enabled = cell.inCurrentMonth) {
                                            selectedDateKey = cell.date.toString()
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = baseColor,
                                    ),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            text = cell.date.dayOfMonth.toString(),
                                            modifier = Modifier.padding(top = 5.dp),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (cell.inCurrentMonth) {
                                                MaterialTheme.colorScheme.onSurface
                                            } else {
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                            },
                                        )
                                        Text(
                                            text = if (cell.inCurrentMonth) {
                                                formatCalendarStudyDuration(
                                                    durationMillis = cell.studyMillis,
                                                    isRestDay = isRestDay,
                                                )
                                            } else {
                                                ""
                                            },
                                            modifier = Modifier.padding(bottom = 5.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (cell.inCurrentMonth) {
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                                            } else {
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCancelConfirm) {
        AlertDialog(
            onDismissRequest = { showCancelConfirm = false },
            title = { Text("取消今天打卡") },
            text = { Text("确认要取消今天的打卡吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelConfirm = false
                        onToggleCheckInToday(false)
                    },
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirm = false }) {
                    Text("取消")
                }
            },
        )
    }

    selectedDateKey?.let { dateKey ->
        AlertDialog(
            onDismissRequest = { selectedDateKey = null },
            title = { Text(DateTimeUtils.dateLabel(dateKey)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onSetRestDay(dateKey, selectedDateRecord?.isRestDay != true)
                            selectedDateKey = null
                        },
                    ) {
                        Text(if (selectedDateRecord?.isRestDay == true) "取消休息日" else "设置休息日")
                    }
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            selectedDateKey = null
                            onOpenDailyDetail(dateKey)
                        },
                    ) {
                        Text("查看当天详情")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedDateKey = null }) {
                    Text("关闭")
                }
            },
        )
    }
}

private fun formatCalendarStudyDuration(
    durationMillis: Long,
    isRestDay: Boolean,
): String {
    if (isRestDay && durationMillis <= 0L) return "休"
    if (durationMillis <= 0L) return "-"
    val totalMinutes = (durationMillis / 60_000).coerceAtLeast(0)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h${minutes}m" else "${minutes}m"
}
