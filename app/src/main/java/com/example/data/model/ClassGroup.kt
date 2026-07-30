package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "class_groups")
data class ClassGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val academicYear: String,
    val major: String = "AKL", // "AKL" or "MPLB"
    val description: String = ""
)
