package com.example.todolist.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.todolist.ui.nav.mainGraph

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    AppScaffold(title = "Todolist") { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
        ) {
            mainGraph(navController)
        }
    }
}
