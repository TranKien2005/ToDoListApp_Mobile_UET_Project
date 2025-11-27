package com.example.todolist.domain.model

import java.time.LocalDateTime

data class Mission(
    val id: Int,
    val title: String,
    val description: String? = null,
    val deadline: LocalDateTime,
    val isCompleted: Boolean = false
)

