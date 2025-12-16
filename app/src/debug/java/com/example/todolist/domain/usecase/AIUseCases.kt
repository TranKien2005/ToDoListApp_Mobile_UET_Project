package com.example.todolist.domain.usecase

import android.util.Log
import com.example.todolist.core.model.CommandParams
import com.example.todolist.core.model.VoiceAction
import com.example.todolist.core.model.VoiceCommand

/**
 * Mock implementation of AI use cases for debug builds
 * This doesn't require Gemini API key - useful for testing
 */
class MockProcessVoiceCommandUseCase : ProcessVoiceCommandUseCase {

    companion object {
        private const val TAG = "MockProcessVoiceCmd"
    }

    override suspend fun invoke(userInput: String): Result<VoiceCommand> {
        Log.d(TAG, "Mock processing voice input: $userInput")

        // Simple keyword-based parsing for testing
        val lowerInput = userInput.lowercase()

        val command = when {
            lowerInput.contains("tạo task") || lowerInput.contains("create task") -> {
                VoiceCommand(
                    action = VoiceAction.CREATE_TASK,
                    params = CommandParams(
                        title = extractTitle(userInput) ?: "Mock Task",
                        description = "Created from mock AI",
                        duration = 60
                    ),
                    responseText = "Mock: Created a new task"
                )
            }

            lowerInput.contains("tạo mission") || lowerInput.contains("create mission") -> {
                VoiceCommand(
                    action = VoiceAction.CREATE_MISSION,
                    params = CommandParams(
                        title = extractTitle(userInput) ?: "Mock Mission",
                        description = "Created from mock AI"
                    ),
                    responseText = "Mock: Created a new mission"
                )
            }

            lowerInput.contains("xóa task") || lowerInput.contains("delete task") -> {
                VoiceCommand(
                    action = VoiceAction.DELETE_TASK,
                    params = CommandParams(
                        title = extractTitle(userInput) ?: "Task"
                    ),
                    responseText = "Mock: Task deleted"
                )
            }

            lowerInput.contains("xóa mission") || lowerInput.contains("delete mission") -> {
                VoiceCommand(
                    action = VoiceAction.DELETE_MISSION,
                    params = CommandParams(
                        title = extractTitle(userInput) ?: "Mission"
                    ),
                    responseText = "Mock: Mission deleted"
                )
            }

            lowerInput.contains("hoàn thành mission") || lowerInput.contains("complete mission") -> {
                VoiceCommand(
                    action = VoiceAction.COMPLETE_MISSION,
                    params = CommandParams(
                        title = extractTitle(userInput) ?: "Mission"
                    ),
                    responseText = "Mock: Mission completed"
                )
            }

            lowerInput.contains("task") || lowerInput.contains("mission") -> {
                VoiceCommand(
                    action = VoiceAction.QUERY,
                    params = CommandParams(query = "tasks_today"),
                    responseText = "Mock: Here are your items"
                )
            }

            else -> {
                VoiceCommand(
                    action = VoiceAction.CHAT,
                    params = CommandParams(),
                    responseText = "Mock: Hello! I'm the debug assistant. Try: 'create task meeting'"
                )
            }
        }

        return Result.success(command)
    }

    private fun extractTitle(input: String): String? {
        val keywords = listOf("tạo task", "create task", "tạo mission", "create mission", 
            "xóa task", "delete task", "xóa mission", "delete mission", 
            "hoàn thành", "complete")
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
class MockProcessAudioCommandUseCase : ProcessAudioCommandUseCase {

    companion object {
        private const val TAG = "MockProcessAudioCmd"
    }

    override suspend fun invoke(audioBytes: ByteArray, mimeType: String): Result<VoiceCommand> {
        Log.d(TAG, "Mock processing audio: ${audioBytes.size} bytes")

        return Result.success(
            VoiceCommand(
                action = VoiceAction.CREATE_TASK,
                params = CommandParams(
                    title = "Mock Task from Audio",
                    duration = 60
                ),
                responseText = "Mock: Created task from audio (debug mode - no Gemini API)"
            )
        )
    }
}

/**
 * Mock execute voice command for debug builds
 */
class MockExecuteVoiceCommandUseCase : ExecuteVoiceCommandUseCase {

    companion object {
        private const val TAG = "MockExecuteVoiceCmd"
    }

    override suspend fun invoke(command: VoiceCommand): Result<String> {
        Log.d(TAG, "Mock executing command: ${command.action}")
        // Just return the response text - no actual execution in debug
        return Result.success(command.responseText)
    }
}

/**
 * Factory function to create fake AIUseCases for debug builds
 */
fun createFakeAIUseCases(): AIUseCases {
    return AIUseCases(
        processVoiceCommand = MockProcessVoiceCommandUseCase(),
        processAudioCommand = MockProcessAudioCommandUseCase(),
        executeVoiceCommand = MockExecuteVoiceCommandUseCase()
    )
}
