package com.example.todolist.feature.mission.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.MissionStatus
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.todolist.ui.theme.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight

// Keep full date+time formatter for Mission (as before) but align layout like TaskCard
private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")

@Composable
fun MissionCardItem(
    mission: Mission,
    modifier: Modifier = Modifier,
    onDelete: (Int) -> Unit = {},
    onToggleCompleted: (Int) -> Unit = {}
) {
    // determine if system is in dark theme so we can pick the right color variants
    val isDark = isSystemInDarkTheme()

    // select container color and appropriate on-container text color based on mission status
    val (containerColor, onContainerColor) = when (mission.status) {
        MissionStatus.COMPLETED -> if (isDark) Pair(missionCompletedContainerDark, onMissionCompletedContainerDark) else Pair(missionCompletedContainerLight, onMissionCompletedContainerLight)
        MissionStatus.MISSED -> if (isDark) Pair(missionMissedContainerDark, onMissionMissedContainerDark) else Pair(missionMissedContainerLight, onMissionMissedContainerLight)
        MissionStatus.UNSPECIFIED -> if (isDark) Pair(missionInProgressContainerDark, onMissionInProgressContainerDark) else Pair(missionInProgressContainerLight, onMissionInProgressContainerLight)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 0.dp, bottom = 12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-4).dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Show full date+time for missions (kept from original implementation)
                Text(text = mission.deadline.format(DATE_FORMATTER), style = MaterialTheme.typography.titleSmall, color = onContainerColor.copy(alpha = 0.6f))

                Box(contentAlignment = Alignment.TopEnd) {
                    val expanded = remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded.value = true }, modifier = Modifier.offset(y = (-6).dp)) {
                        Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More", modifier = Modifier.rotate(90f), tint = onContainerColor)
                    }

                    DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }, modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                        DropdownMenuItem(text = { Text("Delete mission", color = onContainerColor) }, onClick = { expanded.value = false; onDelete(mission.id) })
                        // If mission is MISSED, do not show the toggle-completed action
                        if (mission.status != MissionStatus.MISSED) {
                            val label = if (mission.status == MissionStatus.COMPLETED) "Mark as incomplete" else "Mark as completed"
                            DropdownMenuItem(text = { Text(label, color = onContainerColor) }, onClick = { expanded.value = false; onToggleCompleted(mission.id) })
                        }
                    }
                }
            }

            // Title and description area: layout aligned like TaskCard and add collapse/expand behavior
            Column(modifier = Modifier.offset(y = (-12).dp)) {
                Text(text = mission.title, style = MaterialTheme.typography.titleLarge, color = onContainerColor)
                val descriptionText = mission.description.orEmpty()
                if (descriptionText.isNotBlank()) {

                    Spacer(modifier = Modifier.height(4.dp))

                    var expandedDesc by remember { mutableStateOf(false) }
                    var showExpandButton by remember { mutableStateOf(false) }

                    if (expandedDesc) {
                        Text(
                            text = descriptionText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = onContainerColor.copy(alpha = 0.6f)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Text(
                                text = "View less",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable { expandedDesc = false }
                                    .padding(start = 8.dp)
                            )
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = descriptionText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = onContainerColor.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                                onTextLayout = { textLayoutResult ->
                                    showExpandButton = textLayoutResult.hasVisualOverflow
                                }
                            )

                            if (showExpandButton) {
                                Text(
                                    text = "View more",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier
                                        .clickable { expandedDesc = true }
                                        .padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
