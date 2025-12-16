package com.example.todolist.ui.layout

import android.app.Activity
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.todolist.data.local.database.AppDatabase
import com.example.todolist.feature.common.ViewModelProvider
import com.example.todolist.feature.home.HomeScreen
import com.example.todolist.feature.mission.MissionScreen
import com.example.todolist.feature.common.AddItemDialog
import com.example.todolist.feature.onboarding.OnboardingScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.example.todolist.core.model.Task
import com.example.todolist.core.model.Mission
import com.example.todolist.feature.notification.NotificationScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // create viewmodels here so we can forward save callbacks from the global add dialog
    // IMPORTANT: Use remember to cache ViewModels and prevent recreation on recomposition
    val homeViewModel = remember { ViewModelProvider.provideHomeViewModel(context) }
    val missionViewModel = remember { ViewModelProvider.provideMissionViewModel(context) }
    val addItemViewModel = remember { ViewModelProvider.provideAddItemViewModel(context) }
    val notificationViewModel = remember { ViewModelProvider.provideNotificationViewModel(context) }
    val voiceViewModel = remember { ViewModelProvider.provideVoiceAssistantViewModel(context) }

    // Lấy user data để hiển thị trên TopBar
    val userViewModel = remember { ViewModelProvider.provideUserViewModel(context) }
    val user by userViewModel.user.collectAsState()

    // Lấy số lượng notification chưa đọc
    val notificationUiState by notificationViewModel.uiState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var editingMission by remember { mutableStateOf<Mission?>(null) }

    // observe current route to decide default add type
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    // Kiểm tra user và navigate đến onboarding nếu chưa có user
    LaunchedEffect(user) {
        if (user == null && currentRoute != "onboarding") {
            navController.navigate("onboarding") {
                popUpTo(0) { inclusive = true } // Clear back stack
            }
        }
    }

    // Chỉ hiển thị TopBar và BottomBar khi không ở màn hình onboarding
    val showBars = currentRoute != "onboarding" && currentRoute != "notifications" && currentRoute != "voice"

    AppScaffold(
        title = "Todolist",
        showTopBar = showBars,  // Ẩn TopBar khi ở onboarding hoặc notifications
        showBottomBar = showBars,  // Ẩn BottomBar khi ở onboarding hoặc notifications
        user = user,  // Truyền user data
        unreadNotificationCount = notificationUiState.unreadCount,
        onNotificationClick = { navController.navigate("notifications") },
        onSettingsClick = { navController.navigate("settings") },  // Navigate đến settings
        onHome = { navController.navigate("home") },
        onList = { navController.navigate("missions") },
        onStats = { navController.navigate("analysis") },
        onVoice = { navController.navigate("voice") },
        onAdd = {
            editingTask = null
            editingMission = null
            showAddDialog = true
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (user == null) "onboarding" else "home",
            modifier = Modifier
        ) {
            composable("onboarding") {
                OnboardingScreen(
                    onUserCreated = { newUser ->
                        userViewModel.saveUser(newUser)
                        navController.navigate("home") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }

            composable("home") {
                HomeScreen(
                    homeViewModel = homeViewModel,
                    modifier = Modifier.padding(padding),
                    onEditTask = { task ->
                        editingTask = task
                        editingMission = null
                        showAddDialog = true
                    }
                )
            }

            composable("missions") {
                MissionScreen(
                    missionViewModel = missionViewModel,
                    modifier = Modifier.padding(padding),
                    onEditMission = { mission ->
                        editingMission = mission
                        editingTask = null
                        showAddDialog = true
                    }
                )
            }

            composable("analysis") {
                val analysisVm = ViewModelProvider.provideMissionAnalysisViewModel(context)
                com.example.todolist.feature.analysis.MissionAnalysisScreen(
                    viewModel = analysisVm,
                    modifier = Modifier.padding(padding)
                )
            }

            composable("settings") {
                val settingsVm = ViewModelProvider.provideSettingsViewModel(context)
                val coroutineScope = rememberCoroutineScope()
                com.example.todolist.feature.settings.SettingsScreen(
                    viewModel = settingsVm,
                    userViewModel = userViewModel,
                    onBackClick = { navController.popBackStack() },
                    modifier = Modifier.padding(padding),
                    onDeleteAccount = {
                        coroutineScope.launch(Dispatchers.IO) {
                            // Clear all database tables
                            val database = AppDatabase.getInstance(context)
                            database.clearAllTables()
                            
                            // Exit the app
                            (context as? Activity)?.finishAffinity()
                            exitProcess(0)
                        }
                    }
                )
            }

            composable("notifications") {
                NotificationScreen(
                    viewModel = notificationViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("voice") {
                com.example.todolist.feature.voice.VoiceAssistantScreen(
                    viewModel = voiceViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }

    if (showAddDialog) {
        val defaultIsTask = currentRoute == null || currentRoute == "home"
        AddItemDialog(
            onDismissRequest = {
                showAddDialog = false
                editingTask = null
                editingMission = null
            },
            addItemViewModel = addItemViewModel,
            defaultIsTask = defaultIsTask,
            editTask = editingTask,
            editMission = editingMission
        )
    }
}
