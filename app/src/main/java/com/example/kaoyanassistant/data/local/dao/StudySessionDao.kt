package com.example.kaoyanassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kaoyanassistant.data.local.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY startTime DESC")
    fun observeAll(): Flow<List<StudySessionEntity>>

    @Query(
        """
        SELECT * FROM study_sessions
        WHERE startTime < :rangeEnd AND endTime > :rangeStart
        ORDER BY startTime ASC
        """
    )
    suspend fun getIntersecting(rangeStart: Long, rangeEnd: Long): List<StudySessionEntity>

    @Query(
        """
        SELECT * FROM study_sessions
        WHERE subjectId = :subjectId
          AND startTime < :rangeEnd
          AND endTime > :rangeStart
        ORDER BY startTime ASC
        """
    )
    suspend fun getIntersectingBySubject(
        subjectId: Long,
        rangeStart: Long,
        rangeEnd: Long,
    ): List<StudySessionEntity>

    @Query("DELETE FROM study_sessions")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: StudySessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<StudySessionEntity>): List<Long>

    @Update
    suspend fun update(session: StudySessionEntity)

    @Delete
    suspend fun delete(session: StudySessionEntity)
}
