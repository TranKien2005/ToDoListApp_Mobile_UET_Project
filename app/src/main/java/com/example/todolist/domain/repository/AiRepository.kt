package com.example.todolist.domain.repository

import com.example.todolist.core.model.VoiceCommand

/**
 * Interface để giao tiếp với AI service
 * Implementation ở data/remote/ai/
 */
interface AiRepository {
    /**
     * Xử lý text input và trả về VoiceCommand đã parse
     * @param userInput User's voice/text input
     * @return Parsed VoiceCommand
     */
    suspend fun processTextCommand(userInput: String): Result<VoiceCommand>

    /**
     * Xử lý audio bytes và trả về VoiceCommand đã parse
     * @param audioBytes Audio data
     * @param mimeType Audio format (default: audio/wav)
     * @return Parsed VoiceCommand
     */
    suspend fun processAudioCommand(audioBytes: ByteArray, mimeType: String = "audio/wav"): Result<VoiceCommand>
}
