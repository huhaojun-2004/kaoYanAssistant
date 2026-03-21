package com.example.kaoyanassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kaoyanassistant.ui.components.EmptyStateCard
import com.example.kaoyanassistant.ui.components.HeroCard
import com.example.kaoyanassistant.ui.components.TaskDateSelector
import com.example.kaoyanassistant.ui.model.AppUiState
import com.example.kaoyanassistant.ui.model.displaySubjectName
import com.example.kaoyanassistant.ui.model.flattenedSubjects
import com.example.kaoyanassistant.ui.theme.Cloud
import com.example.kaoyanassistant.util.DateTimeUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TodoEditorScreen(
    uiState: AppUiState,
    todoId: Long?,
    initialDate: String,
    onSave: (Long?, String, Long, String) -> Unit,
) {
    val subjectOptions = flattenedSubjects(uiState.subjects)
    val editingTodo = uiState.todos.firstOrNull { it.id == todoId }
    val isEditing = todoId != null && editingTodo != null
    var content by remember { mutableStateOf("") }
    var taskDate by remember { mutableStateOf(initialDate) }
    var selectedSubjectId by remember { mutableLongStateOf(subjectOptions.firstOrNull()?.id ?: -1L) }

    LaunchedEffect(editingTodo?.id, subjectOptions.firstOrNull()?.id, initialDate) {
        if (editingTodo != null) {
            content = editingTodo.content
            taskDate = editingTodo.dueDate?.takeIf { it.isNotBlank() } ?: initialDate
            selectedSubjectId = editingTodo.subjectId
        } else {
            content = ""
            taskDate = initialDate.ifBlank { DateTimeUtils.todayKey() }
            selectedSubjectId = subjectOptions.firstOrNull()?.id ?: -1L
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            HeroCard(
                title = if (todoId == null) "新增待办" else "编辑待办",
                subtitle = if (isEditing) "编辑时只允许修改内容。" else "每条待办都绑定到某一天，默认使用当前查看日期。",
                compact = true,
            ) {}
        }

        if (subjectOptions.isEmpty()) {
            item {
                EmptyStateCard(text = "请先创建科目，再添加待办。")
            }
        } else {
            item {
                if (isEditing) {
                    ReadOnlyTodoMeta(
                        label = "任务日期",
                        value = DateTimeUtils.dateLabel(taskDate),
                    )
                } else {
                    TaskDateSelector(
                        selectedDate = taskDate,
                        onDateChange = { taskDate = it },
                        label = "任务日期",
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("任务内容") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                if (isEditing) {
                    val subjectName = subjectOptions
                        .firstOrNull { it.id == selectedSubjectId }
                        ?.let { displaySubjectName(it, uiState.subjectMap) }
                        .orEmpty()
                    ReadOnlyTodoMeta(
                        label = "所属科目",
                        value = subjectName,
                    )
                } else {
                    Text("所属科目", style = MaterialTheme.typography.titleMedium)
                }
            }
            if (!isEditing) {
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        subjectOptions.forEach { subject ->
                            FilterChip(
                                selected = selectedSubjectId == subject.id,
                                onClick = { selectedSubjectId = subject.id },
                                label = { Text(displaySubjectName(subject, uiState.subjectMap)) },
                            )
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        onSave(
                            todoId,
                            content,
                            selectedSubjectId,
                            taskDate,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = content.isNotBlank() && selectedSubjectId > 0L,
                ) {
                    Text("保存待办")
                }
            }
        }
    }
}

@Composable
private fun ReadOnlyTodoMeta(
    label: String,
    value: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Cloud),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
