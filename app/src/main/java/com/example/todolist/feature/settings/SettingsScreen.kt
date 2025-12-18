package com.example.todolist.feature.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.R
import com.example.todolist.core.model.AppLanguage
import com.example.todolist.core.model.Gender
import com.example.todolist.core.model.User
import com.example.todolist.feature.user.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    userViewModel: UserViewModel,
    onBackClick: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val user by userViewModel.user.collectAsState()

    var isVisible by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

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
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    // Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.delete_account_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.delete_account_message),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete_account_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.08f),
                            secondaryColor.copy(alpha = 0.04f),
                            tertiaryColor.copy(alpha = 0.06f)
                        ),
                        start = androidx.compose.ui.geometry.Offset(animatedOffset, animatedOffset),
                        end = androidx.compose.ui.geometry.Offset(
                            animatedOffset + 1000f,
                            animatedOffset + 1000f
                        )
                    )
                )
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // User Profile Section với animation
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = tween(600)
                    ) + fadeIn(animationSpec = tween(600))
                ) {
                    SectionHeader(
                        icon = Icons.Default.Person,
                        title = stringResource(R.string.user_profile),
                        primaryColor = primaryColor
                    )
                }

                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(600, delayMillis = 100)
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 100))
                ) {
                    UserProfileSection(
                        user = user,
                        onUpdateUser = { updatedUser ->
                            userViewModel.updateUser(updatedUser)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Language Settings Section
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = tween(600, delayMillis = 150)
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 150))
                ) {
                    LanguageSettingItem(
                        currentLanguage = settings.language,
                        onLanguageChange = { viewModel.updateLanguage(it) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Notification Settings Section
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = tween(600, delayMillis = 200)
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 200))
                ) {
                    SectionHeader(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.notification_settings),
                        primaryColor = primaryColor
                    )
                }

                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(600, delayMillis = 300)
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 300))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        TaskReminderSlider(
                            reminderMinutes = settings.taskReminderMinutes,
                            onValueChange = { viewModel.updateTaskReminderMinutes(it) }
                        )

                        MissionDeadlineWarningSlider(
                            warningMinutes = settings.missionDeadlineWarningMinutes,
                            onValueChange = { viewModel.updateMissionDeadlineWarningMinutes(it) }
                        )

                        DailySummaryHourSlider(
                            summaryHour = settings.dailySummaryHour,
                            onValueChange = { viewModel.updateDailySummaryHour(it) }
                        )

                        SwitchSettingItem(
                            title = stringResource(R.string.daily_mission_notifications),
                            description = stringResource(R.string.daily_mission_notifications_desc),
                            emoji = "📅",
                            checked = settings.notifyDailyMissions,
                            onCheckedChange = { viewModel.updateNotifyDailyMissions(it) }
                        )

                        SwitchSettingItem(
                            title = stringResource(R.string.weekly_mission_notifications),
                            description = stringResource(R.string.weekly_mission_notifications_desc),
                            emoji = "📆",
                            checked = settings.notifyWeeklyMissions,
                            onCheckedChange = { viewModel.updateNotifyWeeklyMissions(it) }
                        )

                        SwitchSettingItem(
                            title = stringResource(R.string.monthly_mission_notifications),
                            description = stringResource(R.string.monthly_mission_notifications_desc),
                            emoji = "🗓️",
                            checked = settings.notifyMonthlyMissions,
                            onCheckedChange = { viewModel.updateNotifyMonthlyMissions(it) }
                        )

                        SwitchSettingItem(
                            title = stringResource(R.string.overdue_notifications),
                            description = stringResource(R.string.overdue_notifications_desc),
                            emoji = "⏰",
                            checked = settings.overdueNotificationEnabled,
                            onCheckedChange = { viewModel.updateOverdueNotificationEnabled(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Delete Account Button
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(600, delayMillis = 400)
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 400))
                ) {
                    Button(
                        onClick = { showDeleteAccountDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.delete_account),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    primaryColor: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.2f),
                            primaryColor.copy(alpha = 0.05f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(28.dp)
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = primaryColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileSection(
    user: User?,
    onUpdateUser: (User) -> Unit
) {
    var editedName by remember(user) { mutableStateOf(user?.fullName ?: "") }
    var editedAge by remember(user) { mutableStateOf(user?.age?.toString() ?: "") }
    var editedGender by remember(user) { mutableStateOf(user?.gender ?: Gender.OTHER) }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = editedName,
                onValueChange = { editedName = it },
                label = { Text(stringResource(R.string.full_name)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = editedAge,
                onValueChange = { if (it.all { char -> char.isDigit() }) editedAge = it },
                label = { Text(stringResource(R.string.age)) },
                modifier = Modifier.fillMaxWidth()
            )

            // Gender Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = editedGender.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.gender)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    Gender.entries.forEach { gender ->
                        DropdownMenuItem(
                            text = { Text(gender.name) },
                            onClick = {
                                editedGender = gender
                                expanded = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val age = editedAge.toIntOrNull() ?: 0
                    if (editedName.isNotBlank() && age > 0) {
                        val updatedUser = User(
                            id = user?.id ?: 0,
                            fullName = editedName,
                            age = age,
                            gender = editedGender,
                            avatarUrl = user?.avatarUrl
                        )
                        onUpdateUser(updatedUser)
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.save_profile))
            }
        }
    }
}

@Composable
fun TaskReminderSlider(
    reminderMinutes: Int,
    onValueChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.task_reminder_time),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.task_reminder_time_desc, reminderMinutes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = reminderMinutes.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 5f..60f,
                steps = 10,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun MissionDeadlineWarningSlider(
    warningMinutes: Int,
    onValueChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.mission_deadline_warning_time),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.mission_deadline_warning_desc, warningMinutes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = warningMinutes.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 0f..120f,
                steps = 12,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DailySummaryHourSlider(
    summaryHour: Int,
    onValueChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.daily_summary_hour),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.daily_summary_hour_desc, summaryHour),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = summaryHour.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 0f..23f,
                steps = 23,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SwitchSettingItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    emoji: String = "" // Thêm parameter emoji
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (emoji.isNotBlank()) {
                    Text(text = emoji, fontSize = 20.sp)
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingItem(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val vietnameseLabel = stringResource(R.string.language_vietnamese)
    val englishLabel = stringResource(R.string.language_english)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "🌐", fontSize = 20.sp)
                Column {
                    Text(
                        text = stringResource(R.string.language_setting),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.language_setting_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = when (currentLanguage) {
                        AppLanguage.VIETNAMESE -> vietnameseLabel
                        AppLanguage.ENGLISH -> englishLabel
                    },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(vietnameseLabel) },
                        onClick = {
                            onLanguageChange(AppLanguage.VIETNAMESE)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(englishLabel) },
                        onClick = {
                            onLanguageChange(AppLanguage.ENGLISH)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
