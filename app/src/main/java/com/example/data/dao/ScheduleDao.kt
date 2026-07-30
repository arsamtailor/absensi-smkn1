package com.example.data.dao

import androidx.room.*
import com.example.data.model.TeachingSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM teaching_schedules ORDER BY id ASC")
    fun getAllSchedules(): Flow<List<TeachingSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: TeachingSchedule): Long

    @Update
    suspend fun updateSchedule(schedule: TeachingSchedule)

    @Delete
    suspend fun deleteSchedule(schedule: TeachingSchedule)
}
