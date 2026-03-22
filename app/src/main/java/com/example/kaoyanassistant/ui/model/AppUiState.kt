package com.example.kaoyanassistant.ui.model

import com.example.kaoyanassistant.data.local.entity.ActiveTimerEntity
import com.example.kaoyanassistant.data.local.entity.DayRecordEntity
import com.example.kaoyanassistant.data.local.entity.SettingsEntity
import com.example.kaoyanassistant.data.local.entity.StudySessionEntity
import com.example.kaoyanassistant.data.local.entity.SubjectEntity
import com.example.kaoyanassistant.data.local.entity.TodoEntity

data class AppUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val sessions: List<StudySessionEntity> = emptyList(),
    val todos: List<TodoEntity> = emptyList(),
    val dayRecords: List<DayRecordEntity> = emptyList(),
    val settings: SettingsEntity = SettingsEntity(),
    val showCountdownBadge: Boolean = true,
    val activeTimer: ActiveTimerEntity? = null,
    val selectedTimerSubjectId: Long? = null,
) {
    val subjectMap: Map<Long, SubjectEntity>
        get() = subjects.associateBy { it.id }

    val dayRecordMap: Map<String, DayRecordEntity>
        get() = dayRecords.associateBy { it.date }

    val activeSubject: SubjectEntity?
        get() = activeTimer?.subjectId?.let(subjectMap::get)

    val selectedTimerSubject: SubjectEntity?
        get() = resolvedTimerSubjectId?.let(subjectMap::get)

    val resolvedTimerSubjectId: Long?
        get() = selectedTimerSubjectId ?: activeTimer?.subjectId
}
