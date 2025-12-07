package com.example.todolist

import android.app.Application
import com.example.todolist.di.AppModule
import com.example.todolist.notification.NotificationForegroundService
import com.example.todolist.util.logger.AppLogger

// To enable Hilt, uncomment the annotation below and add Hilt deps in Gradle
// @HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize ManualProviders with application context to use Room-backed repos
        try {

            AppLogger.i("ManualProviders initialized with Room DB and optional AI remote")
        } catch (t: Throwable) {
            AppLogger.e("Failed to init ManualProviders", t)
        }
        val container = AppModule(this)
        // App-wide initialization (Logging, Crashlytics, WorkManager setup, etc.)

        // Start foreground service to keep notification system running
        NotificationForegroundService.start(this)
    }
}
