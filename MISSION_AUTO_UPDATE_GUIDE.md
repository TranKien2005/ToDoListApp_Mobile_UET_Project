# Mission Auto-Update Feature Guide

## Tổng quan
Hệ thống tự động cập nhật trạng thái Mission từ **UNSPECIFIED** sang **MISSED** khi quá deadline.

## Cách hoạt động

### 1. **Cập nhật khi mở app**
- Mỗi khi `MissionViewModel` được khởi tạo, nó tự động gọi `updateOverdueMissions()`
- Tất cả missions có `status = UNSPECIFIED` và `deadline < now` sẽ được cập nhật sang `status = MISSED`

### 2. **Cập nhật định kỳ (Background)**
- Một `WorkManager` worker chạy mỗi **15 phút** để kiểm tra và cập nhật missions quá hạn
- Worker này hoạt động ngay cả khi app không mở
- Điều kiện: chỉ chạy khi pin không yếu (`setRequiresBatteryNotLow`)

## Các file đã thêm/sửa

### Files mới:
1. **`UpdateOverdueMissionsUseCase.kt`** - Use case để cập nhật missions quá hạn
2. **`MissionStatusUpdateWorker.kt`** - Background worker

### Files đã cập nhật:
1. **`MissionDao.kt`** - Thêm query `updateOverdueMissionsToMissed()`
2. **`MissionRepository.kt`** - Thêm method `updateOverdueMissions()`
3. **`RoomMissionRepositoryImpl.kt`** - Implement `updateOverdueMissions()`
4. **`MissionUseCases.kt`** - Thêm `updateOverdueMissions` use case
5. **`RealMissionUseCases.kt`** - Implement real use case
6. **`DomainModule.kt`** - Khởi tạo use case mới
7. **`MissionViewModel.kt`** - Gọi `updateOverdueMissions()` trong init
8. **`MyApplication.kt`** - Schedule background worker
9. **Debug build** - Thêm `FakeUpdateOverdueMissionsUseCase`

## Cơ chế update

### Database Query:
```sql
UPDATE missions 
SET status = 'MISSED' 
WHERE deadlineEpoch < :currentTimeMillis 
AND status = 'UNSPECIFIED'
```

### Logic trong ViewModel:
```kotlin
init {
    viewModelScope.launch {
        // Update trước khi load data
        missionUseCases.updateOverdueMissions.invoke()
        
        missionUseCases.getMissions.invoke()
            .collect { list ->
                // Hiển thị data đã được update
            }
    }
}
```

### Background Worker:
```kotlin
class MissionStatusUpdateWorker : CoroutineWorker() {
    override suspend fun doWork(): Result {
        val repository = RepositoryModule.provideMissionRepository(context)
        repository.updateOverdueMissions()
        return Result.success()
    }
}
```

## Lợi ích

✅ **Tự động**: Missions tự động chuyển sang MISSED khi quá hạn  
✅ **Realtime**: Update ngay khi mở app  
✅ **Background**: Tiếp tục update ngay cả khi app đóng  
✅ **Hiệu quả**: Chỉ update những missions cần thiết (UNSPECIFIED + overdue)  
✅ **Battery-friendly**: Worker chỉ chạy khi pin đủ  

## Cấu hình

### Thay đổi tần suất update:
Trong `MyApplication.kt`, thay đổi interval:
```kotlin
val workRequest = PeriodicWorkRequestBuilder<MissionStatusUpdateWorker>(
    15, TimeUnit.MINUTES // Thay đổi giá trị này
)
```

**Lưu ý**: WorkManager có giới hạn tối thiểu 15 phút cho periodic work.

## Testing

### Debug mode:
- `FakeUpdateOverdueMissionsUseCase` tự động update fake data
- Có thể test bằng cách thêm missions với deadline trong quá khứ

### Release mode:
1. Tạo mission với deadline trong quá khứ
2. Đóng app và mở lại → Mission sẽ tự động chuyển sang MISSED
3. Hoặc chờ 15 phút để worker tự động chạy

## Troubleshooting

**Mission không tự động chuyển sang MISSED?**
- Kiểm tra status hiện tại (chỉ UNSPECIFIED mới được update)
- Kiểm tra deadline có thực sự < thời gian hiện tại
- Xem log: `AppLogger.i("MissionStatusUpdateWorker scheduled")`

**Worker không chạy?**
- Kiểm tra Battery Optimization settings
- WorkManager có độ trễ, không chạy chính xác mỗi 15 phút
- Sử dụng WorkManager Inspector trong Android Studio để debug

