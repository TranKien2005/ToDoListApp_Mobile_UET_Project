package com.example.todolist.core.model

// Loại thông báo
enum class NotificationType {
    TASK_REMINDER,           // Nhắc nhở task trước X phút
    MISSION_DEADLINE_WARNING, // Cảnh báo mission sắp đến deadline
    MISSION_DAILY_SUMMARY,   // Tóm tắt missions trong ngày
    MISSION_WEEKLY_SUMMARY,  // Tóm tắt missions trong tuần
    MISSION_MONTHLY_SUMMARY, // Tóm tắt missions trong tháng
    TASK_OVERDUE,            // Task đã quá hạn
    MISSION_OVERDUE          // Mission đã quá hạn
}

// Shared core business model for Notification
data class Notification(
    val id: Long = 0,
    val type: NotificationType,
    // ID của task hoặc mission liên quan (nullable nếu là summary)
    val relatedTaskId: Int? = null,
    val relatedMissionId: Int? = null,
    val title: String,
    val message: String,
    // Thời gian được lên lịch gửi thông báo (epoch millis)
    val scheduledTime: Long,
    // Đã được gửi chưa
    val isDelivered: Boolean = false,
    // Đã đọc chưa
    val isRead: Boolean = false,
    // Thời gian tạo notification record (epoch millis)
    val createdAt: Long = System.currentTimeMillis()
)

