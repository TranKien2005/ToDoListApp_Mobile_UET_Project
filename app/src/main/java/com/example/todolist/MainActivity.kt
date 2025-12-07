package com.example.todolist

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.todolist.di.AppModule
import com.example.todolist.ui.layout.AppNavHost
import com.example.todolist.ui.theme.TodolistTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Request permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            android.util.Log.d("MainActivity", "Notification permission granted")
        } else {
            android.util.Log.e("MainActivity", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission on Android 13+
        requestNotificationPermission()

        // Handle notification click
        handleNotificationClick()

        setContent {
            TodolistTheme {
                AppNavHost()
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationClick()
    }

    private fun handleNotificationClick() {
        val notificationId = intent.getLongExtra("notification_id", -1L)
        val shouldMarkAsRead = intent.getBooleanExtra("mark_as_read", false)

        if (notificationId != -1L && shouldMarkAsRead) {
            android.util.Log.d("MainActivity", "Marking notification $notificationId as read")

            lifecycleScope.launch {
                try {
                    val container = AppModule(applicationContext)
                    container.domainModule.notificationUseCases.markNotificationAsRead(notificationId)
                    android.util.Log.d("MainActivity", "Notification $notificationId marked as read successfully")
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Error marking notification as read", e)
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    android.util.Log.d("MainActivity", "Notification permission already granted")
                }
                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}
