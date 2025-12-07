package com.example.todolist.domain.usecase

import com.example.todolist.domain.ai.models.VoiceCommand

/**
 * Interface for AI-related use cases
 * - In main: only interface definition
 * - In release: real implementation using GeminiService
 * - In debug: mock implementation for testing without API key
 */
interface ProcessVoiceCommandUseCase {
    /**
     * Process voice input from user
     * @param userInput Raw text from speech recognition
     * @return Result containing parsed VoiceCommand or error
     */
    suspend operator fun invoke(userInput: String): Result<VoiceCommand>
}

/**
 * Process audio directly with Gemini (better Vietnamese support)
 */
interface ProcessAudioCommandUseCase {
    /**
     * Process audio bytes directly
     * @param audioBytes Audio data
     * @param mimeType Audio format (default: audio/wav)
     * @return Result containing parsed VoiceCommand or error
     */
    suspend operator fun invoke(audioBytes: ByteArray, mimeType: String = "audio/wav"): Result<VoiceCommand>
}

/**
 * Execute voice command (create task, complete mission, etc.)
 * @param command The voice command to execute
 * @return Result containing response message or error
 */
interface ExecuteVoiceCommandUseCase {
    suspend operator fun invoke(command: VoiceCommand): Result<String>
}

/**
 * Aggregator for all AI use cases
 */
data class AIUseCases(
    val processVoiceCommand: ProcessVoiceCommandUseCase,
    val processAudioCommand: ProcessAudioCommandUseCase,
    val executeVoiceCommand: ExecuteVoiceCommandUseCase
)
