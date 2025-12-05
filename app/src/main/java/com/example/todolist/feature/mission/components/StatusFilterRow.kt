package com.example.todolist.feature.mission.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todolist.feature.mission.MissionStatusFilter

@Composable
fun StatusFilterRow(
    selectedFilter: MissionStatusFilter,
    onSelect: (MissionStatusFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        FilterChip(
            selected = selectedFilter == MissionStatusFilter.ALL,
            onClick = { onSelect(MissionStatusFilter.ALL) },
            label = { Text("All") }
        )

        FilterChip(
            selected = selectedFilter == MissionStatusFilter.COMPLETED,
            onClick = { onSelect(MissionStatusFilter.COMPLETED) },
            label = { Text("Completed") }
        )

        FilterChip(
            selected = selectedFilter == MissionStatusFilter.IN_PROGRESS,
            onClick = { onSelect(MissionStatusFilter.IN_PROGRESS) },
            label = { Text("In progress") }
        )

        FilterChip(
            selected = selectedFilter == MissionStatusFilter.MISSED,
            onClick = { onSelect(MissionStatusFilter.MISSED) },
            label = { Text("Missed") }
        )
    }
}
