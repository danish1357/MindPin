package com.mindpin.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mindpin.app.model.Note
import com.mindpin.app.model.NoteSort
import com.mindpin.app.ui.components.NoteEditorSheet
import com.mindpin.app.ui.state.NoteListUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    state: NoteListUiState,
    onAddNote: () -> Unit,
    onEditNote: (Note) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onSortChange: (NoteSort) -> Unit,
    onReminderClear: (Long) -> Unit,
    onSaveNote: (String, Long?, Long?) -> Unit,
    onDismissEditor: () -> Unit,
    onCreateTag: (String, (Long) -> Unit) -> Unit
) {
    var isSortMenuExpanded by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("MindPin") },
                actions = {
                    IconButton(onClick = { isSortMenuExpanded = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(
                        expanded = isSortMenuExpanded,
                        onDismissRequest = { isSortMenuExpanded = false }
                    ) {
                        NoteSort.entries.forEach { sort ->
                            DropdownMenuItem(
                                text = { Text(sort.label()) },
                                onClick = {
                                    isSortMenuExpanded = false
                                    onSortChange(sort)
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNote) {
                Icon(Icons.Default.Add, contentDescription = "Add note")
            }
        }
    ) { innerPadding ->
        if (state.notes.isEmpty()) {
            EmptyState(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.notes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        onEdit = { onEditNote(note) },
                        onDelete = { onDeleteNote(note.id) },
                        onReminderClear = { onReminderClear(note.id) }
                    )
                }
            }
        }
    }

    if (state.isAddEditVisible) {
        val tags = state.tags
        NoteEditorSheet(
            note = state.editingNote,
            tags = tags,
            onDismiss = onDismissEditor,
            onSave = { content, tagId, reminderAt ->
                onSaveNote(content, tagId, reminderAt)
            },
            onCreateTag = onCreateTag
        )
    }
}

@Composable
private fun NoteSort.label(): String = when (this) {
    NoteSort.NEWEST -> "Newest first"
    NoteSort.OLDEST -> "Oldest first"
    NoteSort.TAG -> "By tag"
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Capture every thought",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the + button or use the notification to add your first note.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NoteCard(
    note: Note,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReminderClear: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                note.tag?.let { tag ->
                    AssistChip(
                        onClick = {},
                        label = { Text(tag.name) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            val formatter = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }
            Text(
                text = "Created ${formatter.format(Date(note.createdAt))}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            note.reminderAt?.let { reminder ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Reminder ${formatter.format(Date(reminder))}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = onReminderClear) {
                    Text("Clear reminder")
                }
            }
        }
    }
}
