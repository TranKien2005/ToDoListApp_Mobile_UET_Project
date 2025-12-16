package com.example.todolist.feature.common

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * A stacked preview of images that shows overlapping thumbnails.
 * Max 3 images displayed with a badge showing total count.
 */
@Composable
fun ImageStackPreview(
    images: List<String>,
    onImageClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    maxDisplay: Int = 3
) {
    val displayImages = images.take(maxDisplay)
    val remainingCount = (images.size - maxDisplay).coerceAtLeast(0)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy((-20).dp) // Negative spacing for overlap
    ) {
        // Stacked images
        if (images.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .clickable(onClick = onImageClick)
            ) {
                displayImages.forEachIndexed { index, imageUri ->
                    val offsetX = (index * 24).dp
                    Box(
                        modifier = Modifier
                            .offset(x = offsetX)
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(Uri.parse(imageUri))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Image ${index + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Badge for remaining count
                if (remainingCount > 0) {
                    Box(
                        modifier = Modifier
                            .offset(x = ((displayImages.size * 24) + 8).dp)
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+$remainingCount",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(if (images.isNotEmpty()) ((displayImages.size * 24) + 16).dp else 0.dp))

        // Add button
        IconButton(
            onClick = onAddClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add image",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Simple row showing image count with add button (alternative simpler design)
 */
@Composable
fun ImageCountPreview(
    images: List<String>,
    onImageClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = if (images.isNotEmpty()) Modifier.clickable(onClick = onImageClick) else Modifier
        ) {
            Text(
                text = "📷",
                fontSize = 24.sp
            )
            Text(
                text = if (images.isEmpty()) "No images attached" else "${images.size} image(s)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        FilledTonalButton(
            onClick = onAddClick,
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add")
        }
    }
}
