package com.example.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.ActivityCategory

object ReminderScheduler {
    private const val TAG = "ReminderScheduler"
    const val CHANNEL_ID = "activity_tracker_reminders"
    private const val CHANNEL_NAME = "Activity Tracker Reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to track your activities regularly throughout the day"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun scheduleReminder(context: Context, category: ActivityCategory) {
        if (!category.isReminderEnabled) {
            cancelReminder(context, category)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        // Calculate interval in milliseconds
        val intervalMillis = (category.intervalHours * 3600 * 1000).toLong()
        val triggerTime = System.currentTimeMillis() + intervalMillis

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.example.ACTION_REMIND"
            putExtra("CATEGORY_ID", category.id)
            putExtra("CATEGORY_NAME", category.name)
            putExtra("CATEGORY_ICON", category.iconName)
            putExtra("CATEGORY_COLOR", category.colorHex)
        }

        // PendingIntent flags
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        // Unique pending intent request code per category
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            category.id,
            intent,
            flags
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled reminder for category '${category.name}' (id=${category.id}) in ${category.intervalHours} hours.")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling reminder: ${e.message}", e)
        }
    }

    fun cancelReminder(context: Context, category: ActivityCategory) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.example.ACTION_REMIND"
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            category.id,
            intent,
            flags
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled reminder for category '${category.name}' (id=${category.id})")
        }
    }
}
