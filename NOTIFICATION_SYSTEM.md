# Hệ Thống Thông Báo (Notification System)

## Tổng Quan

Hệ thống thông báo được xây dựng với các tính năng:
- ✅ Thông báo nhắc nhở Task trước X phút (theo settings)
- ✅ Thông báo cảnh báo Mission sắp đến deadline
- ✅ Thông báo tóm tắt Mission hàng ngày/tuần/tháng
- ✅ Thông báo khi Task/Mission quá hạn
- ✅ Sử dụng WorkManager để đảm bảo thông báo được gửi đúng lúc
- ✅ Lưu trữ lịch sử thông báo trong Room Database

## Cấu Trúc

### 1. Models & Entities

**Notification Model** (`core/model/Notification.kt`):
```kotlin
data class Notification(
    val id: Long,
    val type: NotificationType,
    val relatedTaskId: Int?,
    val relatedMissionId: Int?,
    val title: String,
    val message: String,
    val scheduledTime: Long,
    val isDelivered: Boolean,
    val isRead: Boolean,
    val createdAt: Long
)
```

**NotificationType Enum**:
- `TASK_REMINDER` - Nhắc nhở task
- `MISSION_DEADLINE_WARNING` - Cảnh báo mission sắp đến deadline
- `MISSION_DAILY_SUMMARY` - Tóm tắt missions trong ngày
- `MISSION_WEEKLY_SUMMARY` - Tóm tắt missions trong tuần
- `MISSION_MONTHLY_SUMMARY` - Tóm tắt missions trong tháng
- `TASK_OVERDUE` - Task đã quá hạn
- `MISSION_OVERDUE` - Mission đã quá hạn

### 2. Settings Mới

**Settings Model** đã được cập nhật với các trường:
```kotlin
data class Settings(
    val dailySummaryHour: Int = 7,  // Giờ gửi thông báo tóm tắt (7h sáng)
    val missionDeadlineWarningMinutes: Int = 60,  // Cảnh báo trước deadline 60 phút
    val overdueNotificationEnabled: Boolean = true  // Bật thông báo quá hạn
)
```

### 3. Repository & DAO

**NotificationRepository**: Interface định nghĩa các thao tác CRUD
**NotificationDao**: Room DAO để truy vấn database
**RoomNotificationRepositoryImpl**: Implementation sử dụng Room

### 4. Use Cases

**NotificationUseCases** bao gồm:
- `getNotifications()` - Lấy tất cả notifications
- `scheduleTaskNotification(task, reminderMinutes)` - Lên lịch thông báo cho task
- `scheduleMissionNotification(mission, warningMinutes)` - Lên lịch thông báo cho mission
- `cancelTaskNotifications(taskId)` - Hủy thông báo của task
- `cancelMissionNotifications(missionId)` - Hủy thông báo của mission
- `markNotificationAsRead(id)` - Đánh dấu đã đọc
- `deleteReadNotifications()` - Xóa thông báo đã đọc
- `createNotification(notification)` - Tạo thông báo thủ công

### 5. Notification Components

**NotificationHelper** (`notification/NotificationHelper.kt`):
- Tạo và quản lý Android notification channels
- Hiển thị notifications cho người dùng
- Hủy notifications

**NotificationScheduler** (`notification/NotificationScheduler.kt`):
- Lên lịch notifications với WorkManager
- Quản lý periodic notifications (daily/weekly/monthly)
- Hủy scheduled notifications

**Workers** (`notification/workers/`):
- `NotificationWorker` - Worker gửi notification đã được schedule
- `DailyMissionSummaryWorker` - Worker gửi tóm tắt hàng ngày
- `WeeklyMissionSummaryWorker` - Worker gửi tóm tắt hàng tuần
- `MonthlyMissionSummaryWorker` - Worker gửi tóm tắt hàng tháng

## Cách Sử Dụng

### 1. Setup trong Application

Notification system được tự động khởi tạo thông qua DI (DomainModule).

### 2. Lên Lịch Thông Báo Cho Task

```kotlin
// Trong ViewModel khi tạo task
viewModelScope.launch {
    val settings = settingsUseCases.getSettings().first()
    
    // Tạo task
    taskUseCases.createTask(task)
    
    // Lên lịch thông báo
    notificationUseCases.scheduleTaskNotification(
        task = task,
        reminderMinutes = settings.taskReminderMinutes
    )
}
```

### 3. Lên Lịch Thông Báo Cho Mission

```kotlin
// Trong ViewModel khi tạo mission
viewModelScope.launch {
    val settings = settingsUseCases.getSettings().first()
    
    // Tạo mission
    missionUseCases.createMission(mission)
    
    // Lên lịch thông báo cảnh báo deadline
    notificationUseCases.scheduleMissionNotification(
        mission = mission,
        warningMinutes = settings.missionDeadlineWarningMinutes
    )
}
```

