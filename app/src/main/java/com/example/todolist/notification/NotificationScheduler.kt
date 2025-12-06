package com.example.todolist.notification

import android.content.Context
import androidx.work.*
import com.example.todolist.notification.workers.*
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * Class để quản lý việc lên lịch notifications với WorkManager
 */
class NotificationScheduler(private val context: Context) {

    companion object {
        private const val TASK_NOTIFICATION_TAG = "task_notification"
        private const val MISSION_NOTIFICATION_TAG = "mission_notification"
        private const val DAILY_SUMMARY_TAG = "daily_summary"
        private const val WEEKLY_SUMMARY_TAG = "weekly_summary"
        private const val MONTHLY_SUMMARY_TAG = "monthly_summary"
    }

    /**
     * Lên lịch thông báo cho task
     */
    fun scheduleTaskNotification(notificationId: Long, scheduledTime: Long) {
        val delay = calculateDelay(scheduledTime)
        if (delay < 0) return // Không schedule notification đã quá hạn

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("notification_id" to notificationId))
            .addTag("$TASK_NOTIFICATION_TAG-$notificationId")
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    /**
     * Lên lịch thông báo cho mission
     */
    fun scheduleMissionNotification(notificationId: Long, scheduledTime: Long) {
        val delay = calculateDelay(scheduledTime)
        if (delay < 0) return

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("notification_id" to notificationId))
            .addTag("$MISSION_NOTIFICATION_TAG-$notificationId")
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    /**
     * Hủy thông báo theo tag
     */
    fun cancelNotification(notificationId: Long, isTask: Boolean) {
        val tag = if (isTask) {
            "$TASK_NOTIFICATION_TAG-$notificationId"
        } else {
            "$MISSION_NOTIFICATION_TAG-$notificationId"
        }
        WorkManager.getInstance(context).cancelAllWorkByTag(tag)
    }

    /**
     * Lên lịch thông báo tóm tắt hàng ngày
     */
    fun scheduleDailySummary(summaryHour: Int) {
        // Tính toán thời gian đến lần chạy đầu tiên
        val now = LocalDateTime.now()
        var nextRun = now.withHour(summaryHour).withMinute(0).withSecond(0).withNano(0)

        // Nếu đã qua giờ hôm nay, schedule cho ngày mai
        if (nextRun.isBefore(now)) {
            nextRun = nextRun.plusDays(1)
        }

        val initialDelay = Duration.between(now, nextRun).toMillis()

        val workRequest = PeriodicWorkRequestBuilder<DailyMissionSummaryWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(DAILY_SUMMARY_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_SUMMARY_TAG,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Lên lịch thông báo tóm tắt hàng tuần (mỗi thứ 2)
     */
    fun scheduleWeeklySummary(summaryHour: Int) {
        val now = LocalDateTime.now()
        var nextMonday = now.withHour(summaryHour).withMinute(0).withSecond(0).withNano(0)

        // Tìm thứ 2 tiếp theo
        val daysUntilMonday = (8 - now.dayOfWeek.value) % 7
        if (daysUntilMonday == 0 && nextMonday.isBefore(now)) {
            nextMonday = nextMonday.plusWeeks(1)
        } else {
            nextMonday = nextMonday.plusDays(daysUntilMonday.toLong())
        }

        val initialDelay = Duration.between(now, nextMonday).toMillis()

        val workRequest = PeriodicWorkRequestBuilder<WeeklyMissionSummaryWorker>(
            repeatInterval = 7,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(WEEKLY_SUMMARY_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WEEKLY_SUMMARY_TAG,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Lên lịch thông báo tóm tắt hàng tháng (mỗi ngày 1)
     */
    fun scheduleMonthlySummary(summaryHour: Int) {
        val now = LocalDateTime.now()
        var nextFirstDay = now.withDayOfMonth(1).withHour(summaryHour).withMinute(0).withSecond(0).withNano(0)

        // Nếu đã qua ngày 1 tháng này, schedule cho tháng sau
        if (nextFirstDay.isBefore(now)) {
            nextFirstDay = nextFirstDay.plusMonths(1)
        }

        val initialDelay = Duration.between(now, nextFirstDay).toMillis()

        val workRequest = PeriodicWorkRequestBuilder<MonthlyMissionSummaryWorker>(
            repeatInterval = 30,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(MONTHLY_SUMMARY_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            MONTHLY_SUMMARY_TAG,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Hủy tất cả thông báo tóm tắt định kỳ
     */
    fun cancelPeriodicSummaries() {
        WorkManager.getInstance(context).cancelAllWorkByTag(DAILY_SUMMARY_TAG)
        WorkManager.getInstance(context).cancelAllWorkByTag(WEEKLY_SUMMARY_TAG)
        WorkManager.getInstance(context).cancelAllWorkByTag(MONTHLY_SUMMARY_TAG)
    }

    /**
     * Tính toán delay từ bây giờ đến scheduled time
     */
    private fun calculateDelay(scheduledTime: Long): Long {
        return scheduledTime - System.currentTimeMillis()
    }
}
