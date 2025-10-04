package com.mindpin.app.reminder

interface ReminderScheduler {
    fun schedule(noteId: Long, triggerAtMillis: Long, content: String)
    fun cancel(noteId: Long)
}
