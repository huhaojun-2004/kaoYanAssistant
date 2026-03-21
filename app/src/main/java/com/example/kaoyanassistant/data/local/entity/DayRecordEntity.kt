package com.example.kaoyanassistant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_records")
data class DayRecordEntity(
    @PrimaryKey val date: String,
    val isCheckIn: Boolean = false,
    val isRestDay: Boolean = false,
)
