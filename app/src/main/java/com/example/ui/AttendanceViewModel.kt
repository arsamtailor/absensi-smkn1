package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AttendanceRepository
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceSession
import com.example.data.model.ClassGroup
import com.example.data.model.Student
import com.example.data.model.StudentSummary
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ReportPeriod(val label: String) {
    SEMUA("Semua"),
    MINGGUAN("Mingguan (7 Hari)"),
    BULANAN("Bulanan (30 Hari)"),
    SEMESTERAN("Semesteran (6 Bulan)")
}

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    val repository = AttendanceRepository(db.classGroupDao(), db.studentDao(), db.attendanceDao(), db.scheduleDao())

    val allClasses: StateFlow<List<ClassGroup>> = repository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudents: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalStudentsCount: StateFlow<Int> = repository.totalStudentsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allSessions: StateFlow<List<AttendanceSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecords: StateFlow<List<AttendanceRecord>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSchedules: StateFlow<List<com.example.data.model.TeachingSchedule>> = repository.allSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    private val _selectedClassForAttendance = MutableStateFlow<ClassGroup?>(null)
    val selectedClassForAttendance: StateFlow<ClassGroup?> = _selectedClassForAttendance.asStateFlow()

    private val _attendanceDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val attendanceDate: StateFlow<String> = _attendanceDate.asStateFlow()

    private val _attendanceSubject = MutableStateFlow("Absensi Harian")
    val attendanceSubject: StateFlow<String> = _attendanceSubject.asStateFlow()

    private val _attendanceNotes = MutableStateFlow("")
    val attendanceNotes: StateFlow<String> = _attendanceNotes.asStateFlow()

    private val _studentStatusMap = MutableStateFlow<Map<Long, String>>(emptyMap())
    val studentStatusMap: StateFlow<Map<Long, String>> = _studentStatusMap.asStateFlow()

    private val _studentNoteMap = MutableStateFlow<Map<Long, String>>(emptyMap())
    val studentNoteMap: StateFlow<Map<Long, String>> = _studentNoteMap.asStateFlow()

    private val _editingSessionId = MutableStateFlow<Long?>(null)
    val editingSessionId: StateFlow<Long?> = _editingSessionId.asStateFlow()

    private val _classStudentsForAttendance = MutableStateFlow<List<Student>>(emptyList())
    val classStudentsForAttendance: StateFlow<List<Student>> = _classStudentsForAttendance.asStateFlow()

    private val _reportSelectedClassId = MutableStateFlow<Long?>(null)
    val reportSelectedClassId: StateFlow<Long?> = _reportSelectedClassId.asStateFlow()

    private val _reportPeriod = MutableStateFlow(ReportPeriod.SEMUA)
    val reportPeriod: StateFlow<ReportPeriod> = _reportPeriod.asStateFlow()

    private val _reportSummaries = MutableStateFlow<List<StudentSummary>>(emptyList())
    val reportSummaries: StateFlow<List<StudentSummary>> = _reportSummaries.asStateFlow()

    private val _criticalAlpaStudents = MutableStateFlow<List<com.example.data.model.CriticalStudentAlpaInfo>>(emptyList())
    val criticalAlpaStudents: StateFlow<List<com.example.data.model.CriticalStudentAlpaInfo>> = _criticalAlpaStudents.asStateFlow()

    private val _autoDetectedScheduleSubject = MutableStateFlow<String?>(null)
    val autoDetectedScheduleSubject: StateFlow<String?> = _autoDetectedScheduleSubject.asStateFlow()

    private val _reportSelectedSubject = MutableStateFlow<String>("Semua Mapel")
    val reportSelectedSubject: StateFlow<String> = _reportSelectedSubject.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
            refreshCriticalAlpaStudents()
        }
    }

    fun refreshCriticalAlpaStudents() {
        viewModelScope.launch {
            val list = repository.getAllCriticalAlpaStudents(threshold = 3)
            _criticalAlpaStudents.value = list
        }
    }

    private fun getTodayIndonesianDayName(): String {
        val cal = java.util.Calendar.getInstance()
        return when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> "Senin"
            java.util.Calendar.TUESDAY -> "Selasa"
            java.util.Calendar.WEDNESDAY -> "Rabu"
            java.util.Calendar.THURSDAY -> "Kamis"
            java.util.Calendar.FRIDAY -> "Jumat"
            java.util.Calendar.SATURDAY -> "Sabtu"
            else -> "Minggu"
        }
    }

    fun getTeacherSubjectList(): List<String> {
        val raw = _teacherSubject.value
        val items = raw.split(",", ";", "\n", "•", "/")
            .map { it.trim() }
            .filter { it.isNotBlank() }
        return if (items.isEmpty()) listOf("Absensi Harian") else items
    }

    fun selectClassForAttendance(classGroup: ClassGroup) {
        _selectedClassForAttendance.value = classGroup
        _editingSessionId.value = null
        _attendanceNotes.value = ""
        _studentStatusMap.value = emptyMap()
        _studentNoteMap.value = emptyMap()

        val todayDay = getTodayIndonesianDayName()
        val todaySchedules = allSchedules.value.filter {
            it.dayOfWeek.equals(todayDay, ignoreCase = true) &&
                    (it.className.contains(classGroup.name, ignoreCase = true) || classGroup.name.contains(it.className, ignoreCase = true))
        }

        if (todaySchedules.isNotEmpty()) {
            val matchedSchedule = todaySchedules.first()
            _autoDetectedScheduleSubject.value = matchedSchedule.subject
            _attendanceSubject.value = matchedSchedule.subject
        } else {
            _autoDetectedScheduleSubject.value = null
            val teacherSubjects = getTeacherSubjectList()
            _attendanceSubject.value = if (teacherSubjects.isNotEmpty()) teacherSubjects.first() else "Absensi Harian"
        }

        viewModelScope.launch {
            repository.getStudentsForClass(classGroup.id).collect { students ->
                _classStudentsForAttendance.value = students
                val currentMap = _studentStatusMap.value
                val initialMap = students.associate { st ->
                    st.id to (currentMap[st.id] ?: "HADIR")
                }
                _studentStatusMap.value = initialMap
            }
        }
    }

    fun loadExistingSessionForEdit(session: AttendanceSession) {
        viewModelScope.launch {
            val classGroup = repository.getClassById(session.classId)
            if (classGroup != null) {
                _selectedClassForAttendance.value = classGroup
                _editingSessionId.value = session.id
                _attendanceDate.value = session.date
                _attendanceSubject.value = session.subject
                _attendanceNotes.value = session.notes

                val records = repository.getRecordsForSessionList(session.id)
                val statusMap = records.associate { it.studentId to it.status }
                val noteMap = records.associate { it.studentId to it.note }

                _studentStatusMap.value = statusMap
                _studentNoteMap.value = noteMap

                repository.getStudentsForClass(classGroup.id).collect { students ->
                    _classStudentsForAttendance.value = students
                }
            }
        }
    }

    fun updateStudentStatus(studentId: Long, status: String) {
        val updated = _studentStatusMap.value.toMutableMap()
        updated[studentId] = status
        _studentStatusMap.value = updated
    }

    fun updateStudentNote(studentId: Long, note: String) {
        val updated = _studentNoteMap.value.toMutableMap()
        updated[studentId] = note
        _studentNoteMap.value = updated
    }

    fun markAllStudents(status: String) {
        val students = _classStudentsForAttendance.value
        val updated = students.associate { it.id to status }
        _studentStatusMap.value = updated
    }

    fun setAttendanceDate(date: String) {
        _attendanceDate.value = date
    }

    fun setAttendanceSubject(subject: String) {
        _attendanceSubject.value = subject
    }

    fun setAttendanceNotes(notes: String) {
        _attendanceNotes.value = notes
    }

    fun saveAttendanceSession(onSuccess: () -> Unit) {
        val classGroup = _selectedClassForAttendance.value ?: return
        val students = _classStudentsForAttendance.value
        if (students.isEmpty()) {
            viewModelScope.launch { _snackbarMessage.emit("Tidak ada siswa di kelas ini.") }
            return
        }

        viewModelScope.launch {
            val session = AttendanceSession(
                id = _editingSessionId.value ?: 0,
                classId = classGroup.id,
                date = _attendanceDate.value,
                subject = _attendanceSubject.value.ifBlank { "Absensi Harian" },
                notes = _attendanceNotes.value
            )

            val statusMap = _studentStatusMap.value
            val noteMap = _studentNoteMap.value

            val records = students.map { st ->
                AttendanceRecord(
                    sessionId = 0,
                    studentId = st.id,
                    status = statusMap[st.id] ?: "HADIR",
                    note = noteMap[st.id] ?: ""
                )
            }

            repository.saveAttendanceSession(session, records)
            refreshCriticalAlpaStudents()
            _snackbarMessage.emit("Absensi berhasil disimpan!")
            onSuccess()
        }
    }

    fun deleteSession(session: AttendanceSession) {
        viewModelScope.launch {
            repository.deleteSession(session)
            refreshCriticalAlpaStudents()
            _snackbarMessage.emit("Sesi absensi dihapus.")
        }
    }

    fun addOrUpdateClass(id: Long, name: String, academicYear: String, major: String = "AKL", description: String = "") {
        if (name.isBlank()) return
        viewModelScope.launch {
            val classGroup = ClassGroup(id = id, name = name.trim(), academicYear = academicYear.trim(), major = major.trim(), description = description.trim())
            if (id == 0L) {
                repository.insertClass(classGroup)
                _snackbarMessage.emit("Kelas ${name} berhasil ditambahkan.")
            } else {
                repository.updateClass(classGroup)
                _snackbarMessage.emit("Kelas ${name} berhasil diperbarui.")
            }
        }
    }

    fun deleteClass(classGroup: ClassGroup) {
        viewModelScope.launch {
            repository.deleteClass(classGroup)
            _snackbarMessage.emit("Kelas ${classGroup.name} dihapus.")
        }
    }

    fun addOrUpdateStudent(id: Long, classId: Long, nisn: String, name: String, gender: String, phone: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val student = Student(id = id, classId = classId, nisn = nisn.trim(), name = name.trim(), gender = gender, phone = phone.trim())
            if (id == 0L) {
                repository.insertStudent(student)
                _snackbarMessage.emit("Siswa ${name} berhasil ditambahkan.")
            } else {
                repository.updateStudent(student)
                _snackbarMessage.emit("Data siswa ${name} diperbarui.")
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            _snackbarMessage.emit("Siswa ${student.name} dihapus.")
        }
    }

    fun addOrUpdateSchedule(
        id: Long,
        dayOfWeek: String,
        className: String,
        subject: String,
        startTime: String,
        endTime: String,
        room: String,
        isAlarmEnabled: Boolean = true
    ) {
        if (className.isBlank() || subject.isBlank()) return
        viewModelScope.launch {
            val schedule = com.example.data.model.TeachingSchedule(
                id = id,
                dayOfWeek = dayOfWeek,
                className = className.trim(),
                subject = subject.trim(),
                startTime = startTime.trim(),
                endTime = endTime.trim(),
                room = room.trim(),
                isAlarmEnabled = isAlarmEnabled
            )
            if (id == 0L) {
                repository.insertSchedule(schedule)
                _snackbarMessage.emit("Jadwal $className ($subject) ditambahkan.")
            } else {
                repository.updateSchedule(schedule)
                _snackbarMessage.emit("Jadwal $className diperbarui.")
            }
        }
    }

    fun deleteSchedule(schedule: com.example.data.model.TeachingSchedule) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
            _snackbarMessage.emit("Jadwal ${schedule.className} - ${schedule.subject} dihapus.")
        }
    }

    fun toggleScheduleAlarm(schedule: com.example.data.model.TeachingSchedule) {
        viewModelScope.launch {
            val updated = schedule.copy(isAlarmEnabled = !schedule.isAlarmEnabled)
            repository.updateSchedule(updated)
            val statusStr = if (updated.isAlarmEnabled) "diaktifkan" else "dinonaktifkan"
            _snackbarMessage.emit("Alarm jadwal ${schedule.className} $statusStr.")
        }
    }

    fun setReportPeriod(period: ReportPeriod) {
        _reportPeriod.value = period
        val currentClassId = _reportSelectedClassId.value
        if (currentClassId != null) {
            loadReportForClass(currentClassId, period, _reportSelectedSubject.value)
        }
    }

    fun setReportSubjectFilter(subjectFilter: String) {
        _reportSelectedSubject.value = subjectFilter
        val currentClassId = _reportSelectedClassId.value
        if (currentClassId != null) {
            loadReportForClass(currentClassId, _reportPeriod.value, subjectFilter)
        }
    }

    fun loadReportForClass(classId: Long, period: ReportPeriod = _reportPeriod.value, subjectFilter: String = _reportSelectedSubject.value) {
        _reportSelectedClassId.value = classId
        _reportPeriod.value = period
        _reportSelectedSubject.value = subjectFilter
        viewModelScope.launch {
            val startDateFilter = getStartDateForPeriod(period)
            val summaries = repository.getStudentSummariesForClass(classId, startDateFilter, subjectFilter)
            _reportSummaries.value = summaries
        }
    }

    private val prefs = application.getSharedPreferences("app_security_prefs", android.content.Context.MODE_PRIVATE)

    private val _teacherName = MutableStateFlow(prefs.getString("teacher_name", "Ahmad Sanusi, S.Pd") ?: "Ahmad Sanusi, S.Pd")
    val teacherName: StateFlow<String> = _teacherName.asStateFlow()

    private val _teacherNip = MutableStateFlow(prefs.getString("teacher_nip", "19850315 201001 1 002") ?: "19850315 201001 1 002")
    val teacherNip: StateFlow<String> = _teacherNip.asStateFlow()

    private val _teacherSubject = MutableStateFlow(prefs.getString("teacher_subject", "Guru Pengampu AKL & MPLB") ?: "Guru Pengampu AKL & MPLB")
    val teacherSubject: StateFlow<String> = _teacherSubject.asStateFlow()

    private val _schoolName = MutableStateFlow(prefs.getString("school_name", "SMKN 1 Cirinten") ?: "SMKN 1 Cirinten")
    val schoolName: StateFlow<String> = _schoolName.asStateFlow()

    fun updateTeacherProfile(name: String, nip: String, subject: String, school: String) {
        prefs.edit()
            .putString("teacher_name", name)
            .putString("teacher_nip", nip)
            .putString("teacher_subject", subject)
            .putString("school_name", school)
            .apply()

        _teacherName.value = name
        _teacherNip.value = nip
        _teacherSubject.value = subject
        _schoolName.value = school

        viewModelScope.launch {
            _snackbarMessage.emit("Profil Guru berhasil diperbarui.")
        }
    }

    private val _savedPin = MutableStateFlow(prefs.getString("app_pin", "") ?: "")
    val savedPin: StateFlow<String> = _savedPin.asStateFlow()

    private val _isAppLocked = MutableStateFlow(prefs.getString("app_pin", "").orEmpty().isNotBlank())
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    fun unlockApp(pinInput: String): Boolean {
        if (pinInput == _savedPin.value) {
            _isAppLocked.value = false
            return true
        }
        return false
    }

    fun lockApp() {
        if (_savedPin.value.isNotBlank()) {
            _isAppLocked.value = true
        }
    }

    fun setSecurityPin(newPin: String) {
        prefs.edit().putString("app_pin", newPin).apply()
        _savedPin.value = newPin
        if (newPin.isBlank()) {
            _isAppLocked.value = false
        }
        viewModelScope.launch {
            _snackbarMessage.emit(if (newPin.isBlank()) "Kunci PIN Aplikasi dinonaktifkan." else "PIN Keamanan Aplikasi berhasil diperbarui.")
        }
    }

    suspend fun generateBackupJson(): String {
        return com.example.util.BackupManager.exportFullBackupJson(db)
    }

    fun restoreBackup(jsonString: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = com.example.util.BackupManager.importFullBackupJson(db, jsonString)
            if (success) {
                _snackbarMessage.emit("Data berhasil dipulihkan dari backup.")
            } else {
                _snackbarMessage.emit("Gagal memulihkan data. Format file tidak valid.")
            }
            onResult(success)
        }
    }

    private fun getStartDateForPeriod(period: ReportPeriod): String? {
        val cal = java.util.Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return when (period) {
            ReportPeriod.SEMUA -> null
            ReportPeriod.MINGGUAN -> {
                cal.add(java.util.Calendar.DAY_OF_YEAR, -7)
                sdf.format(cal.time)
            }
            ReportPeriod.BULANAN -> {
                cal.add(java.util.Calendar.DAY_OF_YEAR, -30)
                sdf.format(cal.time)
            }
            ReportPeriod.SEMESTERAN -> {
                cal.add(java.util.Calendar.DAY_OF_YEAR, -180)
                sdf.format(cal.time)
            }
        }
    }
}
