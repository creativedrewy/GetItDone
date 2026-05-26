package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    private val TAG = "BootReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device booted. Rescheduling all active reminders.")
            
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    // Get all active categories with reminders enabled
                    db.categoryDao().getAllCategories().collect { categories ->
                        val activeCategories = categories.filter { it.isReminderEnabled }
                        for (category in activeCategories) {
                            ReminderScheduler.scheduleReminder(context, category)
                        }
                        // We only need the first emission to restore state, so cancel flow
                        pendingResult.finish()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error rescheduling reminders on boot: ${e.message}", e)
                    pendingResult.finish()
                }
            }
        }
    }
}
