package com.example.kaoyanassistant.ui.screens

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import com.example.kaoyanassistant.data.local.entity.SettingsEntity
import com.example.kaoyanassistant.util.DateTimeUtils
import com.example.kaoyanassistant.util.formatTimeValue
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    settings: SettingsEntity,
    showCountdownBadge: Boolean,
    onShowCountdownChange: (Boolean) -> Unit,
    onSave: (SettingsEntity) -> Unit,
    onRequestPermission: () -> Unit,
    buildBackupJson: () -> String,
    importBackupJson: suspend (String) -> Result<Unit>,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var checkInEnabled by remember { mutableStateOf(settings.checkInReminderEnabled) }
    var checkInHour by remember { mutableIntStateOf(settings.checkInReminderHour) }
    var checkInMinute by remember { mutableIntStateOf(settings.checkInReminderMinute) }
    var todoEnabled by remember { mutableStateOf(settings.todoReminderEnabled) }
    var todoHour by remember { mutableIntStateOf(settings.todoReminderHour) }
    var todoMinute by remember { mutableIntStateOf(settings.todoReminderMinute) }
    var showCheckInPicker by remember { mutableStateOf(false) }
    var showTodoPicker by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(settings) {
        checkInEnabled = settings.checkInReminderEnabled
        checkInHour = settings.checkInReminderHour
        checkInMinute = settings.checkInReminderMinute
        todoEnabled = settings.todoReminderEnabled
        todoHour = settings.todoReminderHour
        todoMinute = settings.todoReminderMinute
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            exportBackupToUri(
                context = context,
                uri = uri,
                buildBackupJson = buildBackupJson,
            )
        }
    }
    val importDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        pendingImportUri = uri
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            CompactSettingsCard(
                title = "提醒设置",
                subtitle = "打卡提醒和晚间待办提醒都可以单独开关，并保留自定义时间。",
                action = {
                    FilledTonalButton(onClick = onRequestPermission) {
                        Text("通知权限")
                    }
                },
            ) {
                ReminderSettingRow(
                    title = "早上打卡提醒",
                    checked = checkInEnabled,
                    time = formatTimeValue(checkInHour, checkInMinute),
                    onCheckedChange = { checkInEnabled = it },
                    onPickTime = { showCheckInPicker = true },
                )
                ReminderSettingRow(
                    title = "晚上待办提醒",
                    checked = todoEnabled,
                    time = formatTimeValue(todoHour, todoMinute),
                    onCheckedChange = { todoEnabled = it },
                    onPickTime = { showTodoPicker = true },
                )
                FilledTonalButton(
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
                        Toast.makeText(context, "提醒设置已保存", Toast.LENGTH_SHORT).show()
                    },
                ) {
                    Text("保存提醒设置")
                }
            }
        }

        item {
            CompactSettingsCard(
                title = "数据备份",
                subtitle = "导出、分享和导入都使用 JSON 备份文件。",
            ) {
                Text(
                    text = "导入会覆盖当前本地的科目、学习记录、待办、打卡记录和提醒设置。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                FilledTonalButton(
                    onClick = { createDocumentLauncher.launch(defaultBackupFileName()) },
                ) {
                    Text("导出到文件")
                }
                FilledTonalButton(
                    onClick = {
                        scope.launch {
                            shareBackupJson(
                                context = context,
                                buildBackupJson = buildBackupJson,
                            )
                        }
                    },
                ) {
                    Text("分享 JSON")
                }
                FilledTonalButton(
                    onClick = {
                        importDocumentLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                    },
                ) {
                    Text("导入备份")
                }
            }
        }

        item {
            CompactSettingsCard(
                title = "界面显示",
                subtitle = "",
            ) {
                ToggleSettingRow(
                    title = "显示左上角倒计时",
                    subtitle = if (showCountdownBadge) {
                        "首页左上角会显示考试倒计时卡片。"
                    } else {
                        "首页左上角倒计时已隐藏。"
                    },
                    checked = showCountdownBadge,
                    onCheckedChange = onShowCountdownChange,
                )
            }
        }

    }

    pendingImportUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            title = { Text("导入备份") },
            text = { Text("导入后会覆盖当前本地数据。确认继续吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val result = importBackupFromUri(
                                context = context,
                                uri = uri,
                                importBackupJson = importBackupJson,
                            )
                            Toast.makeText(
                                context,
                                result.fold(
                                    onSuccess = { "备份已导入" },
                                    onFailure = { "导入失败：${it.message ?: "未知错误"}" },
                                ),
                                Toast.LENGTH_SHORT,
                            ).show()
                            pendingImportUri = null
                        }
                    },
                ) {
                    Text("导入")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportUri = null }) {
                    Text("取消")
                }
            },
        )
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
private fun CompactSettingsCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                        )
                    }
                }
                action?.invoke()
            }
            content()
        }
    }
}

@Composable
private fun ReminderSettingRow(
    title: String,
    checked: Boolean,
    time: String,
    onCheckedChange: (Boolean) -> Unit,
    onPickTime: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = if (checked) "提醒时间：$time" else "当前已关闭",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
        FilledTonalButton(
            onClick = onPickTime,
            enabled = checked,
        ) {
            Text("时间")
        }
    }
}

@Composable
private fun ToggleSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
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

    Column(
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

private suspend fun exportBackupToUri(
    context: Context,
    uri: Uri,
    buildBackupJson: () -> String,
) {
    runCatching {
        val backupJson = withContext(Dispatchers.Default) { buildBackupJson() }
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                writer.write(backupJson)
            } ?: error("无法写入备份文件")
        }
    }.onSuccess {
        Toast.makeText(context, "备份已导出", Toast.LENGTH_SHORT).show()
    }.onFailure {
        Toast.makeText(context, "导出失败：${it.message ?: "未知错误"}", Toast.LENGTH_SHORT).show()
    }
}

private suspend fun shareBackupJson(
    context: Context,
    buildBackupJson: () -> String,
) {
    runCatching {
        val backupJson = withContext(Dispatchers.Default) { buildBackupJson() }
        val uri = withContext(Dispatchers.IO) {
            writeBackupShareFile(context, backupJson)
        }
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newUri(context.contentResolver, "backup", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "分享备份"))
    }.onFailure {
        Toast.makeText(context, "分享失败：${it.message ?: "未知错误"}", Toast.LENGTH_SHORT).show()
    }
}

private suspend fun importBackupFromUri(
    context: Context,
    uri: Uri,
    importBackupJson: suspend (String) -> Result<Unit>,
): Result<Unit> {
    val json = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: error("无法读取备份文件")
    }
    return importBackupJson(json)
}

private fun writeBackupShareFile(
    context: Context,
    backupJson: String,
): Uri {
    val shareDir = File(context.cacheDir, "shared_backups").apply { mkdirs() }
    val file = File(shareDir, defaultBackupFileName())
    file.writeText(backupJson)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
}

private fun defaultBackupFileName(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
    val timestamp = LocalDateTime.now(DateTimeUtils.zoneId).format(formatter)
    return "kaoyan-assistant-backup-$timestamp.json"
}
