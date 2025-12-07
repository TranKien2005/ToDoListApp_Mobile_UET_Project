package com.example.todolist.feature.common

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.todolist.R

/**
 * Dialog xác nhận xóa Task
 */
@Composable
fun DeleteTaskConfirmationDialog(
    taskTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmationDialog(
        title = stringResource(R.string.delete_task_title),
        message = stringResource(R.string.delete_task_message, taskTitle),
        icon = Icons.Default.Delete,
        iconTint = MaterialTheme.colorScheme.error,
        confirmText = stringResource(R.string.delete),
        confirmButtonColor = MaterialTheme.colorScheme.error,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

/**
 * Dialog xác nhận xóa Mission
 */
@Composable
fun DeleteMissionConfirmationDialog(
    missionTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmationDialog(
        title = stringResource(R.string.delete_mission_title),
        message = stringResource(R.string.delete_mission_message, missionTitle),
        icon = Icons.Default.Delete,
        iconTint = MaterialTheme.colorScheme.error,
        confirmText = stringResource(R.string.delete),
        confirmButtonColor = MaterialTheme.colorScheme.error,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

/**
 * Dialog thông báo thành công
 */
@Composable
fun SuccessDialog(
    message: String,
    onDismiss: () -> Unit
) {
    NotificationDialog(
        title = stringResource(R.string.success_title),
        message = message,
        icon = Icons.Default.CheckCircle,
        iconTint = Color(0xFF4CAF50), // Green
        onDismiss = onDismiss
    )
}

/**
 * Dialog thông báo lỗi
 */
@Composable
fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    NotificationDialog(
        title = stringResource(R.string.error_title),
        message = message,
        icon = Icons.Default.Error,
        iconTint = MaterialTheme.colorScheme.error,
        onDismiss = onDismiss
    )
}

/**
 * Dialog thông báo cảnh báo
 */
@Composable
fun WarningDialog(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmationDialog(
        title = stringResource(R.string.warning_title),
        message = message,
        icon = Icons.Default.Warning,
        iconTint = Color(0xFFFF9800), // Orange
        confirmText = stringResource(R.string.continue_button),
        confirmButtonColor = MaterialTheme.colorScheme.primary,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

/**
 * Dialog thông báo thông tin
 */
@Composable
fun InfoDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    NotificationDialog(
        title = title,
        message = message,
        icon = Icons.Default.Info,
        iconTint = MaterialTheme.colorScheme.primary,
        onDismiss = onDismiss
    )
}

/**
 * Base Confirmation Dialog
 */
@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    icon: ImageVector,
    iconTint: Color,
    confirmText: String,
    confirmButtonColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Dialog(onDismissRequest = onDismiss) {
        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn(
                initialScale = 0.8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn(),
            exit = scaleOut(targetScale = 0.8f) + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Icon với animation
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = iconTint.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(40.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    // Message
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                isVisible = false
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                stringResource(R.string.cancel),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                isVisible = false
                                onConfirm()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = confirmButtonColor
                            )
                        ) {
                            Text(
                                confirmText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Base Notification Dialog (Success, Error, Info)
 */
@Composable
private fun NotificationDialog(
    title: String,
    message: String,
    icon: ImageVector,
    iconTint: Color,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Dialog(onDismissRequest = onDismiss) {
        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn(
                initialScale = 0.8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn(),
            exit = scaleOut(targetScale = 0.8f) + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Icon với animation
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = iconTint.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(40.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    // Message
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // OK Button
                    Button(
                        onClick = {
                            isVisible = false
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = iconTint
                        )
                    ) {
                        Text(
                            stringResource(R.string.ok),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Snackbar-style notification (tự động ẩn sau vài giây)
 */
@Composable
fun AutoDismissNotification(
    message: String,
    type: NotificationType = NotificationType.SUCCESS,
    durationMillis: Long = 3000,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
        kotlinx.coroutines.delay(durationMillis)
        isVisible = false
        kotlinx.coroutines.delay(300) // Wait for exit animation
        onDismiss()
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it }
        ) + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (type) {
                    NotificationType.SUCCESS -> Color(0xFF4CAF50)
                    NotificationType.ERROR -> MaterialTheme.colorScheme.error
                    NotificationType.WARNING -> Color(0xFFFF9800)
                    NotificationType.INFO -> MaterialTheme.colorScheme.primary
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (type) {
                        NotificationType.SUCCESS -> Icons.Default.CheckCircle
                        NotificationType.ERROR -> Icons.Default.Error
                        NotificationType.WARNING -> Icons.Default.Warning
                        NotificationType.INFO -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.White
                )
            }
        }
    }
}

enum class NotificationType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}
