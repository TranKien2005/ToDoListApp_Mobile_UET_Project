package com.example.todolist.data.mapper

import android.util.Log
import com.example.todolist.core.model.VoiceAction
import com.example.todolist.core.model.VoiceCommand
import com.example.todolist.core.model.VoiceResponse
import kotlinx.serialization.json.Json

/**
 * Mapper để chuyển đổi JSON response từ Gemini thành VoiceCommand
 * Được đặt ở data layer vì chịu trách nhiệm parse external data format
 */
object VoiceCommandMapper {

    private const val TAG = "VoiceCommandMapper"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * Parse JSON response từ AI thành VoiceCommand
     */
    fun fromJsonResponse(jsonResponse: String): Result<VoiceCommand> {
        return try {
            // Clean response (remove markdown code blocks if any)
            val cleanedJson = cleanJsonResponse(jsonResponse)

            Log.d(TAG, "Parsing JSON: $cleanedJson")

            // Parse JSON to VoiceResponse
            val response = json.decodeFromString<VoiceResponse>(cleanedJson)

            // Convert action string to enum
            val action = parseAction(response.action)

            // Create VoiceCommand
            val command = VoiceCommand(
                action = action,
                params = response.params,
                responseText = response.response_text
            )

            Log.d(TAG, "Parsed command: $command")
            Result.success(command)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse response: $jsonResponse", e)
            Result.failure(e)
        }
    }

    /**
     * Validate command có đủ params không
     */
    fun validateCommand(command: VoiceCommand): Result<Unit> {
        return when (command.action) {
            VoiceAction.CREATE_TASK -> {
                if (command.params.title.isNullOrBlank()) {
                    Result.failure(Exception("Task title is required"))
                } else {
                    Result.success(Unit)
                }
            }
            VoiceAction.CREATE_MISSION -> {
                if (command.params.title.isNullOrBlank()) {
                    Result.failure(Exception("Mission title is required"))
                } else {
                    Result.success(Unit)
                }
            }
            VoiceAction.DELETE_TASK -> {
                if (command.params.title.isNullOrBlank() && command.params.taskId == null) {
                    Result.failure(Exception("Task title or ID is required"))
                } else {
                    Result.success(Unit)
                }
            }
            VoiceAction.COMPLETE_MISSION, VoiceAction.DELETE_MISSION -> {
                if (command.params.title.isNullOrBlank() && command.params.missionId == null) {
                    Result.failure(Exception("Mission title or ID is required"))
                } else {
                    Result.success(Unit)
                }
            }
            else -> Result.success(Unit)
        }
    }

    /**
     * Clean JSON response (remove markdown, extra whitespace, etc.)
     */
    private fun cleanJsonResponse(response: String): String {
        var cleaned = response.trim()

        // Remove markdown code blocks
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.removePrefix("```json").trim()
        }
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.removePrefix("```").trim()
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.removeSuffix("```").trim()
        }

        return cleaned
    }

    /**
     * Convert action string to VoiceAction enum
     */
    private fun parseAction(actionStr: String): VoiceAction {
        return try {
            VoiceAction.valueOf(actionStr.uppercase())
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Unknown action: $actionStr")
            VoiceAction.UNKNOWN
        }
    }
}