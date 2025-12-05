package com.example.todolist.feature.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.graphics.Color
import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.Task
import com.example.todolist.core.model.RepeatType
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.LocalDate
import com.example.todolist.feature.common.AddItemViewModel

/**
 * A simple dialog that allows creating either a Task or a Mission.
 * It does not call UseCases directly; instead it exposes onSaveTask/onSaveMission callbacks
 * so callers (ViewModels/screens) can pass appropriate use case invocations.
 *
 * Fields: title (required), description (required), date (required), time (required), duration(required for task), repeat (required for task but enum default exists).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    onDismissRequest: () -> Unit,
    addItemViewModel: AddItemViewModel,
    defaultDate: LocalDate = LocalDate.now(),
    defaultIsTask: Boolean = true
) {
    val scrollState = rememberScrollState()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedTypeIsTask by remember { mutableStateOf(defaultIsTask) }
    var date by remember { mutableStateOf(defaultDate) }
    var time by remember { mutableStateOf(LocalTime.of(9, 0)) }

    // Task-specific fields
    var durationText by remember { mutableStateOf("") } // minutes, now required for Task
    var repeatType by remember { mutableStateOf(RepeatType.NONE) }

    // validation errors
    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }
    var durationError by remember { mutableStateOf<String?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun validateAll(): Boolean {
        // reset
        titleError = null
        descriptionError = null
        dateError = null
        timeError = null
        durationError = null

        var ok = true
        if (title.isBlank()) {
            titleError = "Title is required"
            ok = false
        }
        if (description.isBlank()) {
            descriptionError = "Description is required"
            ok = false
        }
        // date/time validation: check parsability by converting back to string/parse
        try {
            LocalDate.parse(date.toString())
        } catch (_: Exception) {
            dateError = "Invalid date"
            ok = false
        }
        try {
            LocalTime.parse(time.toString())
        } catch (_: Exception) {
            timeError = "Invalid time"
            ok = false
        }
        if (selectedTypeIsTask) {
            if (durationText.isBlank()) {
                durationError = "Duration is required for tasks"
                ok = false
            } else if (durationText.toLongOrNull() == null) {
                durationError = "Duration must be a number (minutes)"
                ok = false
            }
        }

        return ok
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .heightIn(max = 520.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // header row with title and actions
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = if (selectedTypeIsTask) "Add Task" else "Add Mission", style = MaterialTheme.typography.titleMedium)
                    Row {
                        TextButton(onClick = {
                            onDismissRequest()
                        }) { Text(text = "Cancel") }

                        TextButton(onClick = {
                            val valid = validateAll()
                            if (!valid) return@TextButton

                            val dateTime = LocalDateTime.of(date, time)

                            if (selectedTypeIsTask) {
                                val durationMinutes = durationText.toLong()

                                val task = Task(
                                    id = 0,
                                    title = title.trim(),
                                    description = description.trim().ifBlank { null },
                                    startTime = dateTime,
                                    durationMinutes = durationMinutes,
                                    repeatType = repeatType
                                )
                                addItemViewModel.saveTask(task)
                            } else {
                                // mission.status defaults to UNSPECIFIED
                                val mission = Mission(id = 0, title = title.trim(), description = description.trim().ifBlank { null }, deadline = dateTime)
                                addItemViewModel.saveMission(mission)
                            }

                            onDismissRequest()
                        }) { Text(text = "Save") }
                    }
                }

                // content fields
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), isError = titleError != null)
                if (titleError != null) Text(text = titleError ?: "", color = Color.Red)

                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), isError = descriptionError != null)
                if (descriptionError != null) Text(text = descriptionError ?: "", color = Color.Red)

                // Simple type selector
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(selected = selectedTypeIsTask, onClick = { selectedTypeIsTask = true })
                        Text(text = "Task", modifier = Modifier.padding(start = 4.dp))
                    }
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(selected = !selectedTypeIsTask, onClick = { selectedTypeIsTask = false })
                        Text(text = "Mission", modifier = Modifier.padding(start = 4.dp))
                    }
                }

                // Date and time inputs: to keep this small and dependency-free we use basic text fields expecting yyyy-mm-dd and HH:mm formats.
                OutlinedTextField(
                    value = date.toString(),
                    onValueChange = { input ->
                        try {
                            val parsed = LocalDate.parse(input)
                            date = parsed
                            dateError = null
                        } catch (_: Exception) {
                            dateError = "Invalid date"
                        }
                    },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = dateError != null
                )
                if (dateError != null) Text(text = dateError ?: "", color = Color.Red)

                OutlinedTextField(
                    value = time.toString(),
                    onValueChange = { input ->
                        try {
                            val parsed = LocalTime.parse(input)
                            time = parsed
                            timeError = null
                        } catch (_: Exception) {
                            timeError = "Invalid time"
                        }
                    },
                    label = { Text("Time (HH:MM)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = timeError != null
                )
                if (timeError != null) Text(text = timeError ?: "", color = Color.Red)

                // Show Task-specific fields only when Task is selected
                if (selectedTypeIsTask) {
                    // Duration (minutes) - required now
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { input ->
                            // keep only digits to simplify parsing
                            durationText = input.filter { it.isDigit() }
                            durationError = null
                        },
                        label = { Text("Duration (minutes)") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = durationError != null
                    )
                    if (durationError != null) Text(text = durationError ?: "", color = Color.Red)

                    // Repeat type selector - vertical list to avoid horizontal overflow
                    Column {
                        Text(text = "Repeat")
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                            RepeatType.entries.forEach { rt ->
                                Row(
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    RadioButton(selected = repeatType == rt, onClick = { repeatType = rt })
                                    Text(text = rt.name, modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
