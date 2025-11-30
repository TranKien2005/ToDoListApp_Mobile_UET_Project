package com.example.todolist.ui.layout

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Mic

/**
 * Rounded bottom bar with 4 icons (2 left, 2 right) and a centered floating '+' button.
 * Responsive: adapts sizes/padding for landscape orientation.
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

    // responsive sizing
    val containerHeight = if (isLandscape) 64.dp else 80.dp
    val barHeight = if (isLandscape) 48.dp else 56.dp
    val fabSize = if (isLandscape) 56.dp else 64.dp
    val fabOffsetY = if (isLandscape) (-18).dp else (-24).dp
    val barPaddingHorizontal = if (isLandscape) 8.dp else 16.dp
    val rowPaddingHorizontal = if (isLandscape) 12.dp else 24.dp
    val iconSpacing = if (isLandscape) 8.dp else 4.dp
    val smallIconSize = if (isLandscape) 20.dp else 24.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(containerHeight),
        contentAlignment = Alignment.TopCenter
    ) {
        // Rounded bar background
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = barPaddingHorizontal)
                .height(barHeight)
                .clip(RoundedCornerShape(28.dp)),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = rowPaddingHorizontal),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onHome) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Home",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(smallIconSize)
                        )
                    }
                    Spacer(modifier = Modifier.width(iconSpacing))
                    IconButton(onClick = onList) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "List",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(smallIconSize)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onStats) {
                        Icon(
                            imageVector = Icons.Filled.BarChart,
                            contentDescription = "Stats",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(smallIconSize)
                        )
                    }
                    Spacer(modifier = Modifier.width(iconSpacing))
                    IconButton(onClick = onVoice) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "Voice",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(smallIconSize)
                        )
                    }
                }
            }
        }

        // Center FAB that rises above the bar. Force circular shape and a plain Add icon
        FloatingActionButton(
            onClick = onAdd,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
            modifier = Modifier
                .size(fabSize)
                .offset(y = fabOffsetY)
        ) {
            Icon(imageVector = Icons.Filled.AddCircle, contentDescription = "Add")
        }
    }
}
