package com.example.data.db

import androidx.room.*
import com.example.data.model.ActivityCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM activity_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<ActivityCategory>>

    @Query("SELECT * FROM activity_categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): ActivityCategory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: ActivityCategory): Long

    @Update
    suspend fun updateCategory(category: ActivityCategory)

    @Delete
    suspend fun deleteCategory(category: ActivityCategory)
}
