package com.example.todolist.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AiResultDto(
    @SerializedName("text") val text: String,
    @SerializedName("metadata") val metadata: Map<String, String>? = null
)

