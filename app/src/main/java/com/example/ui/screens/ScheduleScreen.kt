package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
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
import com.example.data.model.TeachingSchedule
import com.example.ui.AttendanceViewModel
import com.example.util.AlarmUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: AttendanceViewModel,
    onStartTakeAttendanceForSchedule: (className: String, subject: String) -> Unit,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val allSchedules by viewModel.allSchedules.collectAsState()
    val allClasses by viewModel.allClasses.collectAsState()
    val schoolName by viewModel.schoolName.collectAsState()
    val context = LocalContext.current

    var selectedDayFilter by remember { mutableStateOf("SEMUA") }
    var showSettingsPromptDialog by remember { mutableStateOf(false) }

    val daysList = listOf("SEMUA", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")

    val filteredSchedules = remember(allSchedules, selectedDayFilter) {
        if (selectedDayFilter == "SEMUA") allSchedules
        else allSchedules.filter { it.dayOfWeek.equals(selectedDayFilter, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Jadwal Mengajar & Alarm", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("${schoolName.ifBlank { "SMKN 1 Cirinten" }} • AKL & MPLB", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            AlarmUtil.playBellAlarm(context, isStart = true)
                        },
                        modifier = Modifier.testTag("test_bell_btn")
                    ) {
                        Icon(Icons.Filled.NotificationsActive, contentDescription = "Tes Alarm Bell", tint = MaterialTheme.colorScheme.primary)
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
            // Compact Alarm Bell & Master Lock Strip
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                        Column {
                            Text("Alarm Bel Jam Mengajar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Bunyikan bel saat waktu masuk & habis kelas", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = { AlarmUtil.playBellAlarm(context, isStart = true) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("🔊 Masuk", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { AlarmUtil.playBellAlarm(context, isStart = false) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("🔔 Habis", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Day Filter Chips Bar
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(daysList.size) { index ->
                    val day = daysList[index]
                    FilterChip(
                        selected = selectedDayFilter == day,
                        onClick = { selectedDayFilter = day },
                        label = { Text(day, fontSize = 12.sp) }
                    )
                }
            }

            if (filteredSchedules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (selectedDayFilter == "SEMUA") "Belum ada jadwal mengajar." else "Tidak ada jadwal di hari $selectedDayFilter",
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onOpenSettings
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Atur Jadwal di Master Data")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)
                ) {
                    // Header Column Table Row
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("HARI & WAKTU", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White, modifier = Modifier.weight(1.1f))
                                Text("MAPEL & KELAS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White, modifier = Modifier.weight(1.4f))
                                Text("AKSI PRESENSI", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White, modifier = Modifier.weight(1.1f))
                            }
                        }
                    }

                    items(filteredSchedules) { schedule ->
                        ScheduleColumnCard(
                            schedule = schedule,
                            onToggleAlarm = { viewModel.toggleScheduleAlarm(schedule) },
                            onPlayStartAlarm = { AlarmUtil.playBellAlarm(context, isStart = true) },
                            onPlayEndAlarm = { AlarmUtil.playBellAlarm(context, isStart = false) },
                            onStartAttendance = {
                                onStartTakeAttendanceForSchedule(schedule.className, schedule.subject)
                            },
                            onOpenSettings = onOpenSettings
                        )
                    }
                }
            }
        }
    }

    if (showSettingsPromptDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsPromptDialog = false },
            icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Master Jadwal Mengajar", fontWeight = FontWeight.Bold) },
            text = {
                Text("Penambahan, pengeditan, dan penghapusan jadwal mengajar dikelola secara terpusat di menu Pengaturan Master Data.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSettingsPromptDialog = false
                        onOpenSettings()
                    }
                ) {
                    Text("Buka Pengaturan Master Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsPromptDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
fun ScheduleColumnCard(
    schedule: TeachingSchedule,
    onToggleAlarm: () -> Unit,
    onPlayStartAlarm: () -> Unit,
    onPlayEndAlarm: () -> Unit,
    onStartAttendance: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // KOLOM 1: Hari & Waktu
                Column(
                    modifier = Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = schedule.dayOfWeek,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "${schedule.startTime} -\n${schedule.endTime}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            lineHeight = 13.sp
                        )
                    }

                    if (schedule.room.isNotBlank()) {
                        Text(
                            text = "📍 ${schedule.room}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                Divider(
                    modifier = Modifier
                        .height(65.dp)
                        .width(1.dp)
                        .padding(horizontal = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // KOLOM 2: Mapel & Kelas
                Column(
                    modifier = Modifier
                        .weight(1.4f)
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = schedule.subject,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        maxLines = 2
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "Kelas: ${schedule.className}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Divider(
                    modifier = Modifier
                        .height(65.dp)
                        .width(1.dp)
                        .padding(horizontal = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // KOLOM 3: Aksi Presensi & Bell
                Column(
                    modifier = Modifier.weight(1.1f),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = onStartAttendance,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Isi Absen", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = onToggleAlarm, modifier = Modifier.size(26.dp)) {
                            Icon(
                                imageVector = if (schedule.isAlarmEnabled) Icons.Outlined.NotificationsActive else Icons.Outlined.NotificationsOff,
                                contentDescription = "Toggle Alarm",
                                tint = if (schedule.isAlarmEnabled) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            FilledTonalIconButton(onClick = onPlayStartAlarm, modifier = Modifier.size(26.dp)) {
                                Text("🔊", fontSize = 11.sp)
                            }
                            FilledTonalIconButton(onClick = onPlayEndAlarm, modifier = Modifier.size(26.dp)) {
                                Text("🔔", fontSize = 11.sp)
                            }
                        }

                        Box {
                            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More", modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("Kelola Jadwal di Pengaturan", fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    onClick = {
                                        showMenu = false
                                        onOpenSettings()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScheduleDialog(
    schedule: TeachingSchedule?,
    allClasses: List<ClassGroup>,
    masterSubjects: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (id: Long, day: String, className: String, subject: String, startTime: String, endTime: String, room: String, isAlarm: Boolean) -> Unit
) {
    var dayOfWeek by remember { mutableStateOf(schedule?.dayOfWeek ?: "Senin") }
    var className by remember { mutableStateOf(schedule?.className ?: (allClasses.firstOrNull()?.name ?: "X AKL 1")) }
    
    val availableSubjects = remember(masterSubjects) {
        if (masterSubjects.isNotEmpty()) masterSubjects else listOf("Akuntansi Keuangan", "Praktikum Akuntansi", "Otomatisasi Perkantoran", "Matematika", "Bahasa Indonesia")
    }

    var subject by remember { mutableStateOf(schedule?.subject ?: availableSubjects.first()) }
    var subjectDropdownExpanded by remember { mutableStateOf(false) }

    var startTime by remember { mutableStateOf(schedule?.startTime ?: "07:00") }
    var endTime by remember { mutableStateOf(schedule?.endTime ?: "09:00") }
    var room by remember { mutableStateOf(schedule?.room ?: "Lab AKL") }
    var isAlarmEnabled by remember { mutableStateOf(schedule?.isAlarmEnabled ?: true) }

    val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (schedule == null) "Tambah Master Jadwal" else "Edit Master Jadwal", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column {
                    Text("Pilih Hari:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(days.size) { index ->
                            val d = days[index]
                            FilterChip(
                                selected = dayOfWeek == d,
                                onClick = { dayOfWeek = d },
                                label = { Text(d, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Column {
                    Text("Pilih Kelas (dari Master Kelas):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(allClasses.size) { index ->
                            val cg = allClasses[index]
                            FilterChip(
                                selected = className == cg.name,
                                onClick = { className = cg.name },
                                label = { Text("${cg.name} (${cg.major})", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Column {
                    Text("Pilih Mapel (dari Master Mapel):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = subjectDropdownExpanded,
                        onExpandedChange = { subjectDropdownExpanded = !subjectDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = subject,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Mata Pelajaran") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            singleLine = true
                        )

                        ExposedDropdownMenu(
                            expanded = subjectDropdownExpanded,
                            onDismissRequest = { subjectDropdownExpanded = false }
                        ) {
                            availableSubjects.forEach { subName ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Book,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(subName, fontWeight = if (subject == subName) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    },
                                    onClick = {
                                        subject = subName
                                        subjectDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Jam Masuk (07:00)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Jam Selesai (09:00)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Ruangan (Contoh: Lab AKL 1)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Aktifkan Alarm Pengingat", fontSize = 13.sp)
                    Switch(checked = isAlarmEnabled, onCheckedChange = { isAlarmEnabled = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(schedule?.id ?: 0L, dayOfWeek, className, subject, startTime, endTime, room, isAlarmEnabled)
                },
                enabled = className.isNotBlank() && subject.isNotBlank()
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
