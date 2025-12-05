package com.example.todolist.feature.analysis

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.todolist.ui.common.ViewModelProvider
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme

// extracted analysis components
import com.example.todolist.feature.analysis.components.LegendItem
import com.example.todolist.feature.analysis.components.Bar
import com.example.todolist.feature.analysis.components.DetailItem

// reuse shared DateNavigator from mission components
import com.example.todolist.feature.mission.components.DateNavigator

// import mission-specific color tokens
import com.example.todolist.ui.theme.missionCompletedContainerDark
import com.example.todolist.ui.theme.onMissionCompletedContainerDark
import com.example.todolist.ui.theme.missionMissedContainerDark
import com.example.todolist.ui.theme.onMissionMissedContainerDark
import com.example.todolist.ui.theme.missionCompletedContainerLight
import com.example.todolist.ui.theme.onMissionCompletedContainerLight
import com.example.todolist.ui.theme.missionMissedContainerLight
import com.example.todolist.ui.theme.onMissionMissedContainerLight

@Composable
fun MissionAnalysisScreen(
    viewModel: MissionAnalysisViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val stats = uiState.stats
    val gran = uiState.granularity
    val ref = uiState.referenceDate

    val darkTheme = isSystemInDarkTheme()

    // pick mission-specific palette from theme (light/dark variants)
    val colorCompleted = if (darkTheme) missionCompletedContainerDark else missionCompletedContainerLight
    val contentOnCompleted = if (darkTheme) onMissionCompletedContainerDark else onMissionCompletedContainerLight

    val colorMissed = if (darkTheme) missionMissedContainerDark else missionMissedContainerLight
    val contentOnMissed = if (darkTheme) onMissionMissedContainerDark else onMissionMissedContainerLight

    // color for Total (use primary container from Material theme)
    val colorTotal = MaterialTheme.colorScheme.primaryContainer
    val contentOnTotal = MaterialTheme.colorScheme.onPrimaryContainer

    // compute max to scale bars - include total (completed + missed + inProgress)
    val maxValue = remember(stats) {
        (stats.maxOfOrNull { val total = it.completed + it.missed + it.inProgress; maxOf(total, it.completed, it.missed) } ?: 1).coerceAtLeast(1)
    }

    Column(modifier = modifier.padding(16.dp)) {
        // Use shared DateNavigator (prev/title/next + granularity chips)
        DateNavigator(
            referenceDate = ref,
            granularity = gran,
            onPrev = { viewModel.prev() },
            onNext = { viewModel.next() },
            onSetGranularity = { viewModel.setGranularity(it) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Legend - Total, Completed, Missed
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            LegendItem(color = colorTotal, label = "Total")
            LegendItem(color = colorCompleted, label = "Completed")
            LegendItem(color = colorMissed, label = "Missed")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart area with horizontal scroll when many columns - use LazyRow for performance
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(horizontal = 4.dp)) {

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (stats.isEmpty()) {
                Text(text = "No data", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyRow(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter), horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                    items(stats) { entry ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp)) {
                            val total = entry.completed + entry.missed + entry.inProgress
                            val totalHeight = (total.toFloat() / maxValue) * 160f
                            val completedHeight = (entry.completed.toFloat() / maxValue) * 160f
                            val missedHeight = (entry.missed.toFloat() / maxValue) * 160f

                            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.height(180.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Bar(color = colorTotal, contentColor = contentOnTotal, heightDp = totalHeight.dp, label = total)
                                Bar(color = colorCompleted, contentColor = contentOnCompleted, heightDp = completedHeight.dp, label = entry.completed)
                                Bar(color = colorMissed, contentColor = contentOnMissed, heightDp = missedHeight.dp, label = entry.missed)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = entry.label, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Totals / Details
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Totals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DetailItem(label = "Total missions", value = viewModel.totalMissions())
                    DetailItem(label = "Completed", value = viewModel.totalCompleted())
                    DetailItem(label = "Missed", value = viewModel.totalMissed())
                    DetailItem(label = "In Progress", value = viewModel.totalInProgress())
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MissionAnalysisPreview() {
    val context = LocalContext.current
    val vm = ViewModelProvider.provideMissionAnalysisViewModel(context)
    Surface(color = MaterialTheme.colorScheme.background) {
        MissionAnalysisScreen(viewModel = vm)
    }
}