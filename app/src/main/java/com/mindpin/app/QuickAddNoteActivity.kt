package com.mindpin.app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindpin.app.ui.theme.MindPinTheme
import com.mindpin.app.ui.viewmodel.MindPinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class QuickAddNoteActivity : ComponentActivity() {

    private val viewModel: MindPinViewModel by viewModels {
        val app = application as MindPinApplication
        MindPinViewModel.Factory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MindPinTheme {
                QuickAddNoteRoute(
                    viewModel = viewModel,
                    onDone = { finish() }
                )
            }
        }
    }

    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, QuickAddNoteActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun QuickAddNoteRoute(
    viewModel: MindPinViewModel,
    onDone: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var content by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var selectedTagId by rememberSaveable { mutableStateOf<Long?>(null) }
    var reminderAt by rememberSaveable { mutableStateOf<Long?>(null) }
    var showTagDialog by remember { mutableStateOf(false) }
    var newTagName by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val formatter = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }

    if (showTagDialog) {
        androidx.compose.material3.AlertDialog(
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
                        viewModel.addTag(newTagName) { id ->
                            selectedTagId = id
                        }
                        showTagDialog = false
                        newTagName = ""
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showTagDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quick note") },
                actions = {
                    TextButton(
                        enabled = content.text.isNotBlank(),
                        onClick = {
                            viewModel.saveNote(content.text, selectedTagId, reminderAt)
                            onDone()
                        }
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Note") }
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tag", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.tags.forEach { tag ->
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
        }
    }
}
