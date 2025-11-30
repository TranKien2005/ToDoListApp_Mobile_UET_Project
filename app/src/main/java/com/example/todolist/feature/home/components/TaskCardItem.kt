package com.example.todolist.feature.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.rotate
import com.example.todolist.core.model.Task
import androidx.compose.ui.Alignment
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.text.font.FontWeight

// Top-level formatter so it's reused and not recreated on every composition
private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun TaskCardItem(
    task: Task,
    modifier: Modifier = Modifier,
    onDelete: (Int) -> Unit = {},
    onMarkCompleted: (Int) -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 0.dp, bottom = 12.dp)) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-4).dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val startText = task.startTime.toLocalTime().format(TIME_FORMATTER)
                val endTime = task.durationMinutes?.let { task.startTime.plusMinutes(it) }
                val endText = endTime?.toLocalTime()?.format(TIME_FORMATTER)
                val timeText = if (endText.isNullOrBlank()) startText else "$startText - $endText"
                Text(text = timeText, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha= 0.6f))

                Box(contentAlignment = Alignment.TopEnd) {
                    val expandedMenu = remember { mutableStateOf(false) }
                    IconButton(onClick = { expandedMenu.value = true }, modifier = Modifier.offset(y = (-6).dp)) {
                        // Icon uses content color by default; specify onSurface to be explicit and
                        // ensure visibility in dark mode.
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "More",
                            modifier = Modifier.rotate(90f),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = expandedMenu.value,
                        onDismissRequest = { expandedMenu.value = false },
                        modifier = Modifier.wrapContentSize(Alignment.TopEnd)
                    ) {
                        DropdownMenuItem(text = { Text("Xóa task", color = MaterialTheme.colorScheme.onSurface) }, onClick = {
                            expandedMenu.value = false
                            onDelete(task.id)
                        })
                        DropdownMenuItem(text = { Text("Hoàn thành", color = MaterialTheme.colorScheme.onSurface) }, onClick = {
                            expandedMenu.value = false
                            onMarkCompleted(task.id)
                        })
                    }
                }
            }

            // Title and description area. Description shows single-line with ellipsis by default.
            Column(modifier = Modifier.offset(y = (-12).dp)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))

                val descriptionText = task.description ?: ""
                if (descriptionText.isNotBlank()) {
                    // Collapsed: show single-line Text with ellipsis and inline 'Xem thêm' button
                    var expandedDesc by remember { mutableStateOf(false) }
                    var showExpandButton by remember { mutableStateOf(false) }

                    if (expandedDesc) {
                        // Expanded: show full description and a trailing 'Xem bớt' clickable
                        Text(
                            text = descriptionText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                                onTextLayout = { textLayoutResult ->
                                    showExpandButton = textLayoutResult.hasVisualOverflow
                                }
                            )

                            if (showExpandButton) {
                                // Inline clickable label placed after the single-line description
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
