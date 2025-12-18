package com.example.todolist.core.model

// Ngôn ngữ ứng dụng
enum class AppLanguage {
    VIETNAMESE,
    ENGLISH
}

// Shared core business model for App Settings
data class Settings(
    val id: Int = 1, // Usually only one settings record
    // Ngôn ngữ ứng dụng (mặc định tiếng Việt)
    val language: AppLanguage = AppLanguage.VIETNAMESE,
    // Thời gian báo trước khi bắt đầu task (phút)
    val taskReminderMinutes: Int = 15,
    // Thông báo mission vào đầu ngày nếu có deadline trong ngày
    val notifyDailyMissions: Boolean = true,
    // Thông báo mission vào đầu tuần nếu có deadline trong tuần
    val notifyWeeklyMissions: Boolean = true,
    // Thông báo mission vào đầu tháng nếu có deadline trong tháng
    val notifyMonthlyMissions: Boolean = true,
    // Thời gian gửi thông báo tóm tắt hàng ngày (giờ trong ngày: 0-23), mặc định 7h sáng
    val dailySummaryHour: Int = 7,
    // Cảnh báo mission sắp đến deadline trước bao nhiêu phút
    val missionDeadlineWarningMinutes: Int = 60,
    // Bật thông báo khi task/mission đã quá hạn
    val overdueNotificationEnabled: Boolean = true
)
