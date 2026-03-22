package com.example.kaoyanassistant.util

import com.example.kaoyanassistant.data.local.entity.DayRecordEntity
import com.example.kaoyanassistant.data.local.entity.SettingsEntity
import com.example.kaoyanassistant.data.local.entity.StudySessionEntity
import com.example.kaoyanassistant.data.local.entity.SubjectEntity
import com.example.kaoyanassistant.data.local.entity.TodoEntity
import com.example.kaoyanassistant.ui.model.AppUiState
import org.json.JSONArray
import org.json.JSONObject

data class AppBackupPayload(
    val showCountdownBadge: Boolean,
    val settings: SettingsEntity,
    val subjects: List<SubjectEntity>,
    val sessions: List<StudySessionEntity>,
    val todos: List<TodoEntity>,
    val dayRecords: List<DayRecordEntity>,
)

object AppBackupSerializer {
    private const val BACKUP_SCHEMA_VERSION = 1

    fun build(uiState: AppUiState): String {
        return JSONObject()
            .put("schemaVersion", BACKUP_SCHEMA_VERSION)
            .put("exportedAt", DateTimeUtils.nowMillis())
            .put("showCountdownBadge", uiState.showCountdownBadge)
            .put("settings", uiState.settings.toJson())
            .put(
                "subjects",
                JSONArray().apply {
                    uiState.subjects
                        .sortedWith(compareBy(SubjectEntity::sortOrder, SubjectEntity::id))
                        .forEach { put(it.toJson()) }
                },
            )
            .put(
                "sessions",
                JSONArray().apply {
                    uiState.sessions
                        .sortedBy(StudySessionEntity::startTime)
                        .forEach { put(it.toJson()) }
                },
            )
            .put(
                "todos",
                JSONArray().apply {
                    uiState.todos
                        .sortedByDescending(TodoEntity::updatedAt)
                        .forEach { put(it.toJson()) }
                },
            )
            .put(
                "dayRecords",
                JSONArray().apply {
                    uiState.dayRecords
                        .sortedBy(DayRecordEntity::date)
                        .forEach { put(it.toJson()) }
                },
            )
            .toString(2)
    }

    fun parse(json: String): AppBackupPayload {
        val root = JSONObject(json)
        val schemaVersion = root.optInt("schemaVersion", BACKUP_SCHEMA_VERSION)
        require(schemaVersion == BACKUP_SCHEMA_VERSION) { "不支持的备份版本" }
        require(root.has("settings")) { "缺少 settings" }
        require(root.has("subjects")) { "缺少 subjects" }
        require(root.has("sessions")) { "缺少 sessions" }
        require(root.has("todos")) { "缺少 todos" }
        require(root.has("dayRecords")) { "缺少 dayRecords" }
        return AppBackupPayload(
            showCountdownBadge = root.optBoolean("showCountdownBadge", true),
            settings = root.optJSONObject("settings").toSettingsEntity(),
            subjects = root.optJSONArray("subjects").toSubjectEntities(),
            sessions = root.optJSONArray("sessions").toStudySessions(),
            todos = root.optJSONArray("todos").toTodos(),
            dayRecords = root.optJSONArray("dayRecords").toDayRecords(),
        )
    }
}

private fun SubjectEntity.toJson(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("name", name)
        .put("parentId", parentId ?: JSONObject.NULL)
        .put("colorValue", colorValue)
        .put("sortOrder", sortOrder)
        .put("isDefault", isDefault)
}

private fun StudySessionEntity.toJson(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("subjectId", subjectId)
        .put("startTime", startTime)
        .put("endTime", endTime)
        .put("durationMillis", durationMillis)
}

private fun TodoEntity.toJson(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("content", content)
        .put("subjectId", subjectId)
        .put("dueDate", dueDate ?: JSONObject.NULL)
        .put("isCompleted", isCompleted)
        .put("completedAt", completedAt ?: JSONObject.NULL)
        .put("createdAt", createdAt)
        .put("updatedAt", updatedAt)
}

private fun DayRecordEntity.toJson(): JSONObject {
    return JSONObject()
        .put("date", date)
        .put("isCheckIn", isCheckIn)
        .put("isRestDay", isRestDay)
}

private fun SettingsEntity.toJson(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("checkInReminderEnabled", checkInReminderEnabled)
        .put("checkInReminderHour", checkInReminderHour)
        .put("checkInReminderMinute", checkInReminderMinute)
        .put("todoReminderEnabled", todoReminderEnabled)
        .put("todoReminderHour", todoReminderHour)
        .put("todoReminderMinute", todoReminderMinute)
}

private fun JSONObject?.toSettingsEntity(): SettingsEntity {
    val source = this ?: JSONObject()
    return SettingsEntity(
        id = 1,
        checkInReminderEnabled = source.optBoolean("checkInReminderEnabled", true),
        checkInReminderHour = source.optInt("checkInReminderHour", 8),
        checkInReminderMinute = source.optInt("checkInReminderMinute", 0),
        todoReminderEnabled = source.optBoolean("todoReminderEnabled", true),
        todoReminderHour = source.optInt("todoReminderHour", 20),
        todoReminderMinute = source.optInt("todoReminderMinute", 0),
    )
}

private fun JSONArray?.toSubjectEntities(): List<SubjectEntity> {
    if (this == null) return emptyList()
    return List(length()) { index ->
        val source = getJSONObject(index)
        SubjectEntity(
            id = source.optLong("id", 0L),
            name = source.optString("name", ""),
            parentId = source.optNullableLong("parentId"),
            colorValue = source.optLong("colorValue", 0xFF28594A),
            sortOrder = source.optInt("sortOrder", index),
            isDefault = source.optBoolean("isDefault", false),
        )
    }
}

private fun JSONArray?.toStudySessions(): List<StudySessionEntity> {
    if (this == null) return emptyList()
    return List(length()) { index ->
        val source = getJSONObject(index)
        StudySessionEntity(
            id = source.optLong("id", 0L),
            subjectId = source.optLong("subjectId", 0L),
            startTime = source.optLong("startTime", 0L),
            endTime = source.optLong("endTime", 0L),
            durationMillis = source.optLong("durationMillis", 0L),
        )
    }
}

private fun JSONArray?.toTodos(): List<TodoEntity> {
    if (this == null) return emptyList()
    return List(length()) { index ->
        val source = getJSONObject(index)
        TodoEntity(
            id = source.optLong("id", 0L),
            content = source.optString("content", ""),
            subjectId = source.optLong("subjectId", 0L),
            dueDate = source.optNullableString("dueDate"),
            isCompleted = source.optBoolean("isCompleted", false),
            completedAt = source.optNullableLong("completedAt"),
            createdAt = source.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = source.optLong("updatedAt", System.currentTimeMillis()),
        )
    }
}

private fun JSONArray?.toDayRecords(): List<DayRecordEntity> {
    if (this == null) return emptyList()
    return List(length()) { index ->
        val source = getJSONObject(index)
        DayRecordEntity(
            date = source.optString("date", ""),
            isCheckIn = source.optBoolean("isCheckIn", false),
            isRestDay = source.optBoolean("isRestDay", false),
        )
    }
}

private fun JSONObject.optNullableLong(key: String): Long? {
    return if (has(key) && !isNull(key)) optLong(key) else null
}

private fun JSONObject.optNullableString(key: String): String? {
    return if (has(key) && !isNull(key)) optString(key) else null
}
