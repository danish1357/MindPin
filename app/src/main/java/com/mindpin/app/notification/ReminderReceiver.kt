package com.mindpin.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.mindpin.app.MainActivity
import com.mindpin.app.MindPinApplication
import com.mindpin.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1)
        val content = intent.getStringExtra(EXTRA_NOTE_CONTENT).orEmpty()
        if (noteId == -1L) return

        if (action == ACTION_MARK_DONE) {
            val app = context.applicationContext as MindPinApplication
            CoroutineScope(Dispatchers.IO).launch {
                app.repository.clearReminder(noteId)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(noteId.toInt())
            return
        }

        showReminderNotification(context, noteId, content)
    }

    private fun showReminderNotification(context: Context, noteId: Long, content: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)

        val openIntent = PendingIntent.getActivity(
            context,
            noteId.toInt(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val markDoneIntent = PendingIntent.getBroadcast(
            context,
            (noteId + 1000).toInt(),
            createMarkDoneIntent(context, noteId),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .addAction(
                R.drawable.ic_check,
                context.getString(R.string.notification_mark_done),
                markDoneIntent
            )
            .build()

        manager.notify(noteId.toInt(), notification)
    }

    companion object {
        private const val CHANNEL_ID = "mindpin_reminder"
        const val EXTRA_NOTE_ID = "extra_note_id"
        const val EXTRA_NOTE_CONTENT = "extra_note_content"
        const val ACTION_REMINDER = "com.mindpin.app.action.REMINDER"
        const val ACTION_MARK_DONE = "com.mindpin.app.action.MARK_DONE"

        fun createIntent(context: Context, noteId: Long, content: String): Intent =
            Intent(context, ReminderReceiver::class.java).apply {
                action = ACTION_REMINDER
                putExtra(EXTRA_NOTE_ID, noteId)
                putExtra(EXTRA_NOTE_CONTENT, content)
            }

        fun createMarkDoneIntent(context: Context, noteId: Long): Intent =
            Intent(context, ReminderReceiver::class.java).apply {
                action = ACTION_MARK_DONE
                putExtra(EXTRA_NOTE_ID, noteId)
            }
    }
}
