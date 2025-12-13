package com.example.todolist.data.repository

import com.example.todolist.core.model.Task
import java.time.LocalDate

/**
 * Repository interface for Google Calendar synchronization operations
 */
interface GoogleCalendarRepository {
    /**
     * Sync a task to Google Calendar
     * @param task Task to sync
     * @param accessToken OAuth access token for Google Calendar API
     * @return Result containing the created event ID on success
     */
    suspend fun syncTaskToCalendar(task: Task, accessToken: String): Result<String>
    
    /**
     * Delete an event from Google Calendar
     * @param eventId The Google Calendar event ID to delete
     * @param accessToken OAuth access token for Google Calendar API
     */
    suspend fun deleteEventFromCalendar(eventId: String, accessToken: String): Result<Unit>
    
    /**
     * Update an existing event in Google Calendar
     * @param task The updated task
     * @param accessToken OAuth access token for Google Calendar API
     */
    suspend fun updateEventInCalendar(task: Task, accessToken: String): Result<Unit>
    
    /**
     * Import events from Google Calendar within a date range
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @param accessToken OAuth access token for Google Calendar API
     * @return List of tasks created from calendar events
     */
    suspend fun importEventsFromCalendar(
        startDate: LocalDate, 
        endDate: LocalDate, 
        accessToken: String
    ): Result<List<Task>>
}
