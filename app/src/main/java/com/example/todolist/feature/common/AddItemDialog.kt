package com.example.todolist.feature.common

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.R
import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.Task
import com.example.todolist.core.model.RepeatType
import com.example.todolist.core.model.MissionStoredStatus
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.LocalDate
import java.util.Locale
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddItemDialog(
    onDismissRequest: () -> Unit,
    addItemViewModel: AddItemViewModel,
    defaultDate: LocalDate = LocalDate.now(),
    defaultIsTask: Boolean = true,
    editTask: Task? = null,
    editMission: Mission? = null
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val scrollState = rememberScrollState()

    // Kiểm tra xem có phải edit mode không
    val isEditMode = editTask != null || editMission != null

    // Initialize với data từ task/mission cần edit
    var title by remember { mutableStateOf(editTask?.title ?: editMission?.title ?: "") }
    var description by remember { mutableStateOf(editTask?.description ?: editMission?.description ?: "") }
    var selectedTypeIsTask by remember { mutableStateOf(editTask != null || (editMission == null && defaultIsTask)) }
    var date by remember {
        mutableStateOf(
            editTask?.startTime?.toLocalDate()
                ?: editMission?.deadline?.toLocalDate()
                ?: defaultDate
        )
    }
    var time by remember {
        mutableStateOf(
            editTask?.startTime?.toLocalTime()
                ?: editMission?.deadline?.toLocalTime()
                ?: LocalTime.of(9, 0)
        )
    }

    var durationText by remember { mutableStateOf(editTask?.durationMinutes?.toString() ?: "") }
    var repeatType by remember { mutableStateOf(editTask?.repeatType ?: RepeatType.NONE) }

    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var durationError by remember { mutableStateOf<String?>(null) }
    var dateTimeError by remember { mutableStateOf<String?>(null) }

    // Images state
    var images by remember { 
        mutableStateOf(editTask?.images ?: editMission?.images ?: emptyList<String>()) 
    }
    var showImageViewer by remember { mutableStateOf(false) }
    
    // Native gallery picker launcher
    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            // Copy to app's internal storage for persistence
            val persistedUri = copyImageToInternalStorage(context, uri)
            if (persistedUri != null) {
                images = images + persistedUri.toString()
            }
        }
    }

    // State for DatePicker and TimePicker dialogs
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    fun validateAll(): Boolean {
        titleError = null
        descriptionError = null
        durationError = null
        dateTimeError = null

        var ok = true
        if (title.isBlank()) {
            titleError = "Title is required"
            ok = false
        }
        if (description.isBlank()) {
            descriptionError = "Description is required"
            ok = false
        }

        // Validate date/time không được trong quá khứ (chỉ khi tạo mới)
        if (!isEditMode) {
            val selectedDateTime = LocalDateTime.of(date, time)
            val now = LocalDateTime.now()
            if (selectedDateTime.isBefore(now)) {
                dateTimeError = "Cannot create task/mission in the past"
                ok = false
            }
        }

        if (selectedTypeIsTask) {
            if (durationText.isBlank()) {
                durationError = "Duration is required"
                ok = false
            } else if (durationText.toLongOrNull() == null) {
                durationError = "Must be a number"
                ok = false
            }
        }
        return ok
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // DatePicker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.toEpochDay() * 24 * 60 * 60 * 1000
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // TimePicker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }



    // Image Viewer
    if (showImageViewer && images.isNotEmpty()) {
        ImageViewerScreen(
            images = images,
            onDismiss = { showImageViewer = false },
            onDeleteImage = { index ->
                images = images.toMutableList().apply { removeAt(index) }
                if (images.isEmpty()) {
                    showImageViewer = false
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.05f),
                            secondaryColor.copy(alpha = 0.02f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .heightIn(max = 600.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Animated Header
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = tween(400)
                    ) + fadeIn(animationSpec = tween(400))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (isEditMode) {
                                if (selectedTypeIsTask) stringResource(R.string.edit_task) else stringResource(R.string.edit_mission)
                            } else {
                                if (selectedTypeIsTask) stringResource(R.string.add_new_task) else stringResource(R.string.add_new_mission)
                            },
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 26.sp
                            ),
                            color = primaryColor
                        )
                        Text(
                            text = if (isEditMode) stringResource(R.string.update_details) else stringResource(R.string.fill_details),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = primaryColor.copy(alpha = 0.2f))

                // Type Selector với animation - Ẩn khi edit mode
                if (!isEditMode) {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = tween(500, delayMillis = 100)
                        ) + fadeIn(animationSpec = tween(500, delayMillis = 100))
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = primaryColor.copy(alpha = 0.08f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedTypeIsTask,
                                    onClick = { selectedTypeIsTask = true },
                                    label = { Text(stringResource(R.string.task_label)) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = primaryColor,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                                FilterChip(
                                    selected = !selectedTypeIsTask,
                                    onClick = { selectedTypeIsTask = false },
                                    label = { Text(stringResource(R.string.mission_label)) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = primaryColor,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                // Form Fields với animations
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(500, delayMillis = 200)
                    ) + fadeIn(animationSpec = tween(500, delayMillis = 200))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Title Field
                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                titleError = null
                            },
                            label = { Text(stringResource(R.string.title)) },
                            leadingIcon = {
                                Icon(Icons.Default.Title, contentDescription = null, tint = primaryColor)
                            },
                            placeholder = { Text(if (selectedTypeIsTask) stringResource(R.string.enter_task_title) else stringResource(R.string.enter_mission_title)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = titleError != null,
                            supportingText = titleError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                focusedLabelColor = primaryColor
                            )
                        )

                        // Description Field
                        OutlinedTextField(
                            value = description,
                            onValueChange = {
                                description = it
                                descriptionError = null
                            },
                            label = { Text(stringResource(R.string.description)) },
                            leadingIcon = {
                                Icon(Icons.Default.Description, contentDescription = null, tint = primaryColor)
                            },
                            placeholder = { Text(stringResource(R.string.add_details)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            isError = descriptionError != null,
                            supportingText = descriptionError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                focusedLabelColor = primaryColor
                            )
                        )

                        // Date & Time Row - Now clickable with pickers
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = String.format(Locale.getDefault(), "%02d/%02d", date.dayOfMonth, date.monthValue),
                                    onValueChange = {},
                                    label = { Text(stringResource(R.string.date)) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Event, contentDescription = null, tint = primaryColor)
                                    },
                                    readOnly = true,
                                    modifier = Modifier.weight(1f),
                                    isError = dateTimeError != null,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = primaryColor.copy(alpha = 0.5f)
                                    ),
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                        .also { interactionSource ->
                                            LaunchedEffect(interactionSource) {
                                                interactionSource.interactions.collect { interaction ->
                                                    if (interaction is androidx.compose.foundation.interaction.PressInteraction.Release) {
                                                        showDatePicker = true
                                                        dateTimeError = null
                                                    }
                                                }
                                            }
                                        }
                                )

                                OutlinedTextField(
                                    value = String.format(Locale.getDefault(), "%02d:%02d", time.hour, time.minute),
                                    onValueChange = {},
                                    label = { Text(stringResource(R.string.time)) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = primaryColor)
                                    },
                                    readOnly = true,
                                    modifier = Modifier.weight(1f),
                                    isError = dateTimeError != null,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = primaryColor.copy(alpha = 0.5f)
                                    ),
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                        .also { interactionSource ->
                                            LaunchedEffect(interactionSource) {
                                                interactionSource.interactions.collect { interaction ->
                                                    if (interaction is androidx.compose.foundation.interaction.PressInteraction.Release) {
                                                        showTimePicker = true
                                                        dateTimeError = null
                                                    }
                                                }
                                            }
                                        }
                                )
                            }

                            // Error message for date/time validation
                            if (dateTimeError != null) {
                                Text(
                                    text = dateTimeError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }

                        // Task-specific: Duration & Repeat
                        AnimatedVisibility(
                            visible = selectedTypeIsTask,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = durationText,
                                    onValueChange = {
                                        if (it.all { char -> char.isDigit() }) {
                                            durationText = it
                                            durationError = null
                                        }
                                    },
                                    label = { Text(stringResource(R.string.duration_minutes)) },
                                    placeholder = { Text(stringResource(R.string.duration_placeholder)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    isError = durationError != null,
                                    supportingText = durationError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        focusedLabelColor = primaryColor
                                    )
                                )

                                // Repeat Type Selector
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            stringResource(R.string.repeat_label),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = primaryColor
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // None
                                            FilterChip(
                                                selected = repeatType == RepeatType.NONE,
                                                onClick = { repeatType = RepeatType.NONE },
                                                label = { Text(stringResource(R.string.repeat_none), fontSize = 12.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = primaryColor,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            )
                                            // Daily
                                            FilterChip(
                                                selected = repeatType == RepeatType.DAILY,
                                                onClick = { repeatType = RepeatType.DAILY },
                                                label = { Text(stringResource(R.string.repeat_daily), fontSize = 12.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = primaryColor,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            )
                                            // Weekly
                                            FilterChip(
                                                selected = repeatType == RepeatType.WEEKLY,
                                                onClick = { repeatType = RepeatType.WEEKLY },
                                                label = { Text(stringResource(R.string.repeat_weekly), fontSize = 12.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = primaryColor,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            )
                                            // Monthly
                                            FilterChip(
                                                selected = repeatType == RepeatType.MONTHLY,
                                                onClick = { repeatType = RepeatType.MONTHLY },
                                                label = { Text(stringResource(R.string.repeat_monthly), fontSize = 12.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = primaryColor,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Images Section
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "📷 Images",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = primaryColor
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ImageCountPreview(
                                    images = images,
                                    onImageClick = { showImageViewer = true },
                                    onAddClick = { galleryLauncher.launch("image/*") }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(500, delayMillis = 300)
                    ) + fadeIn(animationSpec = tween(500, delayMillis = 300))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismissRequest,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (!validateAll()) return@Button

                                val dateTime = LocalDateTime.of(date, time)

                                if (selectedTypeIsTask) {
                                    val task = Task(
                                        id = editTask?.id ?: 0,
                                        title = title.trim(),
                                        description = description.trim().ifBlank { null },
                                        startTime = dateTime,
                                        durationMinutes = durationText.toLongOrNull(),
                                        repeatType = repeatType,
                                        images = images
                                    )
                                    addItemViewModel.saveTask(task)
                                } else {
                                    val mission = Mission(
                                        id = editMission?.id ?: 0,
                                        title = title.trim(),
                                        description = description.trim().ifBlank { null },
                                        deadline = dateTime,
                                        storedStatus = editMission?.storedStatus ?: MissionStoredStatus.UNSPECIFIED,
                                        images = images
                                    )
                                    addItemViewModel.saveMission(mission)
                                }
                                onDismissRequest()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor
                            )
                        ) {
                            Text(if (isEditMode) "Update ✓" else "Save ✓", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Copy image from external source to internal storage for persistence
 */
private fun copyImageToInternalStorage(context: android.content.Context, sourceUri: Uri): Uri? {
    return try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_${timeStamp}_${System.currentTimeMillis()}.jpg"
        val storageDir = File(context.filesDir, "images")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        val destFile = File(storageDir, imageFileName)

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        Uri.fromFile(destFile)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
