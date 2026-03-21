package com.example.kaoyanassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kaoyanassistant.data.local.entity.DayRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DayRecordDao {
    @Query("SELECT * FROM day_records ORDER BY date DESC")
    fun observeAll(): Flow<List<DayRecordEntity>>

    @Query("SELECT * FROM day_records WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): DayRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: DayRecordEntity)
}
