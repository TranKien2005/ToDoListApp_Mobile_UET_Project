package com.example.todolist.ui.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String = "Todolist",
    showBottomBar: Boolean = true,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = title) })
        },
        bottomBar = {
            if (showBottomBar) {
                BottomAppBar {
                    Text(text = "Bottom")
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        content(innerPadding)
    }
}

