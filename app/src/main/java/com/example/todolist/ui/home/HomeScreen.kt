package com.example.todolist.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todolist.domain.model.Task

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onTaskClick: (Task) -> Unit = {}
) {
    val state = viewModel.uiState
    // Note: In real Compose code you should collectAsState() from a Flow/StateFlow in Composable
    // Here for a skeleton we assume state is observed properly by the caller

    Column {
        Text(text = "Home - Tasks", modifier = Modifier.padding(16.dp))
        LazyColumn {
            items(state.value.tasks) { task ->
                Row(modifier = Modifier
                    .clickable { onTaskClick(task) }
                    .padding(16.dp)) {
                    Text(text = task.title)
                }
            }
        }
    }
}
