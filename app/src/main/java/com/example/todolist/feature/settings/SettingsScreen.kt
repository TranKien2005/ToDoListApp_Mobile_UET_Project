package com.example.todolist.feature.settings

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.R
import com.example.todolist.core.model.Gender
import com.example.todolist.core.model.User
import com.example.todolist.feature.auth.CalendarSyncSection
import com.example.todolist.feature.auth.GoogleAccountCard
import com.example.todolist.feature.auth.GoogleAuthViewModel
import com.example.todolist.feature.auth.GoogleSignInButton
import com.example.todolist.feature.user.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    userViewModel: UserViewModel,
    googleAuthViewModel: GoogleAuthViewModel? = null, // Optional: for Google Sign-In feature
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val user by userViewModel.user.collectAsState()
    val googleAuthState = googleAuthViewModel?.uiState?.collectAsState()?.value
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    // Calendar Permission Launcher using StartIntentSenderForResult for PendingIntent
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
             // Pass the result back to ViewModel to handle token retrieval
             result.data?.let { intent ->
                 googleAuthViewModel?.handleAuthorizationResult(context, intent)
             }
             Toast.makeText(context, "Authorization granted. Syncing...", Toast.LENGTH_SHORT).show()
        } else {
             Toast.makeText(context, "Authorization cancelled", Toast.LENGTH_SHORT).show()
        }
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

                // Google Account & Sync Section (only show if GoogleAuthViewModel is provided)
                if (googleAuthViewModel != null && googleAuthState != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = tween(600, delayMillis = 150)
                        ) + fadeIn(animationSpec = tween(600, delayMillis = 150))
                    ) {
                        SectionHeader(
                            icon = Icons.Default.Cloud,
                            title = "Tài khoản & Đồng bộ",
                            primaryColor = primaryColor
                        )
                    }

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(600, delayMillis = 200)
                        ) + fadeIn(animationSpec = tween(600, delayMillis = 200))
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Show sign-in button or account card
                            if (googleAuthState.isSignedIn && googleAuthState.currentUser != null) {
                                GoogleAccountCard(
                                    user = googleAuthState.currentUser,
                                    onSignOut = { googleAuthViewModel.signOut() }
                                )
                            } else {
                                GoogleSignInButton(
                                    onClick = { 
                                        (context as? Activity)?.let { activity ->
                                            googleAuthViewModel.signIn(activity)
                                        }
                                    },
                                    isLoading = googleAuthState.isLoading,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Calendar sync toggle
                            CalendarSyncSection(
                                isEnabled = user?.isCalendarSyncEnabled == true,
                                isSignedIn = googleAuthState.isSignedIn,
                                isSyncing = googleAuthState.isSyncingCalendar, // Pass new loading state
                                onToggle = { enabled ->
                                    user?.let { currentUser ->
                                        // 1. Update local user setting
                                        userViewModel.updateUser(
                                            currentUser.copy(isCalendarSyncEnabled = enabled)
                                        )
                                        // 2. Orchestrate sync/import via GoogleAuthViewModel
                                        googleAuthViewModel.toggleCalendarSync(context, enabled)
                                    }
                                }
                            )

                            // Manual Sync Buttons (Only visible if enabled and signed in)
                            if (googleAuthState.isSignedIn && user?.isCalendarSyncEnabled == true) {
                                SyncActionsSection(
                                    isSyncing = googleAuthState.isSyncingCalendar,
                                    onSyncAll = { googleAuthViewModel.syncNow(context) },
                                    onImportEvents = { googleAuthViewModel.importEvents(context) }
                                )
                            }

                            // Determine which error to show (Sign-in error or Calendar Sync error)
                            val displayError = googleAuthState.error ?: googleAuthState.calendarSyncError

                            // Error message
                            displayError?.let { error ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = error,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        
                                        // If error is about missing token, show fix button
                                        if (error.contains("Access Token is missing") || error.contains("Authorization requires")) {
                                            Button(
                                                onClick = {
                                                    scope.launch {
                                                        val pendingIntent = googleAuthViewModel.getCalendarPermissionIntent(context)
                                                        if (pendingIntent != null) {
                                                            calendarPermissionLauncher.launch(
                                                                IntentSenderRequest.Builder(pendingIntent).build()
                                                            )
                                                        } else {
                                                            // If null returned, it means permission might already be granted
                                                            // Retry sync directly
                                                            googleAuthViewModel.toggleCalendarSync(context, true)
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.error
                                                ),
                                                modifier = Modifier.align(Alignment.End)
                                            ) {
                                                Text("Cấp quyền Calendar")
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // Success/Result message
                            googleAuthState.lastSyncResult?.let { result ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Text(
                                        text = result,
                                        modifier = Modifier.padding(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
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
                            avatarUrl = user?.avatarUrl,
                            isCalendarSyncEnabled = user?.isCalendarSyncEnabled ?: false // Preserve existing setting
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
private fun SyncActionsSection(
    isSyncing: Boolean,
    onSyncAll: () -> Unit,
    onImportEvents: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Hành động đồng bộ",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = onSyncAll,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSyncing,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isSyncing) "Đang đồng bộ..." else "Đồng bộ tất cả công việc")
            }

            OutlinedButton(
                onClick = onImportEvents,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSyncing,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CloudDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nhập sự kiện từ Calendar")
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
                text = "Task Reminder Time",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Remind $reminderMinutes minutes before task starts",
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
                text = "Mission Deadline Warning Time",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Warn $warningMinutes minutes before mission deadline",
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
                text = "Daily Summary Hour",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Receive daily summary at $summaryHour:00",
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
