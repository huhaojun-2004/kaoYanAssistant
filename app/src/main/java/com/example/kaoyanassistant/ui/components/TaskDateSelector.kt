package com.example.kaoyanassistant.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kaoyanassistant.ui.theme.Pine
import com.example.kaoyanassistant.util.DateTimeUtils
import java.time.LocalDate

enum class TaskDateStepMode { DAY, WEEK, MONTH }

@Composable
fun TaskDateSelector(
    selectedDate: String,
    onDateChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    displayText: String? = null,
    stepMode: TaskDateStepMode = TaskDateStepMode.DAY,
    compact: Boolean = false,
    pickerEnabled: Boolean = true,
) {
    val context = LocalContext.current
    val selectedLocalDate = remember(selectedDate) { DateTimeUtils.parseDate(selectedDate) }
    val todayKey = remember { DateTimeUtils.todayKey() }
    val displayedValue = displayText ?: DateTimeUtils.dateLabel(selectedDate)
    val spacing = if (compact) 6.dp else 8.dp
    val valueStyle = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleSmall
    val labelStyle = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelSmall

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DateShiftButton(
                onClick = {
                    onDateChange(
                        when (stepMode) {
                            TaskDateStepMode.DAY -> selectedLocalDate.minusDays(1).toString()
                            TaskDateStepMode.WEEK -> selectedLocalDate.minusWeeks(1).toString()
                            TaskDateStepMode.MONTH -> selectedLocalDate.minusMonths(1).toString()
                        },
                    )
                },
                direction = DateShiftDirection.PREVIOUS,
                compact = compact,
            )
            if (pickerEnabled) {
                OutlinedButton(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                onDateChange(LocalDate.of(year, month + 1, dayOfMonth).toString())
                            },
                            selectedLocalDate.year,
                            selectedLocalDate.monthValue - 1,
                            selectedLocalDate.dayOfMonth,
                        ).show()
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    DateSelectorLabel(
                        label = label,
                        displayedValue = displayedValue,
                        labelStyle = labelStyle,
                        valueStyle = valueStyle,
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.46f),
                            shape = RoundedCornerShape(999.dp),
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    DateSelectorLabel(
                        label = label,
                        displayedValue = displayedValue,
                        labelStyle = labelStyle,
                        valueStyle = valueStyle,
                    )
                }
            }
            DateShiftButton(
                onClick = {
                    onDateChange(
                        when (stepMode) {
                            TaskDateStepMode.DAY -> selectedLocalDate.plusDays(1).toString()
                            TaskDateStepMode.WEEK -> selectedLocalDate.plusWeeks(1).toString()
                            TaskDateStepMode.MONTH -> selectedLocalDate.plusMonths(1).toString()
                        },
                    )
                },
                direction = DateShiftDirection.NEXT,
                compact = compact,
            )
        }

        if (selectedDate != todayKey) {
            TextButton(
                onClick = { onDateChange(todayKey) },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(if (stepMode == TaskDateStepMode.MONTH) "回到本月" else "回到今天")
            }
        }
    }
}

@Composable
private fun DateSelectorLabel(
    label: String,
    displayedValue: String,
    labelStyle: androidx.compose.ui.text.TextStyle,
    valueStyle: androidx.compose.ui.text.TextStyle,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = labelStyle,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
        )
        Text(
            text = displayedValue,
            style = valueStyle,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private enum class DateShiftDirection { PREVIOUS, NEXT }

@Composable
private fun DateShiftButton(
    onClick: () -> Unit,
    direction: DateShiftDirection,
    compact: Boolean,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(if (compact) 34.dp else 40.dp)
            .clip(CircleShape)
            .background(Pine.copy(alpha = 0.1f), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Icon(
            imageVector = if (direction == DateShiftDirection.PREVIOUS) {
                Icons.Outlined.ChevronLeft
            } else {
                Icons.Outlined.ChevronRight
            },
            contentDescription = if (direction == DateShiftDirection.PREVIOUS) "前一天" else "后一天",
            tint = Pine,
        )
    }
}
