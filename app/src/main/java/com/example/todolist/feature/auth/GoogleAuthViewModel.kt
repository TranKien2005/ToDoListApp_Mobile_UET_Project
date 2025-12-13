package com.example.todolist.feature.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.repository.GoogleAuthRepository
import com.example.todolist.data.repository.GoogleCalendarRepository
import com.example.todolist.data.repository.GoogleUser
import com.example.todolist.domain.repository.TaskRepository
import com.example.todolist.core.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * UI State for Google Authentication
 */
data class GoogleAuthUiState(
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val currentUser: GoogleUser? = null,
    val error: String? = null,
    val isSyncingCalendar: Boolean = false,
    val calendarSyncError: String? = null,
    val lastSyncResult: String? = null
)

/**
 * ViewModel for handling Google Sign-In operations and Calendar Sync orchestration
 */
class GoogleAuthViewModel(
    private val googleAuthRepository: GoogleAuthRepository,
    private val googleCalendarRepository: GoogleCalendarRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "GoogleAuthViewModel"
    }
    
    private val _uiState = MutableStateFlow(GoogleAuthUiState())
    val uiState: StateFlow<GoogleAuthUiState> = _uiState.asStateFlow()
    
    init {
        // Check if already signed in
        _uiState.update { 
            it.copy(
                isSignedIn = googleAuthRepository.isSignedIn(),
                currentUser = googleAuthRepository.getCurrentUser()
            )
        }
    }
    
    /**
     * Initiate Google Sign-In flow
     * @param context Activity context required for Credential Manager
     */
    fun signIn(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            googleAuthRepository.signIn(context)
                .onSuccess { user ->
                    Log.d(TAG, "Sign-in successful: ${user.email}")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isSignedIn = true,
                            currentUser = user,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    Log.e(TAG, "Sign-in failed", exception)
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isSignedIn = false,
                            error = exception.message ?: "Sign-in failed"
                        )
                    }
                }
        }
    }
    
    /**
     * Sign out from Google account
     */
    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            googleAuthRepository.signOut()
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    isSignedIn = false,
                    currentUser = null,
                    error = null
                )
            }
            Log.d(TAG, "Sign-out successful")
        }
    }
    
    /**
     * Toggles the calendar synchronization and performs an initial import/sync if enabling.
     * @param enabled If true, initiates the sync/import.
     */
    fun toggleCalendarSync(context: Context, enabled: Boolean) {
        if (!enabled) return // Only handle the enable case for initial integration
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingCalendar = true, calendarSyncError = null) }
            
            val accessToken = googleAuthRepository.getCalendarAccessToken(context)
            
            if (accessToken == null) {
                _uiState.update { 
                    it.copy(
                        isSyncingCalendar = false,
                        calendarSyncError = "Cannot sync: Google Access Token is missing. Please re-sign in or check token implementation."
                    )
                }
                Log.e(TAG, "Failed to get access token for calendar sync.")
                return@launch
            }
            
            // 1. Import events from Calendar (last 30 days and next 6 months)
            val startDate = LocalDate.now().minusDays(30)
            val endDate = LocalDate.now().plusMonths(6)
            
            var importSuccess = false
            googleCalendarRepository.importEventsFromCalendar(startDate, endDate, accessToken)
                .onSuccess { importedTasks ->
                    // Logic to save imported tasks locally (e.g., using taskRepository.insertTasks(importedTasks))
                    importedTasks.forEach { task ->
                        taskRepository.saveTask(task)
                    }
                    importSuccess = true
                    Log.d(TAG, "Successfully imported ${importedTasks.size} tasks from Google Calendar.")
                }
                .onFailure { e ->
                    _uiState.update { 
                        it.copy(
                            calendarSyncError = "Failed to import tasks: ${e.message}"
                        )
                    }
                    Log.e(TAG, "Calendar import failed", e)
                }
            
            // If import failed, stop here
            if (!importSuccess && _uiState.value.calendarSyncError != null) {
                 _uiState.update { it.copy(isSyncingCalendar = false) }
                 return@launch
            }

            // 2. Sync local tasks to Calendar (Push)
            try {
                val tasks = taskRepository.getTasks().first()
                var pushSuccessCount = 0
                var pushFailCount = 0
                
                tasks.forEach { task ->
                     if (task.googleCalendarEventId == null) {
                         googleCalendarRepository.syncTaskToCalendar(task, accessToken)
                             .onSuccess { eventId ->
                                 val updatedTask = task.copy(googleCalendarEventId = eventId)
                                 taskRepository.saveTask(updatedTask)
                                 pushSuccessCount++
                             }
                             .onFailure { pushFailCount++ }
                     } else {
                         // Update existing?
                         // For now, we only push new ones or update if logic permits
                         googleCalendarRepository.updateEventInCalendar(task, accessToken)
                             .onSuccess { pushSuccessCount++ }
                             .onFailure { pushFailCount++ }
                     }
                }
                
                 _uiState.update { 
                     it.copy(
                         isSyncingCalendar = false, 
                         lastSyncResult = "Sync completed. Pushed: $pushSuccessCount, Failed: $pushFailCount"
                     ) 
                 }
                 
            } catch (e: Exception) {
                Log.e(TAG, "Sync to calendar failed", e)
                 _uiState.update { 
                    it.copy(
                        isSyncingCalendar = false,
                        calendarSyncError = "Sync to calendar failed: ${e.message}"
                    )
                 }
            }
        }
    }
    
    /**
     * Get pending intent to request calendar permission
     */
    suspend fun getCalendarPermissionIntent(context: Context): android.app.PendingIntent? {
        return googleAuthRepository.getCalendarPermissionIntent(context)
    }

    /**
     * Handle permission result from activity
     */
    fun handleAuthorizationResult(context: Context, data: Intent) {
        viewModelScope.launch {
            val success = googleAuthRepository.handleAuthorizationResult(context, data)
            if (success) {
                _uiState.update { it.copy(calendarSyncError = null) }
                // Retry sync/import logic if needed, strictly we might want to re-trigger toggleCalendarSync
                // But for now, just clearing error is enough, user can toggle again
                toggleCalendarSync(context, true)
            } else {
                 _uiState.update { 
                    it.copy(calendarSyncError = "Failed to obtain calendar permission.") 
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
}
