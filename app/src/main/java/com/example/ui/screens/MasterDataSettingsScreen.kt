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
import com.example.data.model.ClassGroup
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
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Profil & PIN", "Impor Siswa Masal", "Master Mapel", "Backup & Riset")

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Pengaturan & Master Data", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Pusat Kontrol Data & Sistem Aplikasi", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            // Tab Selector Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.Badge
                                    1 -> Icons.Default.GroupAdd
                                    2 -> Icons.Default.Book
                                    else -> Icons.Default.SettingsBackupRestore
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> ProfileAndSecurityTab(viewModel = viewModel)
                    1 -> BulkStudentImportTab(viewModel = viewModel, onNavigateToClasses = onNavigateToClasses)
                    2 -> MasterSubjectTab(viewModel = viewModel)
                    3 -> BackupAndResetTab(viewModel = viewModel)
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
    val savedPin by viewModel.savedPin.collectAsState()

    var nameState by remember(teacherName) { mutableStateOf(teacherName) }
    var nipState by remember(teacherNip) { mutableStateOf(teacherNip) }
    var subjectState by remember(teacherSubject) { mutableStateOf(teacherSubject) }
    var schoolState by remember(schoolName) { mutableStateOf(schoolName) }

    var showPinDialog by remember { mutableStateOf(false) }

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
                        Text("Identitas & Profil Guru", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                        label = { Text("Mata Pelajaran yang Diampu (pisahkan koma)") },
                        placeholder = { Text("Contoh: Akuntansi, Keuangan, Etika Profesi") },
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
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Perubahan Profil")
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
}

// TAB 2: Bulk Student Import
@Composable
private fun BulkStudentImportTab(
    viewModel: AttendanceViewModel,
    onNavigateToClasses: () -> Unit
) {
    val allClasses by viewModel.allClasses.collectAsState()
    var selectedClassId by remember { mutableStateOf<Long?>(null) }
    var rawTextImport by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(allClasses) {
        if (selectedClassId == null && allClasses.isNotEmpty()) {
            selectedClassId = allClasses.first().id
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Impor Banyak Siswa Sekaligus", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Text(
                        "Anda dapat menyalin (copy) daftar nama siswa dari WhatsApp, Word, Excel, atau file teks dan menempelkannya di sini. Aplikasi akan otomatis memasukkan semua nama ke dalam kelas!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

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
                        Text("Buat kelas terlebih dahulu sebelum mengimpor siswa.", fontSize = 12.sp)
                        Button(onClick = onNavigateToClasses) {
                            Text("Tambah Kelas Sekarang")
                        }
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
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("1. Pilih Kelas Tujuan:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(allClasses) { cls ->
                                FilterChip(
                                    selected = selectedClassId == cls.id,
                                    onClick = { selectedClassId = cls.id },
                                    label = { Text(cls.name) },
                                    leadingIcon = {
                                        if (selectedClassId == cls.id) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                )
                            }
                        }

                        Divider()

                        Text("2. Tempel / Ketik Daftar Siswa:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(
                            "Format per baris (bebas):\n" +
                                    "• Nama Saja (Contoh: Ahmad Maulana)\n" +
                                    "• Nama, NISN, Gender (Contoh: Budi Santoso, 1002, L)\n" +
                                    "• Nama, NISN, Gender, NoHP (Contoh: Citra Dewi, 1003, P, 08123456)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = rawTextImport,
                            onValueChange = { rawTextImport = it },
                            placeholder = { Text("Contoh:\nAndi Pratama\nBudi Santoso, 1002, L\nCitra Lestari, 1003, P, 081234567") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .testTag("bulk_student_input"),
                            maxLines = 15
                        )

                        val lineCount = remember(rawTextImport) {
                            rawTextImport.split("\n", "\r\n").count { it.isNotBlank() }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Terdeteksi $lineCount Calon Siswa",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (rawTextImport.isNotBlank()) {
                                TextButton(onClick = { rawTextImport = "" }) {
                                    Text("Bersihkan Teks")
                                }
                            }
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("execute_bulk_import_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isProcessing) "Memproses Data..." else "Proses & Simpan $lineCount Siswa Sekaligus")
                        }
                    }
                }
            }
        }
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
                        Text("Master Mata Pelajaran Guru", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Text(
                        "Daftar mata pelajaran ini akan otomatis muncul sebagai pilihan utama saat Anda melakukan presensi harian atau membuat jadwal mengajar.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newSubjectInput,
                            onValueChange = { newSubjectInput = it },
                            label = { Text("Tambah Mapel Baru") },
                            placeholder = { Text("misal: Keuangan Publik") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (newSubjectInput.isNotBlank()) {
                                    val currentList = viewModel.getTeacherSubjectList().toMutableList()
                                    if (!currentList.contains(newSubjectInput.trim())) {
                                        currentList.add(newSubjectInput.trim())
                                        val updatedStr = currentList.joinToString(", ")
                                        viewModel.updateTeacherProfile(
                                            name = viewModel.teacherName.value,
                                            nip = viewModel.teacherNip.value,
                                            subject = updatedStr,
                                            school = viewModel.schoolName.value
                                        )
                                    }
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

                    Text("Mapel Aktif Anda (${teacherSubjectsList.size}):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

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
                                            val currentList = viewModel.getTeacherSubjectList().toMutableList()
                                            currentList.remove(sub)
                                            val updatedStr = currentList.joinToString(", ")
                                            viewModel.updateTeacherProfile(
                                                name = viewModel.teacherName.value,
                                                nip = viewModel.teacherNip.value,
                                                subject = updatedStr.ifBlank { "Absensi Harian" },
                                                school = viewModel.schoolName.value
                                            )
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
                    Text("Tambah Cepat Mapel Standar Jurusan:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

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
                                                val currentList = viewModel.getTeacherSubjectList().toMutableList()
                                                currentList.add(sub)
                                                viewModel.updateTeacherProfile(
                                                    name = viewModel.teacherName.value,
                                                    nip = viewModel.teacherNip.value,
                                                    subject = currentList.joinToString(", "),
                                                    school = viewModel.schoolName.value
                                                )
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
                                val currentList = viewModel.getTeacherSubjectList().toMutableList()
                                defaultAklSubjects.forEach { if (!currentList.contains(it)) currentList.add(it) }
                                viewModel.updateTeacherProfile(
                                    name = viewModel.teacherName.value,
                                    nip = viewModel.teacherNip.value,
                                    subject = currentList.joinToString(", "),
                                    school = viewModel.schoolName.value
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+ Semua Mapel AKL", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val currentList = viewModel.getTeacherSubjectList().toMutableList()
                                defaultMplbSubjects.forEach { if (!currentList.contains(it)) currentList.add(it) }
                                viewModel.updateTeacherProfile(
                                    name = viewModel.teacherName.value,
                                    nip = viewModel.teacherNip.value,
                                    subject = currentList.joinToString(", "),
                                    school = viewModel.schoolName.value
                                )
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

// TAB 4: Backup & Riset
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
                        Text("Backup & Cadangan Data JSON", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
