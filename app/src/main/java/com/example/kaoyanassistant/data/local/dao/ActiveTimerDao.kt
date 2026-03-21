package com.example.kaoyanassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kaoyanassistant.data.local.entity.ActiveTimerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveTimerDao {
    @Query("SELECT * FROM active_timer WHERE id = 1 LIMIT 1")
    fun observe(): Flow<ActiveTimerEntity?>

    @Query("SELECT * FROM active_timer WHERE id = 1 LIMIT 1")
    suspend fun get(): ActiveTimerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timer: ActiveTimerEntity)

    @Query("DELETE FROM active_timer WHERE id = 1")
    suspend fun clear()
}
