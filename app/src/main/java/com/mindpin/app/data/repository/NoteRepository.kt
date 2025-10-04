package com.mindpin.app.data.repository

import com.mindpin.app.model.Note
import com.mindpin.app.model.NoteSort
import com.mindpin.app.model.Tag
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeNotes(sort: NoteSort): Flow<List<Note>>
    fun observeTags(): Flow<List<Tag>>
    fun observeSortOrder(): Flow<NoteSort>
    suspend fun updateSortOrder(sort: NoteSort)
    suspend fun addNote(content: String, tagId: Long?, reminderAt: Long?): Long
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(noteId: Long)
    suspend fun addTag(name: String, color: Long): Long
    suspend fun ensureDefaultTags()
    suspend fun rescheduleReminders()
    suspend fun clearReminder(noteId: Long)
}
