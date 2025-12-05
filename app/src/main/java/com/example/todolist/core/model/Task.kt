package com.example.todolist.core.model

import java.time.LocalDateTime

// Shared core business model for Task
enum class RepeatType {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY
}

data class Task(
    val id: Int,
    val title: String,
    val description: String? = null,
    // Start date/time of the task (this is the anchor for repeats)
    val startTime: LocalDateTime,
    // Duration of the task in minutes. Null means no specified duration.
    val durationMinutes: Long? = null,
    // Repeat type. NONE means single occurrence on startTime date.
    val repeatType: RepeatType = RepeatType.NONE
    // Note: tasks are schedule-only and do NOT have completion state
)
