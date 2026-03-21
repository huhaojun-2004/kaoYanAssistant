package com.example.kaoyanassistant.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kaoyanassistant.ui.components.MetricChip
import com.example.kaoyanassistant.ui.model.AppUiState
import com.example.kaoyanassistant.ui.model.flattenedSubjects
import com.example.kaoyanassistant.ui.model.totalStudyForDate
import com.example.kaoyanassistant.ui.model.totalStudyForSubjectOnDate
import com.example.kaoyanassistant.ui.theme.Paper
import com.example.kaoyanassistant.ui.theme.Pine
import com.example.kaoyanassistant.util.DateTimeUtils
import com.example.kaoyanassistant.util.formatDurationClock
import com.example.kaoyanassistant.util.formatDurationText
import kotlinx.coroutines.delay
import kotlin.math.max

@Composable
fun TimerScreen(
    uiState: AppUiState,
    onSelectSubject: (Long) -> Unit,
    onStartTimer: () -> Unit,
    onStartSubject: (Long) -> Unit,
    onStopTimer: () -> Unit,
) {
    val subjects = remember(uiState.subjects) { flattenedSubjects(uiState.subjects) }
    val selectedSubject = uiState.selectedTimerSubject
    val activeTimer = uiState.activeTimer
    val activeSubject = uiState.activeSubject
    val resolvedSubjectId = activeSubject?.id ?: selectedSubject?.id
    val todayTotal = totalStudyForDate(uiState.sessions, DateTimeUtils.todayKey())
    var subjectMenuExpanded by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(activeTimer?.startedAt, activeTimer?.carriedMillis) {
        if (activeTimer == null) return@LaunchedEffect
        now = maxOf(System.currentTimeMillis(), activeTimer.startedAt)
        while (true) {
            delay(250)
            now = System.currentTimeMillis()
        }
    }
    val carriedMillis = when {
        activeTimer != null -> activeTimer.carriedMillis
        resolvedSubjectId != null -> totalStudyForSubjectOnDate(
            sessions = uiState.sessions,
            subjectId = resolvedSubjectId,
            date = DateTimeUtils.todayKey(),
        )
        else -> 0L
    }
    val elapsed = carriedMillis + if (activeTimer != null) now - activeTimer.startedAt else 0L
    val accent = activeSubject?.let { Color(it.colorValue) }
        ?: selectedSubject?.let { Color(it.colorValue) }
        ?: Pine
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Paper),
                shape = RoundedCornerShape(24.dp),
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    accent.copy(alpha = 0.10f),
                                    MaterialTheme.colorScheme.surface,
                                ),
                            ),
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        androidx.compose.foundation.layout.Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text("专注计时", style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = if (activeSubject == null) {
                                    "选一个科目，让今天的学习进入状态。"
                                } else {
                                    "当前正在 ${activeSubject.name}"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    color = accent.copy(alpha = 0.14f),
                                    shape = RoundedCornerShape(999.dp),
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = if (activeSubject == null) "待开始" else "专注中",
                                style = MaterialTheme.typography.bodyMedium,
                                color = accent,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        FocusDial(
                            timeText = formatDurationClock(elapsed),
                            subjectText = selectedSubject?.name ?: "尚未选择科目",
                            accent = accent,
                            elapsed = elapsed,
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetricChip(
                            label = "今日累计",
                            value = formatDurationText(todayTotal),
                        )
                    }

                    Box {
                        OutlinedButton(
                            onClick = { subjectMenuExpanded = true },
                            enabled = subjects.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (selectedSubject == null) {
                                Text("选择科目")
                            } else {
                                val selectedDuration = totalStudyForSubjectOnDate(
                                    sessions = uiState.sessions,
                                    subjectId = selectedSubject.id,
                                    date = DateTimeUtils.todayKey(),
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(selectedSubject.name)
                                    Text(
                                        text = formatTimerMenuDuration(selectedDuration),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                                    )
                                }
                            }
                        }
                        DropdownMenu(
                            expanded = subjectMenuExpanded,
                            onDismissRequest = { subjectMenuExpanded = false },
                            shape = RoundedCornerShape(20.dp),
                            containerColor = Paper,
                            tonalElevation = 0.dp,
                            shadowElevation = 12.dp,
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = accent.copy(alpha = 0.14f),
                            ),
                        ) {
                            subjects.forEach { subject ->
                                val selected = selectedSubject?.id == subject.id
                                val todayDuration = totalStudyForSubjectOnDate(
                                    sessions = uiState.sessions,
                                    subjectId = subject.id,
                                    date = DateTimeUtils.todayKey(),
                                )
                                DropdownMenuItem(
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .background(
                                            color = if (selected) accent.copy(alpha = 0.12f) else Color.Transparent,
                                            shape = RoundedCornerShape(16.dp),
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (selected) {
                                                accent.copy(alpha = 0.22f)
                                            } else {
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                            },
                                            shape = RoundedCornerShape(16.dp),
                                        ),
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = subject.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                                color = if (selected) accent else MaterialTheme.colorScheme.onSurface,
                                            )
                                            Text(
                                                text = formatTimerMenuDuration(todayDuration),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (selected) {
                                                    accent.copy(alpha = 0.84f)
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                                                },
                                            )
                                        }
                                    },
                                    onClick = {
                                        subjectMenuExpanded = false
                                        onSelectSubject(subject.id)
                                    },
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                when {
                                    selectedSubject == null -> Unit
                                    activeSubject == null -> onStartTimer()
                                    activeSubject.id == selectedSubject.id -> onStopTimer()
                                    else -> onStartSubject(selectedSubject.id)
                                }
                            },
                            enabled = selectedSubject != null,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = accent),
                        ) {
                            Text(
                                when {
                                    selectedSubject == null -> "先选科目"
                                    activeSubject == null -> "开始专注"
                                    activeSubject.id == selectedSubject.id -> "结束专注"
                                    else -> "切换并开始"
                                },
                            )
                        }
                        if (activeSubject != null && selectedSubject?.id != activeSubject.id) {
                            OutlinedButton(
                                onClick = onStopTimer,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("结束当前")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimerMenuDuration(durationMillis: Long): String {
    val totalMinutes = (durationMillis / 60_000).coerceAtLeast(0L)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}时${minutes}分" else "${minutes}分"
}

@Composable
private fun FocusDial(
    timeText: String,
    subjectText: String,
    accent: Color,
    elapsed: Long,
) {
    val progress = when {
        elapsed <= 0L -> 0f
        else -> max((((elapsed / 1000f) % 3600f) / 3600f), 0.01f)
    }

    Box(
        modifier = Modifier.size(220.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val strokeWidth = 18.dp.toPx()
            drawCircle(
                color = accent.copy(alpha = 0.12f),
                style = Stroke(width = strokeWidth),
            )
            if (progress > 0f) {
                drawArc(
                    color = accent,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
            drawCircle(
                color = accent.copy(alpha = 0.08f),
                radius = size.minDimension * 0.33f,
                center = Offset(size.width / 2f, size.height / 2f),
            )
        }

        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subjectText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
            )
        }
    }
}
