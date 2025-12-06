package com.example.todolist.core.model

// Shared core business model for App Settings
data class Settings(
    val id: Int = 1, // Usually only one settings record
    // Thời gian báo trước khi bắt đầu task (phút)
    val taskReminderMinutes: Int = 15,
    // Thông báo mission vào đầu ngày nếu có deadline trong ngày
    val notifyDailyMissions: Boolean = true,
    // Thông báo mission vào đầu tuần nếu có deadline trong tuần
    val notifyWeeklyMissions: Boolean = true,
    // Thông báo mission vào đầu tháng nếu có deadline trong tháng
    val notifyMonthlyMissions: Boolean = true
)

