package com.mindpin.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Transaction
    @Query("SELECT * FROM notes WHERE is_archived = 0 ORDER BY created_at DESC")
    fun observeNotesNewest(): Flow<List<NoteWithTag>>

    @Transaction
    @Query("SELECT * FROM notes WHERE is_archived = 0 ORDER BY created_at ASC")
    fun observeNotesOldest(): Flow<List<NoteWithTag>>

    @Transaction
    @Query(
        "SELECT * FROM notes WHERE is_archived = 0 ORDER BY tag_id IS NULL, tag_id, created_at DESC"
    )
    fun observeNotesByTag(): Flow<List<NoteWithTag>>

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNote(noteId: Long): NoteWithTag?

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteEntity(noteId: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE reminder_at IS NOT NULL AND is_archived = 0")
    suspend fun getNotesWithReminder(): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)
}
