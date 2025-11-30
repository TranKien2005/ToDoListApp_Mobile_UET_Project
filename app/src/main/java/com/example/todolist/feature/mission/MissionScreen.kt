package com.example.todolist.feature.mission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.todolist.ui.common.ViewModelProvider
import com.example.todolist.feature.mission.components.MissionCardItem

@Composable
fun MissionScreen(
    missionViewModel: MissionViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by missionViewModel.uiState.collectAsState()

    val missions = uiState.missions

    LazyColumn(modifier = modifier.padding(16.dp)) {
        item {
            // Tags row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MissionTag.values().forEach { tag ->
                    val selected = tag == uiState.selectedTag
                    TextButton(onClick = { missionViewModel.selectTag(tag) }, colors = ButtonDefaults.textButtonColors(contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)) {
                        Text(text = when(tag) {
                            MissionTag.ALL -> "Tất cả"
                            MissionTag.TODAY -> "Hôm nay"
                            MissionTag.THIS_WEEK -> "Tuần này"
                            MissionTag.THIS_MONTH -> "Tháng này"
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status filter row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                MissionStatusFilter.values().forEach { filter ->
                    val selected = filter == uiState.statusFilter
                    TextButton(onClick = { missionViewModel.setStatusFilter(filter) }, colors = ButtonDefaults.textButtonColors(contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)) {
                        Text(text = when(filter) {
                            MissionStatusFilter.ALL -> "Tất cả"
                            MissionStatusFilter.COMPLETED -> "Đã hoàn thành"
                            MissionStatusFilter.IN_PROGRESS -> "Đang làm"
                            MissionStatusFilter.MISSED -> "Đã trễ"
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (missions.isEmpty()) {
            item { Text(text = "No missions", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(missions) { mission ->
                MissionCardItem(
                    mission = mission,
                    onDelete = { id -> missionViewModel.deleteMission(id) },
                    onMarkCompleted = { id -> missionViewModel.markCompleted(id, true) })
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

