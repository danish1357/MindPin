package com.mindpin.app.ui.state

import com.mindpin.app.model.Note
import com.mindpin.app.model.NoteSort
import com.mindpin.app.model.Tag

data class NoteListUiState(
    val notes: List<Note> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val selectedSort: NoteSort = NoteSort.NEWEST,
    val isAddEditVisible: Boolean = false,
    val editingNote: Note? = null
)
