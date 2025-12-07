package com.example.todolist.domain.usecase

import android.content.Context
import android.util.Log
import com.example.todolist.domain.ai.VoiceCommandExecutor
import com.example.todolist.domain.ai.models.CommandParams
import com.example.todolist.domain.ai.models.VoiceAction
import com.example.todolist.domain.ai.models.VoiceCommand

/**
 * Mock implementation of AI use cases for debug builds
 * This doesn't require Gemini API key - useful for testing
 */
class MockProcessVoiceCommandUseCase : ProcessVoiceCommandUseCase {

    companion object {
        private const val TAG = "MockProcessVoiceCommand"
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
                    responseText = "Đã tạo task mới trong chế độ debug"
                )
            }

            lowerInput.contains("tạo mission") || lowerInput.contains("create mission") -> {
                VoiceCommand(
                    action = VoiceAction.CREATE_MISSION,
                    params = CommandParams(
                        title = extractTitle(userInput) ?: "Mock Mission",
                        description = "Created from mock AI"
                    ),
                    responseText = "Đã tạo mission mới trong chế độ debug"
                )
            }

            lowerInput.contains("danh sách task") || lowerInput.contains("list task") -> {
                VoiceCommand(
                    action = VoiceAction.LIST_TASKS,
                    params = CommandParams(filter = "all"),
                    responseText = "Hiển thị danh sách tasks (mock)"
                )
            }

            lowerInput.contains("danh sách mission") || lowerInput.contains("list mission") -> {
                VoiceCommand(
                    action = VoiceAction.LIST_MISSIONS,
                    params = CommandParams(filter = "all"),
                    responseText = "Hiển thị danh sách missions (mock)"
                )
            }

            lowerInput.contains("hoàn thành task") || lowerInput.contains("complete task") -> {
                VoiceCommand(
                    action = VoiceAction.COMPLETE_TASK,
                    params = CommandParams(
                        title = extractTitle(userInput) ?: "Task"
                    ),
                    responseText = "Đã đánh dấu task hoàn thành (mock)"
                )
            }

            lowerInput.contains("hoàn thành mission") || lowerInput.contains("complete mission") -> {
                VoiceCommand(
                    action = VoiceAction.COMPLETE_MISSION,
                    params = CommandParams(
                        title = extractTitle(userInput) ?: "Mission"
                    ),
                    responseText = "Đã đánh dấu mission hoàn thành (mock)"
                )
            }

            else -> {
                VoiceCommand(
                    action = VoiceAction.UNKNOWN,
                    params = CommandParams(),
                    responseText = "Xin lỗi, tôi chưa hiểu lệnh này trong chế độ debug. Thử: 'tạo task', 'tạo mission', 'danh sách task'"
                )
            }
        }

        return Result.success(command)
    }

    private fun extractTitle(input: String): String? {
        // Simple extraction - get text after common keywords
        val keywords = listOf("tạo task", "create task", "tạo mission", "create mission", "hoàn thành", "complete")
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

        // Mock: giả lập transcribe
        return Result.success(
            VoiceCommand(
                action = VoiceAction.CREATE_TASK,
                params = CommandParams(
                    title = "Mock Task from Audio",
                    duration = 60
                ),
                responseText = "Debug: Đã xử lý audio mock (không có Gemini API)"
            )
        )
    }
}

/**
 * Mock execute voice command for debug builds
 * Uses real executor but with mock data
 */
class MockExecuteVoiceCommandUseCase(
    private val taskUseCases: TaskUseCases,
    private val missionUseCases: MissionUseCases
) : ExecuteVoiceCommandUseCase {

    private val executor = VoiceCommandExecutor(taskUseCases, missionUseCases)

    override suspend fun invoke(command: VoiceCommand): Result<String> {
        return executor.execute(command)
    }
}

/**
 * Factory function to create AIUseCases for debug builds
 */
fun createAIUseCases(
    context: Context,
    taskUseCases: TaskUseCases,
    missionUseCases: MissionUseCases
): AIUseCases {
    return AIUseCases(
        processVoiceCommand = MockProcessVoiceCommandUseCase(),
        processAudioCommand = MockProcessAudioCommandUseCase(),
        executeVoiceCommand = MockExecuteVoiceCommandUseCase(taskUseCases, missionUseCases)
    )
}
