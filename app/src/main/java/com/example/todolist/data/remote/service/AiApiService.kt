package com.example.todolist.data.remote.service

import com.example.todolist.data.remote.dto.AiResultDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AiApiService {
    @POST("ai/text")
    suspend fun processText(@Body request: Map<String, String>): Response<AiResultDto>

    @POST("ai/voice")
    suspend fun processVoice(@Body audioPayload: Map<String, Any>): Response<AiResultDto>
}

