package com.mindpin.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindpin.app.ui.theme.MindPinTheme
import com.mindpin.app.ui.viewmodel.MindPinViewModel

@Composable
fun MindPinApp(viewModel: MindPinViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MindPinTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NoteListScreen(
                state = uiState,
                onAddNote = { viewModel.showAddSheet() },
                onEditNote = { viewModel.showAddSheet(it) },
                onDeleteNote = { viewModel.deleteNote(it) },
                onSortChange = { viewModel.updateSort(it) },
                onReminderClear = { viewModel.clearReminder(it) },
                onSaveNote = { content, tagId, reminderAt ->
                    viewModel.saveNote(content, tagId, reminderAt)
                },
                onDismissEditor = { viewModel.hideAddSheet() },
                onCreateTag = { name, onCreated -> viewModel.addTag(name, onCreated) }
            )
        }
    }
}
