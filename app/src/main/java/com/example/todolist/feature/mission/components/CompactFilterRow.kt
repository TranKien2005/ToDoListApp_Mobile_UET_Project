package com.example.todolist.feature.mission.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.todolist.R
import com.example.todolist.domain.usecase.StatsGranularity
import com.example.todolist.feature.mission.MissionStatusFilter

/**
 * Compact filter row with 2 dropdown buttons on one line
 */
@Composable
fun CompactFilterRow(
    granularity: StatsGranularity,
    statusFilter: MissionStatusFilter,
    onSetGranularity: (StatsGranularity) -> Unit,
    onSetStatusFilter: (MissionStatusFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    
    var showGranularityMenu by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Granularity Dropdown
        Box(modifier = Modifier.weight(1f)) {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showGranularityMenu = true },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (granularity) {
                            StatsGranularity.DAY -> stringResource(R.string.granularity_day)
                            StatsGranularity.DAY_OF_WEEK -> stringResource(R.string.granularity_week)
                            StatsGranularity.WEEK_OF_MONTH -> stringResource(R.string.granularity_month)
                            StatsGranularity.MONTH_OF_YEAR -> stringResource(R.string.granularity_year)
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = primaryColor
                    )
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            DropdownMenu(
                expanded = showGranularityMenu,
                onDismissRequest = { showGranularityMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.granularity_day)) },
                    onClick = {
                        onSetGranularity(StatsGranularity.DAY)
                        showGranularityMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.granularity_week)) },
                    onClick = {
                        onSetGranularity(StatsGranularity.DAY_OF_WEEK)
                        showGranularityMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.granularity_month)) },
                    onClick = {
                        onSetGranularity(StatsGranularity.WEEK_OF_MONTH)
                        showGranularityMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.granularity_year)) },
                    onClick = {
                        onSetGranularity(StatsGranularity.MONTH_OF_YEAR)
                        showGranularityMenu = false
                    }
                )
            }
        }

        // Status Dropdown
        Box(modifier = Modifier.weight(1f)) {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showStatusMenu = true },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (statusFilter) {
                            MissionStatusFilter.ALL -> stringResource(R.string.filter_all)
                            MissionStatusFilter.COMPLETED -> stringResource(R.string.filter_done)
                            MissionStatusFilter.IN_PROGRESS -> stringResource(R.string.filter_active)
                            MissionStatusFilter.MISSED -> stringResource(R.string.filter_missed)
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (statusFilter == MissionStatusFilter.MISSED) 
                            MaterialTheme.colorScheme.error else primaryColor
                    )
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = if (statusFilter == MissionStatusFilter.MISSED) 
                            MaterialTheme.colorScheme.error else primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            DropdownMenu(
                expanded = showStatusMenu,
                onDismissRequest = { showStatusMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.filter_all)) },
                    onClick = {
                        onSetStatusFilter(MissionStatusFilter.ALL)
                        showStatusMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.filter_done)) },
                    onClick = {
                        onSetStatusFilter(MissionStatusFilter.COMPLETED)
                        showStatusMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.filter_active)) },
                    onClick = {
                        onSetStatusFilter(MissionStatusFilter.IN_PROGRESS)
                        showStatusMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { 
                        Text(
                            stringResource(R.string.filter_missed),
                            color = MaterialTheme.colorScheme.error
                        ) 
                    },
                    onClick = {
                        onSetStatusFilter(MissionStatusFilter.MISSED)
                        showStatusMenu = false
                    }
                )
            }
        }
    }
}
