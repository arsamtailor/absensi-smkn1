package com.example.data.dao

import androidx.room.*
import com.example.data.model.ClassGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassGroupDao {
    @Query("SELECT * FROM class_groups ORDER BY name ASC")
    fun getAllClasses(): Flow<List<ClassGroup>>

    @Query("SELECT * FROM class_groups WHERE id = :id LIMIT 1")
    suspend fun getClassById(id: Long): ClassGroup?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classGroup: ClassGroup): Long

    @Update
    suspend fun updateClass(classGroup: ClassGroup)

    @Delete
    suspend fun deleteClass(classGroup: ClassGroup)

    @Query("DELETE FROM class_groups")
    suspend fun deleteAllClasses()
}
