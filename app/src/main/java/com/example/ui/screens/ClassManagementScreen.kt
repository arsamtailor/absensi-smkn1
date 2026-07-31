package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClassGroup
import com.example.data.model.Student
import com.example.ui.AttendanceViewModel

import com.example.ui.theme.HighDensityOutline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassManagementScreen(
    viewModel: AttendanceViewModel,
    onStartTakeAttendance: (ClassGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    val allClasses by viewModel.allClasses.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()

    var selectedClass by remember { mutableStateOf<ClassGroup?>(null) }

    // Dialog States
    var showAddClassDialog by remember { mutableStateOf(false) }
    var classToEdit by remember { mutableStateOf<ClassGroup?>(null) }

    var showAddStudentDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<Student?>(null) }
    var showBulkImportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedClass == null) "Kelola Kelas & Siswa" else "Siswa - ${selectedClass?.name}",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (selectedClass != null) {
                        IconButton(onClick = { selectedClass = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                        }
                    }
                },
                actions = {
                    if (selectedClass == null) {
                        IconButton(
                            onClick = {
                                classToEdit = null
                                showAddClassDialog = true
                            },
                            modifier = Modifier.testTag("add_class_appbar_btn")
                        ) {
                            Icon(Icons.Default.AddBusiness, contentDescription = "Tambah Kelas")
                        }
                    } else {
                        val currentCls = selectedClass
                        if (currentCls != null) {
                            IconButton(
                                onClick = { showBulkImportDialog = true },
                                modifier = Modifier.testTag("bulk_import_students_btn")
                            ) {
                                Icon(Icons.Default.GroupAdd, contentDescription = "Impor Masal")
                            }
                            IconButton(
                                onClick = { onStartTakeAttendance(currentCls) },
                                modifier = Modifier.testTag("take_attendance_for_class_btn")
                            ) {
                                Icon(Icons.Default.PlaylistAddCheck, contentDescription = "Input Absensi")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (selectedClass == null) {
                FloatingActionButton(
                    onClick = {
                        classToEdit = null
                        showAddClassDialog = true
                    },
                    modifier = Modifier.testTag("add_class_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Kelas")
                }
            } else {
                FloatingActionButton(
                    onClick = {
                        studentToEdit = null
                        showAddStudentDialog = true
                    },
                    modifier = Modifier.testTag("add_student_fab")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Tambah Siswa")
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val currentSelectedClass = selectedClass
            if (currentSelectedClass == null) {
                var selectedMajorFilter by remember { mutableStateOf("SEMUA") }

                val filteredClasses = remember(allClasses, selectedMajorFilter) {
                    if (selectedMajorFilter == "SEMUA") allClasses
                    else allClasses.filter { it.major.equals(selectedMajorFilter, ignoreCase = true) }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    // Major Filter Bar
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Jurusan:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            FilterChip(
                                selected = selectedMajorFilter == "SEMUA",
                                onClick = { selectedMajorFilter = "SEMUA" },
                                label = { Text("Semua") }
                            )
                            FilterChip(
                                selected = selectedMajorFilter == "AKL",
                                onClick = { selectedMajorFilter = "AKL" },
                                label = { Text("AKL (Akuntansi)") }
                            )
                            FilterChip(
                                selected = selectedMajorFilter == "MPLB",
                                onClick = { selectedMajorFilter = "MPLB" },
                                label = { Text("MPLB (Perkantoran)") }
                            )
                        }
                    }

                    if (allClasses.isEmpty()) {
                        EmptyClassState(onAddClass = {
                            classToEdit = null
                            showAddClassDialog = true
                        })
                    } else if (filteredClasses.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Tidak ada kelas untuk jurusan $selectedMajorFilter",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
                        ) {
                            items(filteredClasses) { classGroup ->
                                val studentCount = allStudents.count { it.classId == classGroup.id }
                                ClassGroupCard(
                                    classGroup = classGroup,
                                    studentCount = studentCount,
                                    onClick = { selectedClass = classGroup },
                                    onEdit = {
                                        classToEdit = classGroup
                                        showAddClassDialog = true
                                    },
                                    onDelete = { viewModel.deleteClass(classGroup) },
                                    onTakeAttendance = { onStartTakeAttendance(classGroup) }
                                )
                            }
                        }
                    }
                }
            } else {
                // View Students in Selected Class
                val studentsInClass = allStudents.filter { it.classId == currentSelectedClass.id }

                Column(modifier = Modifier.fillMaxSize()) {
                    // Class Header Info Banner
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = currentSelectedClass.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "Tahun Ajaran: ${currentSelectedClass.academicYear} • Total: ${studentsInClass.size} Siswa",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (currentSelectedClass.description.isNotBlank()) {
                                    Text(
                                        text = currentSelectedClass.description,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Button(
                                onClick = { onStartTakeAttendance(currentSelectedClass) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Absensi", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (studentsInClass.isEmpty()) {
                        EmptyStudentState(onAddStudent = {
                            studentToEdit = null
                            showAddStudentDialog = true
                        })
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
                        ) {
                            items(studentsInClass) { student ->
                                StudentCard(
                                    student = student,
                                    onEdit = {
                                        studentToEdit = student
                                        showAddStudentDialog = true
                                    },
                                    onDelete = { viewModel.deleteStudent(student) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Class Dialog
    if (showAddClassDialog) {
        AddEditClassDialog(
            classGroup = classToEdit,
            onDismiss = { showAddClassDialog = false },
            onSave = { name, academicYear, major, description ->
                viewModel.addOrUpdateClass(
                    id = classToEdit?.id ?: 0L,
                    name = name,
                    academicYear = academicYear,
                    major = major,
                    description = description
                )
                showAddClassDialog = false
            }
        )
    }

    // Add / Edit Student Dialog
    val activeClassForStudent = selectedClass
    if (showAddStudentDialog && activeClassForStudent != null) {
        AddEditStudentDialog(
            student = studentToEdit,
            onDismiss = { showAddStudentDialog = false },
            onSave = { nisn, name, gender, phone ->
                viewModel.addOrUpdateStudent(
                    id = studentToEdit?.id ?: 0L,
                    classId = activeClassForStudent.id,
                    nisn = nisn,
                    name = name,
                    gender = gender,
                    phone = phone
                )
                showAddStudentDialog = false
            }
        )
    }

    if (showBulkImportDialog && activeClassForStudent != null) {
        BulkImportStudentsDialog(
            className = activeClassForStudent.name,
            onDismiss = { showBulkImportDialog = false },
            onImport = { rawText ->
                viewModel.importStudentsBatch(activeClassForStudent.id, rawText) { _ ->
                    showBulkImportDialog = false
                }
            }
        )
    }
}

@Composable
fun ClassGroupCard(
    classGroup: ClassGroup,
    studentCount: Int,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTakeAttendance: () -> Unit
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = classGroup.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
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
                        }
                        Text(
                            text = "Tahun Ajaran: ${classGroup.academicYear}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Kelas", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus Kelas", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (classGroup.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = classGroup.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$studentCount Siswa",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = onTakeAttendance,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Presensi")
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Kelas ${classGroup.name}?") },
            text = { Text("Menghapus kelas ini juga akan menghapus seluruh data siswa dan riwayat presensi terkait.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus Permanent")
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
fun StudentCard(
    student: Student,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

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
        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityOutline)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                        text = "NISN: ${if (student.nisn.isBlank()) "-" else student.nisn}${if (student.phone.isNotBlank()) " • ${student.phone}" else ""}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Siswa", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus Siswa", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Siswa?") },
            text = { Text("Apakah Anda yakin ingin menghapus ${student.name} dari kelas ini?") },
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
fun EmptyClassState(onAddClass: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.School,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Belum Ada Kelas Terdaftar",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tambahkan kelas baru untuk mulai memasukkan daftar siswa dan presensi.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onAddClass) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tambah Kelas Pertama")
        }
    }
}

@Composable
fun EmptyStudentState(onAddStudent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PersonOff,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Belum Ada Siswa di Kelas Ini",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tambahkan anggota siswa untuk kelas ini.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddStudent) {
            Icon(Icons.Default.PersonAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tambah Siswa Baru")
        }
    }
}

@Composable
fun AddEditClassDialog(
    classGroup: ClassGroup?,
    onDismiss: () -> Unit,
    onSave: (name: String, academicYear: String, major: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf(classGroup?.name ?: "") }
    var academicYear by remember { mutableStateOf(classGroup?.academicYear ?: "2025/2026") }
    var major by remember { mutableStateOf(classGroup?.major ?: "AKL") }
    var description by remember { mutableStateOf(classGroup?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (classGroup == null) "Tambah Kelas Baru" else "Edit Kelas", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column {
                    Text("Jurusan / Keahlian:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = major == "AKL",
                            onClick = { major = "AKL" },
                            label = { Text("AKL (Akuntansi)") }
                        )
                        FilterChip(
                            selected = major == "MPLB",
                            onClick = { major = "MPLB" },
                            label = { Text("MPLB (Perkantoran)") }
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Kelas (Contoh: X AKL 1)") },
                    modifier = Modifier.fillMaxWidth().testTag("class_name_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = academicYear,
                    onValueChange = { academicYear = it },
                    label = { Text("Tahun Ajaran") },
                    modifier = Modifier.fillMaxWidth().testTag("class_academic_year_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Keterangan / Wali Kelas (Opsional)") },
                    modifier = Modifier.fillMaxWidth().testTag("class_desc_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, academicYear, major, description) },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_class_btn")
            ) {
                Text("Simpan")
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
fun AddEditStudentDialog(
    student: Student?,
    onDismiss: () -> Unit,
    onSave: (nisn: String, name: String, gender: String, phone: String) -> Unit
) {
    var nisn by remember { mutableStateOf(student?.nisn ?: "") }
    var name by remember { mutableStateOf(student?.name ?: "") }
    var gender by remember { mutableStateOf(student?.gender ?: "L") }
    var phone by remember { mutableStateOf(student?.phone ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (student == null) "Tambah Siswa Baru" else "Edit Data Siswa", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lengkap Siswa") },
                    modifier = Modifier.fillMaxWidth().testTag("student_name_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = nisn,
                    onValueChange = { nisn = it },
                    label = { Text("NISN / Nomor Induk") },
                    modifier = Modifier.fillMaxWidth().testTag("student_nisn_input"),
                    singleLine = true
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Jenis Kelamin:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    FilterChip(
                        selected = gender == "L",
                        onClick = { gender = "L" },
                        label = { Text("Laki-Laki (L)") }
                    )
                    FilterChip(
                        selected = gender == "P",
                        onClick = { gender = "P" },
                        label = { Text("Perempuan (P)") }
                    )
                }
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("No. HP Orang Tua / Siswa (Opsional)") },
                    modifier = Modifier.fillMaxWidth().testTag("student_phone_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nisn, name, gender, phone) },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_student_btn")
            ) {
                Text("Simpan")
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
fun BulkImportStudentsDialog(
    className: String,
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var rawText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.GroupAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Impor Siswa Masal - $className", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Tempel / ketik daftar siswa per baris:\n" +
                            "Contoh:\n" +
                            "Andi Pratama\n" +
                            "Budi Santoso, 1002, L\n" +
                            "Citra Dewi, 1003, P, 08123456",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    placeholder = { Text("Ahmad Maulana\nBudi Santoso, 1002, L\nCitra Dewi, 1003, P") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("bulk_import_dialog_input"),
                    maxLines = 15
                )

                val count = remember(rawText) {
                    rawText.split("\n", "\r\n").count { it.isNotBlank() }
                }

                Text(
                    "Total $count calon siswa terdeteksi",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onImport(rawText) },
                enabled = rawText.isNotBlank(),
                modifier = Modifier.testTag("submit_bulk_import_btn")
            ) {
                Text("Impor Sekarang")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
