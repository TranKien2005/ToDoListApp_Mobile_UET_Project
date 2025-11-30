package com.example.todolist.feature.mission.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.rotate
import com.example.todolist.core.model.Mission
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.wrapContentSize

private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")

@Composable
fun MissionCardItem(
    mission: Mission,
    modifier: Modifier = Modifier,
    onDelete: (Int) -> Unit = {},
    onMarkCompleted: (Int) -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = mission.deadline.format(DATE_FORMATTER), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

                Spacer(modifier = Modifier.weight(1f))

                Box(contentAlignment = Alignment.TopEnd) {
                    val expanded = remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded.value = true }, modifier = Modifier.offset(y = (-6).dp)) {
                        Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More", modifier = Modifier.rotate(90f), tint = MaterialTheme.colorScheme.onSurface)
                    }

                    DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }, modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                        DropdownMenuItem(text = { Text("Xóa mission") }, onClick = { expanded.value = false; onDelete(mission.id) })
                        DropdownMenuItem(text = { Text(if (mission.isCompleted) "Đánh dấu chưa hoàn thành" else "Hoàn thành") }, onClick = { expanded.value = false; onMarkCompleted(mission.id) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = mission.title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)

            if (!mission.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                // mission.description is non-null here because of the null/blank check above
                Text(text = mission.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
