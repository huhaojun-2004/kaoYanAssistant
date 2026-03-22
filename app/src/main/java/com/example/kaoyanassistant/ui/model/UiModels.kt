package com.example.kaoyanassistant.ui.model

import com.example.kaoyanassistant.data.local.entity.DayRecordEntity
import com.example.kaoyanassistant.data.local.entity.StudySessionEntity
import com.example.kaoyanassistant.data.local.entity.SubjectEntity
import com.example.kaoyanassistant.data.local.entity.TodoEntity
import com.example.kaoyanassistant.util.DateTimeUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import kotlin.math.max
import kotlin.math.min

private const val DAY_MILLIS = 24 * 60 * 60 * 1000L

data class SubjectTreeNode(
    val subject: SubjectEntity,
    val children: List<SubjectTreeNode> = emptyList(),
)

data class SubjectDurationSummary(
    val subject: SubjectEntity,
    val durationMillis: Long,
)

data class CalendarDayUi(
    val date: LocalDate,
    val inCurrentMonth: Boolean,
    val studyMillis: Long,
    val record: DayRecordEntity?,
) {
    val marker: String
        get() = when {
            studyMillis > 0 -> com.example.kaoyanassistant.util.formatDurationCompact(studyMillis)
            record?.isRestDay == true -> "😴"
            record?.isCheckIn == true -> "✅"
            else -> ""
        }
}

data class DailyTimelineItem(
    val subject: SubjectEntity?,
    val startTime: Long,
    val endTime: Long,
    val durationMillis: Long,
)

data class ClockStudySegment(
    val subjectId: Long,
    val subjectName: String,
    val colorValue: Long,
    val startMillisWithinCycle: Long,
    val endMillisWithinCycle: Long,
)

fun buildSubjectTree(subjects: List<SubjectEntity>): List<SubjectTreeNode> {
    return subjects
        .sortedWith(compareBy(SubjectEntity::sortOrder, SubjectEntity::id))
        .map { SubjectTreeNode(subject = it) }
}

fun flattenedSubjects(subjects: List<SubjectEntity>): List<SubjectEntity> {
    return subjects.sortedWith(compareBy(SubjectEntity::sortOrder, SubjectEntity::id))
}

fun displaySubjectName(subject: SubjectEntity, subjectMap: Map<Long, SubjectEntity>): String {
    return subject.name
}

fun groupTodosBySubject(
    todos: List<TodoEntity>,
    subjectMap: Map<Long, SubjectEntity>,
): List<Pair<SubjectEntity?, List<TodoEntity>>> {
    return todos.groupBy { subjectMap[it.subjectId] }
        .toList()
        .sortedBy { it.first?.sortOrder ?: Int.MAX_VALUE }
}

fun buildDaySubjectSummary(
    subjects: List<SubjectEntity>,
    sessions: List<StudySessionEntity>,
    date: String,
): List<SubjectDurationSummary> {
    val subjectMap = subjects.associateBy { it.id }
    val start = DateTimeUtils.startOfDayMillis(date)
    val end = DateTimeUtils.endOfDayMillis(date)
    return sessions
        .mapNotNull { session ->
            val overlap = overlapDuration(session.startTime, session.endTime, start, end)
            val subject = subjectMap[session.subjectId]
            if (overlap <= 0 || subject == null) null else session.subjectId to overlap
        }
        .groupBy({ it.first }, { it.second })
        .mapNotNull { (subjectId, durations) ->
            subjectMap[subjectId]?.let { SubjectDurationSummary(it, durations.sum()) }
        }
        .sortedByDescending { it.durationMillis }
}

fun buildWeekCategorySummary(
    subjects: List<SubjectEntity>,
    sessions: List<StudySessionEntity>,
    weekStart: LocalDate,
): List<SubjectDurationSummary> {
    val subjectMap = subjects.associateBy { it.id }
    val rangeStart = weekStart.atStartOfDay(DateTimeUtils.zoneId).toInstant().toEpochMilli()
    val rangeEnd = weekStart.plusDays(7).atStartOfDay(DateTimeUtils.zoneId).toInstant().toEpochMilli()
    val totals = mutableMapOf<Long, Long>()

    sessions.forEach { session ->
        val overlap = overlapDuration(session.startTime, session.endTime, rangeStart, rangeEnd)
        if (overlap <= 0) return@forEach
        val subject = subjectMap[session.subjectId] ?: return@forEach
        totals[subject.id] = totals.getOrDefault(subject.id, 0L) + overlap
    }

    return subjects
        .sortedWith(compareBy(SubjectEntity::sortOrder, SubjectEntity::id))
        .mapNotNull { subject ->
            val duration = totals.getOrDefault(subject.id, 0L)
            if (duration <= 0L) null else SubjectDurationSummary(subject, duration)
        }
}

