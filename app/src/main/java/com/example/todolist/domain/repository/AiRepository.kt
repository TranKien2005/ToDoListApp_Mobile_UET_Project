package com.example.todolist.domain.repository

/**
 * Interface này hiện tại KHÔNG được sử dụng
 * Chúng ta đã chuyển sang sử dụng AIUseCases architecture
 * Giữ lại để tham khảo hoặc có thể xóa
 */
@Deprecated("Use AIUseCases instead")
interface AiRepository {
    // Send text command to AI and get a suggestion/result
    suspend fun processTextCommand(text: String): Result<String>

    // Send audio bytes to an external voice-AI service (third-party) and receive result
    suspend fun processVoiceAudio(audioBytes: ByteArray): Result<String>
}
