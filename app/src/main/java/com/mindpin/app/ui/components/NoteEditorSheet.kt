package com.mindpin.app.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mindpin.app.model.Note
import com.mindpin.app.model.Tag
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteEditorSheet(
    note: Note?,
    tags: List<Tag>,
    onDismiss: () -> Unit,
    onSave: (content: String, tagId: Long?, reminderAt: Long?) -> Unit,
    onCreateTag: (String, (Long) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var content by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(note?.content.orEmpty()))
    }
    var selectedTagId by rememberSaveable { mutableStateOf(note?.tag?.id) }
    var reminderAt by rememberSaveable { mutableStateOf(note?.reminderAt) }
    var showTagDialog by remember { mutableStateOf(false) }
    var newTagName by rememberSaveable { mutableStateOf("") }

    val formatter = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }

    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text("Create tag") },
            text = {
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    label = { Text("Tag name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTagName.isNotBlank()) {
                        onCreateTag(newTagName) { id ->
                            selectedTagId = id
                        }
                        showTagDialog = false
                        newTagName = ""
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTagDialog = false }) { Text("Cancel") }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = if (note == null) "New note" else "Edit note", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Note") },
                supportingText = { Text("Add a short note or idea") }
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tags", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.forEach { tag ->
                        FilterChip(
                            selected = selectedTagId == tag.id,
                            onClick = {
                                selectedTagId = if (selectedTagId == tag.id) null else tag.id
                            },
                            label = { Text(tag.name) },
                            colors = FilterChipDefaults.filterChipColors()
                        )
                    }
                    TextButton(onClick = { showTagDialog = true }) {
                        Text("New tag")
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Reminder", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = {
                        val calendar = Calendar.getInstance().apply {
                            reminderAt?.let { timeInMillis = it }
                        }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(Calendar.YEAR, year)
                                calendar.set(Calendar.MONTH, month)
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                                        calendar.set(Calendar.MINUTE, minute)
                                        calendar.set(Calendar.SECOND, 0)
                                        reminderAt = calendar.timeInMillis
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true
                                ).show()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }) {
                        Text("Pick date")
                    }
                    if (reminderAt != null) {
                        TextButton(onClick = { reminderAt = null }) {
                            Text("Clear")
                        }
                    }
                }
                reminderAt?.let {
                    Text("Reminder set for ${formatter.format(Date(it))}", style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    enabled = content.text.isNotBlank(),
                    onClick = {
                        onSave(content.text, selectedTagId, reminderAt)
                    }
                ) {
                    Text("Save")
                }
            }
        }
    }

}
