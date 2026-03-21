package com.example.kaoyanassistant.data.repository

import com.example.kaoyanassistant.data.local.dao.TodoDao
import com.example.kaoyanassistant.data.local.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

class TodoRepository(
    private val todoDao: TodoDao,
) {
    val todos: Flow<List<TodoEntity>> = todoDao.observeAll()

    suspend fun getById(id: Long): TodoEntity? = todoDao.getById(id)

    suspend fun save(
        id: Long?,
        content: String,
        subjectId: Long,
        taskDate: String,
    ) {
        val normalizedContent = content.trim()
        if (normalizedContent.isEmpty()) return

        val now = System.currentTimeMillis()
        if (id == null) {
            todoDao.insert(
                TodoEntity(
                    content = normalizedContent,
                    subjectId = subjectId,
                    dueDate = taskDate,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        } else {
            val existing = todoDao.getById(id) ?: return
            todoDao.update(
                existing.copy(
                    content = normalizedContent,
                    subjectId = subjectId,
                    dueDate = taskDate,
                    updatedAt = now,
                ),
            )
        }
    }

    suspend fun setCompleted(id: Long, completed: Boolean) {
        val existing = todoDao.getById(id) ?: return
        todoDao.update(
            existing.copy(
                isCompleted = completed,
                completedAt = if (completed) System.currentTimeMillis() else null,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun delete(id: Long) {
        val todo = todoDao.getById(id) ?: return
        todoDao.delete(todo)
    }

    suspend fun incompleteCount(): Int = todoDao.getIncompleteCount()

    suspend fun incompleteCountForDate(date: String): Int = todoDao.getIncompleteCountForDate(date)

    suspend fun actualIncompleteCountForDate(date: String): Int {
        return todoDao.getAll().count { todo ->
            !todo.isCompleted && resolveTaskDate(todo) == date
        }
    }

    private fun resolveTaskDate(todo: TodoEntity): String {
        return todo.dueDate?.takeIf { it.isNotBlank() }
            ?: java.time.Instant.ofEpochMilli(todo.createdAt)
                .atZone(com.example.kaoyanassistant.util.DateTimeUtils.zoneId)
                .toLocalDate()
                .toString()
    }
}
