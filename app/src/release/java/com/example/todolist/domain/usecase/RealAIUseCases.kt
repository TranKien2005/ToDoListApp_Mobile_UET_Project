package com.example.todolist.domain.usecase

import android.content.Context
import android.util.Log
import com.example.todolist.domain.ai.GeminiService
import com.example.todolist.domain.ai.VoiceCommandExecutor
import com.example.todolist.domain.ai.VoiceCommandParser
import com.example.todolist.domain.ai.models.VoiceCommand

/**
 * Real implementation of AI use cases using GeminiService
 * This is only used in release builds
 *
 * UseCase xử lý:
 * 1. Build prompt
 * 2. Gọi GeminiService để lấy raw response
 * 3. Parse JSON response
 * 4. Validate command
 */
class RealProcessVoiceCommandUseCase(
    context: Context
) : ProcessVoiceCommandUseCase {

    companion object {
        private const val TAG = "RealProcessVoiceCmd"
    }

    private val geminiService = GeminiService(context)
    private val commandParser = VoiceCommandParser()

    override suspend fun invoke(userInput: String): Result<VoiceCommand> {
        Log.d(TAG, "Processing voice input: $userInput")

        // Step 1: Build prompt (UseCase quyết định prompt format)
        val prompt = geminiService.buildVoiceCommandPrompt(userInput)

        // Step 2: Gọi AI - CHỈ lấy raw response
        val aiResult = geminiService.sendPrompt(prompt)
        if (aiResult.isFailure) {
            Log.e(TAG, "AI service error", aiResult.exceptionOrNull())
            return Result.failure(
                aiResult.exceptionOrNull() ?: Exception("AI service error")
            )
        }

        val rawResponse = aiResult.getOrNull() ?: ""
        Log.d(TAG, "Raw AI response: $rawResponse")

        // Step 3: Parse JSON response (UseCase xử lý parse)
        val parseResult = commandParser.parseResponse(rawResponse)
        if (parseResult.isFailure) {
            Log.e(TAG, "Parse error", parseResult.exceptionOrNull())
            return Result.failure(
                parseResult.exceptionOrNull() ?: Exception("Failed to parse AI response")
            )
        }

        val command = parseResult.getOrNull()!!
        Log.d(TAG, "Parsed command: ${command.action}")

        // Step 4: Validate command (UseCase xử lý validate)
        val validationResult = commandParser.validateCommand(command)
        if (validationResult.isFailure) {
            Log.e(TAG, "Validation error", validationResult.exceptionOrNull())
            return Result.failure(
                validationResult.exceptionOrNull() ?: Exception("Invalid command")
            )
        }

        Log.d(TAG, "Command validated successfully")
        return Result.success(command)
    }
}

/**
 * Process audio directly with Gemini (HỖ TRỢ TIẾNG VIỆT TốT HƠN!)
 * Gemini có thể transcribe và parse audio trực tiếp
 */
class RealProcessAudioCommandUseCase(
    context: Context
) : ProcessAudioCommandUseCase {

    companion object {
        private const val TAG = "RealProcessAudioCmd"
    }

    private val geminiService = GeminiService(context)
    private val commandParser = VoiceCommandParser()

    override suspend fun invoke(audioBytes: ByteArray, mimeType: String): Result<VoiceCommand> {
        Log.d(TAG, "Processing audio bytes: ${audioBytes.size} bytes, type: $mimeType")

        // Step 1: Gửi audio trực tiếp đến Gemini
        val aiResult = geminiService.processAudioBytes(audioBytes, mimeType)
        if (aiResult.isFailure) {
            Log.e(TAG, "AI audio service error", aiResult.exceptionOrNull())
            return Result.failure(
                aiResult.exceptionOrNull() ?: Exception("AI audio service error")
            )
        }

        val rawResponse = aiResult.getOrNull() ?: ""
        Log.d(TAG, "Raw audio response: $rawResponse")

        // Step 2: Parse JSON response
        val parseResult = commandParser.parseResponse(rawResponse)
        if (parseResult.isFailure) {
            Log.e(TAG, "Parse error", parseResult.exceptionOrNull())
            return Result.failure(
                parseResult.exceptionOrNull() ?: Exception("Failed to parse audio response")
            )
        }

        val command = parseResult.getOrNull()!!
        Log.d(TAG, "Parsed audio command: ${command.action}")

        // Step 3: Validate command
        val validationResult = commandParser.validateCommand(command)
        if (validationResult.isFailure) {
            Log.e(TAG, "Validation error", validationResult.exceptionOrNull())
            return Result.failure(
                validationResult.exceptionOrNull() ?: Exception("Invalid command")
            )
        }

        Log.d(TAG, "Audio command validated successfully")
        return Result.success(command)
    }
}

/**
 * Execute voice command (create/update/delete tasks/missions)
 * This is where business logic happens
 */
class RealExecuteVoiceCommandUseCase(
    private val taskUseCases: TaskUseCases,
    private val missionUseCases: MissionUseCases
) : ExecuteVoiceCommandUseCase {

    private val executor = VoiceCommandExecutor(taskUseCases, missionUseCases)

    override suspend fun invoke(command: VoiceCommand): Result<String> {
        return executor.execute(command)
    }
}

/**
 * Factory function to create AIUseCases for release builds
 */
fun createAIUseCases(
    context: Context,
    taskUseCases: TaskUseCases,
    missionUseCases: MissionUseCases
): AIUseCases {
    return AIUseCases(
        processVoiceCommand = RealProcessVoiceCommandUseCase(context),
        processAudioCommand = RealProcessAudioCommandUseCase(context),
        executeVoiceCommand = RealExecuteVoiceCommandUseCase(taskUseCases, missionUseCases)
    )
}
