package com.example.data.model

data class StudentSummary(
    val student: Student,
    val totalHadir: Int = 0,
    val totalIzin: Int = 0,
    val totalSakit: Int = 0,
    val totalAlpa: Int = 0,
    val totalSessions: Int = 0
) {
    val attendancePercentage: Float
        get() = if (totalSessions > 0) (totalHadir.toFloat() / totalSessions.toFloat()) * 100f else 0f
}

data class CriticalStudentAlpaInfo(
    val summary: StudentSummary,
    val className: String,
    val classMajor: String
) {
    val warningLevel: String
        get() = when {
            summary.totalAlpa >= 6 -> "SP 3 (Panggilan Orang Tua & BK)"
            summary.totalAlpa >= 4 -> "SP 2 (Peringatan Keras)"
            summary.totalAlpa >= 3 -> "SP 1 (Peringatan Pertama)"
            else -> "Perhatian"
        }
}
