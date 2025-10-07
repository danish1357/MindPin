package com.mindpin.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mindpin.app.data.repository.NoteRepository
import com.mindpin.app.model.Note
import com.mindpin.app.model.NoteSort
import com.mindpin.app.ui.state.NoteListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MindPinViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    private val isAddEditVisible = MutableStateFlow(false)
    private val editingNote = MutableStateFlow<Note?>(null)

    private val sortOrder = repository.observeSortOrder()
        .stateIn(viewModelScope, SharingStarted.Eagerly, NoteSort.NEWEST)

    private val tags = repository.observeTags()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val notes = sortOrder.flatMapLatest { sort ->
        repository.observeNotes(sort)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val uiState = combine(sortOrder, tags, notes, isAddEditVisible, editingNote) { sort, tags, notes, visible, editing ->
        NoteListUiState(
            notes = notes,
            tags = tags,
            selectedSort = sort,
            isAddEditVisible = visible,
            editingNote = editing
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, NoteListUiState())

    fun showAddSheet(note: Note? = null) {
        editingNote.value = note
        isAddEditVisible.value = true
    }

    fun hideAddSheet() {
        isAddEditVisible.value = false
        editingNote.value = null
    }

    fun updateSort(sort: NoteSort) {
        viewModelScope.launch {
            repository.updateSortOrder(sort)
        }
    }

    fun saveNote(content: String, tagId: Long?, reminderAt: Long?) {
        val editing = editingNote.value
        if (editing == null) {
            viewModelScope.launch {
                repository.addNote(content, tagId, reminderAt)
            }
        } else {
            viewModelScope.launch {
                val selectedTag = tagId?.let { id -> uiState.value.tags.firstOrNull { it.id == id } }
                repository.updateNote(
                    editing.copy(
                        content = content,
                        tag = selectedTag,
                        reminderAt = reminderAt
                    )
                )
            }
        }
        hideAddSheet()
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
        }
    }

    fun clearReminder(noteId: Long) {
        viewModelScope.launch {
            repository.clearReminder(noteId)
        }
    }

    fun addTag(name: String, onCreated: (Long) -> Unit = {}) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val color = palette.random()
            val id = repository.addTag(name.trim(), color)
            onCreated(id)
        }
    }

    private val palette = listOf(
        0xFF6750A4L,
        0xFF7D5260L,
        0xFF386A20L,
        0xFF006494L,
        0xFF964F4CL
    )

    class Factory(
        private val repository: NoteRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MindPinViewModel::class.java)) {
                return MindPinViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
