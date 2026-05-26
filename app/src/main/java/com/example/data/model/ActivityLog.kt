package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_logs",
    foreignKeys = [
        ForeignKey(
            entity = ActivityCategory::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryId: Int,
    val dateString: String, // e.g., "2026-05-26"
    val startTime: Long,    // Milliseconds
    val endTime: Long       // Milliseconds
)
