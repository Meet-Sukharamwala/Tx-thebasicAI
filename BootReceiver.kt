package com.example.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val pendingReminders = db.reminderDao().getPendingUpcomingReminders()
                    val now = System.currentTimeMillis()
                    for (reminder in pendingReminders) {
                        if (reminder.targetTimeMillis > now && !reminder.isCancelled) {
                            ReminderScheduler.schedule(
                                context = context,
                                reminderId = reminder.id,
                                title = reminder.title,
                                triggerAtMillis = reminder.targetTimeMillis
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
