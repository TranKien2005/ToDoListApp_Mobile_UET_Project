package com.example.todolist.data.service

import android.content.Context
import android.util.Log
import com.example.todolist.core.model.Task
import com.example.todolist.data.repository.GoogleCalendarRepository
import com.example.todolist.domain.repository.TaskRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Service that handles synchronization between local tasks and Google Calendar
 * Manages OAuth token retrieval and delegates to GoogleCalendarRepository
 */
class CalendarSyncService(
    private val context: Context,
    private val calendarRepository: GoogleCalendarRepository,
    private val taskRepository: TaskRepository
) {
    companion object {
        private const val TAG = "CalendarSyncService"
    }

    /**
     * Get OAuth access token for Google Calendar API
     * Requires user to be signed in with calendar scope
     */
    private suspend fun getAccessToken(): String? {
        return try {
            val account = GoogleSignIn.getLastSignedInAccount(context) ?: run {
                Log.w(TAG, "No Google account signed in")
                return null
            }
            
            // Check if we have calendar scope
            if (!GoogleSignIn.hasPermissions(account, Scope(CalendarScopes.CALENDAR))) {
                Log.w(TAG, "Missing Calendar permission scope")
                return null
            }
            
            // Get fresh access token
            val tokenResult = com.google.android.gms.auth.GoogleAuthUtil.getToken(
                context,
                account.account!!,
                "oauth2:${CalendarScopes.CALENDAR}"
            )
            
            Log.d(TAG, "Access token obtained successfully")
            tokenResult
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get access token", e)
            null
        }
    }

    /**
     * Sync a single task to Google Calendar
     * Creates event if task doesn't have googleCalendarEventId, updates otherwise
     */
    suspend fun syncTask(task: Task): Result<Task> {
        val accessToken = getAccessToken() ?: return Result.failure(
            Exception("Not authenticated with Google Calendar")
        )

        return if (task.googleCalendarEventId == null) {
            // Create new event
            calendarRepository.syncTaskToCalendar(task, accessToken)
                .map { eventId ->
                    task.copy(googleCalendarEventId = eventId)
                }
        } else {
            // Update existing event
            calendarRepository.updateEventInCalendar(task, accessToken)
                .map { task }
        }
    }

    /**
     * Delete a task's associated calendar event
     */
    suspend fun deleteTaskEvent(task: Task): Result<Unit> {
        val eventId = task.googleCalendarEventId ?: return Result.success(Unit)
        
        val accessToken = getAccessToken() ?: return Result.failure(
            Exception("Not authenticated with Google Calendar")
        )

        return calendarRepository.deleteEventFromCalendar(eventId, accessToken)
    }

    /**
     * Sync all tasks that have calendar sync enabled
     */
    suspend fun syncAllTasks(): Result<Int> {
        val accessToken = getAccessToken() ?: return Result.failure(
            Exception("Not authenticated with Google Calendar")
        )

        return try {
            val tasks = taskRepository.getTasks().first()
            var syncedCount = 0

            tasks.forEach { task ->
                val result = if (task.googleCalendarEventId == null) {
                    calendarRepository.syncTaskToCalendar(task, accessToken)
                        .onSuccess { eventId ->
                            // Update task with event ID
                            val updatedTask = task.copy(googleCalendarEventId = eventId)
                            taskRepository.saveTask(updatedTask)
                            syncedCount++
                        }
                } else {
                    calendarRepository.updateEventInCalendar(task, accessToken)
                        .onSuccess { syncedCount++ }
                }

                if (result.isFailure) {
                    Log.w(TAG, "Failed to sync task ${task.id}: ${result.exceptionOrNull()?.message}")
                }
            }

            Log.d(TAG, "Synced $syncedCount tasks to Google Calendar")
            Result.success(syncedCount)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync all tasks", e)
            Result.failure(e)
        }
    }

    /**
     * Import events from Google Calendar as tasks
     */
    suspend fun importFromCalendar(startDate: LocalDate, endDate: LocalDate): Result<List<Task>> {
        val accessToken = getAccessToken() ?: return Result.failure(
            Exception("Not authenticated with Google Calendar")
        )

        return calendarRepository.importEventsFromCalendar(startDate, endDate, accessToken)
            .onSuccess { importedTasks ->
                // Save imported tasks to local database
                importedTasks.forEach { task ->
                    taskRepository.saveTask(task)
                }
                Log.d(TAG, "Imported ${importedTasks.size} events from Google Calendar")
            }
    }

    /**
     * Check if calendar sync is available (user signed in with proper scope)
     */
    fun isCalendarSyncAvailable(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return false
        return GoogleSignIn.hasPermissions(account, Scope(CalendarScopes.CALENDAR))
    }
}
