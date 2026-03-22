package com.example.kaoyanassistant.core

import android.content.Context
import com.example.kaoyanassistant.data.local.AppDatabase
import com.example.kaoyanassistant.data.repository.CheckInRepository
import com.example.kaoyanassistant.data.repository.SettingsRepository
import com.example.kaoyanassistant.data.repository.StudyRepository
import com.example.kaoyanassistant.data.repository.SubjectRepository
import com.example.kaoyanassistant.data.repository.TodoRepository
import com.example.kaoyanassistant.data.repository.UiPreferencesRepository
import com.example.kaoyanassistant.notification.NotificationHelper
import com.example.kaoyanassistant.notification.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AppContainer(
    context: Context,
    val applicationScope: CoroutineScope,
) {
    private val appContext = context.applicationContext

    val database: AppDatabase by lazy {
        AppDatabase.create(appContext)
    }

    val subjectRepository: SubjectRepository by lazy {
        SubjectRepository(database.subjectDao())
    }

    val studyRepository: StudyRepository by lazy {
        StudyRepository(
            sessionDao = database.studySessionDao(),
            activeTimerDao = database.activeTimerDao(),
        )
    }

    val todoRepository: TodoRepository by lazy {
        TodoRepository(database.todoDao())
    }

    val checkInRepository: CheckInRepository by lazy {
        CheckInRepository(database.dayRecordDao())
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(database.settingsDao())
    }

    val uiPreferencesRepository: UiPreferencesRepository by lazy {
        UiPreferencesRepository(appContext)
    }

    val notificationHelper: NotificationHelper by lazy {
        NotificationHelper(appContext)
    }

    val reminderScheduler: ReminderScheduler by lazy {
        ReminderScheduler(appContext)
    }

    fun bootstrap() {
        notificationHelper.createChannels()
        applicationScope.launch {
            subjectRepository.ensureDefaults()
            settingsRepository.ensureDefaults()
            reminderScheduler.rescheduleAll(settingsRepository.getSettings())
        }
    }
}
