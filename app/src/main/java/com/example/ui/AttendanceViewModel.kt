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

    private val prefs = application.getSharedPreferences("app_security_prefs", android.content.Context.MODE_PRIVATE)

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

    private val _attendanceTeachingTopic = MutableStateFlow("")
    val attendanceTeachingTopic: StateFlow<String> = _attendanceTeachingTopic.asStateFlow()

    private val _attendanceTeachingNotes = MutableStateFlow("")
    val attendanceTeachingNotes: StateFlow<String> = _attendanceTeachingNotes.asStateFlow()

    private val _studentStatusMap = MutableStateFlow<Map<Long, String>>(emptyMap())
    val studentStatusMap: StateFlow<Map<Long, String>> = _studentStatusMap.asStateFlow()

    private val _studentNoteMap = MutableStateFlow<Map<Long, String>>(emptyMap())
    val studentNoteMap: StateFlow<Map<Long, String>> = _studentNoteMap.asStateFlow()

    private val _studentDisciplineNoteMap = MutableStateFlow<Map<Long, String>>(emptyMap())
    val studentDisciplineNoteMap: StateFlow<Map<Long, String>> = _studentDisciplineNoteMap.asStateFlow()

    private val _studentPointImpactMap = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val studentPointImpactMap: StateFlow<Map<Long, Int>> = _studentPointImpactMap.asStateFlow()

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

    // Master Jurusan Management
    private fun loadMajorsFromPrefs(): List<String> {
        val saved = prefs.getString("all_majors", null)
        if (saved.isNullOrBlank()) {
            return listOf("AKL", "MPLB")
        }
        return saved.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    private val _majorsList = MutableStateFlow<List<String>>(loadMajorsFromPrefs())
    val majorsList: StateFlow<List<String>> = _majorsList.asStateFlow()

    fun addMajor(newMajor: String) {
        val trimmed = newMajor.trim().uppercase()
        if (trimmed.isBlank()) return
        val current = _majorsList.value.toMutableList()
        if (!current.contains(trimmed)) {
            current.add(trimmed)
            _majorsList.value = current
            prefs.edit().putString("all_majors", current.joinToString(",")).apply()
            viewModelScope.launch { _snackbarMessage.emit("Jurusan $trimmed berhasil ditambahkan.") }
        } else {
            viewModelScope.launch { _snackbarMessage.emit("Jurusan $trimmed sudah ada.") }
        }
    }

    fun editMajor(oldMajor: String, newMajor: String) {
        val trimmed = newMajor.trim().uppercase()
        if (trimmed.isBlank() || oldMajor == trimmed) return
        val current = _majorsList.value.toMutableList()
        val index = current.indexOf(oldMajor)
        if (index != -1) {
            current[index] = trimmed
            _majorsList.value = current
            prefs.edit().putString("all_majors", current.joinToString(",")).apply()

            viewModelScope.launch {
                val classesToUpdate = allClasses.value.filter { it.major.equals(oldMajor, ignoreCase = true) }
                classesToUpdate.forEach { cg ->
                    repository.updateClass(cg.copy(major = trimmed))
                }
                _snackbarMessage.emit("Jurusan $oldMajor diubah menjadi $trimmed.")
            }
        }
    }

    fun deleteMajor(majorToDelete: String) {
        val current = _majorsList.value.toMutableList()
        if (current.size <= 1) {
            viewModelScope.launch { _snackbarMessage.emit("Minimal harus ada 1 jurusan terdaftar.") }
            return
        }
        if (current.remove(majorToDelete)) {
            _majorsList.value = current
            prefs.edit().putString("all_majors", current.joinToString(",")).apply()
            viewModelScope.launch { _snackbarMessage.emit("Jurusan $majorToDelete berhasil dihapus.") }
        }
    }

    init {
        viewModelScope.launch {
            val resetDone = prefs.getBoolean("data_reset_done", false)
            if (!resetDone) {
                repository.seedSampleDataIfEmpty()
            }
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
            .filter { it.isNotBlank() && !it.startsWith("Guru Pengampu", ignoreCase = true) }
            .distinct()

        if (items.isNotEmpty()) {
            return items
        }

        val scheduleSubjects = allSchedules.value.map { it.subject.trim() }.filter { it.isNotBlank() }.distinct()
        if (scheduleSubjects.isNotEmpty()) {
            return scheduleSubjects
        }

        return listOf("Akuntansi Keuangan", "Praktikum Akuntansi Perusahaan", "Otomatisasi Tata Kelola Perkantoran", "Matematika", "Bahasa Indonesia", "Bahasa Inggris")
    }

    fun addTeacherSubject(newSubject: String) {
        val trimmed = newSubject.trim()
        if (trimmed.isBlank()) return
        val currentSubjects = getTeacherSubjectList().toMutableList()
        if (!currentSubjects.contains(trimmed)) {
            currentSubjects.add(trimmed)
            updateTeacherProfile(
                name = _teacherName.value,
                nip = _teacherNip.value,
                subject = currentSubjects.joinToString(", "),
                school = _schoolName.value
            )
        }
    }

    fun removeTeacherSubject(subjectToRemove: String) {
        val trimmed = subjectToRemove.trim()
        val currentSubjects = getTeacherSubjectList().toMutableList()
        currentSubjects.remove(trimmed)
        val newSubjectStr = if (currentSubjects.isEmpty()) "Absensi Harian" else currentSubjects.joinToString(", ")
        updateTeacherProfile(
            name = _teacherName.value,
            nip = _teacherNip.value,
            subject = newSubjectStr,
            school = _schoolName.value
        )
    }

    fun selectClassForAttendance(classGroup: ClassGroup) {
        _selectedClassForAttendance.value = classGroup
        _editingSessionId.value = null
        _attendanceNotes.value = ""
        _attendanceTeachingTopic.value = ""
        _attendanceTeachingNotes.value = ""
        _studentStatusMap.value = emptyMap()
        _studentNoteMap.value = emptyMap()
        _studentDisciplineNoteMap.value = emptyMap()
        _studentPointImpactMap.value = emptyMap()

        val todayDay = getTodayIndonesianDayName()
        val classSchedules = allSchedules.value.filter {
            it.className.contains(classGroup.name, ignoreCase = true) || classGroup.name.contains(it.className, ignoreCase = true)
        }
        val todaySchedules = classSchedules.filter {
            it.dayOfWeek.equals(todayDay, ignoreCase = true)
        }

        if (todaySchedules.isNotEmpty()) {
            val matchedSchedule = todaySchedules.first()
            _autoDetectedScheduleSubject.value = matchedSchedule.subject
            _attendanceSubject.value = matchedSchedule.subject
        } else if (classSchedules.isNotEmpty()) {
            _autoDetectedScheduleSubject.value = null
            _attendanceSubject.value = classSchedules.first().subject
        } else {
            _autoDetectedScheduleSubject.value = null
            _attendanceSubject.value = ""
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
                _attendanceTeachingTopic.value = session.teachingTopic
                _attendanceTeachingNotes.value = session.teachingNotes

                val records = repository.getRecordsForSessionList(session.id)
                val statusMap = records.associate { it.studentId to it.status }
                val noteMap = records.associate { it.studentId to it.note }
                val discNoteMap = records.associate { it.studentId to it.disciplineNote }
                val pointImpactMap = records.associate { it.studentId to it.pointImpact }

                _studentStatusMap.value = statusMap
                _studentNoteMap.value = noteMap
                _studentDisciplineNoteMap.value = discNoteMap
                _studentPointImpactMap.value = pointImpactMap

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

    fun updateStudentDisciplineNote(studentId: Long, note: String, pointImpact: Int = 0) {
        val updatedNoteMap = _studentDisciplineNoteMap.value.toMutableMap()
        updatedNoteMap[studentId] = note
        _studentDisciplineNoteMap.value = updatedNoteMap

        val updatedPointMap = _studentPointImpactMap.value.toMutableMap()
        updatedPointMap[studentId] = pointImpact
        _studentPointImpactMap.value = updatedPointMap
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

    fun setAttendanceTeachingTopic(topic: String) {
        _attendanceTeachingTopic.value = topic
    }

    fun setAttendanceTeachingNotes(notes: String) {
        _attendanceTeachingNotes.value = notes
    }

    fun generateWhatsAppSummaryMessage(
        overrideClassName: String? = null,
        overrideDateStr: String? = null,
        overrideSubjectStr: String? = null,
        overrideTopicStr: String? = null,
        overrideStudents: List<Student>? = null,
        overrideStatusMap: Map<Long, String>? = null,
        overrideNoteMap: Map<Long, String>? = null
    ): String {
        val className = overrideClassName ?: _selectedClassForAttendance.value?.name ?: "Kelas"
        val dateStr = overrideDateStr ?: _attendanceDate.value
        val subjectName = overrideSubjectStr ?: _attendanceSubject.value
        val teachingTopicStr = overrideTopicStr ?: _attendanceTeachingTopic.value
        val studentsList = overrideStudents ?: _classStudentsForAttendance.value
        val statusMapData = overrideStatusMap ?: _studentStatusMap.value
        val noteMapData = overrideNoteMap ?: _studentNoteMap.value

        val teacherNameStr = _teacherName.value
        val schoolNameStr = _schoolName.value

        val totalHadir = studentsList.count { (statusMapData[it.id] ?: "HADIR").equals("HADIR", true) }
        val totalIzin = studentsList.count { (statusMapData[it.id] ?: "HADIR").equals("IZIN", true) }
        val totalSakit = studentsList.count { (statusMapData[it.id] ?: "HADIR").equals("SAKIT", true) }
        val totalAlpa = studentsList.count { (statusMapData[it.id] ?: "HADIR").equals("ALPA", true) }

        val absentStudents = studentsList.filter {
            val st = statusMapData[it.id] ?: "HADIR"
            st.equals("IZIN", true) || st.equals("SAKIT", true) || st.equals("ALPA", true)
        }

        val sb = StringBuilder()
        sb.append("📋 *REKAP PRESENSI KELAS*\n")
        if (schoolNameStr.isNotBlank()) sb.append("🏫 *${schoolNameStr.trim()}*\n")
        sb.append("🏫 Kelas: *${className}*\n")
        sb.append("📅 Tanggal: *${dateStr}*\n")
        sb.append("📚 Mata Pelajaran: *${subjectName}*\n")
        if (teacherNameStr.isNotBlank()) sb.append("👨‍🏫 Guru: *${teacherNameStr.trim()}*\n")
        sb.append("\n📊 *RINGKASAN KEHADIRAN:*\n")
        sb.append("• Hadir: $totalHadir siswa\n")
        sb.append("• Sakit: $totalSakit siswa\n")
        sb.append("• Izin: $totalIzin siswa\n")
        sb.append("• Alpa (Tanpa Ket): $totalAlpa siswa\n")
        sb.append("• Total Siswa: ${studentsList.size} siswa\n")

        if (teachingTopicStr.isNotBlank()) {
            sb.append("\n📖 *Jurnal / Materi Pembelajaran:* \n$teachingTopicStr\n")
        }

        if (absentStudents.isNotEmpty()) {
            sb.append("\n⚠️ *DAFTAR SISWA TIDAK HADIR / KETERANGAN:*\n")
            absentStudents.forEachIndexed { idx, st ->
                val stStatus = statusMapData[st.id] ?: "HADIR"
                val stNote = noteMapData[st.id] ?: ""
                val noteDesc = if (stNote.isNotBlank()) " ($stNote)" else ""
                sb.append("${idx + 1}. *${st.name}* -> [ $stStatus ]$noteDesc\n")
            }
        } else {
            sb.append("\n✨ *Alhamdulillah, seluruh siswa HADIR LENGKAP (100%)!*\n")
        }

        sb.append("\n_Pesan ini dikirim otomatis via Aplikasi Presensi Guru Offline._")
        return sb.toString()
    }

    fun saveAttendanceSession(onSuccess: () -> Unit) {
        val classGroup = _selectedClassForAttendance.value ?: return
        val students = _classStudentsForAttendance.value
        if (students.isEmpty()) {
            viewModelScope.launch { _snackbarMessage.emit("Tidak ada siswa di kelas ini.") }
            return
        }

        viewModelScope.launch {
            val finalSubject = _attendanceSubject.value.ifBlank { "Absensi Harian" }
            addTeacherSubject(finalSubject)

            val session = AttendanceSession(
                id = _editingSessionId.value ?: 0,
                classId = classGroup.id,
                date = _attendanceDate.value,
                subject = finalSubject,
                notes = _attendanceNotes.value,
                teachingTopic = _attendanceTeachingTopic.value,
                teachingNotes = _attendanceTeachingNotes.value,
                academicYear = _activeAcademicYear.value,
                semester = _activeSemester.value
            )

            val statusMap = _studentStatusMap.value
            val noteMap = _studentNoteMap.value
            val discNoteMap = _studentDisciplineNoteMap.value
            val pointImpactMap = _studentPointImpactMap.value

            val records = students.map { st ->
                AttendanceRecord(
                    sessionId = 0,
                    studentId = st.id,
                    status = statusMap[st.id] ?: "HADIR",
                    note = noteMap[st.id] ?: "",
                    disciplineNote = discNoteMap[st.id] ?: "",
                    pointImpact = pointImpactMap[st.id] ?: 0
                )
            }

            // Update student discipline points if point impacts were assigned
            students.forEach { st ->
                val impact = pointImpactMap[st.id] ?: 0
                val discNote = discNoteMap[st.id] ?: ""
                if (impact != 0 || discNote.isNotBlank()) {
                    val newPoints = (st.disciplinePoints + impact).coerceIn(0, 100)
                    val newNotes = if (discNote.isNotBlank()) "${st.disciplineNotes}\n[${_attendanceDate.value}] $discNote ($impact Poin)".trim() else st.disciplineNotes
                    repository.updateStudent(st.copy(disciplinePoints = newPoints, disciplineNotes = newNotes))
                }
            }

            repository.saveAttendanceSession(session, records)
            refreshCriticalAlpaStudents()
            _snackbarMessage.emit("Absensi & Jurnal Mengajar berhasil disimpan secara offline!")
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

    fun importStudentsBatch(classId: Long, rawText: String, onComplete: (Int) -> Unit = {}) {
        if (rawText.isBlank()) return
        viewModelScope.launch {
            val lines = rawText.split("\n", "\r\n")
            var countInserted = 0
            lines.forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotBlank()) {
                    val parts = trimmed.split(",", ";", "\t").map { it.trim() }
                    val name = parts.getOrNull(0) ?: ""
                    if (name.isNotBlank()) {
                        val nisn = parts.getOrNull(1) ?: ""
                        val genderRaw = parts.getOrNull(2)?.uppercase() ?: "L"
                        val gender = if (genderRaw.startsWith("P") || genderRaw == "PEREMPUAN") "P" else "L"
                        val phone = parts.getOrNull(3) ?: ""

                        repository.insertStudent(
                            Student(
                                id = 0L,
                                classId = classId,
                                nisn = nisn,
                                name = name,
                                gender = gender,
                                phone = phone
                            )
                        )
                        countInserted++
                    }
                }
            }
            _snackbarMessage.emit("$countInserted siswa berhasil diimpor masal!")
            onComplete(countInserted)
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
        addTeacherSubject(subject.trim())
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

    private val _teacherName = MutableStateFlow(prefs.getString("teacher_name", "Ahmad Sanusi, S.Pd") ?: "Ahmad Sanusi, S.Pd")
    val teacherName: StateFlow<String> = _teacherName.asStateFlow()

    private val _teacherNip = MutableStateFlow(prefs.getString("teacher_nip", "19850315 201001 1 002") ?: "19850315 201001 1 002")
    val teacherNip: StateFlow<String> = _teacherNip.asStateFlow()

    private val _teacherSubject = MutableStateFlow(prefs.getString("teacher_subject", "Guru Pengampu AKL & MPLB") ?: "Guru Pengampu AKL & MPLB")
    val teacherSubject: StateFlow<String> = _teacherSubject.asStateFlow()

    private val _schoolName = MutableStateFlow(prefs.getString("school_name", "SMKN 1 Cirinten") ?: "SMKN 1 Cirinten")
    val schoolName: StateFlow<String> = _schoolName.asStateFlow()

    private val _activeAcademicYear = MutableStateFlow(prefs.getString("active_academic_year", "2025/2026") ?: "2025/2026")
    val activeAcademicYear: StateFlow<String> = _activeAcademicYear.asStateFlow()

    private val _activeSemester = MutableStateFlow(prefs.getString("active_semester", "Ganjil") ?: "Ganjil")
    val activeSemester: StateFlow<String> = _activeSemester.asStateFlow()

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

    fun updateActiveAcademicPeriod(year: String, semester: String) {
        prefs.edit()
            .putString("active_academic_year", year)
            .putString("active_semester", semester)
            .apply()

        _activeAcademicYear.value = year
        _activeSemester.value = semester

        viewModelScope.launch {
            _snackbarMessage.emit("Tahun Ajaran ($year) & Semester ($semester) diaktifkan!")
        }
    }

    fun archiveAcademicYearAndSwitch(newYear: String, newSemester: String) {
        prefs.edit()
            .putString("active_academic_year", newYear)
            .putString("active_semester", newSemester)
            .apply()

        _activeAcademicYear.value = newYear
        _activeSemester.value = newSemester

        viewModelScope.launch {
            _snackbarMessage.emit("Berhasil ganti ke TA $newYear ($newSemester)! Data semester lalu tersimpan rapi di Arsip.")
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

    fun quickUpdateStudentRecord(sessionId: Long, studentId: Long, newStatus: String, newNote: String) {
        viewModelScope.launch {
            repository.quickUpdateStudentRecord(sessionId, studentId, newStatus, newNote)
            refreshCriticalAlpaStudents()
            val currentClassId = _reportSelectedClassId.value
            if (currentClassId != null) {
                loadReportForClass(currentClassId, _reportPeriod.value, _reportSelectedSubject.value)
            }
            _snackbarMessage.emit("Presensi siswa berhasil dikoreksi!")
        }
    }

    fun resetAllApplicationData() {
        viewModelScope.launch {
            prefs.edit().putBoolean("data_reset_done", true).apply()
            repository.clearAllData()
            _selectedClassForAttendance.value = null
            _editingSessionId.value = null
            _reportSelectedClassId.value = null
            _reportSummaries.value = emptyList()
            _criticalAlpaStudents.value = emptyList()
            _studentStatusMap.value = emptyMap()
            _studentNoteMap.value = emptyMap()
            _snackbarMessage.emit("Seluruh data aplikasi berhasil dikosongkan. Siap untuk input kelas & siswa baru.")
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
