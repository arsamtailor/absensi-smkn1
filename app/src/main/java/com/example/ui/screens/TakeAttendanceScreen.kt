package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedClass by viewModel.selectedClassForAttendance.collectAsState()
    val allSchedules by viewModel.allSchedules.collectAsState()
    val date by viewModel.attendanceDate.collectAsState()
    val subject by viewModel.attendanceSubject.collectAsState()
    val notes by viewModel.attendanceNotes.collectAsState()
    val teachingTopic by viewModel.attendanceTeachingTopic.collectAsState()
    val teachingNotes by viewModel.attendanceTeachingNotes.collectAsState()
    val statusMap by viewModel.studentStatusMap.collectAsState()
    val noteMap by viewModel.studentNoteMap.collectAsState()
    val disciplineNoteMap by viewModel.studentDisciplineNoteMap.collectAsState()
    val pointImpactMap by viewModel.studentPointImpactMap.collectAsState()
    val students by viewModel.classStudentsForAttendance.collectAsState()
    val editingSessionId by viewModel.editingSessionId.collectAsState()
    val autoDetectedScheduleSubject by viewModel.autoDetectedScheduleSubject.collectAsState()

    val classSchedules = remember(allSchedules, selectedClass) {
        if (selectedClass != null) {
            allSchedules.filter {
                it.className.contains(selectedClass?.name ?: "", ignoreCase = true) ||
                        (selectedClass?.name ?: "").contains(it.className, ignoreCase = true)
            }
        } else {
            allSchedules
        }
    }

    val scheduleSubjects = remember(classSchedules) {
        classSchedules.map { it.subject.trim() }.filter { it.isNotBlank() }.distinct()
    }

    LaunchedEffect(scheduleSubjects) {
        if (scheduleSubjects.isNotEmpty() && !scheduleSubjects.contains(subject)) {
            viewModel.setAttendanceSubject(scheduleSubjects.first())
        }
    }

    val context = LocalContext.current

    val totalHadir = students.count { (statusMap[it.id] ?: "HADIR").equals("HADIR", ignoreCase = true) }
    val totalIzin = students.count { (statusMap[it.id] ?: "HADIR").equals("IZIN", ignoreCase = true) }
    val totalSakit = students.count { (statusMap[it.id] ?: "HADIR").equals("SAKIT", ignoreCase = true) }
    val totalAlpa = students.count { (statusMap[it.id] ?: "HADIR").equals("ALPA", ignoreCase = true) }

    var studentForNoteDialog by remember { mutableStateOf<Student?>(null) }
    var showJournalSection by remember { mutableStateOf(true) }

    val sendWhatsAppMessage = {
        val messageText = viewModel.generateWhatsAppSummaryMessage()
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, messageText)
            }
            context.startActivity(android.content.Intent.createChooser(intent, "Kirim Rekap WA Wali Murid"))
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Tidak ada aplikasi pesan terinstal", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

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
                    IconButton(onClick = sendWhatsAppMessage) {
                        Icon(Icons.Default.Share, contentDescription = "Kirim Rekap WA Wali Murid", tint = MaterialTheme.colorScheme.primary)
                    }
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
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = sendWhatsAppMessage,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("💬 Rekap WA", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.markAllStudents("HADIR") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mark_all_present_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hadir Semua", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.saveAttendanceSession(onSuccess = onNavigateBack)
                            },
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("save_attendance_bottom_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simpan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
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
                // Offline status badge
                item {
                    Surface(
                        color = StatusHadirBg,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CloudOff, contentDescription = null, tint = StatusHadirGreen, modifier = Modifier.size(16.dp))
                            Text(
                                text = "🟢 100% Mode Offline • Data Tersimpan di Database Lokal SQLite",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusHadirGreen
                            )
                        }
                    }
                }

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
                                    text = "Detail Sesi Presensi",
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
                                if (scheduleSubjects.isEmpty()) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "⚠️ Belum Ada Jadwal Mengajar untuk ${selectedClass?.name ?: "Kelas Ini"}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                                Text(
                                                    text = "Mata pelajaran presensi diambil dari Master Jadwal.",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                            TextButton(onClick = onOpenSettings) {
                                                Text("⚙️ Pengaturan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                } else if (autoDetectedScheduleSubject != null) {
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
                                                text = "Mapel Otomatis: $autoDetectedScheduleSubject",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }

                                var subjectDropdownExpanded by remember { mutableStateOf(false) }

                                ExposedDropdownMenuBox(
                                    expanded = subjectDropdownExpanded && scheduleSubjects.isNotEmpty(),
                                    onExpandedChange = {
                                        if (scheduleSubjects.isNotEmpty()) {
                                            subjectDropdownExpanded = !subjectDropdownExpanded
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = if (subject.isNotBlank()) subject else if (scheduleSubjects.isEmpty()) "⚠️ Belum Ada Jadwal (Buat di Pengaturan)" else "Pilih Mapel",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Mata Pelajaran") },
                                        trailingIcon = {
                                            if (scheduleSubjects.isNotEmpty()) {
                                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded)
                                            }
                                        },
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                        enabled = scheduleSubjects.isNotEmpty(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                            .testTag("attendance_subject_input"),
                                        singleLine = true
                                    )

                                    if (scheduleSubjects.isNotEmpty()) {
                                        ExposedDropdownMenu(
                                            expanded = subjectDropdownExpanded,
                                            onDismissRequest = { subjectDropdownExpanded = false }
                                        ) {
                                            scheduleSubjects.forEach { subName ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Book,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(16.dp),
                                                                tint = MaterialTheme.colorScheme.primary
                                                            )
                                                            Text(subName, fontWeight = if (subject == subName) FontWeight.Bold else FontWeight.Normal)
                                                        }
                                                    },
                                                    onClick = {
                                                        viewModel.setAttendanceSubject(subName)
                                                        subjectDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = notes,
                                onValueChange = { viewModel.setAttendanceNotes(it) },
                                label = { Text("Catatan Sesi / Keterangan Tambahan") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("attendance_notes_input")
                            )
                        }
                    }
                }

                // Jurnal Mengajar Guru Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text(
                                        text = "📖 Jurnal Mengajar Guru (Catatan Pertemuan)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                IconButton(onClick = { showJournalSection = !showJournalSection }) {
                                    Icon(
                                        imageVector = if (showJournalSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle Jurnal"
                                    )
                                }
                            }

                            if (showJournalSection) {
                                OutlinedTextField(
                                    value = teachingTopic,
                                    onValueChange = { viewModel.setAttendanceTeachingTopic(it) },
                                    label = { Text("Materi / Topik Pembelajaran Hari Ini") },
                                    placeholder = { Text("Contoh: Pembahasan Jurnal Penyesuaian Akuntansi") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = false,
                                    maxLines = 3
                                )

                                OutlinedTextField(
                                    value = teachingNotes,
                                    onValueChange = { viewModel.setAttendanceTeachingNotes(it) },
                                    label = { Text("Catatan Kendala / Progres Kelas") },
                                    placeholder = { Text("Contoh: Siswa aktif, kendala waktu di bab pengerjaan neraca.") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = false,
                                    maxLines = 3
                                )
                            }
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
                        val currentDiscNote = disciplineNoteMap[student.id] ?: ""
                        val currentPointImpact = pointImpactMap[student.id] ?: 0

                        StudentAttendanceRowCard(
                            student = student,
                            selectedStatus = currentStatus,
                            note = currentNote,
                            disciplineNote = currentDiscNote,
                            pointImpact = currentPointImpact,
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

    // Student Note & Discipline Dialog
    val targetStudentForNote = studentForNoteDialog
    if (targetStudentForNote != null) {
        var tempNote by remember { mutableStateOf(noteMap[targetStudentForNote.id] ?: "") }
        var tempDiscNote by remember { mutableStateOf(disciplineNoteMap[targetStudentForNote.id] ?: "") }
        var tempPointImpact by remember { mutableStateOf(pointImpactMap[targetStudentForNote.id] ?: 0) }

        AlertDialog(
            onDismissRequest = { studentForNoteDialog = null },
            title = {
                Text(
                    text = "Catatan & Kedisiplinan: ${targetStudentForNote.name}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = tempNote,
                        onValueChange = { tempNote = it },
                        label = { Text("Keterangan Absensi (Alasan Izin/Sakit)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_individual_note_input")
                    )

                    Divider()

                    Text(
                        text = "⭐ Catatan Kedisiplinan & Poin Siswa",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = tempDiscNote,
                        onValueChange = { tempDiscNote = it },
                        label = { Text("Catatan Pelanggaran / Prestasi") },
                        placeholder = { Text("Contoh: Terlambat 15 menit, Tidak bawa buku, dsb.") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Penyesuaian Poin Kedisiplinan (Poin Saat Ini: ${targetStudentForNote.disciplinePoints})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = tempPointImpact == 0,
                                onClick = { tempPointImpact = 0 },
                                label = { Text("Normal (0)", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = tempPointImpact == -5,
                                onClick = { tempPointImpact = -5 },
                                label = { Text("Terlambat (-5)", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = tempPointImpact == -10,
                                onClick = { tempPointImpact = -10 },
                                label = { Text("Pelanggaran (-10)", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = tempPointImpact == 5,
                                onClick = { tempPointImpact = 5 },
                                label = { Text("Prestasi (+5)", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateStudentNote(targetStudentForNote.id, tempNote)
                    viewModel.updateStudentDisciplineNote(targetStudentForNote.id, tempDiscNote, tempPointImpact)
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
    disciplineNote: String = "",
    pointImpact: Int = 0,
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = student.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Surface(
                                color = if (student.disciplinePoints >= 90) StatusHadirBg else StatusAlpaBg,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "⭐ ${student.disciplinePoints} Poin",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (student.disciplinePoints >= 90) StatusHadirGreen else StatusAlpaRed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "NISN: ${if (student.nisn.isBlank()) "-" else student.nisn}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onAddNoteClick) {
                    Icon(
                        imageVector = if (note.isBlank() && disciplineNote.isBlank()) Icons.Default.NoteAdd else Icons.Default.StickyNote2,
                        contentDescription = "Catatan & Kedisiplinan",
                        tint = if (note.isBlank() && disciplineNote.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (note.isNotBlank() || disciplineNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (note.isNotBlank()) {
                            Text(
                                text = "📝 Keterangan: $note",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (disciplineNote.isNotBlank()) {
                            Text(
                                text = "⭐ Kedisiplinan: $disciplineNote ${if (pointImpact != 0) "($pointImpact Poin)" else ""}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
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
