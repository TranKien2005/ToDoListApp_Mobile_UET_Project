package com.example.todolist.domain.usecase

import android.util.Log
import com.example.todolist.core.model.AiChatResponse
import com.example.todolist.core.model.ChatMessage
import com.example.todolist.core.model.CommandParams
import com.example.todolist.core.model.PendingCommand
import com.example.todolist.core.model.UserContext

/**
 * Mock implementation of AI use cases for debug builds
 * This doesn't require Gemini API key - useful for testing
 */
class MockChatWithAIUseCase : ChatWithAIUseCase {

    companion object {
        private const val TAG = "MockChatWithAI"
    }

    override suspend fun invoke(
        message: String,
        conversationHistory: List<ChatMessage>,
        userContext: UserContext
    ): Result<AiChatResponse> {
        Log.d(TAG, "Mock processing chat: $message")

        val lowerInput = message.lowercase()

        val response = when {
            lowerInput.contains("tạo task") || lowerInput.contains("create task") ||
            lowerInput.contains("thêm task") || lowerInput.contains("add task") -> {
                val title = extractTitle(message, listOf("tạo task", "create task", "thêm task", "add task")) ?: "Mock Task"
                AiChatResponse(
                    message = "Tôi sẽ tạo task '$title'. Bạn xác nhận nhé?",
                    pending_command = PendingCommand(
                        action = "CREATE_TASK",
                        params = CommandParams(
                            title = title,
                            description = "Created from mock AI",
                            duration = 60
                        ),
                        confirmationMessage = "Tạo task '$title'"
                    )
                )
            }

            lowerInput.contains("tạo mission") || lowerInput.contains("create mission") ||
            lowerInput.contains("thêm mission") || lowerInput.contains("add mission") -> {
                val title = extractTitle(message, listOf("tạo mission", "create mission", "thêm mission", "add mission")) ?: "Mock Mission"
                AiChatResponse(
                    message = "Tôi sẽ tạo mission '$title'. Bạn xác nhận nhé?",
                    pending_command = PendingCommand(
                        action = "CREATE_MISSION",
                        params = CommandParams(
                            title = title,
                            description = "Created from mock AI"
                        ),
                        confirmationMessage = "Tạo mission '$title'"
                    )
                )
            }

            lowerInput.contains("xóa task") || lowerInput.contains("delete task") -> {
                val title = extractTitle(message, listOf("xóa task", "delete task")) ?: "Task"
                AiChatResponse(
                    message = "Tôi sẽ xóa task '$title'. Bạn xác nhận nhé?",
                    pending_command = PendingCommand(
                        action = "DELETE_TASK",
                        params = CommandParams(title = title),
                        confirmationMessage = "Xóa task '$title'"
                    )
                )
            }

            lowerInput.contains("xóa mission") || lowerInput.contains("delete mission") -> {
                val title = extractTitle(message, listOf("xóa mission", "delete mission")) ?: "Mission"
                AiChatResponse(
                    message = "Tôi sẽ xóa mission '$title'. Bạn xác nhận nhé?",
                    pending_command = PendingCommand(
                        action = "DELETE_MISSION",
                        params = CommandParams(title = title),
                        confirmationMessage = "Xóa mission '$title'"
                    )
                )
            }

            lowerInput.contains("hoàn thành") || lowerInput.contains("complete") -> {
                val title = extractTitle(message, listOf("hoàn thành mission", "hoàn thành", "complete mission", "complete")) ?: "Mission"
                AiChatResponse(
                    message = "Tôi sẽ đánh dấu hoàn thành mission '$title'. Bạn xác nhận nhé?",
                    pending_command = PendingCommand(
                        action = "COMPLETE_MISSION",
                        params = CommandParams(title = title),
                        confirmationMessage = "Hoàn thành mission '$title'"
                    )
                )
            }

            lowerInput.contains("task") || lowerInput.contains("mission") ||
            lowerInput.contains("hôm nay") || lowerInput.contains("today") -> {
                val userName = userContext.user.fullName
                val taskCount = userContext.tasks.size
                val missionCount = userContext.missions.size
                AiChatResponse(
                    message = "Xin chào $userName! Bạn có $taskCount tasks và $missionCount missions. Tôi có thể giúp gì cho bạn?",
                    pending_command = null
                )
            }

            else -> {
                AiChatResponse(
                    message = "Xin chào! Tôi là trợ lý debug. Bạn có thể thử: 'tạo task họp team' hoặc 'hôm nay tôi có gì?'",
                    pending_command = null
                )
            }
        }

        return Result.success(response)
    }

    private fun extractTitle(input: String, keywords: List<String>): String? {
        for (keyword in keywords) {
            val index = input.lowercase().indexOf(keyword)
            if (index >= 0) {
                val afterKeyword = input.substring(index + keyword.length).trim()
                if (afterKeyword.isNotEmpty()) {
                    return afterKeyword
                }
            }
        }
        return null
    }
}

/**
 * Mock audio processing for debug builds
 */
class MockChatWithAudioUseCase : ChatWithAudioUseCase {

    companion object {
        private const val TAG = "MockChatWithAudio"
    }

    override suspend fun invoke(
        audioBytes: ByteArray,
        mimeType: String,
        conversationHistory: List<ChatMessage>,
        userContext: UserContext
    ): Result<AiChatResponse> {
        Log.d(TAG, "Mock processing audio: ${audioBytes.size} bytes")

        return Result.success(
            AiChatResponse(
                message = "Mock: Đã nhận audio (debug mode - không có Gemini API). Hãy thử gõ text thay vì nói nhé!",
                pending_command = null
            )
        )
    }
}

/**
 * Mock execute command for debug builds
 */
class MockExecuteCommandUseCase : ExecuteCommandUseCase {

    companion object {
        private const val TAG = "MockExecuteCommand"
    }

    override suspend fun invoke(command: PendingCommand): Result<String> {
        Log.d(TAG, "Mock executing command: ${command.action}")
        // Just return confirmation message - no actual execution in debug
        return Result.success("Mock: ${command.confirmationMessage} - thành công!")
    }
}

/**
 * Factory function to create fake AIUseCases for debug builds
 */
fun createFakeAIUseCases(): AIUseCases {
    return AIUseCases(
        chatWithAI = MockChatWithAIUseCase(),
        chatWithAudio = MockChatWithAudioUseCase(),
        executeCommand = MockExecuteCommandUseCase()
    )
}
