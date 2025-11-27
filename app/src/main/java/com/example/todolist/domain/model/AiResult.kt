package com.example.todolist.domain.model

// Simple wrapper for AI results; extend as needed
data class AiResult(
    val text: String,
    val metadata: Map<String, String>? = null
)

