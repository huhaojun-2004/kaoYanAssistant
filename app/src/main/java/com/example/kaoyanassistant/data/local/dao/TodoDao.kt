package com.example.kaoyanassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kaoyanassistant.data.local.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY isCompleted ASC, dueDate IS NULL, dueDate ASC, updatedAt DESC")
    fun observeAll(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TodoEntity?

    @Query("SELECT * FROM todos")
    suspend fun getAll(): List<TodoEntity>

    @Query("DELETE FROM todos")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM todos WHERE isCompleted = 0")
    suspend fun getIncompleteCount(): Int

    @Query("SELECT COUNT(*) FROM todos WHERE isCompleted = 0 AND dueDate = :date")
    suspend fun getIncompleteCountForDate(date: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: TodoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(todos: List<TodoEntity>): List<Long>

    @Update
    suspend fun update(todo: TodoEntity)

    @Delete
    suspend fun delete(todo: TodoEntity)
}
