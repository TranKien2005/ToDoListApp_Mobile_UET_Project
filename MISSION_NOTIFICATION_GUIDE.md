# Mission Notification System - Complete Guide

## 📋 Tổng quan

Mission notification system đã được cập nhật để hoạt động giống Task - tự động tạo notifications trước khi lưu mission và xóa khi hoàn thành.

## ✅ Những gì đã implement:

### 1. **Dual Notification System**

Mỗi khi tạo/update Mission, hệ thống tự động tạo **2 notifications**:

#### a) Warning Notification (Cảnh báo trước deadline)
- **Thời gian**: `deadline - warningMinutes` (theo setting)
- **Type**: `MISSION_DEADLINE_WARNING`
- **Mục đích**: Nhắc user trước khi mission sắp hết hạn

#### b) Overdue Notification (Thông báo quá hạn)
- **Thời gian**: Ngay tại `deadline`
- **Type**: `MISSION_OVERDUE`
- **Mục đích**: Thông báo khi mission đã MISSED

### 2. **Auto-delete khi hoàn thành**

Khi user đánh dấu mission completed:
- Tất cả notifications của mission đó **tự động bị xóa**
- Không còn nhận thông báo về mission đã hoàn thành

### 3. **Auto-update khi change settings**

Khi update mission hoặc thay đổi setting:
- **Xóa** tất cả notifications cũ
- **Tạo mới** notifications với setting mới

## 🔧 Implementation Details

### File đã cập nhật:

1. **`RealScheduleMissionNotificationUseCase`**
```kotlin
override suspend fun invoke(mission: Mission, warningMinutes: Int) {
    // 1. Tạo warning notification (trước deadline)
    val warningTime = mission.deadline.minusMinutes(warningMinutes)
    // Lên lịch notification...
    
    // 2. Tạo overdue notification (tại deadline)
    val deadlineTime = mission.deadline
    // Lên lịch notification...
}
```

2. **`MissionViewModel`**
```kotlin
fun toggleMissionCompleted(id: Int) {
    // Set status
    missionUseCases.setMissionStatus(id, newStatus)
    
    // Xóa notifications khi completed
    if (newStatus == COMPLETED) {
        notificationUseCases.cancelMissionNotifications(id)
    }
}
```

3. **`AddItemViewModel`** (đã có sẵn)
```kotlin
// Khi save mission
if (updating) {
    cancelMissionNotifications(mission.id) // Xóa cũ
}
scheduleMissionNotification(mission, warningMinutes) // Tạo mới
```

### String Resources đã thêm:

```xml
<string name="notification_mission_overdue_title">Mission Overdue: %1$s</string>
<string name="notification_mission_overdue_message">This mission has passed its deadline</string>
```

## 🎯 Workflow

### Khi tạo Mission mới:
1. User tạo mission với deadline
2. System tự động tạo 2 notifications:
   - Warning (X phút trước deadline - theo setting)
   - Overdue (tại deadline)
3. Notifications được lưu vào DB và schedule với WorkManager

### Khi update Mission:
1. Xóa tất cả notifications cũ của mission
2. Tạo lại 2 notifications mới với thông tin mới

### Khi hoàn thành Mission:
1. User toggle mission status → COMPLETED
2. System tự động xóa tất cả notifications
3. User không còn nhận thông báo về mission này

### Khi Mission MISSED:
1. Mission tự động có `status = MISSED` (computed)
2. Overdue notification đã được gửi tại deadline
3. User thấy notification và biết mission đã trễ

## 🎨 UI Flow

```
Create Mission
    ↓
[Auto] Create 2 notifications
    ↓
    ├── Warning: deadline - X min
    └── Overdue: at deadline
    ↓
User sees mission in list
    ↓
    ├─→ Complete → [Auto] Delete notifications ✓
    ├─→ Update → [Auto] Recreate notifications 🔄
    └─→ Delete → [Auto] Delete notifications 🗑️
```

## ⚠️ Action Required

**Xóa file không cần thiết:**
- `app/src/main/java/com/example/todolist/domain/usecase/CheckAndNotifyMissedMissionsUseCase.kt`

File này không còn cần thiết vì:
- Overdue notification đã được tạo sẵn tại deadline
- Không cần background worker để check missed missions
- Approach mới đơn giản hơn và reliable hơn

## 🔍 Debugging

Nếu notifications không hoạt động:

1. **Check Settings**: Đảm bảo `missionDeadlineWarningMinutes` > 0
2. **Check Time**: Deadline phải > hiện tại
3. **Check WorkManager**: Xem logs trong Android Studio
4. **Check Database**: Query notifications table để xem notifications đã được tạo chưa

## 🆚 So sánh với approach cũ

| Khía cạnh | Approach Cũ | Approach Mới |
|-----------|-------------|--------------|
| Khi tạo mission | Chỉ 1 notification | 2 notifications |
| Overdue detection | Runtime check | Pre-scheduled notification |
| Background worker | Cần | Không cần |
| Code complexity | Cao | Thấp |
| Reliability | Phụ thuộc worker | Guaranteed bởi WorkManager |
| User experience | Có thể miss notification | Luôn nhận notification |

## ✅ Hoàn tất

Mission notification system bây giờ hoạt động chính xác như Task:
- ✅ Auto-create notifications khi tạo/update
- ✅ Auto-delete khi complete
- ✅ Cảnh báo trước deadline (setting-based)
- ✅ Thông báo khi quá deadline
- ✅ Không cần background worker
- ✅ Clean & Simple code

