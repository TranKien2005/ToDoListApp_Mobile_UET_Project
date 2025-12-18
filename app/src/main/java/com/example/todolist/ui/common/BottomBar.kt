package com.example.todolist.ui.layout

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SmartToy
import com.example.todolist.R

/**
 * Rounded bottom bar với gradient, 4 icons và centered FAB button đẹp hơn
 */
@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    onHome: () -> Unit = {},
    onList: () -> Unit = {},
    onStats: () -> Unit = {},
    onVoice: () -> Unit = {},
    onAdd: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val containerHeight = if (isLandscape) 64.dp else 80.dp
    val barHeight = if (isLandscape) 48.dp else 60.dp
    val fabSize = if (isLandscape) 56.dp else 68.dp
    val fabOffsetY = if (isLandscape) (-18).dp else (-28).dp
    val barPaddingHorizontal = if (isLandscape) 8.dp else 12.dp
    val rowPaddingHorizontal = if (isLandscape) 12.dp else 20.dp
    val iconSpacing = if (isLandscape) 8.dp else 12.dp
    val smallIconSize = if (isLandscape) 22.dp else 26.dp

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(containerHeight),
        contentAlignment = Alignment.TopCenter
    ) {
        // Rounded bar background với gradient
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = barPaddingHorizontal)
                .height(barHeight)
                .shadow(8.dp, RoundedCornerShape(30.dp))
                .clip(RoundedCornerShape(30.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.05f),
                                secondaryColor.copy(alpha = 0.03f),
                                primaryColor.copy(alpha = 0.05f)
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = rowPaddingHorizontal),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (isLandscape) Arrangement.SpaceEvenly else Arrangement.Start
                ) {
                    // Left icons
                    if (!isLandscape) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(iconSpacing)
                        ) {
                            IconButton(onClick = onHome) {
                                Icon(
                                    imageVector = Icons.Filled.Home,
                                    contentDescription = stringResource(R.string.cd_home),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(smallIconSize)
                                )
                            }
                            IconButton(onClick = onList) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = stringResource(R.string.cd_list),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(smallIconSize)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Right icons
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(iconSpacing)
                        ) {
                            IconButton(onClick = onStats) {
                                Icon(
                                    imageVector = Icons.Filled.BarChart,
                                    contentDescription = stringResource(R.string.cd_stats),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(smallIconSize)
                                )
                            }
                            IconButton(onClick = onVoice) {
                                Icon(
                                    imageVector = Icons.Filled.SmartToy,
                                    contentDescription = stringResource(R.string.cd_ai_assistant),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(smallIconSize)
                                )
                            }
                        }
                    } else {
                        // Landscape: phân bố đều 4 icons
                        IconButton(onClick = onHome) {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = stringResource(R.string.cd_home),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(smallIconSize)
                            )
                        }
                        IconButton(onClick = onList) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = stringResource(R.string.cd_list),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(smallIconSize)
                            )
                        }
                        IconButton(onClick = onStats) {
                            Icon(
                                imageVector = Icons.Filled.BarChart,
                                contentDescription = "Analytics",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(smallIconSize)
                            )
                        }
                        IconButton(onClick = onVoice) {
                            Icon(
                                imageVector = Icons.Filled.SmartToy,
                                contentDescription = stringResource(R.string.cd_ai_assistant),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(smallIconSize)
                            )
                        }
                    }
                }
            }
        }

        // Center FAB với gradient và shadow đẹp hơn
        FloatingActionButton(
            onClick = onAdd,
            containerColor = primaryColor,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
            modifier = Modifier
                .size(fabSize)
                .offset(y = fabOffsetY)
                .shadow(12.dp, CircleShape),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp,
                pressedElevation = 16.dp
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.cd_add),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
