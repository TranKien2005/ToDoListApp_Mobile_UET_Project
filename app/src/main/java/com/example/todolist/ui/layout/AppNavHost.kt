package com.example.todolist.ui.layout

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.todolist.ui.common.ViewModelProvider
import com.example.todolist.feature.home.HomeScreen
import com.example.todolist.feature.mission.MissionScreen
import com.example.todolist.feature.common.AddItemDialog
import com.example.todolist.feature.onboarding.OnboardingScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.example.todolist.core.model.Task
import com.example.todolist.core.model.Mission

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // create viewmodels here so we can forward save callbacks from the global add dialog
    // IMPORTANT: Use remember to cache ViewModels and prevent recreation on recomposition
    val homeViewModel = remember { ViewModelProvider.provideHomeViewModel(context) }
    val missionViewModel = remember { ViewModelProvider.provideMissionViewModel(context) }
    val addItemViewModel = remember { ViewModelProvider.provideAddItemViewModel(context) }

    // Lấy user data để hiển thị trên TopBar
    val userViewModel = remember { ViewModelProvider.provideUserViewModel(context) }
    val user by userViewModel.user.collectAsState()

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
    val showBars = currentRoute != "onboarding"

    AppScaffold(
        title = "Todolist",
        showTopBar = showBars,  // Ẩn TopBar khi ở onboarding
        showBottomBar = showBars,  // Ẩn BottomBar khi ở onboarding
        user = user,  // Truyền user data
        onSettingsClick = { navController.navigate("settings") },  // Navigate đến settings
        onHome = { navController.navigate("home") },
        onList = { navController.navigate("missions") },
        onStats = { navController.navigate("analysis") },
        onVoice = {},
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
                com.example.todolist.feature.settings.SettingsScreen(
                    viewModel = settingsVm,
                    userViewModel = userViewModel,
                    onBackClick = { navController.popBackStack() },
                    modifier = Modifier.padding(padding)
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
