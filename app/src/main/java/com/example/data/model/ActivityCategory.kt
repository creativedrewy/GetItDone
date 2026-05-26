package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_categories")
data class ActivityCategory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val iconName: String, // e.g. "water_drop", "fitness_center", "self_improvement" etc.
    val colorHex: String, // e.g. "#2196F3"
    val isReminderEnabled: Boolean = false,
    val timesPerDay: Int = 5,
    val intervalHours: Float = 2.0f,
    val lastReminderTime: Long = 0L,
    val currentSessionStartTime: Long = 0L // 0L means no active timer running
)
