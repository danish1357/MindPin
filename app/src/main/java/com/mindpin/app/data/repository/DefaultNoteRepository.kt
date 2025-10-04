package com.mindpin.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mindpin.app.data.local.NoteDao
import com.mindpin.app.data.local.NoteEntity
import com.mindpin.app.data.local.NoteWithTag
import com.mindpin.app.data.local.TagDao
import com.mindpin.app.data.local.TagEntity
import com.mindpin.app.model.Note
import com.mindpin.app.model.NoteSort
import com.mindpin.app.model.Tag
import com.mindpin.app.reminder.ReminderScheduler
import kotlinx.coroutines.flow.map

class DefaultNoteRepository(
    private val noteDao: NoteDao,
    private val tagDao: TagDao,
    private val reminderScheduler: ReminderScheduler,
    private val preferences: DataStore<Preferences>
) : NoteRepository {

    private val sortKey = stringPreferencesKey("note_sort_order")

    override fun observeNotes(sort: NoteSort): Flow<List<Note>> {
        val source = when (sort) {
            NoteSort.NEWEST -> noteDao.observeNotesNewest()
            NoteSort.OLDEST -> noteDao.observeNotesOldest()
            NoteSort.TAG -> noteDao.observeNotesByTag()
        }
        return source.map { notes ->
            notes.map { it.toNote() }
        }
    }

    override fun observeTags(): Flow<List<Tag>> =
        tagDao.observeTags().map { entities ->
            entities.map { it.toTag() }
        }

    override fun observeSortOrder(): Flow<NoteSort> =
        preferences.data.map { prefs ->
            NoteSort.fromKey(prefs[sortKey] ?: NoteSort.NEWEST.storageKey)
        }

    override suspend fun updateSortOrder(sort: NoteSort) {
        preferences.edit { prefs ->
            prefs[sortKey] = sort.storageKey
        }
    }

    override suspend fun addNote(content: String, tagId: Long?, reminderAt: Long?): Long {
        val note = NoteEntity(
            content = content.trim(),
            tagId = tagId,
            createdAt = System.currentTimeMillis(),
            reminderAt = reminderAt,
            isArchived = false
        )
        val id = noteDao.upsertNote(note)
        if (reminderAt != null) {
            reminderScheduler.schedule(id, reminderAt, content)
        }
        return id
    }

    override suspend fun updateNote(note: Note) {
        val existing = noteDao.getNoteEntity(note.id) ?: return
        val updated = existing.copy(
            content = note.content.trim(),
            tagId = note.tag?.id,
            reminderAt = note.reminderAt
        )
        noteDao.updateNote(updated)
        if (note.reminderAt != null) {
            reminderScheduler.schedule(note.id, note.reminderAt, note.content)
        } else {
            reminderScheduler.cancel(note.id)
        }
    }

    override suspend fun deleteNote(noteId: Long) {
        val existing = noteDao.getNoteEntity(noteId) ?: return
        noteDao.deleteNote(existing)
        reminderScheduler.cancel(noteId)
    }

    override suspend fun clearReminder(noteId: Long) {
        val existing = noteDao.getNoteEntity(noteId) ?: return
        val updated = existing.copy(reminderAt = null)
        noteDao.updateNote(updated)
        reminderScheduler.cancel(noteId)
    }

    override suspend fun addTag(name: String, color: Long): Long {
        return tagDao.insert(
            TagEntity(
                name = name.trim(),
                color = color
            )
        )
    }

    override suspend fun ensureDefaultTags() {
        if (tagDao.count() > 0) return
        listOf(
            TagEntity(name = "Personal", color = randomPastel()),
            TagEntity(name = "Work", color = randomPastel()),
            TagEntity(name = "Ideas", color = randomPastel())
        ).forEach { tagDao.insert(it) }
    }

    override suspend fun rescheduleReminders() {
        val notes = noteDao.getNotesWithReminder()
        notes.forEach { note ->
            note.reminderAt?.let { reminderAt ->
                if (reminderAt >= System.currentTimeMillis()) {
                    reminderScheduler.schedule(note.id, reminderAt, note.content)
                }
            }
        }
    }

    private fun NoteWithTag.toNote(): Note = Note(
        id = note.id,
        content = note.content,
        tag = tag?.toTag(),
        createdAt = note.createdAt,
        reminderAt = note.reminderAt
    )

    private fun TagEntity.toTag(): Tag = Tag(
        id = id,
        name = name,
        color = color
    )

    private fun randomPastel(): Long {
        val base = listOf(0xFFBB86FCL, 0xFF6200EEL, 0xFF03DAC5L, 0xFFFFB74DL, 0xFF4FC3F7L)
        return base.random()
    }
}
