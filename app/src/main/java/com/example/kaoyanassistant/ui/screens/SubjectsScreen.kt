package com.example.kaoyanassistant.ui.screens

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.example.kaoyanassistant.data.local.entity.SubjectEntity
import com.example.kaoyanassistant.ui.components.EmptyStateCard
import com.example.kaoyanassistant.ui.components.HeroCard
import com.example.kaoyanassistant.ui.model.AppUiState
import com.example.kaoyanassistant.ui.model.flattenedSubjects
import com.example.kaoyanassistant.ui.model.totalStudyForSubjectOnDate
import com.example.kaoyanassistant.util.DateTimeUtils
import com.example.kaoyanassistant.util.formatDurationCompact

private data class SubjectEditorDraft(
    val id: Long? = null,
    val name: String = "",
    val colorValue: Long = 0xFF28594A,
)

@Composable
fun SubjectsScreen(
    uiState: AppUiState,
    onStartTimerForSubject: (Long) -> Unit,
    onSaveSubject: (Long?, String, Long?, Long?) -> Unit,
    onDeleteSubject: (Long) -> Unit,
) {
    val subjects = remember(uiState.subjects) { flattenedSubjects(uiState.subjects) }
    val todayKey = remember { DateTimeUtils.todayKey() }
    val activeSubjectId = uiState.activeTimer?.subjectId
    var editorDraft by remember { mutableStateOf<SubjectEditorDraft?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            HeroCard(
                title = "科目管理",
                subtitle = "所有科目平级显示，右侧开始按钮可直接进入专注。",
                compact = true,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (subjects.isEmpty()) {
                            "先新增一个科目，再开始今天的学习。"
                        } else {
                            "当前共 ${subjects.size} 门科目，保持扁平列表更适合快速切换。"
                        },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                    OutlinedButton(
                        onClick = { editorDraft = SubjectEditorDraft(colorValue = 0xFF28594A) },
                        modifier = Modifier.padding(start = 10.dp),
                    ) {
                        Text("新增")
                    }
                }
            }
        }

        if (subjects.isEmpty()) {
            item {
                EmptyStateCard(text = "还没有科目，先新增一个。")
            }
        } else {
            items(subjects, key = SubjectEntity::id) { subject ->
                val subjectColor = Color(subject.colorValue)
                val todayDuration = totalStudyForSubjectOnDate(uiState.sessions, subject.id, todayKey)
                val isActive = activeSubjectId == subject.id

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = subjectColor.copy(alpha = if (isActive) 0.28f else 0.14f),
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(subjectColor.copy(alpha = 0.11f), RoundedCornerShape(14.dp))
                                .border(1.dp, subjectColor.copy(alpha = 0.18f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(subjectColor, CircleShape),
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                SubjectMetaBadge(
                                    text = if (todayDuration > 0L) {
                                        "今日 ${formatDurationCompact(todayDuration)}"
                                    } else {
                                        "今日未学"
                                    },
                                    tint = subjectColor,
                                )
                                if (isActive) {
                                    SubjectMetaBadge(
                                        text = "当前计时",
                                        tint = subjectColor,
                                        highlighted = true,
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SubjectActionButton(
                                onClick = { onStartTimerForSubject(subject.id) },
                                tint = subjectColor,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PlayArrow,
                                    contentDescription = "进入计时",
                                    tint = subjectColor,
                                )
                            }
                            SubjectActionButton(
                                onClick = {
                                    editorDraft = SubjectEditorDraft(
                                        id = subject.id,
                                        name = subject.name,
                                        colorValue = subject.colorValue,
                                    )
                                },
                                tint = subjectColor,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "编辑",
                                    tint = subjectColor,
                                )
                            }
                            SubjectActionButton(
                                onClick = { onDeleteSubject(subject.id) },
                                tint = subjectColor,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = "删除",
                                    tint = subjectColor,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    editorDraft?.let { draft ->
        SubjectEditorDialog(
            draft = draft,
            onDismiss = { editorDraft = null },
            onSave = { id, name, colorValue ->
                onSaveSubject(id, name, null, colorValue)
                editorDraft = null
            },
        )
    }
}

@Composable
private fun SubjectMetaBadge(
    text: String,
    tint: Color,
    highlighted: Boolean = false,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (highlighted) tint.copy(alpha = 0.14f) else tint.copy(alpha = 0.08f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (highlighted) tint else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontWeight = if (highlighted) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

@Composable
private fun SubjectActionButton(
    onClick: () -> Unit,
    tint: Color,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(tint.copy(alpha = 0.1f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun SubjectEditorDialog(
    draft: SubjectEditorDraft,
    onDismiss: () -> Unit,
    onSave: (Long?, String, Long) -> Unit,
) {
    var name by remember(draft.id) { mutableStateOf(draft.name) }
    var selectedColor by remember(draft.id) { mutableStateOf(draft.colorValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (draft.id == null) "新增科目" else "编辑科目") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("科目名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "科目颜色",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    WpfColorPicker(
                        colorValue = selectedColor,
                        onColorChange = { selectedColor = it },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(draft.id, name, selectedColor) },
                enabled = name.isNotBlank(),
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun WpfColorPicker(
    colorValue: Long,
    onColorChange: (Long) -> Unit,
) {
    val hsv = remember(colorValue) { hsvFromColorValue(colorValue) }
    val hue = hsv[0]
    val saturation = hsv[1]
    val value = hsv[2]
    val currentHue by rememberUpdatedState(hue)
    val currentSaturation by rememberUpdatedState(saturation)
    val currentValue by rememberUpdatedState(value)
    var spectrumSize by remember { mutableStateOf(IntSize.Zero) }
    var hueSliderSize by remember { mutableStateOf(IntSize.Zero) }

    fun updateSpectrum(offset: Offset) {
        if (spectrumSize.width == 0 || spectrumSize.height == 0) return
        val nextSaturation = (offset.x / spectrumSize.width.toFloat()).coerceIn(0f, 1f)
        val nextValue = (1f - offset.y / spectrumSize.height.toFloat()).coerceIn(0f, 1f)
        onColorChange(colorValueFromHsv(currentHue, nextSaturation, nextValue))
    }

    fun updateHue(offset: Offset) {
        if (hueSliderSize.width == 0) return
        val progress = (offset.x / hueSliderSize.width.toFloat()).coerceIn(0f, 1f)
        val nextHue = progress * 360f
        onColorChange(colorValueFromHsv(nextHue, currentSaturation, currentValue))
    }

    val hueColor = remember(hue) { Color(colorValueFromHsv(hue, 1f, 1f)) }
    val sliderColors = remember {
        listOf(
            Color(0xFFFF0000),
            Color(0xFFFFFF00),
            Color(0xFF00FF00),
            Color(0xFF00FFFF),
            Color(0xFF0000FF),
            Color(0xFFFF00FF),
            Color(0xFFFF0000),
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.horizontalGradient(listOf(Color.White, hueColor)))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f), RoundedCornerShape(24.dp))
                .onSizeChanged { spectrumSize = it }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = ::updateSpectrum,
                        onDrag = { change, _ ->
                            updateSpectrum(change.position)
                            change.consume()
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                        ),
                    ),
            )
            Canvas(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                val indicatorCenter = Offset(
                    x = saturation * size.width,
                    y = (1f - value) * size.height,
                )
                drawCircle(
                    color = Color.White,
                    radius = 11.dp.toPx(),
                    center = indicatorCenter,
                )
                drawCircle(
                    color = Color.Black.copy(alpha = 0.22f),
                    radius = 11.dp.toPx(),
                    center = indicatorCenter,
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Brush.horizontalGradient(sliderColors))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f), RoundedCornerShape(999.dp))
                .onSizeChanged { hueSliderSize = it }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = ::updateHue,
                        onDrag = { change, _ ->
                            updateHue(change.position)
                            change.consume()
                        },
                    )
                },
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize(),
            ) {
                val progress = (hue / 360f).coerceIn(0f, 1f)
                val centerX = progress * size.width
                val handleWidth = 16.dp.toPx()
                val handleHeight = size.height - 6.dp.toPx()
                val top = (size.height - handleHeight) / 2f
                val left = (centerX - handleWidth / 2f).coerceIn(2.dp.toPx(), size.width - handleWidth - 2.dp.toPx())
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(left, top),
                    size = Size(handleWidth, handleHeight),
                    cornerRadius = CornerRadius(999f, 999f),
                )
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.18f),
                    topLeft = Offset(left, top),
                    size = Size(handleWidth, handleHeight),
                    cornerRadius = CornerRadius(999f, 999f),
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(Color(colorValue), RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
            )
            Text(
                text = "上方调明暗和浓度，下方调色相",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            )
        }
    }
}

private fun hsvFromColorValue(colorValue: Long): FloatArray {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV((colorValue and 0xFFFFFFFFL).toInt(), hsv)
    return hsv
}

private fun colorValueFromHsv(
    hue: Float,
    saturation: Float,
    value: Float,
): Long {
    return AndroidColor.HSVToColor(
        floatArrayOf(
            ((hue % 360f) + 360f) % 360f,
            saturation.coerceIn(0f, 1f),
            value.coerceIn(0f, 1f),
        ),
    ).toLong() and 0xFFFFFFFFL
}
