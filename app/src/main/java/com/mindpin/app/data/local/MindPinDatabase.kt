package com.mindpin.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [NoteEntity::class, TagEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MindPinDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun tagDao(): TagDao
}
