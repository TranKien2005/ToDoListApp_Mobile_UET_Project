package com.example.todolist.feature.mission

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.R
import com.example.todolist.feature.mission.components.MissionCardItem
import com.example.todolist.feature.mission.components.DateNavigator
import com.example.todolist.feature.mission.components.StatusFilterRow
import com.example.todolist.core.model.Mission

@Composable
fun MissionScreen(
    missionViewModel: MissionViewModel,
    modifier: Modifier = Modifier,
    debug: Boolean = false,
    onEditMission: (Mission) -> Unit = {}
) {
    val uiState by missionViewModel.uiState.collectAsState()
    val missions = uiState.missions

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.06f),
                        secondaryColor.copy(alpha = 0.03f),
                        tertiaryColor.copy(alpha = 0.04f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(animatedOffset, animatedOffset),
                    end = androidx.compose.ui.geometry.Offset(
                        animatedOffset + 1000f,
                        animatedOffset + 1000f
                    )
                )
            )
    ) {
        LazyColumn(modifier = modifier.padding(16.dp)) {
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = tween(500)
                    ) + fadeIn(animationSpec = tween(500))
                ) {
                    DateNavigator(
                        referenceDate = uiState.referenceDate,
                        granularity = uiState.granularity,
                        onPrev = { missionViewModel.prev() },
                        onNext = { missionViewModel.next() },
                        onSetGranularity = { g -> missionViewModel.setGranularity(g) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(500, delayMillis = 100)
                    ) + fadeIn(animationSpec = tween(500, delayMillis = 100))
                ) {
                    StatusFilterRow(
                        selectedFilter = uiState.statusFilter,
                        onSelect = { f -> missionViewModel.setStatusFilter(f) }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(500, delayMillis = 200))
                ) {
                    Text(
                        text = stringResource(R.string.missions_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = primaryColor,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }

            if (missions.isEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(600, delayMillis = 300))
                    ) {
                        Text(
                            text = stringResource(R.string.no_missions_found),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                }
            } else {
                items(missions) { mission ->
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(400, delayMillis = 300)
                        ) + fadeIn(animationSpec = tween(400, delayMillis = 300))
                    ) {
                        MissionCardItem(
                            mission = mission,
                            onDelete = { id -> missionViewModel.deleteMission(id) },
                            onToggleCompleted = { id -> missionViewModel.toggleMissionCompleted(id) },
                            onEdit = { onEditMission(mission) }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}
