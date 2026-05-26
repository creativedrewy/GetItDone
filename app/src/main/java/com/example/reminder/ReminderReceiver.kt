package com.example.reminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.model.ActivityLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderReceiver : BroadcastReceiver() {
    private val TAG = "ReminderReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, "onReceive action = $action")

        when (action) {
            "com.example.ACTION_REMIND" -> {
                handleReminder(context, intent)
            }
            "com.example.ACTION_QUICK_LOG" -> {
                handleQuickLog(context, intent)
            }
        }
    }

    private fun handleReminder(context: Context, intent: Intent) {
        val categoryId = intent.getIntExtra("CATEGORY_ID", -1)
        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Activity"
        
        if (categoryId == -1) return

        // 1. Show the notification
        showNotification(context, categoryId, categoryName)

        // 2. Reschedule the next alarm recursively based on current DB values
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val category = db.categoryDao().getCategoryById(categoryId)
                if (category != null && category.isReminderEnabled) {
                    // Update database with last reminder timestamp
                    val updatedCategory = category.copy(lastReminderTime = System.currentTimeMillis())
                    db.categoryDao().updateCategory(updatedCategory)
                    
                    // Reschedule for next interval
                    ReminderScheduler.scheduleReminder(context, updatedCategory)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling alarm: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleQuickLog(context: Context, intent: Intent) {
        val categoryId = intent.getIntExtra("CATEGORY_ID", -1)
        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Activity"
        if (categoryId == -1) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                
                // Track standard duration (e.g. 5 minutes ago to now, or just an instant log)
                val endTime = System.currentTimeMillis()
                val startTime = endTime - (5 * 60 * 1000) // 5 minutes duration by default
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val currentDateStr = dateFormat.format(Date(endTime))

                val log = ActivityLog(
                    categoryId = categoryId,
                    dateString = currentDateStr,
                    startTime = startTime,
                    endTime = endTime
                )
                
                db.logDao().insertLog(log)
                Log.d(TAG, "Quick logged activity for '$categoryName' (id=$categoryId)")

                // Dismiss notification
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(categoryId)
            } catch (e: Exception) {
                Log.e(TAG, "Error performing background quick log: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, categoryId: Int, categoryName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel if needed
        ReminderScheduler.createNotificationChannel(context)

        // Activity click intent
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            categoryId * 10,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // Quick log action intent
        val quickLogIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.example.ACTION_QUICK_LOG"
            putExtra("CATEGORY_ID", categoryId)
            putExtra("CATEGORY_NAME", categoryName)
        }
        val quickLogPendingIntent = PendingIntent.getBroadcast(
            context,
            categoryId * 100,
            quickLogIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notificationBuilder = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // System default icon initially
            .setContentTitle("Activity Reminder")
            .setContentText("It's time to do your activity: $categoryName!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_edit,
                "Log Done Now",
                quickLogPendingIntent
            )

        notificationManager.notify(categoryId, notificationBuilder.build())
    }
}
