package com.example.todolist.domain.repository

import com.example.todolist.core.model.AiResult

interface AiRepository {
    // Send text command to AI and get a suggestion/result
    suspend fun processTextCommand(text: String): AiResult

    // Send audio bytes to an external voice-AI service (third-party) and receive result
    suspend fun processVoiceAudio(audioBytes: ByteArray): AiResult
}

