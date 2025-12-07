# Dialog Usage Guide

## Tổng quan

Project đã được tích hợp các dialog components để cải thiện UX khi thực hiện các tác vụ quan trọng như xóa, thông báo thành công/thất bại.

## Các Dialog có sẵn

### 1. Delete Confirmation Dialogs

#### DeleteTaskConfirmationDialog
Sử dụng khi xóa Task:

```kotlin
var showDeleteDialog by remember { mutableStateOf(false) }

if (showDeleteDialog) {
    DeleteTaskConfirmationDialog(
        taskTitle = task.title,
        onConfirm = {
            // Thực hiện xóa task
            viewModel.deleteTask(taskId)
            showDeleteDialog = false
        },
        onDismiss = {
            showDeleteDialog = false
        }
    )
}

// Trigger dialog
IconButton(onClick = { showDeleteDialog = true }) {
    Icon(Icons.Default.Delete, contentDescription = "Delete")
}
```

#### DeleteMissionConfirmationDialog
Sử dụng khi xóa Mission:

```kotlin
var showDeleteDialog by remember { mutableStateOf(false) }

if (showDeleteDialog) {
    DeleteMissionConfirmationDialog(
        missionTitle = mission.title,
        onConfirm = {
            // Thực hiện xóa mission
            viewModel.deleteMission(missionId)
            showDeleteDialog = false
        },
        onDismiss = {
            showDeleteDialog = false
        }
    )
}
```

### 2. Notification Dialogs

#### SuccessDialog
Thông báo khi thao tác thành công:

```kotlin
var showSuccess by remember { mutableStateOf(false) }

if (showSuccess) {
    SuccessDialog(
        message = "Task created successfully!",
        onDismiss = { showSuccess = false }
    )
}
```

#### ErrorDialog
Thông báo khi có lỗi:

```kotlin
var showError by remember { mutableStateOf(false) }

if (showError) {
    ErrorDialog(
        message = "Failed to save task. Please try again.",
        onDismiss = { showError = false }
    )
}
```

#### WarningDialog
Cảnh báo người dùng:

```kotlin
var showWarning by remember { mutableStateOf(false) }

if (showWarning) {
    WarningDialog(
        message = "This action may affect existing data.",
        onConfirm = {
            // Tiếp tục thực hiện
            showWarning = false
        },
        onDismiss = {
            showWarning = false
        }
    )
}
```

#### InfoDialog
Hiển thị thông tin:

```kotlin
var showInfo by remember { mutableStateOf(false) }

if (showInfo) {
    InfoDialog(
        title = "How to use",
        message = "Swipe left to delete, tap to edit.",
        onDismiss = { showInfo = false }
    )
}
```

### 3. Auto-Dismiss Notification

Thông báo tự động ẩn sau vài giây (giống Snackbar):

```kotlin
var showNotification by remember { mutableStateOf(false) }

if (showNotification) {
    AutoDismissNotification(
        message = "Task saved!",
        type = NotificationType.SUCCESS,
        durationMillis = 3000,
        onDismiss = { showNotification = false }
    )
}
```

#### Các loại NotificationType:
- `NotificationType.SUCCESS` - Màu xanh lá
- `NotificationType.ERROR` - Màu đỏ
- `NotificationType.WARNING` - Màu cam
- `NotificationType.INFO` - Màu primary

## Đã Implement

### ✅ TaskCardItem
- **Delete confirmation** khi xóa task
- User phải confirm trước khi xóa

### ✅ MissionCardItem  
- **Delete confirmation** khi xóa mission
- User phải confirm trước khi xóa

## Gợi ý Implement thêm

### 1. AddItemDialog
Thêm thông báo thành công khi tạo/cập nhật:

```kotlin
@Composable
fun AddItemDialog(
    // ... existing params
) {
    var showSuccess by remember { mutableStateOf(false) }
    
    // ... existing code
    
    Button(
        onClick = {
            if (validateAll()) {
                // Save task/mission
                viewModel.saveTask(task)
                showSuccess = true
                
                // Auto dismiss dialog after showing success
                kotlinx.coroutines.delay(1500)
                onDismissRequest()
            }
        }
    ) {
        Text("Save")
    }
    
    if (showSuccess) {
        AutoDismissNotification(
            message = if (isEditMode) "Updated successfully!" else "Created successfully!",
            type = NotificationType.SUCCESS,
            onDismiss = { showSuccess = false }
        )
    }
}
```

### 2. SettingsScreen
Thêm thông báo khi cập nhật profile:

```kotlin
Button(
    onClick = {
        viewModel.updateUser(updatedUser)
        showSuccess = true
    }
) {
    Text("Save Profile")
}

if (showSuccess) {
    AutoDismissNotification(
        message = "Profile updated successfully!",
        type = NotificationType.SUCCESS,
        onDismiss = { showSuccess = false }
    )
}
```

### 3. OnboardingScreen
Thông báo lỗi khi validation fail:

```kotlin
if (showError) {
    ErrorDialog(
        message = "Please fill in all required fields correctly.",
        onDismiss = { showError = false }
    )
}
```

## Best Practices

1. **Luôn sử dụng confirmation dialog cho delete operations**
2. **Hiển thị success notification sau khi save/update thành công**
3. **Sử dụng AutoDismissNotification cho các thông báo không quan trọng**
4. **Sử dụng ErrorDialog cho các lỗi cần user acknowledge**
5. **Đặt tên state variables rõ ràng**: `showDeleteDialog`, `showSuccess`, etc.

## Animation

Tất cả dialog đều có animation:
- **Entry**: Scale in với bounce effect
- **Exit**: Scale out và fade
- **AutoDismiss**: Slide in từ trên xuống, slide out khi dismiss

## Customization

Nếu cần customize dialog, có thể:
1. Tạo variant của dialog base functions
2. Thay đổi colors, icons, animations
3. Thêm custom actions

## Notes

- Tất cả strings trong dialog hiện tại đang hardcoded. Nên di chuyển sang `strings.xml` để hỗ trợ đa ngôn ngữ
- Dialog sẽ tự động dismiss khi user tap outside (cho notification dialogs)
- Confirmation dialogs yêu cầu user chọn action rõ ràng (không dismiss khi tap outside)

