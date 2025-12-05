package com.example.todolist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String?,
    val deadlineEpoch: Long,
    // store status as string: UNSPECIFIED, COMPLETED, MISSED (MISSED is derived but stored for compatibility)
    val status: String = "UNSPECIFIED"
)
