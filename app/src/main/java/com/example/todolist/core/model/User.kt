package com.example.todolist.core.model

// Shared core business model for User
enum class Gender {
    MALE,
    FEMALE,
    OTHER
}

data class User(
    val id: Int = 0,
    val fullName: String,
    val age: Int,
    val gender: Gender,
    val avatarUrl: String? = null, // URL hoặc path đến avatar, null = dùng icon mặc định
    // Google Sign-In fields
    val googleId: String? = null,
    val email: String? = null,
    val isCalendarSyncEnabled: Boolean = false
)
