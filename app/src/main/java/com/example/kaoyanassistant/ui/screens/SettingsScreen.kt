package com.example.kaoyanassistant.ui.screens

import android.widget.NumberPicker
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.kaoyanassistant.data.local.entity.SettingsEntity
import com.example.kaoyanassistant.ui.components.HeroCard
import com.example.kaoyanassistant.util.formatTimeValue

@Composable
fun SettingsScreen(
    settings: SettingsEntity,
    onSave: (SettingsEntity) -> Unit,
    onRequestPermission: () -> Unit,
) {
    val context = LocalContext.current
    var checkInEnabled by remember { mutableStateOf(settings.checkInReminderEnabled) }
    var checkInHour by remember { mutableIntStateOf(settings.checkInReminderHour) }
    var checkInMinute by remember { mutableIntStateOf(settings.checkInReminderMinute) }
    var todoEnabled by remember { mutableStateOf(settings.todoReminderEnabled) }
    var todoHour by remember { mutableIntStateOf(settings.todoReminderHour) }
    var todoMinute by remember { mutableIntStateOf(settings.todoReminderMinute) }
    var showCheckInPicker by remember { mutableStateOf(false) }
    var showTodoPicker by remember { mutableStateOf(false) }

    LaunchedEffect(settings) {
        checkInEnabled = settings.checkInReminderEnabled
        checkInHour = settings.checkInReminderHour
        checkInMinute = settings.checkInReminderMinute
        todoEnabled = settings.todoReminderEnabled
        todoHour = settings.todoReminderHour
        todoMinute = settings.todoReminderMinute
    }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HeroCard(
                title = "提醒设置",
                subtitle = "打卡提醒和晚间待办提醒都支持单独开关与自定义时间。",
            ) {
                Button(onClick = onRequestPermission) {
                    Text("申请通知权限")
                }
            }
        }

        item {
            ReminderSettingCard(
                title = "早上打卡提醒",
                checked = checkInEnabled,
                time = formatTimeValue(checkInHour, checkInMinute),
                onCheckedChange = { checkInEnabled = it },
                onPickTime = { showCheckInPicker = true },
            )
        }

        item {
            ReminderSettingCard(
                title = "晚上待办提醒",
                checked = todoEnabled,
                time = formatTimeValue(todoHour, todoMinute),
                onCheckedChange = { todoEnabled = it },
                onPickTime = { showTodoPicker = true },
            )
        }

        item {
            Button(
                onClick = {
                    onSave(
                        settings.copy(
                            checkInReminderEnabled = checkInEnabled,
                            checkInReminderHour = checkInHour,
                            checkInReminderMinute = checkInMinute,
                            todoReminderEnabled = todoEnabled,
                            todoReminderHour = todoHour,
                            todoReminderMinute = todoMinute,
                        ),
                    )
                    Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                },
            ) {
                Text("保存提醒设置")
            }
        }
    }

    if (showCheckInPicker) {
        DualColumnTimePickerDialog(
            title = "选择打卡提醒时间",
            initialHour = checkInHour,
            initialMinute = checkInMinute,
            onDismiss = { showCheckInPicker = false },
            onConfirm = { hour, minute ->
                checkInHour = hour
                checkInMinute = minute
                showCheckInPicker = false
            },
        )
    }

    if (showTodoPicker) {
        DualColumnTimePickerDialog(
            title = "选择待办提醒时间",
            initialHour = todoHour,
            initialMinute = todoMinute,
            onDismiss = { showTodoPicker = false },
            onConfirm = { hour, minute ->
                todoHour = hour
                todoMinute = minute
                showTodoPicker = false
            },
        )
    }
}

@Composable
private fun ReminderSettingCard(
    title: String,
    checked: Boolean,
    time: String,
    onCheckedChange: (Boolean) -> Unit,
    onPickTime: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Switch(checked = checked, onCheckedChange = onCheckedChange)
            }
            Text(
                text = "提醒时间：$time",
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(onClick = onPickTime, enabled = checked) {
                Text("选择时间")
            }
        }
    }
}

@Composable
private fun DualColumnTimePickerDialog(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    var hour by remember(initialHour) { mutableIntStateOf(initialHour) }
    var minute by remember(initialMinute) { mutableIntStateOf(initialMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TimeNumberPicker(
                    modifier = Modifier.weight(1f),
                    label = "小时",
                    range = 0..23,
                    value = hour,
                    onValueChange = { hour = it },
                )
                TimeNumberPicker(
                    modifier = Modifier.weight(1f),
                    label = "分钟",
                    range = 0..59,
                    value = minute,
                    onValueChange = { minute = it },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(hour, minute) }) {
                Text("确定")
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
private fun TimeNumberPicker(
    modifier: Modifier = Modifier,
    label: String,
    range: IntRange,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    val context = LocalContext.current

    androidx.compose.foundation.layout.Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
        )
        AndroidView(
            factory = {
                NumberPicker(context).apply {
                    minValue = range.first
                    maxValue = range.last
                    displayedValues = range.map { "%02d".format(it) }.toTypedArray()
                    wrapSelectorWheel = true
                    descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                    setOnValueChangedListener { _, _, newVal -> onValueChange(newVal) }
                    this.value = value
                }
            },
            update = { picker ->
                picker.displayedValues = null
                picker.minValue = range.first
                picker.maxValue = range.last
                picker.displayedValues = range.map { "%02d".format(it) }.toTypedArray()
                if (picker.value != value) {
                    picker.value = value
                }
                picker.setOnValueChangedListener { _, _, newVal -> onValueChange(newVal) }
            },
            modifier = Modifier.size(width = 120.dp, height = 160.dp),
        )
    }
}
