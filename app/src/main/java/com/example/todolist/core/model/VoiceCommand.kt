package com.example.todolist.core.model

import kotlinx.serialization.Serializable

/**
 * Voice command action types
 */
enum class VoiceAction {
    // Task operations
    CREATE_TASK,
    DELETE_TASK,
    UPDATE_TASK,
    
    // Mission operations
    CREATE_MISSION,
    DELETE_MISSION,
    UPDATE_MISSION,
    COMPLETE_MISSION,
    
    // Query - để app xử lý và lấy data
    QUERY,
    
    // Chat - trò chuyện thông thường
    CHAT,
    
    // Unknown - không hiểu yêu cầu
    UNKNOWN
}

// NOTE: CommandParams is now defined in ChatMessage.kt - reuse it from there

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
