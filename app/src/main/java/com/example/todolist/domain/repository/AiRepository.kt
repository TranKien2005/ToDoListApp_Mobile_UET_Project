package com.example.todolist.domain.repository

import com.example.todolist.core.model.AiChatResponse
import com.example.todolist.core.model.ChatMessage
import com.example.todolist.core.model.UserContext

/**
 * Interface để giao tiếp với AI service
 * Implementation ở data/remote/ai/
 */
interface AiRepository {
    /**
     * Chat với AI, gửi kèm context và conversation history
     * @param userMessage Tin nhắn mới từ user
     * @param conversationHistory Lịch sử hội thoại (để AI nhớ ngữ cảnh)
     * @param userContext Thông tin user + tasks + missions
     * @return AiChatResponse chứa message và optional pending command
     */
    suspend fun chat(
        userMessage: String,
        conversationHistory: List<ChatMessage>,
        userContext: UserContext
    ): Result<AiChatResponse>

    /**
     * Chat với AI bằng audio
     * @param audioBytes Audio data
     * @param mimeType Audio format (default: audio/mp4)
     * @param conversationHistory Lịch sử hội thoại
     * @param userContext Thông tin user + tasks + missions
     * @return AiChatResponse chứa message và optional pending command
     */
    suspend fun chatWithAudio(
        audioBytes: ByteArray,
        mimeType: String = "audio/mp4",
        conversationHistory: List<ChatMessage>,
        userContext: UserContext
    ): Result<AiChatResponse>
}
