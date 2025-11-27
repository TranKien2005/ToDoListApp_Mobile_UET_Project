package com.example.todolist.ui.home

import com.example.todolist.domain.model.Task

data class HomeUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

