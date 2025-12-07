package com.example.todolist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String?,
    val deadlineEpoch: Long,
    // Only store UNSPECIFIED or COMPLETED - MISSED is computed based on deadline
    val status: String = "UNSPECIFIED" // "UNSPECIFIED" or "COMPLETED" only
)
