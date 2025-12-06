package com.example.todolist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1, // Chỉ có 1 bản ghi settings
    val taskReminderMinutes: Int = 15,
    val notifyDailyMissions: Boolean = true,
    val notifyWeeklyMissions: Boolean = true,
    val notifyMonthlyMissions: Boolean = true
)

