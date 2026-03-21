package com.example.kaoyanassistant.util

import kotlin.math.roundToInt

fun formatDurationClock(durationMillis: Long): String {
    val totalSeconds = (durationMillis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

fun formatDurationText(durationMillis: Long): String {
    val totalMinutes = (durationMillis / 60000).coerceAtLeast(0)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}小时${minutes}分"
        hours > 0 -> "${hours}小时"
        else -> "${minutes}分钟"
    }
}

fun formatDurationCompact(durationMillis: Long): String {
    if (durationMillis <= 0) return ""
    val hours = durationMillis / 3600000f
    return if (hours >= 1f) {
        "${(hours * 10).roundToInt() / 10f}h"
    } else {
        "${(durationMillis / 60000).coerceAtLeast(1)}m"
    }
}

fun formatTimeValue(hour: Int, minute: Int): String = "%02d:%02d".format(hour, minute)
