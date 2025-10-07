package com.mindpin.app

import android.app.Application
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.mindpin.app.data.local.MindPinDatabase
import com.mindpin.app.data.repository.DefaultNoteRepository
import com.mindpin.app.data.repository.NoteRepository
import com.mindpin.app.reminder.AlarmReminderScheduler
import com.mindpin.app.reminder.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val Application.userPreferencesStore by preferencesDataStore(name = "mindpin_preferences")

class MindPinApplication : Application() {

    lateinit var repository: NoteRepository
        private set

    lateinit var reminderScheduler: ReminderScheduler
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(
            applicationContext,
            MindPinDatabase::class.java,
            "mindpin.db"
        ).fallbackToDestructiveMigration().build()

        reminderScheduler = AlarmReminderScheduler(this)

        repository = DefaultNoteRepository(
            noteDao = database.noteDao(),
            tagDao = database.tagDao(),
            reminderScheduler = reminderScheduler,
            preferences = userPreferencesStore
        )

        applicationScope.launch {
            repository.ensureDefaultTags()
        }
    }
}
