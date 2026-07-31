package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceSession
import com.example.data.model.ClassGroup
import com.example.data.model.Student
import com.example.ui.AttendanceViewModel
import com.example.ui.components.AttendanceStatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class HistoryPeriodFilter(val label: String) {
    HARI_INI("Hari Ini"),
    KEMARIN("Kemarin"),
    MINGGUAN("7 Hari Terakhir"),
    BULANAN("30 Hari Terakhir"),
    SEMUA("Semua Tanggal")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceHistoryDialog(
    viewModel: AttendanceViewModel,
    onEditSession: (AttendanceSession) -> Unit,
    onDismiss: () -> Unit
) {
    val allSessions by viewModel.allSessions.collectAsState()
    val allClasses by viewModel.allClasses.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val allRecords by viewModel.allRecords.collectAsState()

    val context = LocalContext.current

    var selectedPeriod by remember { mutableStateOf(HistoryPeriodFilter.MINGGUAN) }
    var selectedClassId by remember { mutableStateOf<Long?>(null) }
    var selectedStatusFilter by remember { mutableStateOf("SEMUA") }
    var searchQuery by remember { mutableStateOf("") }
    var customSelectedDate by remember { mutableStateOf<String?>(null) }

    // Correction dialog state
    var editingRecordStudent by remember { mutableStateOf<Pair<AttendanceSession, Student>?>(null) }
    var editingRecordCurrentStatus by remember { mutableStateOf("HADIR") }
    var editingRecordCurrentNote by remember { mutableStateOf("") }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val yesterdayStr = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }
    val weekAgoStr = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -7)
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }
    val monthAgoStr = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -30)
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    // Date Picker launcher
    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                customSelectedDate = sdf.format(selectedCal.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // Filtered Sessions
    val filteredSessions = remember(
        allSessions,
        selectedPeriod,
        selectedClassId,
        customSelectedDate,
        searchQuery,
        allClasses,
        allStudents,
        allRecords
    ) {
        allSessions.filter { session ->
            // Date filter
            val dateOk = if (customSelectedDate != null) {
                session.date == customSelectedDate
            } else {
                when (selectedPeriod) {
                    HistoryPeriodFilter.HARI_INI -> session.date == todayStr
                    HistoryPeriodFilter.KEMARIN -> session.date == yesterdayStr
                    HistoryPeriodFilter.MINGGUAN -> session.date >= weekAgoStr
                    HistoryPeriodFilter.BULANAN -> session.date >= monthAgoStr
                    HistoryPeriodFilter.SEMUA -> true
                }
            }

            // Class filter
            val classOk = selectedClassId == null || session.classId == selectedClassId

            // Search query (Student name, Subject, Class name)
            val sessionClass = allClasses.find { it.id == session.classId }
            val className = sessionClass?.name ?: ""

            val sessionRecords = allRecords.filter { it.sessionId == session.id }
            val sessionStudents = allStudents.filter { st -> sessionRecords.any { it.studentId == st.id } }

            val searchOk = searchQuery.isBlank() ||
                    session.subject.contains(searchQuery, ignoreCase = true) ||
                    session.date.contains(searchQuery, ignoreCase = true) ||
                    className.contains(searchQuery, ignoreCase = true) ||
                    sessionStudents.any { it.name.contains(searchQuery, ignoreCase = true) || it.nisn.contains(searchQuery, ignoreCase = true) }

            dateOk && classOk && searchOk
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top Title Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column {
                            Text("Histori & Koreksi Presensi", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Cek riwayat per hari/minggu & tangani komplen siswa", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                HorizontalDivider()

                // Filter Period Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Filter Waktu Presensi:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(HistoryPeriodFilter.values()) { period ->
                            val isSelected = selectedPeriod == period && customSelectedDate == null
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedPeriod = period
                                    customSelectedDate = null
                                },
                                label = {
                                    Text(
                                        text = when (period) {
                                            HistoryPeriodFilter.KEMARIN -> "Kemarin 🕒"
                                            HistoryPeriodFilter.HARI_INI -> "Hari Ini 📅"
                                            else -> period.label
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    if (period == HistoryPeriodFilter.KEMARIN) {
                                        Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                }
                            )
                        }

                        item {
                            FilterChip(
                                selected = customSelectedDate != null,
                                onClick = { datePickerDialog.show() },
                                label = { Text(customSelectedDate ?: "Pilih Tanggal...", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                    }
                }

                // Filter Class & Search Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari nama siswa / mapel...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Filter Class Chips
                if (allClasses.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(
                                selected = selectedClassId == null,
                                onClick = { selectedClassId = null },
                                label = { Text("Semua Kelas", fontSize = 11.sp) }
                            )
                        }
                        items(allClasses) { cls ->
                            FilterChip(
                                selected = selectedClassId == cls.id,
                                onClick = { selectedClassId = cls.id },
                                label = { Text(cls.name, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Sessions List Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ditemukan ${filteredSessions.size} Sesi Absensi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (customSelectedDate != null) {
                        TextButton(onClick = { customSelectedDate = null }) {
                            Text("Reset Tanggal", fontSize = 11.sp)
                        }
                    }
                }

                if (filteredSessions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.FindInPage, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                            Text("Tidak ada riwayat absensi pada filter ini.", fontWeight = FontWeight.SemiBold, color = Color.Gray)
                            Text("Ganti filter hari/minggu di atas atau buat absensi baru.", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredSessions) { session ->
                            val classGroup = allClasses.find { it.id == session.classId }
                            val sessionRecords = allRecords.filter { it.sessionId == session.id }
                            val sessionStudents = allStudents.filter { st -> sessionRecords.any { it.studentId == st.id } }

                            HistorySessionCard(
                                session = session,
                                classGroup = classGroup,
                                records = sessionRecords,
                                students = sessionStudents,
                                searchQuery = searchQuery,
                                onEditSession = {
                                    onDismiss()
                                    onEditSession(session)
                                },
                                onDeleteSession = { viewModel.deleteSession(session) },
                                onCorrectStudent = { student, record ->
                                    editingRecordStudent = Pair(session, student)
                                    editingRecordCurrentStatus = record?.status ?: "HADIR"
                                    editingRecordCurrentNote = record?.note ?: ""
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Quick Correction Dialog
    editingRecordStudent?.let { (session, student) ->
        var statusState by remember { mutableStateOf(editingRecordCurrentStatus) }
        var noteState by remember { mutableStateOf(editingRecordCurrentNote) }

        AlertDialog(
            onDismissRequest = { editingRecordStudent = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("Koreksi Status Siswa", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${student.name} • ${session.date}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Ganti status presensi untuk menangani komplen / perbaikan surat kemarin:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("HADIR", "IZIN", "SAKIT", "ALPA").forEach { st ->
                            val isSel = statusState.equals(st, ignoreCase = true)
                            val btnColor = when (st) {
                                "HADIR" -> StatusHadirGreen
                                "IZIN" -> StatusIzinBlue
                                "SAKIT" -> StatusSakitAmber
                                else -> StatusAlpaRed
                            }

                            Button(
                                onClick = { statusState = st },
                                modifier = Modifier.weight(1f),
                                colors = if (isSel) ButtonDefaults.buttonColors(containerColor = btnColor) else ButtonDefaults.outlinedButtonColors(),
                                contentPadding = PaddingValues(4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(st, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else Color.Unspecified)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = noteState,
                        onValueChange = { noteState = it },
                        label = { Text("Keterangan / Alasan Koreksi") },
                        placeholder = { Text("misal: Ortu mengonfirmasi surat via WA") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.quickUpdateStudentRecord(session.id, student.id, statusState, noteState)
                        editingRecordStudent = null
                    }
                ) {
                    Text("Simpan Koreksi")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingRecordStudent = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun HistorySessionCard(
    session: AttendanceSession,
    classGroup: ClassGroup?,
    records: List<AttendanceRecord>,
    students: List<Student>,
    searchQuery: String,
    onEditSession: () -> Unit,
    onDeleteSession: () -> Unit,
    onCorrectStudent: (Student, AttendanceRecord?) -> Unit
) {
    var isExpanded by remember { mutableStateOf(searchQuery.isNotBlank()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val totalHadir = records.count { it.status == "HADIR" }
    val totalIzin = records.count { it.status == "IZIN" }
    val totalSakit = records.count { it.status == "SAKIT" }
    val totalAlpa = records.count { it.status == "ALPA" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = classGroup?.name ?: "Kelas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Column {
                        Text(session.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(session.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row {
                    IconButton(onClick = onEditSession, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Form Presensi", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus Sesi", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Summary Stats Pill Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MiniStatusBadge("Hadir: $totalHadir", StatusHadirGreen)
                MiniStatusBadge("Izin: $totalIzin", StatusIzinBlue)
                MiniStatusBadge("Sakit: $totalSakit", StatusSakitAmber)
                MiniStatusBadge("Alpa: $totalAlpa", StatusAlpaRed)
            }

            if (session.notes.isNotBlank()) {
                Text(
                    text = "Catatan: ${session.notes}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Toggle Expand Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Sembunyikan Daftar Siswa (${students.size})" else "Lihat & Koreksi Siswa (${students.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Expandable Student List
            if (isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    HorizontalDivider()

                    students.forEach { student ->
                        val record = records.find { it.studentId == student.id }
                        val status = record?.status ?: "HADIR"
                        val note = record?.note ?: ""

                        val isHighlighted = searchQuery.isNotBlank() &&
                                (student.name.contains(searchQuery, ignoreCase = true) || student.nisn.contains(searchQuery, ignoreCase = true))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isHighlighted) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = student.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    if (note.isNotBlank()) {
                                        Text(
                                            text = "Ket: $note",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    AttendanceStatusBadge(status = status)

                                    IconButton(
                                        onClick = { onCorrectStudent(student, record) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.EditNote,
                                            contentDescription = "Koreksi Status",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Sesi Presensi?") },
            text = { Text("Apakah Anda yakin ingin menghapus sesi ${session.subject} tanggal ${session.date}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSession()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun MiniStatusBadge(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
