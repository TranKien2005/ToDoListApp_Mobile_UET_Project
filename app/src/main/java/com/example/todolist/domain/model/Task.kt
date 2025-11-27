package com.example.todolist.domain.model

import java.time.LocalDateTime

// Business model (no Room/Network annotations)
data class Task(
    val id: Int,
    val title: String,
    val description: String? = null,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val isCompleted: Boolean = false
)