fun buildMonthCategorySummary(
    subjects: List<SubjectEntity>,
    sessions: List<StudySessionEntity>,
    month: YearMonth,
): List<SubjectDurationSummary> {
    val subjectMap = subjects.associateBy { it.id }
    val rangeStart = month.atDay(1).atStartOfDay(DateTimeUtils.zoneId).toInstant().toEpochMilli()
    val rangeEnd = month.plusMonths(1).atDay(1).atStartOfDay(DateTimeUtils.zoneId).toInstant().toEpochMilli()
    val totals = mutableMapOf<Long, Long>()

    sessions.forEach { session ->
        val overlap = overlapDuration(session.startTime, session.endTime, rangeStart, rangeEnd)
        if (overlap <= 0) return@forEach
        val subject = subjectMap[session.subjectId] ?: return@forEach
        totals[subject.id] = totals.getOrDefault(subject.id, 0L) + overlap
    }

    return subjects
        .sortedWith(compareBy(SubjectEntity::sortOrder, SubjectEntity::id))
        .mapNotNull { subject ->
            val duration = totals.getOrDefault(subject.id, 0L)
            if (duration <= 0L) null else SubjectDurationSummary(subject, duration)
        }
}

fun buildFourOhEightDetailSummary(
    subjects: List<SubjectEntity>,
    sessions: List<StudySessionEntity>,
    weekStart: LocalDate,
): List<SubjectDurationSummary> {
    val subjectMap = subjects.associateBy { it.id }
    val fourOhEight = subjects.firstOrNull { it.parentId == null && it.name == "408" } ?: return emptyList()
    val children = subjects.filter { it.parentId == fourOhEight.id }.sortedBy { it.sortOrder }
    val rangeStart = weekStart.atStartOfDay(DateTimeUtils.zoneId).toInstant().toEpochMilli()
    val rangeEnd = weekStart.plusDays(7).atStartOfDay(DateTimeUtils.zoneId).toInstant().toEpochMilli()
    val totals = mutableMapOf<Long, Long>()

    sessions.forEach { session ->
        val overlap = overlapDuration(session.startTime, session.endTime, rangeStart, rangeEnd)
        if (overlap <= 0) return@forEach
        val directChild = findDirectChildUnder(fourOhEight.id, session.subjectId, subjectMap) ?: return@forEach
        totals[directChild.id] = totals.getOrDefault(directChild.id, 0L) + overlap
    }

    return children.map { child ->
        SubjectDurationSummary(child, totals.getOrDefault(child.id, 0L))
    }
}

fun buildWeeklyTrend(
    sessions: List<StudySessionEntity>,
    weekStart: LocalDate,
): List<Pair<LocalDate, Long>> {
    return (0..6).map { offset ->
        val date = weekStart.plusDays(offset.toLong())
        date to totalStudyForDate(sessions, date.toString())
    }
}

fun buildCalendarMonth(
    month: YearMonth,
    sessions: List<StudySessionEntity>,
    records: List<DayRecordEntity>,
): List<CalendarDayUi> {
    val monthStart = month.atDay(1)
    val gridStart = monthStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val recordMap = records.associateBy { it.date }

    return (0..41).map { index ->
        val date = gridStart.plusDays(index.toLong())
        CalendarDayUi(
            date = date,
            inCurrentMonth = date.month == month.month,
            studyMillis = totalStudyForDate(sessions, date.toString()),
            record = recordMap[date.toString()],
        )
    }
}

fun buildTimeline(
    date: String,
    subjects: List<SubjectEntity>,
    sessions: List<StudySessionEntity>,
): List<DailyTimelineItem> {
    val subjectMap = subjects.associateBy { it.id }
    val start = DateTimeUtils.startOfDayMillis(date)
    val end = DateTimeUtils.endOfDayMillis(date)

    return sessions.mapNotNull { session ->
        val clippedStart = max(session.startTime, start)
        val clippedEnd = min(session.endTime, end)
        if (clippedEnd <= clippedStart) {
            null
        } else {
            DailyTimelineItem(
                subject = subjectMap[session.subjectId],
                startTime = clippedStart,
                endTime = clippedEnd,
                durationMillis = clippedEnd - clippedStart,
            )
        }
    }.sortedBy { it.startTime }
}

fun totalStudyForDate(
    sessions: List<StudySessionEntity>,
    date: String,
): Long {
    val start = DateTimeUtils.startOfDayMillis(date)
    val end = DateTimeUtils.endOfDayMillis(date)
    return sessions.sumOf { overlapDuration(it.startTime, it.endTime, start, end) }
}

fun totalStudyForSubjectOnDate(
    sessions: List<StudySessionEntity>,
    subjectId: Long,
    date: String,
): Long {
    val start = DateTimeUtils.startOfDayMillis(date)
    val end = DateTimeUtils.endOfDayMillis(date)
    return sessions
        .filter { it.subjectId == subjectId }
        .sumOf { overlapDuration(it.startTime, it.endTime, start, end) }
}

