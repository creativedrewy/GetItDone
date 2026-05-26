package com.example.data.repository

import com.example.data.db.CategoryDao
import com.example.data.db.LogDao
import com.example.data.model.ActivityCategory
import com.example.data.model.ActivityLog
import kotlinx.coroutines.flow.Flow

class ActivityRepository(
    private val categoryDao: CategoryDao,
    private val logDao: LogDao
) {
    val allCategories: Flow<List<ActivityCategory>> = categoryDao.getAllCategories()
    val allLogs: Flow<List<ActivityLog>> = logDao.getAllLogs()

    fun getLogsForDate(dateString: String): Flow<List<ActivityLog>> {
        return logDao.getLogsForDate(dateString)
    }

    fun getLogsForCategory(categoryId: Int): Flow<List<ActivityLog>> {
        return logDao.getLogsForCategory(categoryId)
    }

    suspend fun getCategoryById(id: Int): ActivityCategory? {
        return categoryDao.getCategoryById(id)
    }

    suspend fun insertCategory(category: ActivityCategory): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: ActivityCategory) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: ActivityCategory) {
        categoryDao.deleteCategory(category)
    }

    suspend fun insertLog(log: ActivityLog): Long {
        return logDao.insertLog(log)
    }

    suspend fun deleteLog(log: ActivityLog) {
        logDao.deleteLog(log)
    }
}
