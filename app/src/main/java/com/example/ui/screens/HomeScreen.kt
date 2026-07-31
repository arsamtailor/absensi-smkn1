package com.example.ui.screens

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
    val activeAcademicYear by viewModel.activeAcademicYear.collectAsState()
    val activeSemester by viewModel.activeSemester.collectAsState()
    val criticalAlpaStudents by viewModel.criticalAlpaStudents.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedClassForQuickInput by remember { mutableStateOf<ClassGroup?>(null) }
    var showQuickStartDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showCriticalAlpaDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showUserGuideDialog by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }

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
                            text = schoolName.ifBlank { "SMKN 1 Cirinten" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Presensi Siswa • $currentDateStr • TA $activeAcademicYear ($activeSemester)",
                            fontSize = 11.sp,
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
                                            text = if (teacherSubject.isNotBlank()) "Pengampu $teacherSubject • TA $activeAcademicYear ($activeSemester)" else "Guru Pengampu • TA $activeAcademicYear ($activeSemester)",
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

            // Main Menu Grid Section (6 Fitur Utama)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ Menu Utama Aplikasi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "TA $activeAcademicYear ($activeSemester)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Row 1: Presensi & Arsip Periode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MainMenuGridCard(
                            title = "Input Presensi",
                            subtitle = "Mulai Absen & Jurnal",
                            icon = Icons.Default.PlayCircle,
                            badgeText = "Mulai",
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            iconColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            onClick = { showQuickStartDialog = true }
                        )

                        MainMenuGridCard(
                            title = "Arsip & Periode",
                            subtitle = "Ganti Semester / TA",
                            icon = Icons.Default.Inventory2,
                            badgeText = "Arsip",
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            iconColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                            onClick = { showArchiveDialog = true }
                        )
                    }

                    // Row 2: Panduan & Jadwal Alarm
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MainMenuGridCard(
                            title = "Panduan Aplikasi",
                            subtitle = "Petunjuk + Gambar",
                            icon = Icons.Default.MenuBook,
                            badgeText = "Buku PDF",
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            iconColor = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f),
                            onClick = { showUserGuideDialog = true }
                        )

                        MainMenuGridCard(
                            title = "Jadwal & Alarm",
                            subtitle = "${allSchedules.size} Sesi Mengajar",
                            icon = Icons.Default.Schedule,
                            badgeText = "Jadwal",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            iconColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToSchedule
                        )
                    }

                    // Row 3: Rekap Laporan & Histori Koreksi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MainMenuGridCard(
                            title = "Rekap & Laporan",
                            subtitle = "Export Excel & WA",
                            icon = Icons.Default.Assessment,
                            badgeText = "Laporan",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                            iconColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToReports
                        )

                        MainMenuGridCard(
                            title = "Histori & Koreksi",
                            subtitle = "${allSessions.size} Sesi Terdaftar",
                            icon = Icons.Default.ManageHistory,
                            badgeText = "Histori",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                            iconColor = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f),
                            onClick = { showHistoryDialog = true }
                        )
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
                                text = "Gunakan menu Jadwal atau Kelas & Siswa untuk melakukan presensi.",
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
                            text = "Versi 1.1.0",
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
        val majorsList by viewModel.majorsList.collectAsState()
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
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("⚠️ Master Data Belum Lengkap", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(
                            "Silakan lengkapi Master Data (Profil, Kelas, Mapel, dan Siswa) di menu Pengaturan & Master Data terlebih dahulu agar data terpusat.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = {
                            showQuickStartDialog = false
                            onOpenSettings()
                        }) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buka Pengaturan Master Data")
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedMajorFilter == "SEMUA",
                                    onClick = { selectedMajorFilter = "SEMUA" },
                                    label = { Text("Semua") }
                                )
                            }
                            items(majorsList) { mj ->
                                FilterChip(
                                    selected = selectedMajorFilter == mj,
                                    onClick = { selectedMajorFilter = mj },
                                    label = { Text(mj) }
                                )
                            }
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
            currentAcademicYear = activeAcademicYear,
            currentSemester = activeSemester,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, nip, subject, school, year, sem ->
                viewModel.updateTeacherProfile(name, nip, subject, school)
                viewModel.updateActiveAcademicPeriod(year, sem)
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

    if (showUserGuideDialog) {
        UserGuideDialog(
            teacherName = teacherName,
            schoolName = schoolName,
            onDismiss = { showUserGuideDialog = false }
        )
    }

    if (showArchiveDialog) {
        AcademicPeriodArchiveDialog(
            activeYear = activeAcademicYear,
            activeSemester = activeSemester,
            onDismiss = { showArchiveDialog = false },
            onSavePeriod = { y, s -> viewModel.updateActiveAcademicPeriod(y, s) },
            onArchiveAndSwitch = { y, s -> viewModel.archiveAcademicYearAndSwitch(y, s) }
        )
    }
}

