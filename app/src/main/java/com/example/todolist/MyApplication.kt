package com.example.todolist

import android.app.Application
import com.example.todolist.di.ManualProviders
import com.example.todolist.util.logger.AppLogger

// To enable Hilt, uncomment the annotation below and add Hilt deps in Gradle
// @HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()d
        // Initialize ManualProviders with application context to use Room-backed repos
        try {
            // Pass BuildConfig AI settings (empty by default)
            ManualProviders.initWithContext(
                context = applicationContext,
                aiBaseUrl = BuildConfig.AI_BASE_URL.takeIf { it.isNotBlank() },
                aiApiKey = BuildConfig.AI_API_KEY.takeIf { it.isNotBlank() }
            )
            AppLogger.i("ManualProviders initialized with Room DB and optional AI remote")
        } catch (t: Throwable) {
            AppLogger.e("Failed to init ManualProviders", t)
        }
        // App-wide initialization (Logging, Crashlytics, WorkManager setup, etc.)
    }
}
