package com.example.todolist.core.model

import java.time.LocalDateTime

enum class MissionStatus {
    UNSPECIFIED,
    COMPLETED,
    MISSED
}

data class Mission(
    val id: Int,
    val title: String,
    val description: String? = null,
    val deadline: LocalDateTime,
    // status: UNSPECIFIED, COMPLETED, or MISSED
    val status: MissionStatus = MissionStatus.UNSPECIFIED
)