@Composable
fun EditTeacherProfileDialog(
    currentName: String,
    currentNip: String,
    currentSubject: String,
    currentSchool: String,
    currentAcademicYear: String = "2025/2026",
    currentSemester: String = "Ganjil",
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var nameInput by remember { mutableStateOf(currentName) }
    var nipInput by remember { mutableStateOf(currentNip) }
    var subjectInput by remember { mutableStateOf(currentSubject) }
    var schoolInput by remember { mutableStateOf(currentSchool) }
    var academicYearInput by remember { mutableStateOf(currentAcademicYear) }
    var semesterInput by remember { mutableStateOf(currentSemester) }

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
                Text("Edit Profil Guru & Periode", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                    "Informasi profil dan Tahun Ajaran ini akan ditampilkan di halaman utama serta laporan.",
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
                    supportingText = { Text("Pisahkan dengan koma jika mengampu lebih dari 1 mapel (misal: Praktik Akuntansi, Perpajakan)", fontSize = 10.sp) },
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

                Text("📅 Tahun Ajaran & Semester Aktif", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = academicYearInput,
                        onValueChange = { academicYearInput = it },
                        label = { Text("Tahun Ajaran") },
                        placeholder = { Text("2026/2027") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f).testTag("profile_year_input")
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Semester", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = semesterInput.equals("Ganjil", ignoreCase = true),
                                onClick = { semesterInput = "Ganjil" },
                                label = { Text("Ganjil", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = semesterInput.equals("Genap", ignoreCase = true),
                                onClick = { semesterInput = "Genap" },
                                label = { Text("Genap", fontSize = 11.sp) }
                            )
                        }
                    }
                }

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
                        schoolInput.ifBlank { "SMKN 1 Cirinten" },
                        academicYearInput.ifBlank { "2025/2026" },
                        semesterInput
                    )
                },
                enabled = nameInput.isNotBlank() && academicYearInput.isNotBlank()
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

@Composable
fun MainMenuGridCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeText: String,
    containerColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.testTag("menu_grid_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconColor,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = iconColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = iconColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun UserGuideDialog(
    teacherName: String,
    schoolName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "📖 Buku Panduan Aplikasi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Text(
                    text = "Petunjuk penggunaan lengkap presensi, jurnal, & pengarsipan",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    Image(
                        painter = painterResource(id = com.example.R.drawable.img_guide_banner),
                        contentDescription = "Panduan Aplikasi",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("🚀 Presensi & KBM", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp))
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("⭐ Poin Disiplin", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp))
                    }
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                        Text("🗄️ Arsip & Semester", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp))
                    }
                    Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                        Text("📊 Laporan & Excel", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp))
                    }
                    Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }) {
                        Text("🔐 PIN Keamanan", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp))
                    }
                }

                when (selectedTab) {
                    0 -> GuidePresensiSection()
                    1 -> GuideDisciplineSection()
                    2 -> GuideArchiveSection()
                    3 -> GuideReportSection()
                    4 -> GuideSecuritySection()
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    com.example.util.PdfGuideGenerator.generateAndOpenManualPdf(
                        context = context,
                        teacherName = teacherName,
                        schoolName = schoolName
                    )
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cetak PDF Panduan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
private fun GuidePresensiSection() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        GuideStepCard(
            stepNumber = "1",
            title = "Pilih Kelas & Sesi Mengajar",
            description = "Klik 'Input Presensi' di menu utama, lalu pilih kelas dan mata pelajaran yang sedang diajarkan."
        )

        GuideStepCard(
            stepNumber = "2",
            title = "Tandai Kehadiran Siswa",
            description = "Tandai status dengan sekali tap:\n• 🟢 Hadir\n• 🟡 Sakit\n• 🔵 Izin\n• 🔴 Alpa"
        )

        GuideStepCard(
            stepNumber = "3",
            title = "Isi Jurnal & Pokok Bahasan KBM",
            description = "Ketik Pokok Bahasan / Materi Pembelajaran dan Catatan Kejadian KBM pada formulir bagian atas layar."
        )

        GuideStepCard(
            stepNumber = "4",
            title = "Simpan & Kirim Rekap WA",
            description = "Klik 'Simpan Presensi'. Kemudian tekan tombol 'Kirim Rekap WA' untuk mengirimkan pesan rapi ke WhatsApp Group Kelas/Orang Tua."
        )
    }
}

