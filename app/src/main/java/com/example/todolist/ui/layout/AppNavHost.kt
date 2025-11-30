package com.example.todolist.ui.layout

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.todolist.ui.common.ViewModelProvider
import com.example.todolist.feature.home.HomeScreen
import com.example.todolist.feature.mission.MissionScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    AppScaffold(title = "Todolist", showBottomBar = true,
        onHome = { navController.navigate("home") },
        onList = { navController.navigate("missions") },
        onStats = {},
        onVoice = {},
        onAdd = {}
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
        ) {
            composable("home") {
                val context = LocalContext.current
                val homeViewModel = ViewModelProvider.provideHomeViewModel(context)
                HomeScreen(
                    homeViewModel = homeViewModel,
                    modifier = Modifier.padding(padding)
                )
            }

            composable("missions") {
                val context = LocalContext.current
                val missionViewModel = ViewModelProvider.provideMissionViewModel(context)
                MissionScreen(
                    missionViewModel = missionViewModel,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}
