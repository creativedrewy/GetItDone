package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ActivityCategory
import com.example.data.model.ActivityLog
import com.example.data.repository.ActivityRepository
import com.example.reminder.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityViewModel(private val repository: ActivityRepository) : ViewModel() {

    // Theme preference persistence
    private var sharedPrefs: android.content.SharedPreferences? = null
    val isDarkTheme = MutableStateFlow(true)

    fun initPrefs(context: Context) {
        if (sharedPrefs == null) {
            sharedPrefs = context.applicationContext.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
            isDarkTheme.value = sharedPrefs?.getBoolean("is_dark_theme", true) ?: true
        }
    }

    fun toggleTheme() {
        val newValue = !isDarkTheme.value
        isDarkTheme.value = newValue
        sharedPrefs?.edit()?.putBoolean("is_dark_theme", newValue)?.apply()
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val todayString = dateFormat.format(Date())

    // All categories
    val categories: StateFlow<List<ActivityCategory>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Today's logs
    val logsToday: StateFlow<List<ActivityLog>> = repository.getLogsForDate(todayString)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // All logs
    val allLogs: StateFlow<List<ActivityLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Active Category Timer session management
    fun startTimer(category: ActivityCategory) {
        viewModelScope.launch {
            val updated = category.copy(currentSessionStartTime = System.currentTimeMillis())
            repository.updateCategory(updated)
        }
    }

    fun stopTimer(context: Context, category: ActivityCategory) {
        val startTime = category.currentSessionStartTime
        if (startTime <= 0L) return

        val endTime = System.currentTimeMillis()
        val dateStr = dateFormat.format(Date(endTime))

        viewModelScope.launch {
            // Save log entry
            val newLog = ActivityLog(
                categoryId = category.id,
                dateString = dateStr,
                startTime = startTime,
                endTime = endTime
            )
            repository.insertLog(newLog)

            // Reset timer in category
            val updated = category.copy(currentSessionStartTime = 0L)
            repository.updateCategory(updated)
        }
    }

    // Standard Logging Actions
    fun logQuickInstant(category: ActivityCategory) {
        viewModelScope.launch {
            val endTime = System.currentTimeMillis()
            val startTime = endTime - (10 * 60 * 1000) // Default log is 10 min duration
            val dateStr = dateFormat.format(Date(endTime))

            val newLog = ActivityLog(
                categoryId = category.id,
                dateString = dateStr,
                startTime = startTime,
                endTime = endTime
            )
            repository.insertLog(newLog)
        }
    }

    fun logCustomDuration(id: Int, date: Date, startMillis: Long, endMillis: Long) {
        viewModelScope.launch {
            val dateStr = dateFormat.format(date)
            val newLog = ActivityLog(
                categoryId = id,
                dateString = dateStr,
                startTime = startMillis,
                endTime = endMillis
            )
            repository.insertLog(newLog)
        }
    }

    fun deleteLog(log: ActivityLog) {
        viewModelScope.launch {
            repository.deleteLog(log)
        }
    }

    // Category CRUD
    fun addCategory(
        context: Context,
        name: String,
        iconName: String,
        colorHex: String,
        isReminderEnabled: Boolean,
        timesPerDay: Int,
        intervalHours: Float
    ) {
        viewModelScope.launch {
            val newCategory = ActivityCategory(
                name = name,
                iconName = iconName,
                colorHex = colorHex,
                isReminderEnabled = isReminderEnabled,
                timesPerDay = timesPerDay,
                intervalHours = intervalHours
            )
            val insertedId = repository.insertCategory(newCategory).toInt()
            
            // Schedule if relevant
            if (isReminderEnabled) {
                val savedCategory = newCategory.copy(id = insertedId)
                ReminderScheduler.scheduleReminder(context, savedCategory)
            }
        }
    }

    fun updateCategory(context: Context, category: ActivityCategory) {
        viewModelScope.launch {
            repository.updateCategory(category)
            
            // Adjust reminders
            if (category.isReminderEnabled) {
                ReminderScheduler.scheduleReminder(context, category)
            } else {
                ReminderScheduler.cancelReminder(context, category)
            }
        }
    }

    fun deleteCategory(context: Context, category: ActivityCategory) {
        viewModelScope.launch {
            // Cancel reminders
            ReminderScheduler.cancelReminder(context, category)
            // Delete category (will cascade delete logs)
            repository.deleteCategory(category)
        }
    }
}

class ActivityViewModelFactory(private val repository: ActivityRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActivityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
