package com.example.todolist.data.repository

import com.example.todolist.data.remote.dto.AiResultDto
import com.example.todolist.data.remote.service.AiApiService
import com.example.todolist.core.model.AiResult
import com.example.todolist.domain.repository.AiRepository

class RemoteAiRepositoryImpl(
    private val service: AiApiService
) : AiRepository {
    override suspend fun processTextCommand(text: String): AiResult {
        val resp = service.processText(mapOf("text" to text))
        val dto: AiResultDto? = resp.body()
        return AiResult(text = dto?.text ?: "", metadata = dto?.metadata)
    }

    override suspend fun processVoiceAudio(audioBytes: ByteArray): AiResult {
        // In real impl you'd send bytes or a base64 payload. Here we send a placeholder map.
        val payload = mapOf("size" to audioBytes.size)
        val resp = service.processVoice(payload)
        val dto = resp.body()
        return AiResult(text = dto?.text ?: "", metadata = dto?.metadata)
    }
}
