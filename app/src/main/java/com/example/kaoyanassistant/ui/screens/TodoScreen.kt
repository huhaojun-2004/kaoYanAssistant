package com.example.kaoyanassistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kaoyanassistant.data.local.entity.TodoEntity
import com.example.kaoyanassistant.ui.components.EmptyStateCard
import com.example.kaoyanassistant.ui.components.HeroCard
import com.example.kaoyanassistant.ui.components.MetricChip
import com.example.kaoyanassistant.ui.components.TaskDateSelector
import com.example.kaoyanassistant.ui.model.AppUiState
import com.example.kaoyanassistant.ui.theme.Cloud
import com.example.kaoyanassistant.ui.theme.Paper
import com.example.kaoyanassistant.ui.theme.Pine
import com.example.kaoyanassistant.util.DateTimeUtils

@Composable
fun TodoScreen(
    uiState: AppUiState,
    onAddTask: (String) -> Unit,
    onEditTask: (Long) -> Unit,
    onToggleComplete: (Long, Boolean) -> Unit,
    onDeleteTodo: (Long) -> Unit,
) {
    val todayKey = remember { DateTimeUtils.todayKey() }
    var selectedDate by rememberSaveable { mutableStateOf(todayKey) }
    val dayTodos = remember(uiState.todos, selectedDate) {
        uiState.todos
            .filter { resolveTodoDate(it) == selectedDate }
            .sortedWith(
                compareBy<TodoEntity> { it.isCompleted }
                    .thenBy { it.updatedAt },
            )
    }
    val completed = dayTodos.count { it.isCompleted }
    val pending = dayTodos.size - completed

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            HeroCard(
                title = "待办清单",
                subtitle = "",
                accent = Pine,
                compact = true,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    MetricChip(
                        label = "未完成",
                        value = pending.toString(),
                        modifier = Modifier.weight(1f),
                        borderColor = Pine.copy(alpha = 0.16f),
                        compact = true,
                    )
                    MetricChip(
                        label = "已完成",
                        value = completed.toString(),
                        modifier = Modifier.weight(1f),
                        borderColor = Pine.copy(alpha = 0.16f),
                        compact = true,
                    )
                    MetricChip(
                        label = "全部",
                        value = dayTodos.size.toString(),
                        modifier = Modifier.weight(1f),
                        borderColor = Pine.copy(alpha = 0.16f),
                        compact = true,
                    )
                }
                TaskDateSelector(
                    selectedDate = selectedDate,
                    onDateChange = { selectedDate = it },
                    label = "查看日期",
                    compact = true,
                )
                Button(
                    onClick = { onAddTask(selectedDate) },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "新增待办",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        item {
            TodoSectionHeader(
                dateLabel = displayTodoDateLabel(selectedDate),
                count = dayTodos.size,
            )
        }

        if (dayTodos.isEmpty()) {
            item {
                EmptyStateCard(text = "这一天还没有待办任务。")
            }
        } else {
            items(dayTodos, key = TodoEntity::id) { todo ->
                val subject = uiState.subjectMap[todo.subjectId]
                TodoTaskCard(
                    todo = todo,
                    subjectName = subject?.name,
                    subjectColor = subject?.let { Color(it.colorValue) } ?: Pine,
                    onToggleComplete = onToggleComplete,
                    onDeleteTodo = onDeleteTodo,
                    onEditTask = onEditTask,
                )
            }
        }
    }
}

@Composable
private fun TodoSectionHeader(
    dateLabel: String,
    count: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Box(
            modifier = Modifier
                .background(Cloud, RoundedCornerShape(999.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$count 项",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
        }
    }
}

@Composable
private fun TodoTaskCard(
    todo: TodoEntity,
    subjectName: String?,
    subjectColor: Color,
    onToggleComplete: (Long, Boolean) -> Unit,
    onDeleteTodo: (Long) -> Unit,
    onEditTask: (Long) -> Unit,
) {
    val strikeColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.52f)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (todo.isCompleted) {
                Cloud.copy(alpha = 0.7f)
            } else {
                Paper
            },
        ),
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = todo.isCompleted,
                modifier = Modifier.size(36.dp),
                onCheckedChange = { checked ->
                    onToggleComplete(todo.id, checked)
                },
            )
            if (subjectName != null) {
                Box(
                    modifier = Modifier
                        .background(subjectColor.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = subjectName,
                        style = MaterialTheme.typography.labelSmall,
                        color = subjectColor,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = todo.content,
                    modifier = if (todo.isCompleted) {
                        Modifier.drawWithContent {
                            drawContent()
                            val stroke = 1.8.dp.toPx()
                            val lineY = size.height * 0.48f
                            drawLine(
                                color = strikeColor,
                                start = androidx.compose.ui.geometry.Offset(0f, lineY),
                                end = androidx.compose.ui.geometry.Offset(size.width, lineY),
                                strokeWidth = stroke,
                            )
                        }
                    } else {
                        Modifier
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (todo.isCompleted) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(
                    modifier = Modifier.size(34.dp),
                    onClick = { onEditTask(todo.id) },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "编辑待办",
                        modifier = Modifier.size(18.dp),
                    )
                }
                IconButton(
                    modifier = Modifier.size(34.dp),
                    onClick = { onDeleteTodo(todo.id) },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "删除待办",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

private fun resolveTodoDate(todo: TodoEntity): String {
    return todo.dueDate?.takeIf { it.isNotBlank() }
        ?: DateTimeUtils.toLocalDateTime(todo.createdAt).toLocalDate().toString()
}

private fun displayTodoDateLabel(dateKey: String): String {
    return runCatching { DateTimeUtils.dateLabel(dateKey) }.getOrDefault(dateKey)
}
