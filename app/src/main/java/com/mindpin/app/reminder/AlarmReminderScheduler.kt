package com.mindpin.app.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.mindpin.app.notification.ReminderReceiver
import java.util.concurrent.TimeUnit

class AlarmReminderScheduler(private val context: Context) : ReminderScheduler {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(noteId: Long, triggerAtMillis: Long, content: String) {
        val intent = ReminderReceiver.createIntent(context, noteId, content)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            noteId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    override fun cancel(noteId: Long) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            noteId.toInt(),
            ReminderReceiver.createIntent(context, noteId, ""),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

}
