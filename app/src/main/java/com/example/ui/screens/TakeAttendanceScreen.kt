package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClassGroup
import com.example.data.model.Student
import com.example.ui.AttendanceViewModel
import com.example.ui.components.AttendanceStatusBadge
import com.example.ui.components.StatusSelectorGroup
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeAttendanceScreen(
    viewModel: AttendanceViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedClass by viewModel.selectedClassForAttendance.collectAsState()
    val date by viewModel.attendanceDate.collectAsState()
    val subject by viewModel.attendanceSubject.collectAsState()
    val notes by viewModel.attendanceNotes.collectAsState()
    val statusMap by viewModel.studentStatusMap.collectAsState()
    val noteMap by viewModel.studentNoteMap.collectAsState()
    val students by viewModel.classStudentsForAttendance.collectAsState()
    val editingSessionId by viewModel.editingSessionId.collectAsState()
    val autoDetectedScheduleSubject by viewModel.autoDetectedScheduleSubject.collectAsState()
    val rawTeacherSubject by viewModel.teacherSubject.collectAsState()
    val teacherSubjects = remember(rawTeacherSubject) { viewModel.getTeacherSubjectList() }

    val context = LocalContext.current

    val totalHadir = students.count { (statusMap[it.id] ?: "HADIR").equals("HADIR", ignoreCase = true) }
    val totalIzin = students.count { (statusMap[it.id] ?: "HADIR").equals("IZIN", ignoreCase = true) }
    val totalSakit = students.count { (statusMap[it.id] ?: "HADIR").equals("SAKIT", ignoreCase = true) }
    val totalAlpa = students.count { (statusMap[it.id] ?: "HADIR").equals("ALPA", ignoreCase = true) }

    var studentForNoteDialog by remember { mutableStateOf<Student?>(null) }

    // Date Picker Dialog Launcher
    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                viewModel.setAttendanceDate(sdf.format(selectedCal.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (editingSessionId != null) "Edit Presensi" else "Input Presensi Kelas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = selectedClass?.name ?: "Pilih Kelas",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveAttendanceSession(onSuccess = onNavigateBack)
                        },
                        modifier = Modifier.testTag("save_attendance_appbar_btn")
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.markAllStudents("HADIR") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mark_all_present_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hadir Semua")
                    }

                    Button(
                        onClick = {
                            viewModel.saveAttendanceSession(onSuccess = onNavigateBack)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_attendance_bottom_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Data")
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (selectedClass == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Pilih kelas terlebih dahulu untuk mengisi presensi.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp)
            ) {
                // Session Configuration Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Detail Sesi",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                OutlinedButton(
                                    onClick = { datePickerDialog.show() },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(date, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (autoDetectedScheduleSubject != null) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Text(
                                                text = "Mapel Otomatis: $autoDetectedScheduleSubject (Terdeteksi dari Jadwal)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = subject,
                                    onValueChange = { viewModel.setAttendanceSubject(it) },
                                    label = { Text("Mata Pelajaran / Sesi") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("attendance_subject_input"),
                                    singleLine = true
                                )

                                if (teacherSubjects.isNotEmpty()) {
                                    Text(
                                        text = "Pilih Mapel Anda (Profil Guru):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    androidx.compose.foundation.lazy.LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items(teacherSubjects.size) { index ->
                                            val subName = teacherSubjects[index]
                                            FilterChip(
                                                selected = subject.equals(subName, ignoreCase = true),
                                                onClick = { viewModel.setAttendanceSubject(subName) },
                                                label = { Text(subName, fontSize = 11.sp) },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(12.dp))
                                                }
                                            )
                                        }
                                    }
                                }

                                val classMajor = selectedClass?.major ?: "AKL"
                                val presetSubjects = remember(classMajor) {
                                    when (classMajor.uppercase()) {
                                        "AKL" -> listOf(
                                            "Akuntansi Keuangan",
                                            "Praktikum Akuntansi",
                                            "Perpajakan",
                                            "Spreadsheet",
                                            "B. Indonesia",
                                            "Matematika",
                                            "B. Inggris"
                                        )
                                        "MPLB" -> listOf(
                                            "Otomatisasi Perkantoran",
                                            "Kepegawaian",
                                            "Sarana & Prasarana",
                                            "Kearsipan",
                                            "Humas & Protokol",
                                            "B. Indonesia",
                                            "Matematika"
                                        )
                                        else -> listOf(
                                            "B. Indonesia",
                                            "Matematika",
                                            "B. Inggris",
                                            "PPKn",
                                            "PJOK",
                                            "Agama"
                                        )
                                    }
                                }

                                Text(
                                    text = "Pilih Cepat Mapel Jurusan (${classMajor}):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                androidx.compose.foundation.lazy.LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(presetSubjects.size) { index ->
                                        val subName = presetSubjects[index]
                                        FilterChip(
                                            selected = subject.equals(subName, ignoreCase = true),
                                            onClick = { viewModel.setAttendanceSubject(subName) },
                                            label = { Text(subName, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = notes,
                                onValueChange = { viewModel.setAttendanceNotes(it) },
                                label = { Text("Catatan Sesi (Opsional)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("attendance_notes_input")
                            )
                        }
                    }
                }

                // Summary Counter Bar
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CountPill(label = "Hadir", count = totalHadir, color = StatusHadirGreen, bgColor = StatusHadirBg)
                            CountPill(label = "Izin", count = totalIzin, color = StatusIzinBlue, bgColor = StatusIzinBg)
                            CountPill(label = "Sakit", count = totalSakit, color = StatusSakitAmber, bgColor = StatusSakitBg)
                            CountPill(label = "Alpa", count = totalAlpa, color = StatusAlpaRed, bgColor = StatusAlpaBg)
                        }
                    }
                }

                item {
                    Text(
                        text = "Daftar Presensi Siswa (${students.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (students.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Tidak ada siswa terdaftar di kelas ini.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    items(students) { student ->
                        val currentStatus = statusMap[student.id] ?: "HADIR"
                        val currentNote = noteMap[student.id] ?: ""

                        StudentAttendanceRowCard(
                            student = student,
                            selectedStatus = currentStatus,
                            note = currentNote,
                            onStatusSelected = { newStatus ->
                                viewModel.updateStudentStatus(student.id, newStatus)
                            },
                            onAddNoteClick = {
                                studentForNoteDialog = student
                            }
                        )
                    }
                }
            }
        }
    }

    // Student Note Dialog
    val targetStudentForNote = studentForNoteDialog
    if (targetStudentForNote != null) {
        var tempNote by remember { mutableStateOf(noteMap[targetStudentForNote.id] ?: "") }

        AlertDialog(
            onDismissRequest = { studentForNoteDialog = null },
            title = { Text("Catatan untuk ${targetStudentForNote.name}") },
            text = {
                OutlinedTextField(
                    value = tempNote,
                    onValueChange = { tempNote = it },
                    label = { Text("Keterangan Tambahan (Contoh: Alasan izin/sakit)") },
                    modifier = Modifier.fillMaxWidth().testTag("student_individual_note_input")
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateStudentNote(targetStudentForNote.id, tempNote)
                    studentForNoteDialog = null
                }) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentForNoteDialog = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun CountPill(
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    bgColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        color = bgColor,
        contentColor = color,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "$label:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "$count", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun StudentAttendanceRowCard(
    student: Student,
    selectedStatus: String,
    note: String,
    onStatusSelected: (String) -> Unit,
    onAddNoteClick: () -> Unit
) {
    val initials = remember(student.name) {
        student.name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .ifEmpty { "S" }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityOutline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Column {
                        Text(
                            text = student.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "NISN: ${if (student.nisn.isBlank()) "-" else student.nisn}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onAddNoteClick) {
                    Icon(
                        imageVector = if (note.isBlank()) Icons.Default.NoteAdd else Icons.Default.StickyNote2,
                        contentDescription = "Catatan Siswa",
                        tint = if (note.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (note.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Catatan: $note",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            StatusSelectorGroup(
                selectedStatus = selectedStatus,
                onStatusSelected = onStatusSelected
            )
        }
    }
}
