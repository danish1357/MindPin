package com.mindpin.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.RemoteInput
import com.mindpin.app.MindPinApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_QUICK_ADD) return
        val input = RemoteInput.getResultsFromIntent(intent)
        val content = input?.getCharSequence(KEY_CONTENT)?.toString()?.trim().orEmpty()
        if (content.isBlank()) {
            Toast.makeText(context, "Unable to save empty note", Toast.LENGTH_SHORT).show()
            return
        }
        val app = context.applicationContext as MindPinApplication
        CoroutineScope(Dispatchers.IO).launch {
            app.repository.addNote(content, tagId = null, reminderAt = null)
        }
        Toast.makeText(context, "Note saved", Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val KEY_CONTENT = "note_content"
        const val ACTION_QUICK_ADD = "com.mindpin.app.action.QUICK_ADD"

        fun createIntent(context: Context): Intent =
            Intent(context, NoteActionReceiver::class.java).apply {
                action = ACTION_QUICK_ADD
            }
    }
}
