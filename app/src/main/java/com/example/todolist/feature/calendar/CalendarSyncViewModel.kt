package com.example.todolist.feature.calendar

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.core.model.Task
import com.example.todolist.data.repository.GoogleCalendarRepository
import com.example.todolist.data.repository.GoogleSignInRepository
import com.example.todolist.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * UI State for Calendar Sync
 */
data class CalendarSyncUiState(
    val isSignedIn: Boolean = false,
    val hasCalendarPermission: Boolean = false,
    val userEmail: String? = null,
    val userPhotoUrl: String? = null,
    val isSyncing: Boolean = false,
    val lastSyncResult: String? = null,
    val error: String? = null
)

/**
 * ViewModel for managing Google Calendar synchronization
 */
class CalendarSyncViewModel(
    private val signInRepository: GoogleSignInRepository,
    private val calendarRepository: GoogleCalendarRepository,
    private val taskRepository: TaskRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    companion object {
        private const val TAG = "CalendarSyncViewModel"
    }

    private val _uiState = MutableStateFlow(CalendarSyncUiState())
    val uiState: StateFlow<CalendarSyncUiState> = _uiState.asStateFlow()

    init {
        checkSignInStatus()
    }

    /**
     * Check current sign-in status
     */
    fun checkSignInStatus() {
        val account = signInRepository.getCurrentAccount()
        val hasPermission = signInRepository.hasCalendarPermission()
        
        _uiState.update {
            it.copy(
                isSignedIn = account != null,
                hasCalendarPermission = hasPermission,
                userEmail = account?.email,
                userPhotoUrl = account?.photoUrl?.toString()
            )
        }
    }

    /**
     * Get sign-in intent to launch Google Sign-In
     */
    fun getSignInIntent(): Intent {
        return signInRepository.getSignInIntent()
    }

    /**
     * Handle sign-in result from activity
     */
    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch {
            try {
                signInRepository.handleSignInResult(data)
                    .onSuccess { account ->
                        _uiState.update {
                            it.copy(
                                isSignedIn = true,
                                hasCalendarPermission = signInRepository.hasCalendarPermission(),
                                userEmail = account.email,
                                userPhotoUrl = account.photoUrl?.toString(),
                                error = null
                            )
                        }
                    }
                    .onFailure { exception ->
                        _uiState.update {
                            it.copy(
                                isSignedIn = false,
                                error = exception.message ?: "Sign-in failed"
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSignedIn = false,
                        error = e.message ?: "Unknown error during sign-in"
                    )
                }
            }
        }
    }

    /**
     * Sign out from Google
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                signInRepository.signOut()
                _uiState.update {
                    CalendarSyncUiState() // Reset state
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Sign-out failed")
                }
            }
        }
    }

    /**
     * Sync a single task to Google Calendar
     */
    fun syncTask(task: Task) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isSyncing = true, error = null) }

            try {
                val accessToken = signInRepository.getAccessToken()
                if (accessToken == null) {
                    _uiState.update {
                        it.copy(isSyncing = false, error = "Not authenticated")
                    }
                    return@launch
                }

                val result = if (task.googleCalendarEventId == null) {
                    calendarRepository.syncTaskToCalendar(task, accessToken)
                        .onSuccess { eventId ->
                            // Update task with event ID in database
                            val updatedTask = task.copy(googleCalendarEventId = eventId)
                            taskRepository.saveTask(updatedTask)
                        }
                } else {
                    calendarRepository.updateEventInCalendar(task, accessToken)
                }

                result
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isSyncing = false,
                                lastSyncResult = "Task synced successfully",
                                error = null
                            )
                        }
                    }
                    .onFailure { exception ->
                        _uiState.update {
                            it.copy(
                                isSyncing = false,
                                error = exception.message ?: "Sync failed"
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        error = e.message ?: "Unknown sync error"
                    )
                }
            }
        }
    }

    /**
     * Sync all local tasks to Google Calendar
     */
    fun syncAllTasks() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isSyncing = true, error = null) }

            try {
                val accessToken = signInRepository.getAccessToken()
                if (accessToken == null) {
                    _uiState.update {
                        it.copy(isSyncing = false, error = "Not authenticated")
                    }
                    return@launch
                }

                val tasks = taskRepository.getTasks().first()
                var successCount = 0
                var failCount = 0

                tasks.forEach { task ->
                    try {
                        val result = if (task.googleCalendarEventId == null) {
                            calendarRepository.syncTaskToCalendar(task, accessToken)
                                .onSuccess { eventId ->
                                    val updatedTask = task.copy(googleCalendarEventId = eventId)
                                    taskRepository.saveTask(updatedTask)
                                    successCount++
                                }
                        } else {
                            calendarRepository.updateEventInCalendar(task, accessToken)
                                .onSuccess { successCount++ }
                        }

                        if (result.isFailure) failCount++
                    } catch (e: Exception) {
                        failCount++
                        Log.e(TAG, "Error syncing task ${task.id}", e)
                    }
                }

                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        lastSyncResult = "Synced $successCount tasks ($failCount failed)",
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sync all failed", e)
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        error = e.message ?: "Sync failed"
                    )
                }
            }
        }
    }

    /**
     * Delete a task from Google Calendar
     */
    fun deleteTaskFromCalendar(task: Task) {
        val eventId = task.googleCalendarEventId ?: return

        viewModelScope.launch(ioDispatcher) {
            try {
                val accessToken = signInRepository.getAccessToken() ?: return@launch

                calendarRepository.deleteEventFromCalendar(eventId, accessToken)
                    .onSuccess {
                        Log.d(TAG, "Event deleted from calendar: $eventId")
                    }
                    .onFailure { exception ->
                        Log.e(TAG, "Failed to delete event", exception)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception deleting event", e)
            }
        }
    }

    /**
     * Import events from Google Calendar
     */
    fun importFromCalendar(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isSyncing = true, error = null) }

            try {
                val accessToken = signInRepository.getAccessToken()
                if (accessToken == null) {
                    _uiState.update {
                        it.copy(isSyncing = false, error = "Not authenticated")
                    }
                    return@launch
                }

                calendarRepository.importEventsFromCalendar(startDate, endDate, accessToken)
                    .onSuccess { tasks ->
                        // Save imported tasks
                        tasks.forEach { task ->
                            taskRepository.saveTask(task)
                        }
                        _uiState.update {
                            it.copy(
                                isSyncing = false,
                                lastSyncResult = "Imported ${tasks.size} events",
                                error = null
                            )
                        }
                    }
                    .onFailure { exception ->
                        _uiState.update {
                            it.copy(
                                isSyncing = false,
                                error = exception.message ?: "Import failed"
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        error = e.message ?: "Unknown import error"
                    )
                }
            }
        }
    }

    /**
     * Clear any error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Clear sync result message
     */
    fun clearSyncResult() {
        _uiState.update { it.copy(lastSyncResult = null) }
    }
}
