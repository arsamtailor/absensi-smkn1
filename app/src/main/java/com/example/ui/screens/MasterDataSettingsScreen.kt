package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClassGroup
import com.example.data.model.Student
import com.example.data.model.TeachingSchedule
import com.example.ui.AttendanceViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterDataSettingsScreen(
    viewModel: AttendanceViewModel,
    onNavigateToClasses: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf<Int?>(null) }
    val menuItems = listOf(
        Triple("Profil & PIN", Icons.Default.Badge, "Kelola profil guru, NIP, sekolah, dan PIN keamanan"),
        Triple("Master Kelas", Icons.Default.School, "Kelola daftar kelas, jurusan, dan tingkat"),
        Triple("Master Mapel", Icons.Default.Book, "Atur mata pelajaran terdaftar dan standar jurusan"),
        Triple("Master Siswa", Icons.Default.People, "Kelola data siswa per kelas, NISN, serta impor/ekspor CSV"),
        Triple("Master Jadwal", Icons.Default.CalendarMonth, "Atur jam mengajar harian & alokasi kelas/mapel"),
        Triple("Backup & Data", Icons.Default.SettingsBackupRestore, "Cadangkan data, ekspor laporan, atau reset data")
    )

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Pengaturan & Master Data", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Pusat Kontrol Terpusat: Seluruh Referensi & Master Data", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    if (selectedTab != null) {
                        IconButton(onClick = { selectedTab = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali ke Menu Utama")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val currentTab = selectedTab
            if (currentTab == null) {
                // Tampilan Depan Pengaturan: Grid Ikon dan Judul (seperti menu aplikasi di HP)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(span = { GridItemSpan(3) }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "💡 Ketuk salah satu ikon menu di bawah ini untuk membuka pengaturannya.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    items(menuItems.size) { index ->
                        val item = menuItems[index]
                        ElevatedCard(
                            onClick = { selectedTab = index },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("setting_menu_card_$index"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(54.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = item.second,
                                            contentDescription = item.first,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = item.first,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            } else {
                // Tampilan Detail Menu yang Dipilih (Hanya menampilkan isi menu terpilih secara mandiri)
                val currentItem = menuItems[currentTab]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = currentItem.second,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = currentItem.first,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = currentItem.third,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { selectedTab = null },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kembali", fontSize = 12.sp)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    when (currentTab) {
                        0 -> ProfileAndSecurityTab(viewModel = viewModel)
                        1 -> MasterClassTab(viewModel = viewModel)
                        2 -> MasterSubjectTab(viewModel = viewModel)
                        3 -> MasterStudentTab(viewModel = viewModel)
                        4 -> MasterScheduleTab(viewModel = viewModel)
                        5 -> BackupAndResetTab(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

// TAB 1: Profil Guru & PIN
@Composable
private fun ProfileAndSecurityTab(viewModel: AttendanceViewModel) {
    val teacherName by viewModel.teacherName.collectAsState()
    val teacherNip by viewModel.teacherNip.collectAsState()
    val teacherSubject by viewModel.teacherSubject.collectAsState()
    val schoolName by viewModel.schoolName.collectAsState()
    val activeAcademicYear by viewModel.activeAcademicYear.collectAsState()
    val activeSemester by viewModel.activeSemester.collectAsState()
    val savedPin by viewModel.savedPin.collectAsState()

    var nameState by remember(teacherName) { mutableStateOf(teacherName) }
    var nipState by remember(teacherNip) { mutableStateOf(teacherNip) }
    var subjectState by remember(teacherSubject) { mutableStateOf(teacherSubject) }
    var schoolState by remember(schoolName) { mutableStateOf(schoolName) }
    var yearState by remember(activeAcademicYear) { mutableStateOf(activeAcademicYear) }
    var semState by remember(activeSemester) { mutableStateOf(activeSemester) }

    var showPinDialog by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Identitas & Profil Guru (Step 1)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    OutlinedTextField(
                        value = nameState,
                        onValueChange = { nameState = it },
                        label = { Text("Nama Lengkap Guru + Gelar") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = nipState,
                        onValueChange = { nipState = it },
                        label = { Text("NIP / NUPTK") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = subjectState,
                        onValueChange = { subjectState = it },
                        label = { Text("Mata Pelajaran Utama Guru (pisahkan koma)") },
                        placeholder = { Text("Contoh: Akuntansi Keuangan, Perbankan Dasar") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = schoolState,
                        onValueChange = { schoolState = it },
                        label = { Text("Nama Sekolah / Instansi") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            viewModel.updateTeacherProfile(nameState, nipState, subjectState, schoolState)
                            viewModel.updateActiveAcademicPeriod(yearState, semState)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Perubahan Profil & Periode")
                    }
                }
            }
        }

        // CARD PERIODE & ARSIP SEMESTER
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("📅 Periode Aktif & Arsip Semester", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Text(
                        text = "Setiap sesi absensi dan jurnal akan dicatat sesuai Tahun Ajaran dan Semester aktif di bawah ini.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = yearState,
                            onValueChange = { yearState = it },
                            label = { Text("Tahun Ajaran") },
                            placeholder = { Text("2025/2026") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Semester", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                FilterChip(
                                    selected = semState.equals("Ganjil", ignoreCase = true),
                                    onClick = { semState = "Ganjil" },
                                    label = { Text("Ganjil", fontSize = 12.sp) }
                                )
                                FilterChip(
                                    selected = semState.equals("Genap", ignoreCase = true),
                                    onClick = { semState = "Genap" },
                                    label = { Text("Genap", fontSize = 12.sp) }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.updateActiveAcademicPeriod(yearState, semState)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Aktifkan Periode")
                        }

                        OutlinedButton(
                            onClick = { showArchiveDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🗄️ Fitur Arsip")
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Keamanan & Kunci PIN", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Text(
                        text = if (savedPin.isBlank()) "Status: Kunci PIN Nonaktif" else "Status: PIN Aktif (${savedPin.length} Digit)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (savedPin.isBlank()) Color.Gray else MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Aktifkan PIN untuk melindungi data siswa & rekap presensi dari perubahan yang tidak disengaja.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showPinDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (savedPin.isBlank()) "Atur PIN" else "Ganti PIN")
                        }

                        if (savedPin.isNotBlank()) {
                            OutlinedButton(
                                onClick = { viewModel.setSecurityPin("") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Hapus PIN")
                            }
                        }
                    }
                }
            }
        }

        item {
            val context = LocalContext.current
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Buku Panduan & Dokumentasi (PDF)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Text(
                        text = "Cetak atau simpan dokumen Buku Panduan Penggunaan Aplikasi lengkap dalam format PDF ke HP Anda.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            com.example.util.PdfGuideGenerator.generateAndOpenManualPdf(
                                context = context,
                                teacherName = teacherName,
                                schoolName = schoolName
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("download_pdf_manual_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unduh / Cetak Buku Panduan (PDF)")
                    }
                }
            }
        }
    }

    if (showPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Atur / Ganti PIN Keamanan") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Masukkan 4-6 angka PIN keamanan baru:", fontSize = 12.sp)
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) pinInput = it },
                        label = { Text("PIN Angka") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setSecurityPin(pinInput)
                        showPinDialog = false
                    },
                    enabled = pinInput.length >= 4
                ) {
                    Text("Simpan PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showArchiveDialog) {
        var archiveYearInput by remember { mutableStateOf("2026/2027") }
        var archiveSemInput by remember { mutableStateOf("Ganjil") }

        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Fitur Arsip & Pergantian Periode", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("ℹ️ Informasi Fitur Arsip:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Text("1. Seluruh data presensi & jurnal mengajar periode lama tetap tersimpan rapi dan tidak akan hilang.", fontSize = 11.sp)
                            Text("2. Anda dapat melihat atau mencetak rekap periode lama kapan saja di menu Laporan.", fontSize = 11.sp)
                            Text("3. Masukkan Tahun Ajaran dan Semester baru untuk mulai pencatatan periode baru.", fontSize = 11.sp)
                        }
                    }

                    OutlinedTextField(
                        value = archiveYearInput,
                        onValueChange = { archiveYearInput = it },
                        label = { Text("Tahun Ajaran Baru (Contoh: 2026/2027)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Semester Baru:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = archiveSemInput.equals("Ganjil", ignoreCase = true),
                            onClick = { archiveSemInput = "Ganjil" },
                            label = { Text("Ganjil") }
                        )
                        FilterChip(
                            selected = archiveSemInput.equals("Genap", ignoreCase = true),
                            onClick = { archiveSemInput = "Genap" },
                            label = { Text("Genap") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (archiveYearInput.isNotBlank()) {
                            viewModel.archiveAcademicYearAndSwitch(archiveYearInput, archiveSemInput)
                            showArchiveDialog = false
                        }
                    }
                ) {
                    Text("Ganti Periode & Arsipkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

// TAB 2: Master Kelas (Add, Edit, Delete)
@Composable
private fun MasterClassTab(viewModel: AttendanceViewModel) {
    val allClasses by viewModel.allClasses.collectAsState()
    val majorsList by viewModel.majorsList.collectAsState()
    val activeAcademicYear by viewModel.activeAcademicYear.collectAsState()

    var classNameInput by remember { mutableStateOf("") }
    var academicYearInput by remember(activeAcademicYear) { mutableStateOf(activeAcademicYear) }
    var majorInput by remember { mutableStateOf(majorsList.firstOrNull() ?: "AKL") }

    var classToEdit by remember { mutableStateOf<ClassGroup?>(null) }
    var classToDelete by remember { mutableStateOf<ClassGroup?>(null) }

    var newMajorNameInput by remember { mutableStateOf("") }
    var showAddMajorDialog by remember { mutableStateOf(false) }
    var majorToEdit by remember { mutableStateOf<String?>(null) }
    var majorToDelete by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Section: Master Jurusan Management
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            Icon(Icons.Default.Category, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Kelola Master Jurusan / Keahlian", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        IconButton(onClick = { showAddMajorDialog = true }) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Tambah Jurusan", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Text(
                        "Tambahkan, ubah, atau hapus jurusan/keahlian sekolah. Pilihan ini akan otomatis muncul pada filter & pembuatan kelas.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(majorsList) { mj ->
                            ElevatedCard(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(mj, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                    IconButton(
                                        onClick = { majorToEdit = mj },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Jurusan", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                    }
                                    if (majorsList.size > 1) {
                                        IconButton(
                                            onClick = { majorToDelete = mj },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Hapus Jurusan", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            OutlinedButton(
                                onClick = { showAddMajorDialog = true },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tambah Jurusan", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Tambah Master Kelas Baru (Step 2)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    OutlinedTextField(
                        value = classNameInput,
                        onValueChange = { classNameInput = it },
                        label = { Text("Nama Kelas (Contoh: X AKL 1, XI MPLB 2)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = academicYearInput,
                        onValueChange = { academicYearInput = it },
                        label = { Text("Tahun Ajaran") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Column {
                        Text("Pilih Jurusan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(majorsList) { mj ->
                                FilterChip(
                                    selected = majorInput == mj,
                                    onClick = { majorInput = mj },
                                    label = { Text(mj, fontSize = 12.sp) }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (classNameInput.isNotBlank()) {
                                viewModel.addOrUpdateClass(
                                    id = 0L,
                                    name = classNameInput.trim(),
                                    academicYear = academicYearInput.trim(),
                                    major = if (majorsList.contains(majorInput)) majorInput else (majorsList.firstOrNull() ?: "AKL")
                                )
                                classNameInput = ""
                            }
                        },
                        enabled = classNameInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Kelas Ke Master")
                    }
                }
            }
        }

        item {
            Text("Daftar Master Kelas Terdaftar (${allClasses.size}):", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        if (allClasses.isEmpty()) {
            item {
                Text("Belum ada kelas terdaftar. Tambahkan kelas di atas.", fontSize = 13.sp, color = Color.Gray)
            }
        } else {
            items(allClasses) { cls ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = cls.major,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Column {
                                Text(cls.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Tahun Ajaran: ${cls.academicYear}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row {
                            IconButton(onClick = { classToEdit = cls }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Kelas", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { classToDelete = cls }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus Kelas", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Tambah Jurusan Baru
    if (showAddMajorDialog) {
        var inputMajor by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddMajorDialog = false },
            title = { Text("Tambah Jurusan Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Singkatan/Nama Jurusan (contoh: TJKT, RPL, DKV, DMM):", fontSize = 12.sp)
                    OutlinedTextField(
                        value = inputMajor,
                        onValueChange = { inputMajor = it },
                        label = { Text("Kode Jurusan") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputMajor.isNotBlank()) {
                            viewModel.addMajor(inputMajor)
                            showAddMajorDialog = false
                        }
                    },
                    enabled = inputMajor.isNotBlank()
                ) {
                    Text("Tambah")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMajorDialog = false }) { Text("Batal") }
            }
        )
    }

    // Dialog: Edit Jurusan
    if (majorToEdit != null) {
        val targetMajor = majorToEdit!!
        var editedMajorName by remember { mutableStateOf(targetMajor) }
        AlertDialog(
            onDismissRequest = { majorToEdit = null },
            title = { Text("Edit Nama Jurusan", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ubah nama jurusan. Seluruh kelas yang menggunakan jurusan $targetMajor akan otomatis diperbarui.", fontSize = 12.sp)
                    OutlinedTextField(
                        value = editedMajorName,
                        onValueChange = { editedMajorName = it },
                        label = { Text("Nama Jurusan") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedMajorName.isNotBlank()) {
                            viewModel.editMajor(targetMajor, editedMajorName)
                            majorToEdit = null
                        }
                    },
                    enabled = editedMajorName.isNotBlank()
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { majorToEdit = null }) { Text("Batal") }
            }
        )
    }

    // Dialog: Hapus Jurusan
    if (majorToDelete != null) {
        val targetMajor = majorToDelete!!
        AlertDialog(
            onDismissRequest = { majorToDelete = null },
            title = { Text("Hapus Jurusan $targetMajor?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Apakah Anda yakin ingin menghapus jurusan $targetMajor dari daftar referensi?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMajor(targetMajor)
                        majorToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { majorToDelete = null }) { Text("Batal") }
            }
        )
    }

    if (classToEdit != null) {
        val target = classToEdit!!
        var editName by remember { mutableStateOf(target.name) }
        var editYear by remember { mutableStateOf(target.academicYear) }
        var editMajor by remember { mutableStateOf(target.major) }

        AlertDialog(
            onDismissRequest = { classToEdit = null },
            title = { Text("Edit Master Kelas") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nama Kelas") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editYear,
                        onValueChange = { editYear = it },
                        label = { Text("Tahun Ajaran") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Column {
                        Text("Pilih Jurusan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(majorsList) { mj ->
                                FilterChip(
                                    selected = editMajor == mj,
                                    onClick = { editMajor = mj },
                                    label = { Text(mj, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addOrUpdateClass(
                            id = target.id,
                            name = editName.trim(),
                            academicYear = editYear.trim(),
                            major = editMajor
                        )
                        classToEdit = null
                    },
                    enabled = editName.isNotBlank()
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { classToEdit = null }) { Text("Batal") }
            }
        )
    }

    if (classToDelete != null) {
        val target = classToDelete!!
        AlertDialog(
            onDismissRequest = { classToDelete = null },
            title = { Text("Hapus Kelas ${target.name}?") },
            text = { Text("Seluruh data siswa dan presensi di kelas ini juga akan terhapus. Yakin ingin menghapus?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteClass(target)
                        classToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus Permanen")
                }
            },
            dismissButton = {
                TextButton(onClick = { classToDelete = null }) { Text("Batal") }
            }
        )
    }
}

// TAB 3: Master Subject
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MasterSubjectTab(viewModel: AttendanceViewModel) {
    val teacherSubject by viewModel.teacherSubject.collectAsState()
    val teacherSubjectsList = remember(teacherSubject) { viewModel.getTeacherSubjectList() }

    var newSubjectInput by remember { mutableStateOf("") }

    val defaultAklSubjects = listOf("Akuntansi Keuangan", "Praktikum Akuntansi Perusahaan", "Perbankan Dasar", "Etika Profesi", "Informatika / SIM")
    val defaultMplbSubjects = listOf("Otomatisasi Tata Kelola Perkantoran", "Kearsipan Digital", "Korespondensi Bahasa", "Humas & Protokol", "Informatika / SIM")

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Book, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Master Mata Pelajaran Terpusat (Step 3)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Text(
                        "Seluruh mata pelajaran di sini akan otomatis terdeteksi di dropdown 'Input Absensi', 'Laporan', dan 'Jadwal Mengajar'.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newSubjectInput,
                            onValueChange = { newSubjectInput = it },
                            label = { Text("Tambah Mapel Manual") },
                            placeholder = { Text("misal: Keuangan Publik") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (newSubjectInput.isNotBlank()) {
                                    viewModel.addTeacherSubject(newSubjectInput.trim())
                                    newSubjectInput = ""
                                }
                            },
                            enabled = newSubjectInput.isNotBlank(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Tambah")
                        }
                    }

                    Divider()

                    Text("Mapel Aktif Terdaftar (${teacherSubjectsList.size}):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        teacherSubjectsList.forEach { sub ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                        Text(sub, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    }

                                    IconButton(
                                        onClick = {
                                            viewModel.removeTeacherSubject(sub)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Atur Cepat Mapel Standar Jurusan (Dropdown):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                    var dropdownExpanded by remember { mutableStateOf(false) }
                    var selectedPresetText by remember { mutableStateOf("Pilih Mapel Standar dari Dropdown...") }

                    val mapelCategories = listOf(
                        "AKL - Akuntansi Keuangan" to defaultAklSubjects,
                        "MPLB - Perkantoran" to defaultMplbSubjects,
                        "Umum / Wajib" to listOf("Matematika", "Bahasa Indonesia", "Bahasa Inggris", "Pendidikan Agama", "Pancasila / PPKn", "Sejarah Indonesia")
                    )

                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedPresetText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilih Mapel dari Dropdown") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            mapelCategories.forEach { (categoryName, subjects) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            categoryName,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 12.sp
                                        )
                                    },
                                    onClick = {},
                                    enabled = false
                                )
                                subjects.forEach { sub ->
                                    val exists = teacherSubjectsList.contains(sub)
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(sub, fontSize = 13.sp)
                                                if (exists) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = "Sudah Ada",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                } else {
                                                    Icon(
                                                        Icons.Default.Add,
                                                        contentDescription = "Tambah",
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            if (!exists) {
                                                viewModel.addTeacherSubject(sub)
                                            }
                                            selectedPresetText = sub
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                                Divider()
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                defaultAklSubjects.forEach { viewModel.addTeacherSubject(it) }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+ Semua Mapel AKL", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                defaultMplbSubjects.forEach { viewModel.addTeacherSubject(it) }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+ Semua Mapel MPLB", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// TAB 4: Master Student (Single Add & Bulk Import)
@Composable
private fun MasterStudentTab(viewModel: AttendanceViewModel) {
    val allClasses by viewModel.allClasses.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()

    var selectedClassId by remember { mutableStateOf<Long?>(null) }
    var singleName by remember { mutableStateOf("") }
    var singleNisn by remember { mutableStateOf("") }
    var singleGender by remember { mutableStateOf("L") }
    var singlePhone by remember { mutableStateOf("") }

    var rawTextImport by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    var studentToEdit by remember { mutableStateOf<Student?>(null) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }

    LaunchedEffect(allClasses) {
        if (selectedClassId == null && allClasses.isNotEmpty()) {
            selectedClassId = allClasses.first().id
        }
    }

    val selectedClassStudents = remember(allStudents, selectedClassId) {
        if (selectedClassId == null) emptyList()
        else allStudents.filter { it.classId == selectedClassId }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        if (allClasses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Belum Ada Kelas Terdaftar", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                        Text("Silakan buat kelas di tab '2. Master Kelas' terlebih dahulu.", fontSize = 12.sp)
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("1. Pilih Kelas Target Siswa (Step 4):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(allClasses) { cls ->
                                FilterChip(
                                    selected = selectedClassId == cls.id,
                                    onClick = { selectedClassId = cls.id },
                                    label = { Text("${cls.name} (${cls.major})") },
                                    leadingIcon = {
                                        if (selectedClassId == cls.id) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("2. Tambah Siswa Manual Satu per Satu:", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        OutlinedTextField(
                            value = singleName,
                            onValueChange = { singleName = it },
                            label = { Text("Nama Lengkap Siswa") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = singleNisn,
                                onValueChange = { singleNisn = it },
                                label = { Text("NISN / NIS") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Gender:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    FilterChip(selected = singleGender == "L", onClick = { singleGender = "L" }, label = { Text("L") })
                                    FilterChip(selected = singleGender == "P", onClick = { singleGender = "P" }, label = { Text("P") })
                                }
                            }
                        }

                        OutlinedTextField(
                            value = singlePhone,
                            onValueChange = { singlePhone = it },
                            label = { Text("No. HP / WA Orang Tua (Opsional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                val targetId = selectedClassId
                                if (targetId != null && singleName.isNotBlank()) {
                                    viewModel.addOrUpdateStudent(
                                        id = 0L,
                                        classId = targetId,
                                        nisn = singleNisn.trim(),
                                        name = singleName.trim(),
                                        gender = singleGender,
                                        phone = singlePhone.trim()
                                    )
                                    singleName = ""
                                    singleNisn = ""
                                    singlePhone = ""
                                }
                            },
                            enabled = selectedClassId != null && singleName.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simpan Siswa Ke Kelas")
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("3. Impor Masal Banyak Siswa Sekaligus", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(
                            "Tempel daftar nama siswa dari WA / Excel di bawah ini (1 nama per baris):",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        OutlinedTextField(
                            value = rawTextImport,
                            onValueChange = { rawTextImport = it },
                            placeholder = { Text("Contoh:\nAndi Pratama\nBudi Santoso, 1002, L\nCitra Lestari, 1003, P, 081234567") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .testTag("bulk_student_input"),
                            maxLines = 10
                        )

                        val lineCount = remember(rawTextImport) {
                            rawTextImport.split("\n", "\r\n").count { it.isNotBlank() }
                        }

                        Button(
                            onClick = {
                                val targetId = selectedClassId
                                if (targetId != null && rawTextImport.isNotBlank()) {
                                    isProcessing = true
                                    viewModel.importStudentsBatch(targetId, rawTextImport) { count ->
                                        isProcessing = false
                                        rawTextImport = ""
                                    }
                                }
                            },
                            enabled = selectedClassId != null && rawTextImport.isNotBlank() && !isProcessing,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.GroupAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isProcessing) "Memproses..." else "Impor $lineCount Siswa Sekaligus")
                        }
                    }
                }
            }

            item {
                Text(
                    "Daftar Siswa di Kelas Ini (${selectedClassStudents.size} Orang):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            if (selectedClassStudents.isEmpty()) {
                item {
                    Text("Belum ada siswa terdaftar di kelas ini.", fontSize = 13.sp, color = Color.Gray)
                }
            } else {
                items(selectedClassStudents) { student ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (student.gender == "L") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(student.gender, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }

                                Column {
                                    Text(student.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("NISN: ${student.nisn.ifBlank { "-" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Row {
                                IconButton(onClick = { studentToEdit = student }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Siswa", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { studentToDelete = student }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus Siswa", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (studentToEdit != null) {
        val st = studentToEdit!!
        var eName by remember { mutableStateOf(st.name) }
        var eNisn by remember { mutableStateOf(st.nisn) }
        var eGender by remember { mutableStateOf(st.gender) }
        var ePhone by remember { mutableStateOf(st.phone) }

        AlertDialog(
            onDismissRequest = { studentToEdit = null },
            title = { Text("Edit Master Siswa") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = eName, onValueChange = { eName = it }, label = { Text("Nama Siswa") }, singleLine = true)
                    OutlinedTextField(value = eNisn, onValueChange = { eNisn = it }, label = { Text("NISN") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = eGender == "L", onClick = { eGender = "L" }, label = { Text("Laki-laki") })
                        FilterChip(selected = eGender == "P", onClick = { eGender = "P" }, label = { Text("Perempuan") })
                    }
                    OutlinedTextField(value = ePhone, onValueChange = { ePhone = it }, label = { Text("No. HP Orang Tua") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addOrUpdateStudent(
                            id = st.id,
                            classId = st.classId,
                            nisn = eNisn.trim(),
                            name = eName.trim(),
                            gender = eGender,
                            phone = ePhone.trim()
                        )
                        studentToEdit = null
                    },
                    enabled = eName.isNotBlank()
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToEdit = null }) { Text("Batal") }
            }
        )
    }

    if (studentToDelete != null) {
        val st = studentToDelete!!
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = { Text("Hapus Siswa ${st.name}?") },
            text = { Text("Data absensi siswa ini juga akan terhapus. Yakin hapus?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStudent(st)
                        studentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus Siswa")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) { Text("Batal") }
            }
        )
    }
}

// TAB 5: Master Schedule (Add, Edit, Delete Schedules)
@Composable
private fun MasterScheduleTab(viewModel: AttendanceViewModel) {
    val allSchedules by viewModel.allSchedules.collectAsState()
    val allClasses by viewModel.allClasses.collectAsState()
    val teacherSubject by viewModel.teacherSubject.collectAsState()
    val teacherSubjectsList = remember(teacherSubject) { viewModel.getTeacherSubjectList() }

    var showAddScheduleDialog by remember { mutableStateOf(false) }
    var scheduleToEdit by remember { mutableStateOf<TeachingSchedule?>(null) }
    var scheduleToDelete by remember { mutableStateOf<TeachingSchedule?>(null) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Master Jadwal Mengajar & Alarm Bell (Step 5)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Text(
                        "Jadwal mengajar ini akan otomatis mendeteksi mata pelajaran dan memicu alarm bel jam masuk/selesai.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            scheduleToEdit = null
                            showAddScheduleDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tambah Master Jadwal Baru")
                    }
                }
            }
        }

        item {
            Text("Daftar Master Jadwal Mengajar (${allSchedules.size}):", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        if (allSchedules.isEmpty()) {
            item {
                Text("Belum ada jadwal mengajar. Tambahkan jadwal di atas.", fontSize = 13.sp, color = Color.Gray)
            }
        } else {
            items(allSchedules) { schedule ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(schedule.dayOfWeek, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Text("${schedule.startTime} - ${schedule.endTime}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text("${schedule.className} • ${schedule.subject}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Ruang: ${schedule.room}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Row {
                            IconButton(onClick = {
                                scheduleToEdit = schedule
                                showAddScheduleDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Jadwal", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { scheduleToDelete = schedule }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus Jadwal", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddScheduleDialog) {
        AddEditScheduleDialog(
            schedule = scheduleToEdit,
            allClasses = allClasses,
            masterSubjects = teacherSubjectsList,
            onDismiss = { showAddScheduleDialog = false },
            onSave = { id, day, className, subject, startTime, endTime, room, isAlarm ->
                viewModel.addOrUpdateSchedule(id, day, className, subject, startTime, endTime, room, isAlarm)
                showAddScheduleDialog = false
            }
        )
    }

    if (scheduleToDelete != null) {
        val target = scheduleToDelete!!
        AlertDialog(
            onDismissRequest = { scheduleToDelete = null },
            title = { Text("Hapus Jadwal Mengajar?") },
            text = { Text("Yakin ingin menghapus jadwal ${target.className} - ${target.subject}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSchedule(target)
                        scheduleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { scheduleToDelete = null }) { Text("Batal") }
            }
        )
    }
}

// TAB 6: Backup & Riset
@Composable
private fun BackupAndResetTab(viewModel: AttendanceViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonText by remember { mutableStateOf("") }

    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var confirmResetInput by remember { mutableStateOf("") }

    // Backup Export File Launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = viewModel.generateBackupJson()
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(json.toByteArray(Charsets.UTF_8))
                    }
                    Toast.makeText(context, "Backup berhasil disimpan!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal menyimpan backup: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Backup & Cadangan Data JSON (Step 6)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Text(
                        "Simpan cadangan seluruh data kelas, siswa, absensi, dan jadwal ke dalam file JSON agar aman saat berpindah HP.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { exportLauncher.launch("Backup_Absensi_${System.currentTimeMillis()}.json") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export JSON")
                        }

                        OutlinedButton(
                            onClick = { showRestoreDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import Backup")
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Text("Riset Data Total (Aplikasi Kosong)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.error)
                    }

                    Text(
                        "Gunakan fitur ini jika Anda ingin mengosongkan seluruh data bawaan aplikasi agar siap diisi dengan kelas & daftar siswa baru sekolah Anda.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )

                    Button(
                        onClick = { showResetConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_reset_all_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kosongkan Semua Data Sekarang")
                    }
                }
            }
        }
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Pulihkan / Import Data Backup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tempel teks isi file JSON backup Anda di bawah ini:", fontSize = 12.sp)
                    OutlinedTextField(
                        value = restoreJsonText,
                        onValueChange = { restoreJsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        placeholder = { Text("{\"classes\": ...}") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.restoreBackup(restoreJsonText) { success ->
                            if (success) {
                                showRestoreDialog = false
                                restoreJsonText = ""
                            }
                        }
                    },
                    enabled = restoreJsonText.isNotBlank()
                ) {
                    Text("Jalankan Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showResetConfirmDialog = false
                confirmResetInput = ""
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text("Konfirmasi Kosongkan Aplikasi", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Seluruh data kelas, siswa, absensi, dan jadwal akan dihapus permanen. Aplikasi akan menjadi kosong total.\n\nKetik kata 'RISET' untuk mengonfirmasi:",
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = confirmResetInput,
                        onValueChange = { confirmResetInput = it },
                        label = { Text("Ketik RISET") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("confirm_reset_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllApplicationData()
                        showResetConfirmDialog = false
                        confirmResetInput = ""
                    },
                    enabled = confirmResetInput.trim().equals("RISET", ignoreCase = true) || confirmResetInput.trim().equals("HAPUS", ignoreCase = true),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("execute_reset_btn")
                ) {
                    Text("Kosongkan Total Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
