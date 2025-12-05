package com.example.todolist.feature.mission

import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.todolist.ui.common.ViewModelProvider
import com.example.todolist.feature.mission.components.MissionCardItem
import com.example.todolist.feature.mission.components.DateNavigator
import com.example.todolist.feature.mission.components.StatusFilterRow

@Composable
fun MissionScreen(
    missionViewModel: MissionViewModel,
    modifier: Modifier = Modifier,
    debug: Boolean = false
) {
    val uiState by missionViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val missions = uiState.missions
    val gran = uiState.granularity
    val ref = uiState.referenceDate

    LazyColumn(modifier = modifier.padding(16.dp)) {
        item {
            // Use shared DateNavigator component which includes prev/title/next and granularity chips
            DateNavigator(
                referenceDate = ref,
                granularity = gran,
                onPrev = {
                    missionViewModel.prev()
                    if (debug) Toast.makeText(context, "Prev pressed", Toast.LENGTH_SHORT).show()
                },
                onNext = {
                    missionViewModel.next()
                    if (debug) Toast.makeText(context, "Next pressed", Toast.LENGTH_SHORT).show()
                },
                onSetGranularity = { g ->
                    missionViewModel.setGranularity(g)
                    if (debug) Toast.makeText(context, "Granularity: $g", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Status filter row (All / Completed / In progress / Missed) - use component
            StatusFilterRow(selectedFilter = uiState.statusFilter, onSelect = { f ->
                missionViewModel.setStatusFilter(f)
                if (debug) Toast.makeText(context, "Status: $f", Toast.LENGTH_SHORT).show()
            })

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (missions.isEmpty()) {
            item { Text(text = "No missions", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(missions) { mission ->
                MissionCardItem(
                    mission = mission,
                    onDelete = { id -> missionViewModel.deleteMission(id) },
                    onToggleCompleted = { id -> missionViewModel.toggleMissionCompleted(id) })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MissionScreenPreview() {
    val context = LocalContext.current
    val vm = ViewModelProvider.provideMissionViewModel(context)
    Surface(color = MaterialTheme.colorScheme.background) {
        MissionScreen(missionViewModel = vm)
    }
}

@Preview(showBackground = true)
@Composable
fun MissionScreenDebugPreview() {
    val context = LocalContext.current
    val vm = ViewModelProvider.provideMissionViewModel(context)
    Surface(color = MaterialTheme.colorScheme.background) {
        MissionScreen(missionViewModel = vm, debug = true)
    }
}