### 4. Hủy Thông Báo

```kotlin
// Khi xóa task
viewModelScope.launch {
    taskUseCases.deleteTask(taskId)
    notificationUseCases.cancelTaskNotifications(taskId)
}

// Khi hoàn thành mission
viewModelScope.launch {
    missionUseCases.updateMission(mission.copy(status = MissionStatus.COMPLETED))
    notificationUseCases.cancelMissionNotifications(mission.id)
}
```

### 5. Setup Periodic Summaries

Trong Application hoặc MainActivity khi khởi động app:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val domainModule = DomainModule(this)
        val scheduler = NotificationScheduler(this)
        
        // Lấy settings
        lifecycleScope.launch {
            val settings = domainModule.settingsUseCases.getSettings().first()
            
            // Setup periodic summaries
            if (settings.notifyDailyMissions) {
                scheduler.scheduleDailySummary(settings.dailySummaryHour)
            }
            if (settings.notifyWeeklyMissions) {
                scheduler.scheduleWeeklySummary(settings.dailySummaryHour)
            }
            if (settings.notifyMonthlyMissions) {
                scheduler.scheduleMonthlySummary(settings.dailySummaryHour)
            }
        }
    }
}
```

### 6. Request Notification Permission (Android 13+)

Trong Activity hoặc Screen chính:

```kotlin
@Composable
fun RequestNotificationPermission() {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            
            // Check và request permission
            if (ContextCompat.checkSelfPermission(context, permission) 
                != PackageManager.PERMISSION_GRANTED) {
                // Show permission dialog
                // ActivityCompat.requestPermissions(...)
            }
        }
    }
}
```

## Notification Channels

Hệ thống tạo 4 notification channels:

1. **Task Reminders** (High Priority)
   - ID: `channel_task_reminder`
   - Nhắc nhở về task sắp bắt đầu

2. **Mission Summary** (Default Priority)
   - ID: `channel_mission_summary`
   - Tóm tắt missions định kỳ

3. **Mission Warnings** (High Priority)
   - ID: `channel_mission_warning`
   - Cảnh báo mission sắp đến deadline

4. **Overdue Alerts** (High Priority)
   - ID: `channel_overdue`
   - Cảnh báo task/mission đã quá hạn

## Database Schema

**Table: notifications**
```sql
CREATE TABLE notifications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type TEXT NOT NULL,
    relatedTaskId INTEGER,
    relatedMissionId INTEGER,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    scheduledTime INTEGER NOT NULL,
    isDelivered INTEGER NOT NULL DEFAULT 0,
    isRead INTEGER NOT NULL DEFAULT 0,
    createdAt INTEGER NOT NULL
)
```

## Permissions Required

Trong `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
```

## Dependencies

```gradle
// WorkManager
implementation("androidx.work:work-runtime-ktx:2.9.0")
```

## TODO / Improvements

1. **UI cho Notification List**: Tạo màn hình hiển thị danh sách notifications
2. **Notification Actions**: Thêm quick actions (Complete, Snooze) vào notification
3. **Custom Notification Icon**: Thay icon mặc định bằng icon đẹp hơn
4. **Sound & Vibration Settings**: Cho phép user tùy chỉnh âm thanh/rung
5. **Notification History**: UI để xem lịch sử thông báo
6. **Smart Notifications**: Học thói quen user và gửi thông báo vào thời điểm phù hợp
7. **Batch Notifications**: Group nhiều notifications cùng loại
8. **Priority Settings**: Cho phép user set priority cho từng loại notification

## Testing

Để test notification system:

1. Tạo task với thời gian bắt đầu trong vài phút tới
2. Kiểm tra notification xuất hiện đúng lúc
3. Tạo mission với deadline gần
4. Verify notification cảnh báo được gửi
5. Đợi đến giờ summary (hoặc thay đổi thời gian trong settings)
6. Verify summary notification được gửi

## Troubleshooting

**Notification không xuất hiện:**
- Kiểm tra permission POST_NOTIFICATIONS đã được cấp
- Kiểm tra Battery Optimization không chặn app
- Kiểm tra Notification channels chưa bị tắt bởi user
- Verify WorkManager đang chạy bình thường

**Notification bị delay:**
- Android Doze mode có thể delay notifications
- Sử dụng `setExactAndAllowWhileIdle()` cho notifications quan trọng
- Request SCHEDULE_EXACT_ALARM permission

---

**Version**: 1.0
**Last Updated**: December 2025

