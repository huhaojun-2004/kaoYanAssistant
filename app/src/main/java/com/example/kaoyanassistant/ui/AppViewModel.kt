package com.example.kaoyanassistant.ui

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.room.withTransaction
import com.example.kaoyanassistant.KaoYanAssistantApplication
import com.example.kaoyanassistant.core.AppContainer
import com.example.kaoyanassistant.data.local.entity.ActiveTimerEntity
import com.example.kaoyanassistant.data.local.entity.DayRecordEntity
import com.example.kaoyanassistant.data.local.entity.SettingsEntity
import com.example.kaoyanassistant.data.local.entity.StudySessionEntity
import com.example.kaoyanassistant.data.local.entity.SubjectEntity
import com.example.kaoyanassistant.data.local.entity.TodoEntity
import com.example.kaoyanassistant.service.StudyTimerService
import com.example.kaoyanassistant.ui.model.AppUiState
import com.example.kaoyanassistant.ui.model.totalStudyForSubjectOnDate
import com.example.kaoyanassistant.util.AppBackupPayload
import com.example.kaoyanassistant.util.AppBackupSerializer
import com.example.kaoyanassistant.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(
    application: Application,
    private val container: AppContainer,
) : AndroidViewModel(application) {
    private val selectedTimerSubjectId = MutableStateFlow<Long?>(null)

    val uiState = combine(
        container.subjectRepository.subjects,
        container.studyRepository.sessions,
        container.todoRepository.todos,
        container.checkInRepository.dayRecords,
        container.settingsRepository.settings,
        container.uiPreferencesRepository.showCountdownBadge,
        container.studyRepository.activeTimer,
        selectedTimerSubjectId,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        AppUiState(
            subjects = values[0] as List<SubjectEntity>,
            sessions = values[1] as List<StudySessionEntity>,
            todos = values[2] as List<TodoEntity>,
            dayRecords = values[3] as List<DayRecordEntity>,
            settings = values[4] as SettingsEntity,
            showCountdownBadge = values[5] as Boolean,
            activeTimer = values[6] as ActiveTimerEntity?,
            selectedTimerSubjectId = values[7] as Long?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppUiState(),
    )

    fun selectTimerSubject(subjectId: Long) {
        selectedTimerSubjectId.value = subjectId
    }

    fun saveSubject(id: Long?, name: String, parentId: Long?, colorValue: Long?) {
        viewModelScope.launch {
            container.subjectRepository.save(
                id = id,
                name = name,
                parentId = parentId,
                colorValue = colorValue,
            )
        }
    }

    fun deleteSubject(id: Long) {
        viewModelScope.launch {
            val subjectMap = uiState.value.subjectMap
            val activeSubjectId = container.studyRepository.getActiveTimer()?.subjectId
            if (activeSubjectId != null && isAncestorOrSelf(id, activeSubjectId, subjectMap)) {
                stopTimerService()
                container.studyRepository.clearActiveTimer()
            }
            container.subjectRepository.delete(id)
        }
    }

    fun startTimerForSelected() {
        uiState.value.resolvedTimerSubjectId?.let(::startTimerForSubject)
    }

    fun startTimerForSubject(subjectId: Long) {
        viewModelScope.launch {
            val subject = container.subjectRepository.getById(subjectId) ?: return@launch
            val active = container.studyRepository.getActiveTimer()

            if (active?.subjectId == subjectId) {
                return@launch
            }

            if (active != null) {
                container.studyRepository.stopTimer()
            }

            val startedAt = System.currentTimeMillis()
            val carriedMillis = totalStudyForSubjectOnDate(
                sessions = uiState.value.sessions,
                subjectId = subjectId,
                date = DateTimeUtils.todayKey(),
            )
            container.studyRepository.startTimer(
                subjectId = subjectId,
                startedAt = startedAt,
                carriedMillis = carriedMillis,
            )
            selectedTimerSubjectId.value = subjectId
            startTimerService(subject.name, startedAt, carriedMillis)
        }
    }

    fun stopTimer() {
        viewModelScope.launch {
            container.studyRepository.getActiveTimer()?.subjectId?.let { activeSubjectId ->
                selectedTimerSubjectId.value = activeSubjectId
            }
            container.studyRepository.stopTimer()
            stopTimerService()
        }
    }

    fun deleteStudyTimeInRange(subjectId: Long, rangeStart: Long, rangeEnd: Long) {
        viewModelScope.launch {
            container.studyRepository.deleteSubjectTimeInRange(
                subjectId = subjectId,
                rangeStart = rangeStart,
                rangeEnd = rangeEnd,
            )
        }
    }

    fun saveTodo(id: Long?, content: String, subjectId: Long, taskDate: String) {
        viewModelScope.launch {
            container.todoRepository.save(id, content, subjectId, taskDate)
        }
    }

    fun setTodoCompleted(id: Long, completed: Boolean) {
        viewModelScope.launch {
            container.todoRepository.setCompleted(id, completed)
        }
    }

    fun deleteTodo(id: Long) {
        viewModelScope.launch {
            container.todoRepository.delete(id)
        }
    }

    fun setCheckIn(date: String, checkedIn: Boolean) {
        viewModelScope.launch {
            container.checkInRepository.setCheckIn(date, checkedIn)
        }
    }

    fun setRestDay(date: String, isRestDay: Boolean) {
        viewModelScope.launch {
            container.checkInRepository.setRestDay(date, isRestDay)
        }
    }

    fun saveSettings(settings: SettingsEntity) {
        viewModelScope.launch {
            container.settingsRepository.update(settings)
            container.reminderScheduler.rescheduleAll(settings)
        }
    }

    fun setShowCountdownBadge(show: Boolean) {
        container.uiPreferencesRepository.setShowCountdownBadge(show)
    }

    fun buildBackupJson(): String {
        return AppBackupSerializer.build(uiState.value)
    }

    suspend fun importBackupJson(json: String): Result<Unit> {
        stopTimerService()
        return withContext(Dispatchers.IO) {
            runCatching {
                val payload = AppBackupSerializer.parse(json)
                restoreBackupPayload(payload)
            }
        }
    }

    private fun startTimerService(subjectName: String, startedAt: Long, carriedMillis: Long) {
        val context = getApplication<Application>()
        val intent = Intent(context, StudyTimerService::class.java)
            .setAction(StudyTimerService.ACTION_START)
            .putExtra(StudyTimerService.EXTRA_SUBJECT_NAME, subjectName)
            .putExtra(StudyTimerService.EXTRA_STARTED_AT, startedAt)
            .putExtra(StudyTimerService.EXTRA_CARRIED_MILLIS, carriedMillis)
        ContextCompat.startForegroundService(context, intent)
    }

    private fun stopTimerService() {
        val context = getApplication<Application>()
        val intent = Intent(context, StudyTimerService::class.java)
            .setAction(StudyTimerService.ACTION_STOP)
        context.startService(intent)
    }

    private fun isAncestorOrSelf(
        targetId: Long,
        subjectId: Long,
        subjectMap: Map<Long, com.example.kaoyanassistant.data.local.entity.SubjectEntity>,
    ): Boolean {
        var cursor = subjectMap[subjectId]
        while (cursor != null) {
            if (cursor.id == targetId) return true
            cursor = cursor.parentId?.let(subjectMap::get)
        }
        return false
    }

    private suspend fun restoreBackupPayload(payload: AppBackupPayload) {
        container.database.withTransaction {
            container.database.activeTimerDao().clear()
            container.database.studySessionDao().clearAll()
            container.database.todoDao().clearAll()
            container.database.dayRecordDao().clearAll()
            container.database.subjectDao().clearAll()

            val orderedSubjects = payload.subjects.sortedWith(
                compareBy<SubjectEntity> { it.parentId != null }
                    .thenBy(SubjectEntity::sortOrder)
                    .thenBy(SubjectEntity::id),
            )
            if (orderedSubjects.isNotEmpty()) {
                container.database.subjectDao().insertAll(orderedSubjects)
            }
            if (payload.sessions.isNotEmpty()) {
                container.database.studySessionDao().insertAll(payload.sessions.sortedBy(StudySessionEntity::startTime))
            }
            if (payload.todos.isNotEmpty()) {
                container.database.todoDao().insertAll(payload.todos.sortedBy(TodoEntity::id))
            }
            if (payload.dayRecords.isNotEmpty()) {
                container.database.dayRecordDao().insertAll(payload.dayRecords.sortedBy(DayRecordEntity::date))
            }
            container.database.settingsDao().insert(payload.settings.copy(id = 1))
        }

        container.uiPreferencesRepository.setShowCountdownBadge(payload.showCountdownBadge)
        container.reminderScheduler.rescheduleAll(payload.settings.copy(id = 1))
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras,
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val container = (application as KaoYanAssistantApplication).container
                require(modelClass.isAssignableFrom(AppViewModel::class.java)) {
                    "Unsupported ViewModel class: ${modelClass.name}"
                }
                return checkNotNull(modelClass.cast(AppViewModel(application, container)))
            }
        }
    }
}
