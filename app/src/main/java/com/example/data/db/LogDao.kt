package com.example.data.db

import androidx.room.*
import com.example.data.model.ActivityLog
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM activity_logs ORDER BY startTime DESC")
    fun getAllLogs(): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE categoryId = :categoryId ORDER BY startTime DESC")
    fun getLogsForCategory(categoryId: Int): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE dateString = :dateString ORDER BY startTime DESC")
    fun getLogsForDate(dateString: String): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLog): Long

    @Delete
    suspend fun deleteLog(log: ActivityLog)
}
