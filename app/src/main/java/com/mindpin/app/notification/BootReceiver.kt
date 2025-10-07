package com.mindpin.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mindpin.app.MindPinApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED != intent.action) return
        PersistentNotificationService.start(context)
        val app = context.applicationContext as MindPinApplication
        CoroutineScope(Dispatchers.IO).launch {
            app.repository.rescheduleReminders()
        }
    }
}
