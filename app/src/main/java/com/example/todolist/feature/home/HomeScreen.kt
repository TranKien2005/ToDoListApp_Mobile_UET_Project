@file:Suppress("DEPRECATION")

package com.example.todolist.feature.home

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.todolist.feature.home.components.CalendarGrid
import com.example.todolist.feature.home.components.MonthHeader
import com.example.todolist.feature.home.components.TaskCardItem
import com.example.todolist.feature.home.components.WeekDaysRow
import com.example.todolist.ui.common.ViewModelProvider


/**
 * Single HomeScreen: uses a HomeViewModel instance directly. This keeps UI wiring simple and
 * ensures all business logic (state, events) stays in the ViewModel.
 *
 * NOTE: Do not change DI or create providers here. ViewModel construction/DI remains responsibility
 * of the app (debug/release); this file only consumes a HomeViewModel instance.
 */
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by homeViewModel.uiState.collectAsState()

    // Make the entire Home screen scrollable by using a single LazyColumn. This allows
    // the user to swipe/scroll down the whole screen (calendar + tasks) as requested.
    // Tasks are schedules only; show all tasks for the day (filtering by completion removed)
    val tasksForDay = uiState.tasks

    LazyColumn(modifier = modifier.padding(16.dp)) {
        item {
            MonthHeader(
                yearMonth = uiState.currentMonth,
                onPrev = { homeViewModel.prevMonth() },
                onNext = { homeViewModel.nextMonth() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            WeekDaysRow()

            Spacer(modifier = Modifier.height(8.dp))

            // For preview mode show a simple placeholder to avoid composition-time crashes.
            if (LocalInspectionMode.current) {
                Text(text = "Calendar preview not available", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                CalendarGrid(
                    yearMonth = uiState.currentMonth,
                    selectedDate = uiState.selectedDate,
                    onDateSelected = { date -> homeViewModel.selectDate(date) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))


        }

        if (tasksForDay.isEmpty()) {
            item { Text(text = "No tasks", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(tasksForDay) { task ->
                TaskCardItem(
                    task = task,
                    onDelete = { id -> homeViewModel.deleteTask(id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val context = LocalContext.current
    val vm = ViewModelProvider.provideHomeViewModel(context)
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeScreen(homeViewModel = vm)
        }
    }
}
