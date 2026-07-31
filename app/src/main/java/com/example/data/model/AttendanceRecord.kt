package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = AttendanceSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"]), Index(value = ["studentId"])]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val studentId: Long,
    val status: String, // HADIR, IZIN, SAKIT, ALPA
    val note: String = "",
    val disciplineNote: String = "",
    val pointImpact: Int = 0
)
