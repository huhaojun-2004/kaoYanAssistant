package com.example.kaoyanassistant.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter

object DateTimeUtils {
    val zoneId: ZoneId = ZoneId.systemDefault()
    private val dateFormatter = DateTimeFormatter.ofPattern("M月d日")
    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy年M月")
    private val examDate: LocalDate = LocalDate.of(2026, 12, 21)

    fun nowMillis(): Long = System.currentTimeMillis()

    fun today(): LocalDate = LocalDate.now(zoneId)

    fun todayKey(): String = today().toString()

    fun parseDate(date: String): LocalDate = LocalDate.parse(date)

    fun dateKey(date: LocalDate): String = date.toString()

    fun dateLabel(date: String): String = parseDate(date).format(dateFormatter)

    fun monthLabel(month: YearMonth): String = month.format(monthFormatter)

    fun timeLabel(millis: Long): String {
        return Instant.ofEpochMilli(millis)
            .atZone(zoneId)
            .toLocalTime()
            .format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    fun dateTimeLabel(millis: Long): String {
        return Instant.ofEpochMilli(millis)
            .atZone(zoneId)
            .toLocalDateTime()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    }

    fun toLocalDateTime(millis: Long): LocalDateTime {
        return Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDateTime()
    }

    fun startOfDayMillis(date: String): Long {
        return parseDate(date).atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    fun endOfDayMillis(date: String): Long {
        return parseDate(date).plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    fun nextTriggerMillis(hour: Int, minute: Int): Long {
        val now = LocalDateTime.now(zoneId)
        var target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!target.isAfter(now)) {
            target = target.plusDays(1)
        }
        return target.atZone(zoneId).toInstant().toEpochMilli()
    }

    fun daysUntilExam(): Long {
        return ChronoUnit.DAYS.between(today(), examDate).coerceAtLeast(0)
    }
}
