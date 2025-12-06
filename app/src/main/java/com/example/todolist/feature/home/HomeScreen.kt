@file:Suppress("DEPRECATION")

package com.example.todolist.feature.home

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.feature.home.components.CalendarGrid
import com.example.todolist.feature.home.components.MonthHeader
import com.example.todolist.feature.home.components.TaskCardItem
import com.example.todolist.feature.home.components.WeekDaysRow
import com.example.todolist.ui.common.ViewModelProvider


/**
 * Single HomeScreen: uses a HomeViewModel instance directly. This keeps UI wiring simple and
 * ensures all business logic (state, events) stays in the ViewModel.
 *
 * NOTE: Do not change DI or create providers here. ViewModel construction/DI remains responsibility
 * of the app (debug/release); this file only consumes a HomeViewModel instance.
 */
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val tasksForDay = uiState.tasks

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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
        if (isLandscape) {
            // Layout ngang: Lịch bên trái, Tasks bên phải
            Row(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Phần Calendar bên trái
                LazyColumn(
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxHeight()
                ) {
                    item {
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = slideInVertically(
                                initialOffsetY = { -it },
                                animationSpec = tween(500)
                            ) + fadeIn(animationSpec = tween(500))
                        ) {
                            MonthHeader(
                                yearMonth = uiState.currentMonth,
                                onPrev = { homeViewModel.prevMonth() },
                                onNext = { homeViewModel.nextMonth() }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        AnimatedVisibility(
                            visible = isVisible,
                            enter = slideInVertically(
                                initialOffsetY = { -it },
                                animationSpec = tween(500, delayMillis = 100)
                            ) + fadeIn(animationSpec = tween(500, delayMillis = 100))
                        ) {
                            WeekDaysRow()
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        AnimatedVisibility(
                            visible = isVisible,
                            enter = scaleIn(
                                initialScale = 0.9f,
                                animationSpec = tween(600, delayMillis = 200)
                            ) + fadeIn(animationSpec = tween(600, delayMillis = 200))
                        ) {
                            CalendarGrid(
                                yearMonth = uiState.currentMonth,
                                selectedDate = uiState.selectedDate,
                                onDateSelected = { date -> homeViewModel.selectDate(date) }
                            )
                        }
                    }
                }

                // Phần Tasks bên phải
                LazyColumn(
                    modifier = Modifier
                        .weight(0.55f)
                        .fillMaxHeight()
                ) {
                    item {
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(500, delayMillis = 300)
                            ) + fadeIn(animationSpec = tween(500, delayMillis = 300))
                        ) {
                            Text(
                                text = "📝 Tasks for ${uiState.selectedDate.dayOfMonth} ${uiState.selectedDate.month.name.lowercase().replaceFirstChar { it.uppercase() }}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = primaryColor,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }

                    if (tasksForDay.isEmpty()) {
                        item {
                            AnimatedVisibility(
                                visible = isVisible,
                                enter = fadeIn(animationSpec = tween(600, delayMillis = 400))
                            ) {
                                Text(
                                    text = "No tasks for this day",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 24.dp)
                                )
                            }
                        }
                    } else {
                        items(tasksForDay) { task ->
                            AnimatedVisibility(
                                visible = isVisible,
                                enter = slideInVertically(
                                    initialOffsetY = { it },
                                    animationSpec = tween(400, delayMillis = 400)
                                ) + fadeIn(animationSpec = tween(400, delayMillis = 400))
                            ) {
                                TaskCardItem(
                                    task = task,
                                    onDelete = { id -> homeViewModel.deleteTask(id) }
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        } else {
            // Layout dọc: giữ nguyên như cũ
            LazyColumn(modifier = modifier.padding(16.dp)) {
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = tween(500)
                        ) + fadeIn(animationSpec = tween(500))
                    ) {
                        MonthHeader(
                            yearMonth = uiState.currentMonth,
                            onPrev = { homeViewModel.prevMonth() },
                            onNext = { homeViewModel.nextMonth() }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = tween(500, delayMillis = 100)
                        ) + fadeIn(animationSpec = tween(500, delayMillis = 100))
                    ) {
                        WeekDaysRow()
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = scaleIn(
                            initialScale = 0.9f,
                            animationSpec = tween(600, delayMillis = 200)
                        ) + fadeIn(animationSpec = tween(600, delayMillis = 200))
                    ) {
                        CalendarGrid(
                            yearMonth = uiState.currentMonth,
                            selectedDate = uiState.selectedDate,
                            onDateSelected = { date -> homeViewModel.selectDate(date) }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(500, delayMillis = 300)
                        ) + fadeIn(animationSpec = tween(500, delayMillis = 300))
                    ) {
                        Text(
                            text = "📝 Tasks for ${uiState.selectedDate.dayOfMonth} ${uiState.selectedDate.month.name.lowercase().replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = primaryColor,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }

                if (tasksForDay.isEmpty()) {
                    item {
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn(animationSpec = tween(600, delayMillis = 400))
                        ) {
                            Text(
                                text = "No tasks for this day",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 24.dp)
                            )
                        }
                    }
                } else {
                    items(tasksForDay) { task ->
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(400, delayMillis = 400)
                            ) + fadeIn(animationSpec = tween(400, delayMillis = 400))
                        ) {
                            TaskCardItem(
                                task = task,
                                onDelete = { id -> homeViewModel.deleteTask(id) }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val context = LocalContext.current
    val vm = ViewModelProvider.provideHomeViewModel(context)
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeScreen(homeViewModel = vm)
        }
    }
}
