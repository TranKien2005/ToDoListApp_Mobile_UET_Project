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

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // create viewmodels here so we can forward save callbacks from the global add dialog
    val homeViewModel = ViewModelProvider.provideHomeViewModel(context)
    val missionViewModel = ViewModelProvider.provideMissionViewModel(context)
    val addItemViewModel = ViewModelProvider.provideAddItemViewModel(context)

    var showAddDialog by remember { mutableStateOf(false) }

    // observe current route to decide default add type
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    AppScaffold(title = "Todolist", showBottomBar = true,
        onHome = { navController.navigate("home") },
        onList = { navController.navigate("missions") },
        onStats = { navController.navigate("analysis") },
        onVoice = {},
        onAdd = { showAddDialog = true }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
        ) {
            composable("home") {
                HomeScreen(
                    homeViewModel = homeViewModel,
                    modifier = Modifier.padding(padding)
                )
            }

            composable("missions") {
                MissionScreen(
                    missionViewModel = missionViewModel,
                    modifier = Modifier.padding(padding)
                )
            }

            composable("analysis") {
                val analysisVm = ViewModelProvider.provideMissionAnalysisViewModel(context)
                com.example.todolist.feature.analysis.MissionAnalysisScreen(
                    viewModel = analysisVm,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    if (showAddDialog) {
        val defaultIsTask = currentRoute == null || currentRoute == "home"
        AddItemDialog(
            onDismissRequest = { showAddDialog = false },
            addItemViewModel = addItemViewModel,
            defaultIsTask = defaultIsTask
        )
    }
}
