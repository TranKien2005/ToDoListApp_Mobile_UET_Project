package com.example.todolist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String?,
    // store start timestamp as epoch millis for compatibility
    val startTimeEpoch: Long,
    // duration in minutes (nullable)
    val durationMinutes: Long?,
    // repeat type stored as String name of enum (NONE/DAILY/WEEKLY/MONTHLY)
    val repeatType: String = "NONE",
    // images stored as JSON array of URIs
    val images: String? = null
    // Note: removed isCompleted field; tasks are schedule-only
)

