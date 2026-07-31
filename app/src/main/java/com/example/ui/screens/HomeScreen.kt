package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceSession
import com.example.data.model.ClassGroup
import com.example.ui.AttendanceViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AttendanceViewModel,
    onStartTakeAttendance: (ClassGroup) -> Unit,
    onEditSession: (AttendanceSession) -> Unit,
    onNavigateToClasses: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSchedule: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val allClasses by viewModel.allClasses.collectAsState()
    val totalStudents by viewModel.totalStudentsCount.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val allSchedules by viewModel.allSchedules.collectAsState()

    val teacherName by viewModel.teacherName.collectAsState()
    val teacherNip by viewModel.teacherNip.collectAsState()
    val teacherSubject by viewModel.teacherSubject.collectAsState()
    val schoolName by viewModel.schoolName.collectAsState()
    val criticalAlpaStudents by viewModel.criticalAlpaStudents.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedClassForQuickInput by remember { mutableStateOf<ClassGroup?>(null) }
    var showQuickStartDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showCriticalAlpaDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }

    val currentDateStr = remember {
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        sdf.format(Date())
    }

    val filteredSessions = remember(allSessions, allClasses, searchQuery) {
        allSessions.filter { session ->
            val classGroup = allClasses.find { it.id == session.classId }
            val className = classGroup?.name ?: ""
            searchQuery.isBlank() ||
                    session.subject.contains(searchQuery, ignoreCase = true) ||
                    session.date.contains(searchQuery, ignoreCase = true) ||
                    className.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SMKN 1 Cirinten",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Presensi Siswa • $currentDateStr",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("open_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Pengaturan & Master Data")
                    }
                    IconButton(
                        onClick = onNavigateToClasses,
                        modifier = Modifier.testTag("manage_classes_button")
                    ) {
                        Icon(Icons.Default.School, contentDescription = "Kelola Kelas")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showQuickStartDialog = true },
                icon = { Icon(Icons.Default.AddTask, contentDescription = null) },
                text = { Text("Input Absensi", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("quick_input_fab")
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Hero Card with Teacher Profile
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.25f),
                                        modifier = Modifier.size(52.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = teacherName,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        if (teacherNip.isNotBlank()) {
                                            Text(
                                                text = "NIP: $teacherNip",
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.85f)
                                            )
                                        }
                                        Text(
                                            text = "$schoolName • $teacherSubject",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White.copy(alpha = 0.95f)
                                        )
                                    }
                                }

                                Surface(
                                    onClick = { showEditProfileDialog = true },
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(36.dp).testTag("edit_profile_btn")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Profil Guru",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { showQuickStartDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("hero_start_attendance_btn")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Mulai Presensi", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                 OutlinedButton(
                                    onClick = { showEditProfileDialog = true },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.White
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Profil Guru", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Urgent Warning Card: Students with Alpa >= 3x
            if (criticalAlpaStudents.isNotEmpty()) {
                item {
                    Card(
                        onClick = { showCriticalAlpaDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("critical_alpa_warning_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🚨 PERINGATAN SANGAT PENTING",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "${criticalAlpaStudents.size} Siswa Terdeteksi Alpa ≥ 3 Kali!",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "Siswa telah melampaui batas toleransi alpa. Diperlukan tindakan/surat panggilan wali.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                                    )
                                }
                            }

                            Button(
                                onClick = { showCriticalAlpaDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Kelola Kelompok Alpa > 3x & Kirim SP (WA)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Summary Stats Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = "Total Kelas",
                        value = "${allClasses.size}",
                        icon = Icons.Default.Class,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToClasses
                    )
                    StatMetricCard(
                        title = "Total Siswa",
                        value = "$totalStudents",
                        icon = Icons.Default.People,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToClasses
                    )
                    StatMetricCard(
                        title = "Histori Absen",
                        value = "${allSessions.size} Sesi",
                        icon = Icons.Default.History,
                        modifier = Modifier.weight(1f),
                        onClick = { showHistoryDialog = true }
                    )
                }
            }

            // Quick Access History & Correction Card
            item {
                Card(
                    onClick = { showHistoryDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_history_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.ManageHistory, contentDescription = null, tint = Color.White)
                                }
                            }
                            Column {
                                Text("Histori Absen (Harian & Mingguan)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Cek riwayat kemarin, tangani komplen & koreksi status", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Icon(Icons.Default.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Quick Access Schedule & Alarm Card
            item {
                Card(
                    onClick = onNavigateToSchedule,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.White)
                                }
                            }
                            Column {
                                Text("Jadwal & Alarm Mengajar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("${allSchedules.size} Sesi terdaftar • Bunyikan Bel Sesi", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Icon(Icons.Default.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Search Bar for Sessions
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari sesi, mata pelajaran, atau kelas...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_session_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }

            // Recent Sessions Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Riwayat Presensi Terbaru",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (allSessions.isNotEmpty()) {
                        TextButton(onClick = onNavigateToReports) {
                            Text("Lihat Rekap", fontSize = 13.sp)
                        }
                    }
                }
            }

            // Sessions List or Empty State
            if (filteredSessions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isBlank()) "Belum ada sesi presensi." else "Sesi tidak ditemukan.",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Klik tombol 'Input Absensi' untuk memulai pencatatan.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            } else {
                items(filteredSessions) { session ->
                    val classGroup = allClasses.find { it.id == session.classId }
                    SessionItemCard(
                        session = session,
                        className = classGroup?.name ?: "Kelas Tidak Ditemukan",
                        onClick = { onEditSession(session) },
                        onDelete = { viewModel.deleteSession(session) }
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp)
                        .testTag("app_developer_credit_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Aplikasi Presensi Siswa",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Developed by arsam © 2026",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Versi 1.0.0 • Siap Diuji & Diinstal di HP",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Quick Select Class Dialog
    if (showQuickStartDialog) {
        var selectedMajorFilter by remember { mutableStateOf("SEMUA") }

        val filteredClasses = remember(allClasses, selectedMajorFilter) {
            if (selectedMajorFilter == "SEMUA") allClasses
            else allClasses.filter { it.major.equals(selectedMajorFilter, ignoreCase = true) }
        }

        AlertDialog(
            onDismissRequest = { showQuickStartDialog = false },
            title = { Text("Pilih Kelas Presensi", fontWeight = FontWeight.Bold) },
            text = {
                if (allClasses.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    ) {
                        Text("Belum ada kelas terdaftar.")
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = {
                            showQuickStartDialog = false
                            onNavigateToClasses()
                        }) {
                            Text("+ Tambah Kelas Baru")
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = selectedMajorFilter == "SEMUA",
                                onClick = { selectedMajorFilter = "SEMUA" },
                                label = { Text("Semua") }
                            )
                            FilterChip(
                                selected = selectedMajorFilter == "AKL",
                                onClick = { selectedMajorFilter = "AKL" },
                                label = { Text("AKL") }
                            )
                            FilterChip(
                                selected = selectedMajorFilter == "MPLB",
                                onClick = { selectedMajorFilter = "MPLB" },
                                label = { Text("MPLB") }
                            )
                        }

                        if (filteredClasses.isEmpty()) {
                            Text(
                                "Tidak ada kelas untuk jurusan ${selectedMajorFilter}.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            filteredClasses.forEach { classGroup ->
                                Card(
                                    onClick = {
                                        showQuickStartDialog = false
                                        onStartTakeAttendance(classGroup)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (classGroup.major.equals("AKL", ignoreCase = true))
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.tertiary
                                            ) {
                                                Text(
                                                    text = classGroup.major,
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = classGroup.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                )
                                                Text(
                                                    text = "TA: ${classGroup.academicYear}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ArrowForwardIos,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQuickStartDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showEditProfileDialog) {
        EditTeacherProfileDialog(
            currentName = teacherName,
            currentNip = teacherNip,
            currentSubject = teacherSubject,
            currentSchool = schoolName,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, nip, subject, school ->
                viewModel.updateTeacherProfile(name, nip, subject, school)
                showEditProfileDialog = false
            }
        )
    }

    if (showCriticalAlpaDialog) {
        CriticalAlpaDialog(
            criticalStudents = criticalAlpaStudents,
            teacherName = teacherName,
            teacherNip = teacherNip,
            schoolName = schoolName,
            onDismiss = { showCriticalAlpaDialog = false },
            onSendWarningLetter = { info ->
                sendWarningLetterViaWhatsApp(context, info, teacherName, teacherNip, schoolName)
            }
        )
    }

    if (showHistoryDialog) {
        AttendanceHistoryDialog(
            viewModel = viewModel,
            onEditSession = { session ->
                viewModel.loadExistingSessionForEdit(session)
                onEditSession(session)
            },
            onDismiss = { showHistoryDialog = false }
        )
    }
}

@Composable
fun EditTeacherProfileDialog(
    currentName: String,
    currentNip: String,
    currentSubject: String,
    currentSchool: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var nameInput by remember { mutableStateOf(currentName) }
    var nipInput by remember { mutableStateOf(currentNip) }
    var subjectInput by remember { mutableStateOf(currentSubject) }
    var schoolInput by remember { mutableStateOf(currentSchool) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Edit Profil Guru", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Informasi profil ini akan ditampilkan di halaman utama serta digunakan pada laporan resmi.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Nama Lengkap & Gelar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                )

                OutlinedTextField(
                    value = nipInput,
                    onValueChange = { nipInput = it },
                    label = { Text("NIP Guru (Bisa dikosongkan jika Non-ASN)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("profile_nip_input")
                )

                OutlinedTextField(
                    value = subjectInput,
                    onValueChange = { subjectInput = it },
                    label = { Text("Mata Pelajaran Pengampu") },
                    supportingText = { Text("Pisahkan dengan koma jika mengampu lebih dari 1 mapel (misal: Praktik Akuntansi, Spreadsheet, Perpajakan)", fontSize = 10.sp) },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("profile_subject_input")
                )

                OutlinedTextField(
                    value = schoolInput,
                    onValueChange = { schoolInput = it },
                    label = { Text("Nama Sekolah") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("profile_school_input")
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Developed by arsam © 2026",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        nameInput.ifBlank { "Nama Guru" },
                        nipInput,
                        subjectInput.ifBlank { "Guru Pengampu" },
                        schoolInput.ifBlank { "SMKN 1 Cirinten" }
                    )
                },
                enabled = nameInput.isNotBlank()
            ) {
                Text("Simpan Perubahan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SessionItemCard(
    session: AttendanceSession,
    className: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityOutline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EventAvailable,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Column {
                    Text(
                        text = session.subject,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "$className • ${session.date}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (session.notes.isNotBlank()) {
                        Text(
                            text = session.notes,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                }
            }

            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Hapus Sesi",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Sesi Presensi?") },
            text = { Text("Riwayat presensi ${session.subject} pada tanggal ${session.date} akan dihapus secara permanen.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
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
fun CriticalAlpaDialog(
    criticalStudents: List<com.example.data.model.CriticalStudentAlpaInfo>,
    teacherName: String,
    teacherNip: String,
    schoolName: String,
    onDismiss: () -> Unit,
    onSendWarningLetter: (com.example.data.model.CriticalStudentAlpaInfo) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Column {
                    Text("Kelompok Siswa Alpa ≥ 3 Kali", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Peringatan Sangat Penting - Tindakan Sekolah", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Siswa yang terdaftar di bawah ini telah mengumpulkan 3x atau lebih ketidakhadiran tanpa keterangan (Alpa). Ketuk 'Kirim SP' untuk membagikan pesan resmi peringatan ke Orang Tua / Wali.",
                        fontSize = 11.sp,
                        modifier = Modifier.padding(10.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                criticalStudents.forEach { info ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = info.summary.student.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "NISN: ${info.summary.student.nisn} • Kelas: ${info.className}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.error,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${info.summary.totalAlpa}x ALPA",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Status: ${info.warningLevel}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.error
                                )

                                Button(
                                    onClick = { onSendWarningLetter(info) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Kirim SP (WA)", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

fun sendWarningLetterViaWhatsApp(
    context: android.content.Context,
    info: com.example.data.model.CriticalStudentAlpaInfo,
    teacherName: String,
    teacherNip: String,
    schoolName: String
) {
    val sb = StringBuilder()
    sb.append("⚠️ *SURAT PERINGATAN (SP) & NOTIFIKASI ORANG TUA / WALI*\n")
    sb.append("*$schoolName*\n\n")
    sb.append("Yth. Bapak/Ibu Orang Tua / Wali dari:\n")
    sb.append("• *Nama Siswa*: ${info.summary.student.name}\n")
    sb.append("• *NISN*: ${info.summary.student.nisn}\n")
    sb.append("• *Kelas*: ${info.className}\n\n")
    sb.append("Dengan ini kami beritahukan bahwa berdasarkan rekapitulasi presensi, siswa ybs telah *TIDAK HADIR TANPA KETERANGAN (ALPA)* sebanyak *${info.summary.totalAlpa} KALI*.\n\n")
    sb.append("🔴 *Status Peringatan*: ${info.warningLevel}\n")
    sb.append("• Total Hadir: ${info.summary.totalHadir} kali\n")
    sb.append("• Total Izin: ${info.summary.totalIzin} kali\n")
    sb.append("• Total Sakit: ${info.summary.totalSakit} kali\n")
    sb.append("• Persentase Kehadiran: ${String.format("%.1f", info.summary.attendancePercentage)}%\n\n")
    sb.append("Mohon perhatian dan kerja sama Bapak/Ibu untuk segera menghubungi atau datang ke sekolah berkoordinasi dengan Wali Kelas/BK demi kebaikan pendidikan putra/putri Bapak/Ibu.\n\n")
    sb.append("Hormat kami,\n")
    sb.append("*$teacherName*\n")
    if (teacherNip.isNotBlank()) {
        sb.append("NIP. $teacherNip\n")
    }
    sb.append("Guru Pengampu / Wali Kelas - $schoolName")

    val sendIntent = android.content.Intent().apply {
        action = android.content.Intent.ACTION_SEND
        putExtra(android.content.Intent.EXTRA_TEXT, sb.toString())
        type = "text/plain"
    }
    val shareIntent = android.content.Intent.createChooser(sendIntent, "Kirim Surat Peringatan Siswa")
    context.startActivity(shareIntent)
}
