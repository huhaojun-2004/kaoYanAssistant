package com.example.kaoyanassistant.data.repository

import com.example.kaoyanassistant.data.local.dao.DayRecordDao
import com.example.kaoyanassistant.data.local.entity.DayRecordEntity
import kotlinx.coroutines.flow.Flow

class CheckInRepository(
    private val dayRecordDao: DayRecordDao,
) {
    val dayRecords: Flow<List<DayRecordEntity>> = dayRecordDao.observeAll()

    suspend fun getByDate(date: String): DayRecordEntity? = dayRecordDao.getByDate(date)

    suspend fun setCheckIn(date: String, checkedIn: Boolean) {
        val current = dayRecordDao.getByDate(date)
        dayRecordDao.insert(
            (current ?: DayRecordEntity(date = date)).copy(
                isCheckIn = checkedIn,
                isRestDay = if (checkedIn) false else current?.isRestDay ?: false,
            ),
        )
    }

    suspend fun setRestDay(date: String, isRestDay: Boolean) {
        val current = dayRecordDao.getByDate(date)
        dayRecordDao.insert(
            (current ?: DayRecordEntity(date = date)).copy(
                isRestDay = isRestDay,
                isCheckIn = if (isRestDay) false else current?.isCheckIn ?: false,
            ),
        )
    }
}
