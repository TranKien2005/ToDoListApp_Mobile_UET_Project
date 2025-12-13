package com.example.todolist.data.repository

import android.util.Log
import com.example.todolist.core.model.RepeatType
import com.example.todolist.core.model.Task
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

/**
 * Implementation of GoogleCalendarRepository using Google Calendar API
 */
class GoogleCalendarRepositoryImpl : GoogleCalendarRepository {
    
    companion object {
        private const val TAG = "GoogleCalendarRepo"
        private const val APPLICATION_NAME = "ToDoListApp"
        private const val CALENDAR_ID = "primary"
    }
    
    private fun buildCalendarService(accessToken: String): Calendar {
        val credentials = GoogleCredentials.create(AccessToken(accessToken, null))
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()
        
        return Calendar.Builder(httpTransport, jsonFactory, HttpCredentialsAdapter(credentials))
            .setApplicationName(APPLICATION_NAME)
            .build()
    }
    
    override suspend fun syncTaskToCalendar(task: Task, accessToken: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val calendarService = buildCalendarService(accessToken)
                
                val event = Event().apply {
                    summary = task.title
                    description = task.description
                    
                    val startDateTime = task.startTime
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .let { com.google.api.client.util.DateTime(Date.from(it)) }
                    
                    start = EventDateTime().setDateTime(startDateTime)
                    
                    // Calculate end time based on duration
                    val endDateTime = task.startTime
                        .plusMinutes(task.durationMinutes ?: 60)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .let { com.google.api.client.util.DateTime(Date.from(it)) }
                    
                    end = EventDateTime().setDateTime(endDateTime)
                    
                    // Add recurrence rule if task repeats
                    recurrence = when (task.repeatType) {
                        RepeatType.DAILY -> listOf("RRULE:FREQ=DAILY")
                        RepeatType.WEEKLY -> listOf("RRULE:FREQ=WEEKLY")
                        RepeatType.MONTHLY -> listOf("RRULE:FREQ=MONTHLY")
                        RepeatType.NONE -> null
                    }
                }
                
                val createdEvent = calendarService.events()
                    .insert(CALENDAR_ID, event)
                    .execute()
                
                Log.d(TAG, "Event created: ${createdEvent.id}")
                Result.success(createdEvent.id)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync task to calendar", e)
                Result.failure(e)
            }
        }
    }
    
    override suspend fun deleteEventFromCalendar(eventId: String, accessToken: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val calendarService = buildCalendarService(accessToken)
                calendarService.events().delete(CALENDAR_ID, eventId).execute()
                Log.d(TAG, "Event deleted: $eventId")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete event from calendar", e)
                Result.failure(e)
            }
        }
    }
    
    override suspend fun updateEventInCalendar(task: Task, accessToken: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val eventId = task.googleCalendarEventId 
                    ?: return@withContext Result.failure(Exception("No calendar event ID"))
                
                val calendarService = buildCalendarService(accessToken)
                
                val event = calendarService.events().get(CALENDAR_ID, eventId).execute()
                
                event.apply {
                    summary = task.title
                    description = task.description
                    
                    val startDateTime = task.startTime
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .let { com.google.api.client.util.DateTime(Date.from(it)) }
                    
                    start = EventDateTime().setDateTime(startDateTime)
                    
                    val endDateTime = task.startTime
                        .plusMinutes(task.durationMinutes ?: 60)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .let { com.google.api.client.util.DateTime(Date.from(it)) }
                    
                    end = EventDateTime().setDateTime(endDateTime)
                }
                
                calendarService.events().update(CALENDAR_ID, eventId, event).execute()
                Log.d(TAG, "Event updated: $eventId")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update event in calendar", e)
                Result.failure(e)
            }
        }
    }
    
    override suspend fun importEventsFromCalendar(
        startDate: LocalDate, 
        endDate: LocalDate, 
        accessToken: String
    ): Result<List<Task>> {
        return withContext(Dispatchers.IO) {
            try {
                val calendarService = buildCalendarService(accessToken)
                
                val timeMin = startDate.atStartOfDay()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .let { com.google.api.client.util.DateTime(Date.from(it)) }
                
                val timeMax = endDate.plusDays(1).atStartOfDay()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .let { com.google.api.client.util.DateTime(Date.from(it)) }
                
                val events = calendarService.events()
                    .list(CALENDAR_ID)
                    .setTimeMin(timeMin)
                    .setTimeMax(timeMax)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute()
                
                val tasks = events.items?.mapNotNull { event ->
                    val startTime = event.start?.dateTime?.value 
                        ?: event.start?.date?.value 
                        ?: return@mapNotNull null
                    
                    val endTime = event.end?.dateTime?.value 
                        ?: event.end?.date?.value 
                        ?: startTime
                    
                    val duration = (endTime - startTime) / (1000 * 60) // Convert to minutes
                    
                    Task(
                        id = 0, // New task
                        title = event.summary ?: "Imported Event",
                        description = event.description,
                        startTime = LocalDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(startTime),
                            ZoneId.systemDefault()
                        ),
                        durationMinutes = duration,
                        repeatType = RepeatType.NONE, // Imported as single event
                        googleCalendarEventId = event.id
                    )
                } ?: emptyList()
                
                Log.d(TAG, "Imported ${tasks.size} events from calendar")
                Result.success(tasks)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to import events from calendar", e)
                Result.failure(e)
            }
        }
    }
}
