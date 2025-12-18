package com.example.todolist.domain.usecase

import com.example.todolist.core.model.AiChatResponse
import com.example.todolist.core.model.ChatMessage
import com.example.todolist.core.model.PendingCommand
import com.example.todolist.core.model.UserContext

/**
 * Use case để chat với AI
 */
interface ChatWithAIUseCase {
    /**
     * Chat với AI
     * @param message Tin nhắn từ user
     * @param conversationHistory Lịch sử hội thoại
     * @param userContext Context của user (info + tasks + missions)
     * @return AiChatResponse chứa message và optional pending command
     */
    suspend operator fun invoke(
        message: String,
        conversationHistory: List<ChatMessage>,
        userContext: UserContext
    ): Result<AiChatResponse>
}

/**
 * Use case để chat với AI bằng audio
 */
interface ChatWithAudioUseCase {
    /**
     * Chat với AI bằng audio
     * @param audioBytes Audio data
     * @param mimeType Audio format
     * @param conversationHistory Lịch sử hội thoại
     * @param userContext Context của user
     * @return AiChatResponse
     */
    suspend operator fun invoke(
        audioBytes: ByteArray,
        mimeType: String = "audio/mp4",
        conversationHistory: List<ChatMessage>,
        userContext: UserContext
    ): Result<AiChatResponse>
}

/**
 * Use case để thực thi command đã được confirm
 */
interface ExecuteCommandUseCase {
    /**
     * Thực thi command
     * @param command Command đã được user confirm
     * @return Result với message thành công hoặc error
     */
    suspend operator fun invoke(command: PendingCommand): Result<String>
}

/**
 * Aggregator cho tất cả AI use cases
 */
data class AIUseCases(
    val chatWithAI: ChatWithAIUseCase,
    val chatWithAudio: ChatWithAudioUseCase,
    val executeCommand: ExecuteCommandUseCase
)