@Composable
private fun GuideDisciplineSection() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("⭐ Sistem Poin Kedisiplinan", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Text("Setiap siswa baru dibekali 100 Poin Kedisiplinan Awal.", fontSize = 11.sp)
            }
        }

        GuideStepCard(
            stepNumber = "•",
            title = "Catat Pelanggaran / Keaktifan",
            description = "Saat presensi, tekan ikon catatan (📝) di samping nama siswa untuk menambahkan:\n• Terlambat (-5 Poin)\n• Pelanggaran / Cabut (-10 Poin)\n• Prestasi / Keaktifan (+5 Poin)"
        )

        GuideStepCard(
            stepNumber = "🚨",
            title = "Deteksi Otomatis Alpa ≥ 3 Kali",
            description = "Jika siswa mencapai ≥ 3 kali Alpa, beranda utama akan menampilkan Kartu Peringatan Merah secara otomatis.\n\nAnda dapat langsung menekan 'Kirim SP (WA)' untuk membuat Surat Panggilan Wali murid!"
        )
    }
}

@Composable
private fun GuideArchiveSection() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("🗄️ Pergantian Semester & Pengarsipan", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                Text("Data semester lama tersimpan rapi dan tidak akan pernah terhapus!", fontSize = 11.sp)
            }
        }

        GuideStepCard(
            stepNumber = "1",
            title = "Buka Menu 'Arsip & Periode'",
            description = "Tekan tombol 'Arsip & Periode' pada grid menu utama beranda."
        )

        GuideStepCard(
            stepNumber = "2",
            title = "Set Tahun Ajaran & Semester Baru",
            description = "Masukkan Tahun Ajaran baru (misal 2026/2027) dan pilih Semester (Ganjil/Genap)."
        )

        GuideStepCard(
            stepNumber = "3",
            title = "Ganti Periode & Arsipkan",
            description = "Klik 'Ganti Periode & Arsipkan'. Data presensi semester lalu otomatis tersimpan aman di database arsip."
        )
    }
}

@Composable
private fun GuideReportSection() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        GuideStepCard(
            stepNumber = "📊",
            title = "Export File Excel & PDF",
            description = "Masuk ke menu 'Rekap & Laporan'. Anda dapat mengunduh Laporan Excel (.xlsx) atau mencetak Dokumen Rekap PDF resmi untuk sekolah."
        )

        GuideStepCard(
            stepNumber = "💬",
            title = "Format Pesan WA Rapi",
            description = "Laporan dapat dibagikan langsung ke grup WhatsApp dengan format ringkasan persentase kehadiran."
        )
    }
}

@Composable
private fun GuideSecuritySection() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        GuideStepCard(
            stepNumber = "🔐",
            title = "PIN Keamanan Aplikasi",
            description = "Aktifkan PIN Keamanan di menu Kelola Data / Profil untuk melestarikan dan mengamankan data presensi guru dari pengguna yang tidak berwenang."
        )
    }
}

@Composable
private fun GuideStepCard(
    stepNumber: String,
    title: String,
    description: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stepNumber,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun AcademicPeriodArchiveDialog(
    activeYear: String,
    activeSemester: String,
    onDismiss: () -> Unit,
    onSavePeriod: (String, String) -> Unit,
    onArchiveAndSwitch: (String, String) -> Unit
) {
    var yearInput by remember(activeYear) { mutableStateOf(activeYear) }
    var semInput by remember(activeSemester) { mutableStateOf(activeSemester) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("🗄️ Arsip & Periode Semester", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "📌 Periode Aktif Saat Ini:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Tahun Ajaran $activeYear — Semester $activeSemester",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("ℹ️ Cara Kerja Fitur Arsip:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("• Data presensi & jurnal semester sebelumnya tersimpan aman di database lokal.", fontSize = 10.sp)
                        Text("• Anda dapat melihat & cetak rekap periode lama kapan saja di menu Laporan.", fontSize = 10.sp)
                        Text("• Masukkan Tahun Ajaran dan Semester baru di bawah ini saat memasuki ajaran baru.", fontSize = 10.sp)
                    }
                }

                OutlinedTextField(
                    value = yearInput,
                    onValueChange = { yearInput = it },
                    label = { Text("Tahun Ajaran (Contoh: 2026/2027)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Semester Baru:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = semInput.equals("Ganjil", ignoreCase = true),
                        onClick = { semInput = "Ganjil" },
                        label = { Text("Ganjil") }
                    )
                    FilterChip(
                        selected = semInput.equals("Genap", ignoreCase = true),
                        onClick = { semInput = "Genap" },
                        label = { Text("Genap") }
                    )
                }
            }
        },
        confirmButton = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = {
                        if (yearInput.isNotBlank()) {
                            onArchiveAndSwitch(yearInput, semInput)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ganti Periode & Arsipkan")
                }

                OutlinedButton(
                    onClick = {
                        if (yearInput.isNotBlank()) {
                            onSavePeriod(yearInput, semInput)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Simpan Periode Ini Saja")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Batal")
            }
        }
    )
}