fun totalStudyForMonth(
    sessions: List<StudySessionEntity>,
    month: YearMonth,
): Long {
    return (1..month.lengthOfMonth()).sumOf { day ->
        totalStudyForDate(sessions, month.atDay(day).toString())
    }
}

fun restDaysOfMonth(records: List<DayRecordEntity>, month: YearMonth): Int {
    return records.count { it.isRestDay && YearMonth.from(DateTimeUtils.parseDate(it.date)) == month }
}

fun currentStreak(records: List<DayRecordEntity>): Int {
    val recordMap = records.associateBy { it.date }
    var cursor = DateTimeUtils.today()
    val todayRecord = recordMap[cursor.toString()]
    if (todayRecord == null || (!todayRecord.isCheckIn && !todayRecord.isRestDay)) {
        cursor = cursor.minusDays(1)
    }

    var streak = 0
    while (true) {
        val record = recordMap[cursor.toString()]
        if (record?.isCheckIn == true || record?.isRestDay == true) {
            streak += 1
            cursor = cursor.minusDays(1)
        } else {
            break
        }
    }
    return streak
}

fun buildTwentyFourHourClockStudySegments(
    subjects: List<SubjectEntity>,
    sessions: List<StudySessionEntity>,
    rangeStart: Long,
    rangeEnd: Long,
): List<ClockStudySegment> {
    if (rangeEnd <= rangeStart) return emptyList()
    val subjectMap = subjects.associateBy { it.id }

    data class RawSegment(
        val subjectId: Long,
        val subjectName: String,
        val colorValue: Long,
        val start: Long,
        val end: Long,
    )

    val rawSegments = buildList {
        sessions.forEach { session ->
            val subject = subjectMap[session.subjectId] ?: return@forEach
            val clippedStart = max(session.startTime, rangeStart)
            val clippedEnd = min(session.endTime, rangeEnd)
            if (clippedEnd <= clippedStart) return@forEach

            var current = DateTimeUtils.toLocalDateTime(clippedStart)
            val end = DateTimeUtils.toLocalDateTime(clippedEnd)
            while (current.isBefore(end)) {
                val boundary = nextDayBoundary(current)
                val segmentEnd = if (boundary.isBefore(end)) boundary else end
                val startOffset = millisWithinDay(current)
                val endOffset = if (segmentEnd == boundary) {
                    DAY_MILLIS
                } else {
                    millisWithinDay(segmentEnd)
                }
                if (endOffset > startOffset) {
                    add(
                        RawSegment(
                            subjectId = subject.id,
                            subjectName = subject.name,
                            colorValue = subject.colorValue,
                            start = startOffset,
                            end = endOffset,
                        ),
                    )
                }
                current = segmentEnd
            }
        }
    }.sortedWith(
        compareBy<RawSegment>(RawSegment::subjectId)
            .thenBy(RawSegment::start),
    )

    if (rawSegments.isEmpty()) return emptyList()

    val merged = mutableListOf<RawSegment>()
    rawSegments.forEach { segment ->
        val last = merged.lastOrNull()
        if (
            last == null ||
            segment.subjectId != last.subjectId ||
            segment.start > last.end
        ) {
            merged += segment
        } else {
            merged[merged.lastIndex] = last.copy(end = max(last.end, segment.end))
        }
    }

    return merged.map { segment ->
        ClockStudySegment(
            subjectId = segment.subjectId,
            subjectName = segment.subjectName,
            colorValue = segment.colorValue,
            startMillisWithinCycle = segment.start,
            endMillisWithinCycle = segment.end,
        )
    }
}

private fun nextDayBoundary(dateTime: LocalDateTime): LocalDateTime {
    return dateTime.toLocalDate().plusDays(1).atStartOfDay()
}

private fun millisWithinDay(dateTime: LocalDateTime): Long {
    val time = dateTime.toLocalTime()
    val millisSinceMidnight = time.toSecondOfDay() * 1_000L + time.nano / 1_000_000L
    return millisSinceMidnight % DAY_MILLIS
}

fun startOfCurrentWeek(): LocalDate {
    return DateTimeUtils.today().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
}

private fun overlapDuration(start: Long, end: Long, rangeStart: Long, rangeEnd: Long): Long {
    return max(0L, min(end, rangeEnd) - max(start, rangeStart))
}

private fun findTopLevelSubject(
    subjectId: Long,
    subjectMap: Map<Long, SubjectEntity>,
): SubjectEntity? {
    var current = subjectMap[subjectId] ?: return null
    while (current.parentId != null) {
        current = subjectMap[current.parentId] ?: return current
    }
    return current
}

private fun findDirectChildUnder(
    rootId: Long,
    subjectId: Long,
    subjectMap: Map<Long, SubjectEntity>,
): SubjectEntity? {
    var current = subjectMap[subjectId] ?: return null
    if (current.id == rootId) return null
    while (true) {
        val parentId = current.parentId ?: return null
        if (parentId == rootId) return current
        current = subjectMap[parentId] ?: return null
    }
}
