package com.example.todolist.core.model

import kotlinx.serialization.Serializable

/**
 * Role trong cuộc hội thoại
 */
enum class ChatRole {
    USER,
    ASSISTANT
}

/**
 * Loại command AI có thể yêu cầu thực hiện
 */
enum class CommandAction {
    // Task operations
    CREATE_TASK,
    DELETE_TASK,
    UPDATE_TASK,
    
    // Mission operations
    CREATE_MISSION,
    DELETE_MISSION,
    UPDATE_MISSION,
    COMPLETE_MISSION
}

/**
 * Tham số cho command
 */
@Serializable
data class CommandParams(
    val title: String? = null,
    val description: String? = null,
    val date: String? = null,           // Format: dd/MM/yyyy
    val time: String? = null,           // Format: HH:mm
    val duration: Int? = null,          // In minutes (for tasks only)
    val taskId: Int? = null,            // For update/delete operations
    val missionId: Int? = null,         // For update/complete/delete operations
    val query: String? = null           // For QUERY action: "tasks_today", "missions_week", etc.
)

/**
 * Command đang chờ xác nhận từ người dùng
 */
@Serializable
data class PendingCommand(
    val action: String,                  // Sẽ convert sang CommandAction
    val params: CommandParams = CommandParams(),
    val confirmationMessage: String      // Message hiển thị cho user để confirm
) {
    fun toCommandAction(): CommandAction? {
        return try {
            CommandAction.valueOf(action)
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Response từ AI
 */
@Serializable
data class AiChatResponse(
    val message: String,
    val pending_command: PendingCommand? = null
)

/**
 * Tin nhắn trong cuộc hội thoại
 */
data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val role: ChatRole,
    val content: String,
    val pendingCommand: PendingCommand? = null,  // Command đang chờ confirm (chỉ cho ASSISTANT)
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Context của người dùng để gửi cho AI
 */
data class UserContext(
    val user: User,
    val tasks: List<Task>,
    val missions: List<Mission>,
    val preferredLanguage: AppLanguage = AppLanguage.ENGLISH
)
