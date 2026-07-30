package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ClassGroup::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["classId"])]
)
data class AttendanceSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val classId: Long,
    val date: String, // YYYY-MM-DD
    val subject: String,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
