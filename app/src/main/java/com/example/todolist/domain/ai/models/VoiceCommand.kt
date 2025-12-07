package com.example.todolist.domain.ai.models

import kotlinx.serialization.Serializable

/**
 * Voice command action types
 */
enum class VoiceAction {
    CREATE_TASK,
    CREATE_MISSION,
    LIST_TASKS,
    LIST_MISSIONS,
    COMPLETE_TASK,
    COMPLETE_MISSION,
    DELETE_TASK,
    DELETE_MISSION,
    UPDATE_TASK,
    UPDATE_MISSION,
    UNKNOWN
}

/**
 * Parameters for voice commands
 */
@Serializable
data class CommandParams(
    val title: String? = null,
    val description: String? = null,
    val date: String? = null,           // Format: dd/MM/yyyy
    val time: String? = null,           // Format: HH:mm
    val duration: Int? = null,          // In minutes (for tasks only)
    val taskId: Int? = null,            // For update/complete/delete operations
    val missionId: Int? = null,         // For update/complete/delete operations
    val filter: String? = null          // For list operations: "today", "week", "month", "all"
)

/**
 * Voice command response from Gemini
 */
@Serializable
data class VoiceResponse(
    val action: String,                 // Will be converted to VoiceAction enum
    val params: CommandParams = CommandParams(),
    val response_text: String           // Natural language response in Vietnamese
)

/**
 * Parsed voice command ready for execution
 */
data class VoiceCommand(
    val action: VoiceAction,
    val params: CommandParams,
    val responseText: String
)

