package com.example.data

import com.example.data.dao.AttendanceDao
import com.example.data.dao.ClassGroupDao
import com.example.data.dao.ScheduleDao
import com.example.data.dao.StudentDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceSession
import com.example.data.model.ClassGroup
import com.example.data.model.Student
import com.example.data.model.StudentSummary
import com.example.data.model.TeachingSchedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendanceRepository(
    private val classGroupDao: ClassGroupDao,
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao,
    private val scheduleDao: ScheduleDao
) {
    val allClasses: Flow<List<ClassGroup>> = classGroupDao.getAllClasses()
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()
    val totalStudentsCount: Flow<Int> = studentDao.getTotalStudentCount()
    val allSessions: Flow<List<AttendanceSession>> = attendanceDao.getAllSessions()
    val allRecords: Flow<List<AttendanceRecord>> = attendanceDao.getAllRecords()
    val allSchedules: Flow<List<TeachingSchedule>> = scheduleDao.getAllSchedules()

    fun getStudentsForClass(classId: Long): Flow<List<Student>> = studentDao.getStudentsByClass(classId)

    fun getSessionsForClass(classId: Long): Flow<List<AttendanceSession>> = attendanceDao.getSessionsByClass(classId)

    suspend fun getClassById(id: Long): ClassGroup? = classGroupDao.getClassById(id)

    suspend fun insertClass(classGroup: ClassGroup): Long = classGroupDao.insertClass(classGroup)

    suspend fun updateClass(classGroup: ClassGroup) = classGroupDao.updateClass(classGroup)

    suspend fun deleteClass(classGroup: ClassGroup) = classGroupDao.deleteClass(classGroup)

    suspend fun insertStudent(student: Student): Long = studentDao.insertStudent(student)

    suspend fun updateStudent(student: Student) = studentDao.updateStudent(student)

    suspend fun deleteStudent(student: Student) = studentDao.deleteStudent(student)

    suspend fun insertSchedule(schedule: TeachingSchedule): Long = scheduleDao.insertSchedule(schedule)

    suspend fun updateSchedule(schedule: TeachingSchedule) = scheduleDao.updateSchedule(schedule)

    suspend fun deleteSchedule(schedule: TeachingSchedule) = scheduleDao.deleteSchedule(schedule)

    suspend fun saveAttendanceSession(
        session: AttendanceSession,
        records: List<AttendanceRecord>
    ): Long {
        val sessionId = attendanceDao.insertSession(session)
        val recordsWithSession = records.map { it.copy(sessionId = sessionId) }
        attendanceDao.deleteRecordsForSession(sessionId)
        attendanceDao.insertRecords(recordsWithSession)
        return sessionId
    }

    suspend fun getRecordsForSessionList(sessionId: Long): List<AttendanceRecord> {
        return attendanceDao.getRecordsForSessionList(sessionId)
    }

    suspend fun deleteSession(session: AttendanceSession) {
        attendanceDao.deleteSession(session)
    }

    suspend fun getStudentSummariesForClass(classId: Long, startDateFilter: String? = null, subjectFilter: String? = null): List<StudentSummary> {
        val students = studentDao.getStudentsByClassList(classId)
        val allSessionsForClass = attendanceDao.getSessionsByClass(classId).first()
        val sessions = allSessionsForClass.filter { session ->
            val dateOk = startDateFilter.isNullOrBlank() || session.date >= startDateFilter
            val subjectOk = subjectFilter.isNullOrBlank() || 
                    subjectFilter.equals("Semua Mapel", ignoreCase = true) || 
                    session.subject.contains(subjectFilter, ignoreCase = true)
            dateOk && subjectOk
        }
        val allRecs = attendanceDao.getAllRecords().first()

        val sessionIds = sessions.map { it.id }.toSet()
        val classRecords = allRecs.filter { sessionIds.contains(it.sessionId) }

        return students.map { student ->
            val studentRecs = classRecords.filter { it.studentId == student.id }
            val hadir = studentRecs.count { it.status == "HADIR" }
            val izin = studentRecs.count { it.status == "IZIN" }
            val sakit = studentRecs.count { it.status == "SAKIT" }
            val alpa = studentRecs.count { it.status == "ALPA" }
            StudentSummary(
                student = student,
                totalHadir = hadir,
                totalIzin = izin,
                totalSakit = sakit,
                totalAlpa = alpa,
                totalSessions = sessions.size
            )
        }
    }

    suspend fun getAllCriticalAlpaStudents(threshold: Int = 3): List<com.example.data.model.CriticalStudentAlpaInfo> {
        val allClassesList = classGroupDao.getAllClasses().first()
        val resultList = mutableListOf<com.example.data.model.CriticalStudentAlpaInfo>()
        for (c in allClassesList) {
            val summaries = getStudentSummariesForClass(c.id)
            val critical = summaries.filter { it.totalAlpa >= threshold }
            for (sum in critical) {
                resultList.add(
                    com.example.data.model.CriticalStudentAlpaInfo(
                        summary = sum,
                        className = c.name,
                        classMajor = c.major
                    )
                )
            }
        }
        return resultList.sortedByDescending { it.summary.totalAlpa }
    }

    suspend fun seedSampleDataIfEmpty() {
        val existingClasses = classGroupDao.getAllClasses().first()
        if (existingClasses.isNotEmpty()) return

        val class1Id = classGroupDao.insertClass(
            ClassGroup(
                name = "X AKL 1",
                academicYear = "2025/2026",
                major = "AKL",
                description = "Akuntansi Keuangan Lembaga - Wali Kelas: Bu Sri Wahyuni, S.E."
            )
        )
        val class2Id = classGroupDao.insertClass(
            ClassGroup(
                name = "XI AKL 1",
                academicYear = "2025/2026",
                major = "AKL",
                description = "Akuntansi Keuangan Lembaga - Wali Kelas: Bp. Ahmad Dahlan, S.Pd."
            )
        )
        val class3Id = classGroupDao.insertClass(
            ClassGroup(
                name = "X MPLB 1",
                academicYear = "2025/2026",
                major = "MPLB",
                description = "Manajemen Perkantoran & Layanan Bisnis - Wali Kelas: Bu Ratna Dewi, S.Pd."
            )
        )
        val class4Id = classGroupDao.insertClass(
            ClassGroup(
                name = "XI MPLB 1",
                academicYear = "2025/2026",
                major = "MPLB",
                description = "Manajemen Perkantoran & Layanan Bisnis - Wali Kelas: Bp. Hendra, M.M."
            )
        )

        val students1 = listOf(
            Student(classId = class1Id, nisn = "0051234001", name = "Aditya Pratama", gender = "L"),
            Student(classId = class1Id, nisn = "0051234002", name = "Aulia Rahmawati", gender = "P"),
            Student(classId = class1Id, nisn = "0051234003", name = "Bagas Saputra", gender = "L"),
            Student(classId = class1Id, nisn = "0051234004", name = "Citra Kirana", gender = "P"),
            Student(classId = class1Id, nisn = "0051234005", name = "Daffa Ibnu", gender = "L"),
            Student(classId = class1Id, nisn = "0051234006", name = "Eka Nurhaliza", gender = "P"),
            Student(classId = class1Id, nisn = "0051234007", name = "Fajar Rizky", gender = "L"),
            Student(classId = class1Id, nisn = "0051234008", name = "Gita Savitri", gender = "P")
        )
        studentDao.insertStudents(students1)

        val students2 = listOf(
            Student(classId = class2Id, nisn = "0042234001", name = "Hendra Wijaya", gender = "L"),
            Student(classId = class2Id, nisn = "0042234002", name = "Intan Permata", gender = "P"),
            Student(classId = class2Id, nisn = "0042234003", name = "Joko Susilo", gender = "L"),
            Student(classId = class2Id, nisn = "0042234004", name = "Lani Febriani", gender = "P"),
            Student(classId = class2Id, nisn = "0042234005", name = "Muhammad Farhan", gender = "L"),
            Student(classId = class2Id, nisn = "0042234006", name = "Nabila Putri", gender = "P")
        )
        studentDao.insertStudents(students2)

        val students3 = listOf(
            Student(classId = class3Id, nisn = "0053234001", name = "Oky Setiawan", gender = "L"),
            Student(classId = class3Id, nisn = "0053234002", name = "Putri Anindya", gender = "P"),
            Student(classId = class3Id, nisn = "0053234003", name = "Rian Hidayat", gender = "L"),
            Student(classId = class3Id, nisn = "0053234004", name = "Siti Zulaikha", gender = "P"),
            Student(classId = class3Id, nisn = "0053234005", name = "Tari Anggraini", gender = "P")
        )
        studentDao.insertStudents(students3)

        val students4 = listOf(
            Student(classId = class4Id, nisn = "0044234001", name = "Umar Hasan", gender = "L"),
            Student(classId = class4Id, nisn = "0044234002", name = "Vina Melati", gender = "P"),
            Student(classId = class4Id, nisn = "0044234003", name = "Wahyudi", gender = "L"),
            Student(classId = class4Id, nisn = "0044234004", name = "Yulia Ningsih", gender = "P")
        )
        studentDao.insertStudents(students4)

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val sampleSession = AttendanceSession(
            classId = class1Id,
            date = todayStr,
            subject = "Akuntansi Keuangan",
            notes = "Pembahasan jurnal penyesuaian. Daffa izin, Bagas Tanpa Keterangan."
        )
        val sId = attendanceDao.insertSession(sampleSession)

        val insertedStudents1 = studentDao.getStudentsByClassList(class1Id)
        val sampleRecords = insertedStudents1.map { st ->
            val status = when {
                st.name.contains("Daffa") -> "IZIN"
                st.name.contains("Bagas") -> "ALPA"
                else -> "HADIR"
            }
            AttendanceRecord(
                sessionId = sId,
                studentId = st.id,
                status = status,
                note = if (st.name.contains("Daffa")) "Acara keluarga" else if (st.name.contains("Bagas")) "Tidak ada surat" else ""
            )
        }
        attendanceDao.insertRecords(sampleRecords)

        // Additional sample past sessions to demonstrate Alpa > 3x warning features
        val pastDates = listOf("2026-07-20", "2026-07-22", "2026-07-25")
        pastDates.forEachIndexed { idx, pDate ->
            val pSession = AttendanceSession(
                classId = class1Id,
                date = pDate,
                subject = if (idx % 2 == 0) "Akuntansi Keuangan" else "Perpajakan",
                notes = "Sesi pembelajaran rutin $pDate."
            )
            val psId = attendanceDao.insertSession(pSession)
            val pRecs = insertedStudents1.map { st ->
                AttendanceRecord(
                    sessionId = psId,
                    studentId = st.id,
                    status = if (st.name.contains("Bagas")) "ALPA" else "HADIR",
                    note = if (st.name.contains("Bagas")) "Tanpa keterangan" else ""
                )
            }
            attendanceDao.insertRecords(pRecs)
        }

        // Seed default teaching schedules
        val sampleSchedules = listOf(
            TeachingSchedule(dayOfWeek = "Senin", className = "X AKL 1", subject = "Akuntansi Keuangan", startTime = "07:00", endTime = "09:00", room = "Lab AKL 1"),
            TeachingSchedule(dayOfWeek = "Senin", className = "XI AKL 1", subject = "Praktikum Akuntansi", startTime = "09:15", endTime = "11:15", room = "Lab AKL 2"),
            TeachingSchedule(dayOfWeek = "Selasa", className = "X MPLB 1", subject = "Otomatisasi Perkantoran", startTime = "07:00", endTime = "09:00", room = "Lab Simulasi Digital"),
            TeachingSchedule(dayOfWeek = "Selasa", className = "XI MPLB 1", subject = "Kearsipan", startTime = "09:15", endTime = "11:15", room = "R. 102"),
            TeachingSchedule(dayOfWeek = "Rabu", className = "X AKL 1", subject = "Perpajakan", startTime = "07:00", endTime = "08:30", room = "R. 101"),
            TeachingSchedule(dayOfWeek = "Rabu", className = "XI MPLB 1", subject = "Humas & Protokol", startTime = "08:30", endTime = "10:30", room = "R. 103"),
            TeachingSchedule(dayOfWeek = "Kamis", className = "XI AKL 1", subject = "Spreadsheet", startTime = "07:00", endTime = "09:00", room = "Lab Komputer"),
            TeachingSchedule(dayOfWeek = "Jumat", className = "X MPLB 1", subject = "Kepegawaian", startTime = "07:00", endTime = "08:30", room = "R. 102")
        )
        sampleSchedules.forEach { scheduleDao.insertSchedule(it) }
    }
}
