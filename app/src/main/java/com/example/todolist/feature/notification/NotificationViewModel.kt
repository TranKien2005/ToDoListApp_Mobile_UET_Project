package com.example.todolist.feature.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.core.model.Notification
import com.example.todolist.domain.usecase.NotificationUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationUiState(
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class NotificationViewModel(
    private val notificationUseCases: NotificationUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                notificationUseCases.getNotifications().collect { allNotifications ->
                    // Chỉ hiện notifications đã được deliver
                    val deliveredNotifications = allNotifications.filter { it.isDelivered }
                    val unreadCount = deliveredNotifications.count { !it.isRead }

                    _uiState.value = _uiState.value.copy(
                        notifications = deliveredNotifications.sortedByDescending { it.scheduledTime },
                        unreadCount = unreadCount,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            try {
                notificationUseCases.markNotificationAsRead(notificationId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                _uiState.value.notifications
                    .filter { !it.isRead }
                    .forEach { notification ->
                        notificationUseCases.markNotificationAsRead(notification.id)
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteNotification(notificationId: Long) {
        viewModelScope.launch {
            try {
                // Cần thêm delete use case
                // notificationUseCases.deleteNotification(notificationId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch {
            try {
                notificationUseCases.deleteReadNotifications()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}

