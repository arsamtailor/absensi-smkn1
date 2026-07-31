package com.example.data.dao

import androidx.room.*
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceSession
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<AttendanceSession>>

    @Query("SELECT * FROM attendance_sessions WHERE classId = :classId ORDER BY timestamp DESC")
    fun getSessionsByClass(classId: Long): Flow<List<AttendanceSession>>

    @Query("SELECT * FROM attendance_sessions WHERE date = :date ORDER BY timestamp DESC")
    fun getSessionsByDate(date: String): Flow<List<AttendanceSession>>

    @Query("SELECT * FROM attendance_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Long): AttendanceSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AttendanceSession): Long

    @Delete
    suspend fun deleteSession(session: AttendanceSession)

    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId")
    fun getRecordsForSession(sessionId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId")
    suspend fun getRecordsForSessionList(sessionId: Long): List<AttendanceRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<AttendanceRecord>)

    @Query("DELETE FROM attendance_records WHERE sessionId = :sessionId")
    suspend fun deleteRecordsForSession(sessionId: Long)

    @Query("SELECT * FROM attendance_records")
    fun getAllRecords(): Flow<List<AttendanceRecord>>

    @Query("DELETE FROM attendance_sessions")
    suspend fun deleteAllSessions()

    @Query("DELETE FROM attendance_records")
    suspend fun deleteAllRecords()
}
