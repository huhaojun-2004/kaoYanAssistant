package com.example.kaoyanassistant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val checkInReminderEnabled: Boolean = true,
    val checkInReminderHour: Int = 8,
    val checkInReminderMinute: Int = 0,
    val todoReminderEnabled: Boolean = true,
    val todoReminderHour: Int = 20,
    val todoReminderMinute: Int = 0,
)
