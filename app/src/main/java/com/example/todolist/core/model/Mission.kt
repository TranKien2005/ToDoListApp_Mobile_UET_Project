package com.example.todolist.core.model

import java.time.LocalDateTime

// Stored status in database - only what user explicitly sets
enum class MissionStoredStatus {
    UNSPECIFIED,
    COMPLETED
}

// Display status - includes computed MISSED state
enum class MissionStatus {
    ACTIVE,      // UNSPECIFIED + deadline in future
    COMPLETED,   // User marked as completed
    MISSED       // UNSPECIFIED + deadline in past (computed)
}

data class Mission(
    val id: Int,
    val title: String,
    val description: String? = null,
    val deadline: LocalDateTime,
    // Only store UNSPECIFIED or COMPLETED in database
    val storedStatus: MissionStoredStatus = MissionStoredStatus.UNSPECIFIED
) {
    // Computed property - NEVER stored in database
    val status: MissionStatus
        get() = when (storedStatus) {
            MissionStoredStatus.COMPLETED -> MissionStatus.COMPLETED
            MissionStoredStatus.UNSPECIFIED -> {
                if (deadline.isBefore(LocalDateTime.now())) {
                    MissionStatus.MISSED
                } else {
                    MissionStatus.ACTIVE
                }
            }
        }
}
