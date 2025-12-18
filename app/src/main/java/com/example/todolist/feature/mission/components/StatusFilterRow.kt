package com.example.todolist.feature.mission.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.R
import com.example.todolist.feature.mission.MissionStatusFilter

@Composable
fun StatusFilterRow(
    selectedFilter: MissionStatusFilter,
    onSelect: (MissionStatusFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.filter_by_status),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = selectedFilter == MissionStatusFilter.ALL,
                onClick = { onSelect(MissionStatusFilter.ALL) },
                label = { Text(stringResource(R.string.filter_all), fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f)),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = primaryColor,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    labelColor = primaryColor
                )
            )

            FilterChip(
                selected = selectedFilter == MissionStatusFilter.COMPLETED,
                onClick = { onSelect(MissionStatusFilter.COMPLETED) },
                label = { Text(stringResource(R.string.filter_done), fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f)),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = primaryColor,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    labelColor = primaryColor
                )
            )

            FilterChip(
                selected = selectedFilter == MissionStatusFilter.IN_PROGRESS,
                onClick = { onSelect(MissionStatusFilter.IN_PROGRESS) },
                label = { Text(stringResource(R.string.filter_active), fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f)),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = primaryColor,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    labelColor = primaryColor
                )
            )

            FilterChip(
                selected = selectedFilter == MissionStatusFilter.MISSED,
                onClick = { onSelect(MissionStatusFilter.MISSED) },
                label = { Text(stringResource(R.string.filter_missed), fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.error,
                    selectedLabelColor = MaterialTheme.colorScheme.onError,
                    labelColor = MaterialTheme.colorScheme.error
                )
            )
        }
    }
}
