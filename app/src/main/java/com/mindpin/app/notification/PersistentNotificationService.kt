package com.mindpin.app.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.mindpin.app.MainActivity
import com.mindpin.app.QuickAddNoteActivity
import com.mindpin.app.R

class PersistentNotificationService : Service() {

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val quickAddIntent = PendingIntent.getActivity(
            this,
            1,
            QuickAddNoteActivity.createIntent(this),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val remoteInput = RemoteInput.Builder(NoteActionReceiver.KEY_CONTENT)
            .setLabel(getString(R.string.notification_add_note))
            .build()

        val saveIntent = PendingIntent.getBroadcast(
            this,
            2,
            NoteActionReceiver.createIntent(this),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val saveAction = NotificationCompat.Action.Builder(
            R.drawable.ic_note_add,
            getString(R.string.notification_add_note),
            saveIntent
        ).addRemoteInput(remoteInput).build()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_add_note))
            .setSmallIcon(R.drawable.ic_note)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(saveAction)
            .addAction(
                R.drawable.ic_note,
                getString(R.string.notification_open_app),
                quickAddIntent
            )
            .build()
    }

    private fun createChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.notification_channel_description)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "mindpin_notes"
        private const val NOTIFICATION_ID = 1

        fun start(context: Context) {
            val intent = Intent(context, PersistentNotificationService::class.java)
            context.startForegroundService(intent)
        }
    }
}
