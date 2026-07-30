package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teaching_schedules")
data class TeachingSchedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: String, // "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu"
    val className: String, // e.g. "XII AKL 1", "X MPLB 1"
    val subject: String, // e.g. "Akuntansi Keuangan"
    val startTime: String, // e.g. "07:00"
    val endTime: String, // e.g. "09:00"
    val room: String = "R. Kelas",
    val isAlarmEnabled: Boolean = true
)
