package com.example.todolist.core.model

// Shared core model for AI results
data class AiResult(
    val text: String,
    val metadata: Map<String, String>? = null
)

