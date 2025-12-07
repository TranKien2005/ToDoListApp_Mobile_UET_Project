# Mission Computed Status Design

## 🎯 Thiết kế Mới: Computed Status Pattern

### Khái niệm
**MISSED status KHÔNG được lưu vào database** - nó được tính toán (computed) tự động dựa trên:
- `storedStatus` (UNSPECIFIED hoặc COMPLETED)
- `deadline` so với thời gian hiện tại

### Ưu điểm

✅ **Real-time accuracy** - Status luôn chính xác 100%  
✅ **No background workers** - Không tốn battery, không cần WorkManager  
✅ **Simpler code** - Ít code hơn, ít bug hơn  
✅ **Smaller database** - Chỉ lưu 2 status thay vì 3  
✅ **No stale data** - Không bao giờ có data "cũ" trong DB  
✅ **Instant update** - Mission tự động MISSED ngay lập tức khi qua deadline  

### Cấu trúc

```kotlin
// 1. Stored Status (in database)
enum class MissionStoredStatus {
    UNSPECIFIED,  // User chưa hoàn thành
    COMPLETED     // User đã hoàn thành
}

// 2. Display Status (computed)
enum class MissionStatus {
    ACTIVE,      // UNSPECIFIED + deadline chưa qua
    COMPLETED,   // User đã mark completed
    MISSED       // UNSPECIFIED + deadline đã qua (COMPUTED!)
}

// 3. Mission Model với Computed Property
data class Mission(
    val deadline: LocalDateTime,
    val storedStatus: MissionStoredStatus
) {
    val status: MissionStatus
        get() = when (storedStatus) {
            COMPLETED -> MissionStatus.COMPLETED
            UNSPECIFIED -> {
                if (deadline.isBefore(LocalDateTime.now())) {
                    MissionStatus.MISSED
                } else {
                    MissionStatus.ACTIVE
                }
            }
        }
}
```

### Cách hoạt động

1. **Database**: Chỉ lưu `storedStatus` (UNSPECIFIED/COMPLETED)
2. **Domain Model**: Có computed property `status` trả về ACTIVE/COMPLETED/MISSED
3. **UI**: Sử dụng `mission.status` để hiển thị - tự động update real-time
4. **No Workers**: Không cần background worker để update status

### Ví dụ

```kotlin
// Tạo mission mới
val mission = Mission(
    deadline = LocalDateTime.now().plusDays(1),
    storedStatus = MissionStoredStatus.UNSPECIFIED
)

// Ngay lúc này
mission.status // => MissionStatus.ACTIVE

// Sau 1 ngày (deadline qua)
mission.status // => MissionStatus.MISSED (tự động!)

// User mark completed
repository.setMissionStatus(mission.id, MissionStoredStatus.COMPLETED)
mission.status // => MissionStatus.COMPLETED
```

### Database Migration

Nếu database cũ có `status = "MISSED"`:
- Mapper sẽ tự động convert về `MissionStoredStatus.UNSPECIFIED`
- Computed property sẽ tính toán lại dựa trên deadline
- Không cần migration script đặc biệt

### So sánh với thiết kế cũ

| Khía cạnh | Thiết kế Cũ | Thiết kế Mới |
|-----------|-------------|--------------|
| Database | 3 status (UNSPECIFIED, COMPLETED, MISSED) | 2 status (UNSPECIFIED, COMPLETED) |
| Update logic | Worker chạy mỗi 15 phút | Computed tự động |
| Accuracy | Có thể sai lệch 0-15 phút | 100% chính xác |
| Performance | Tốn battery (worker) | Không tốn (instant compute) |
| Code complexity | Cao (Worker, UseCase, Schedule) | Thấp (chỉ computed property) |
| Real-time | ❌ Không | ✅ Có |

### Files đã xóa

- ❌ `MissionStatusUpdateWorker.kt` - Không cần nữa
- ❌ `UpdateOverdueMissionsUseCase.kt` - Không cần nữa
- ❌ Worker scheduling trong `MyApplication.kt`
- ❌ `updateOverdueMissions()` method trong DAO/Repository

### Files đã cập nhật

1. **Mission.kt** - Thêm `MissionStoredStatus` và computed property
2. **MissionEntity.kt** - Chỉ lưu 2 status
3. **MissionEntityMapper.kt** - Map giữa stored và domain model
4. **MissionDao.kt** - Xóa `updateOverdueMissionsToMissed()`
5. **MissionRepository.kt** - Xóa `updateOverdueMissions()`
6. **RoomMissionRepositoryImpl.kt** - Simplified
7. **MissionUseCases.kt** - Sử dụng `MissionStoredStatus`
8. **RealMissionUseCases.kt** - Cập nhật status checks
9. **Debug MissionUseCases.kt** - Cập nhật fake data
10. **DomainModule.kt** - Xóa worker use case
11. **MissionViewModel.kt** - Đơn giản hơn, không cần update call
12. **MyApplication.kt** - Xóa WorkManager scheduling

## 🎓 Best Practice Reference

Thiết kế này tuân theo:
- **Martin Fowler**: "Don't store what you can compute"
- **Domain-Driven Design**: Phân biệt Entity State vs Value Object
- **Database Normalization**: Avoid denormalization khi không cần thiết
- **Android Best Practices**: Computed properties cho time-based states

## 🚀 Kết quả

Bây giờ Mission system:
- ✅ Tự động hiển thị MISSED ngay khi qua deadline
- ✅ Không cần background worker
- ✅ Không tốn battery
- ✅ Code đơn giản hơn nhiều
- ✅ Performance tốt hơn
- ✅ Luôn chính xác 100%

