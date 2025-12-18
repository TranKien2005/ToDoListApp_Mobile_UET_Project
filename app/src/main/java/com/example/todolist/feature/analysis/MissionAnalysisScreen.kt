package com.example.todolist.feature.analysis

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.todolist.R
import com.example.todolist.feature.common.ViewModelProvider

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
import com.example.todolist.ui.common.rememberBounceOverscrollEffect
import com.example.todolist.ui.common.bounceOverscroll

@Composable
fun MissionAnalysisScreen(
    viewModel: MissionAnalysisViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val stats = uiState.stats
    val gran = uiState.granularity
    val ref = uiState.referenceDate

    var isVisible by remember { mutableStateOf(false) }
    val bounceState = rememberBounceOverscrollEffect()

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val darkTheme = isSystemInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

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

    // Static gradient - no animation for better scroll performance
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.06f),
                        secondaryColor.copy(alpha = 0.03f),
                        tertiaryColor.copy(alpha = 0.04f)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = modifier
                .padding(16.dp)
                .bounceOverscroll(bounceState)
        ) {
            item {
                // Animated Header
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = tween(500)
                    ) + fadeIn(animationSpec = tween(500))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.mission_analytics),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp
                            ),
                            color = primaryColor
                        )
                        Text(
                            text = stringResource(R.string.track_mission_progress),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Use shared DateNavigator (prev/title/next + granularity chips)
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = tween(500, delayMillis = 100)
                    ) + fadeIn(animationSpec = tween(500, delayMillis = 100))
                ) {
                    DateNavigator(
                        referenceDate = ref,
                        granularity = gran,
                        onPrev = { viewModel.prev() },
                        onNext = { viewModel.next() },
                        onSetGranularity = { viewModel.setGranularity(it) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Legend - Total, Completed, Missed with animation
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(500, delayMillis = 200)
                    ) + fadeIn(animationSpec = tween(500, delayMillis = 200))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem(color = colorTotal, label = stringResource(R.string.legend_total))
                        LegendItem(color = colorCompleted, label = stringResource(R.string.legend_completed))
                        LegendItem(color = colorMissed, label = stringResource(R.string.legend_missed))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                // Chart area with horizontal scroll when many columns - use LazyRow for performance
                AnimatedVisibility(
                    visible = isVisible,
                    enter = scaleIn(
                        initialScale = 0.95f,
                        animationSpec = tween(600, delayMillis = 300)
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 300))
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.mission_statistics),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = primaryColor
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                } else if (stats.isEmpty()) {
                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "📭",
                                            fontSize = 48.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = stringResource(R.string.no_data_available),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    LazyRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.BottomCenter),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        items(stats) { entry ->
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.width(100.dp)
                                            ) {
                                                val total = entry.completed + entry.missed + entry.inProgress
                                                val totalHeight = (total.toFloat() / maxValue) * 160f
                                                val completedHeight = (entry.completed.toFloat() / maxValue) * 160f
                                                val missedHeight = (entry.missed.toFloat() / maxValue) * 160f

                                                Row(
                                                    verticalAlignment = Alignment.Bottom,
                                                    modifier = Modifier.height(180.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Bar(
                                                        color = colorTotal,
                                                        contentColor = contentOnTotal,
                                                        heightDp = totalHeight.dp,
                                                        label = total
                                                    )
                                                    Bar(
                                                        color = colorCompleted,
                                                        contentColor = contentOnCompleted,
                                                        heightDp = completedHeight.dp,
                                                        label = entry.completed
                                                    )
                                                    Bar(
                                                        color = colorMissed,
                                                        contentColor = contentOnMissed,
                                                        heightDp = missedHeight.dp,
                                                        label = entry.missed
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = entry.label,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Totals / Details with animation
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(500, delayMillis = 400)
                    ) + fadeIn(animationSpec = tween(500, delayMillis = 400))
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.summary),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = primaryColor
                                )
                                Surface(
                                    color = primaryColor.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.total_count, viewModel.totalMissions()),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = primaryColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                DetailItem(
                                    label = stringResource(R.string.legend_completed), 
                                    value = viewModel.totalCompleted(),
                                    modifier = Modifier.weight(1f)
                                )
                                DetailItem(
                                    label = stringResource(R.string.legend_missed), 
                                    value = viewModel.totalMissed(),
                                    modifier = Modifier.weight(1f)
                                )
                                DetailItem(
                                    label = stringResource(R.string.in_progress), 
                                    value = viewModel.totalInProgress(),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
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