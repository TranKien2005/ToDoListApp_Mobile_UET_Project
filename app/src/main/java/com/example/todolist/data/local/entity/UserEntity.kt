package com.example.todolist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val age: Int,
    val gender: String, // Lưu dạng String: "MALE", "FEMALE", "OTHER"
    val avatarUrl: String? = null,
    // Google Sign-In fields
    val googleId: String? = null,
    val email: String? = null,
    val isCalendarSyncEnabled: Boolean = false
)
