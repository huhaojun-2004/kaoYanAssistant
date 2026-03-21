package com.example.kaoyanassistant.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "active_timer",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("subjectId"),
    ],
)
data class ActiveTimerEntity(
    @PrimaryKey val id: Int = 1,
    val subjectId: Long,
    val startedAt: Long,
    val carriedMillis: Long = 0L,
)
