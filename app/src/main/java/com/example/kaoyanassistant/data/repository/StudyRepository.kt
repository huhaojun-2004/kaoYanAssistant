package com.example.kaoyanassistant.data.repository

import com.example.kaoyanassistant.data.local.dao.ActiveTimerDao
import com.example.kaoyanassistant.data.local.dao.StudySessionDao
import com.example.kaoyanassistant.data.local.entity.ActiveTimerEntity
import com.example.kaoyanassistant.data.local.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

class StudyRepository(
    private val sessionDao: StudySessionDao,
    private val activeTimerDao: ActiveTimerDao,
) {
    val sessions: Flow<List<StudySessionEntity>> = sessionDao.observeAll()
    val activeTimer: Flow<ActiveTimerEntity?> = activeTimerDao.observe()

    suspend fun getActiveTimer(): ActiveTimerEntity? = activeTimerDao.get()

    suspend fun startTimer(
        subjectId: Long,
        startedAt: Long = System.currentTimeMillis(),
        carriedMillis: Long = 0L,
    ) {
        activeTimerDao.insert(
            ActiveTimerEntity(
                subjectId = subjectId,
                startedAt = startedAt,
                carriedMillis = carriedMillis,
            ),
        )
    }

    suspend fun stopTimer(stoppedAt: Long = System.currentTimeMillis()): StudySessionEntity? {
        val active = activeTimerDao.get() ?: return null
        val end = maxOf(stoppedAt, active.startedAt)
        val session = StudySessionEntity(
            subjectId = active.subjectId,
            startTime = active.startedAt,
            endTime = end,
            durationMillis = end - active.startedAt,
        )
        sessionDao.insert(session)
        activeTimerDao.clear()
        return session
    }

    suspend fun clearActiveTimer() {
        activeTimerDao.clear()
    }

    suspend fun deleteSubjectTimeInRange(
        subjectId: Long,
        rangeStart: Long,
        rangeEnd: Long,
    ) {
        if (rangeEnd <= rangeStart) return

        val sessions = sessionDao.getIntersectingBySubject(
            subjectId = subjectId,
            rangeStart = rangeStart,
            rangeEnd = rangeEnd,
        )

        sessions.forEach { session ->
            val start = session.startTime
            val end = session.endTime

            when {
                start >= rangeStart && end <= rangeEnd -> {
                    sessionDao.delete(session)
                }

                start < rangeStart && end > rangeEnd -> {
                    sessionDao.update(
                        session.copy(
                            endTime = rangeStart,
                            durationMillis = rangeStart - start,
                        ),
                    )
                    sessionDao.insert(
                        session.copy(
                            id = 0,
                            startTime = rangeEnd,
                            endTime = end,
                            durationMillis = end - rangeEnd,
                        ),
                    )
                }

                start < rangeStart && end > rangeStart -> {
                    sessionDao.update(
                        session.copy(
                            endTime = rangeStart,
                            durationMillis = rangeStart - start,
                        ),
                    )
                }

                start < rangeEnd && end > rangeEnd -> {
                    sessionDao.update(
                        session.copy(
                            startTime = rangeEnd,
                            durationMillis = end - rangeEnd,
                        ),
                    )
                }
            }
        }
    }
}
