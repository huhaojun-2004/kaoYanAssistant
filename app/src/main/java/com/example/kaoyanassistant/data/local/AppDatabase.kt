package com.example.kaoyanassistant.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kaoyanassistant.data.local.dao.ActiveTimerDao
import com.example.kaoyanassistant.data.local.dao.DayRecordDao
import com.example.kaoyanassistant.data.local.dao.SettingsDao
import com.example.kaoyanassistant.data.local.dao.StudySessionDao
import com.example.kaoyanassistant.data.local.dao.SubjectDao
import com.example.kaoyanassistant.data.local.dao.TodoDao
import com.example.kaoyanassistant.data.local.entity.ActiveTimerEntity
import com.example.kaoyanassistant.data.local.entity.DayRecordEntity
import com.example.kaoyanassistant.data.local.entity.SettingsEntity
import com.example.kaoyanassistant.data.local.entity.StudySessionEntity
import com.example.kaoyanassistant.data.local.entity.SubjectEntity
import com.example.kaoyanassistant.data.local.entity.TodoEntity

@Database(
    entities = [
        SubjectEntity::class,
        StudySessionEntity::class,
        TodoEntity::class,
        DayRecordEntity::class,
        SettingsEntity::class,
        ActiveTimerEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun todoDao(): TodoDao
    abstract fun dayRecordDao(): DayRecordDao
    abstract fun settingsDao(): SettingsDao
    abstract fun activeTimerDao(): ActiveTimerDao

    companion object {
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "kaoyan_assistant.db",
            ).fallbackToDestructiveMigration(true).build()
        }
    }
}
