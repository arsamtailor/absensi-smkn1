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
    modifier: Modifier = Modifier
) {
    val allSchedules by viewModel.allSchedules.collectAsState()
    val allClasses by viewModel.allClasses.collectAsState()
    val context = LocalContext.current

    var selectedDayFilter by remember { mutableStateOf("SEMUA") }
    var showAddScheduleDialog by remember { mutableStateOf(false) }
    var scheduleToEdit by remember { mutableStateOf<TeachingSchedule?>(null) }

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
                        Text("SMKN 1 Cirinten • AKL & MPLB", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scheduleToEdit = null
                    showAddScheduleDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_schedule_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Jadwal")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Active Class Bell Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
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
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White)
                            }
                        }
                        Column {
                            Text("Alarm Bell Jam Mengajar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Bunyikan bel saat Waktu Masuk & Habis", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilledTonalButton(
                            onClick = { AlarmUtil.playBellAlarm(context, isStart = true) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("🔊 Masuk", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { AlarmUtil.playBellAlarm(context, isStart = false) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
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
                            onClick = {
                                scheduleToEdit = null
                                showAddScheduleDialog = true
                            }
                        ) {
                            Text("Tambah Jadwal Baru")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)
                ) {
                    items(filteredSchedules) { schedule ->
                        ScheduleCard(
                            schedule = schedule,
                            onToggleAlarm = { viewModel.toggleScheduleAlarm(schedule) },
                            onPlayStartAlarm = { AlarmUtil.playBellAlarm(context, isStart = true) },
                            onPlayEndAlarm = { AlarmUtil.playBellAlarm(context, isStart = false) },
                            onStartAttendance = {
                                onStartTakeAttendanceForSchedule(schedule.className, schedule.subject)
                            },
                            onEdit = {
                                scheduleToEdit = schedule
                                showAddScheduleDialog = true
                            },
                            onDelete = {
                                viewModel.deleteSchedule(schedule)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddScheduleDialog) {
        AddEditScheduleDialog(
            schedule = scheduleToEdit,
            allClasses = allClasses,
            onDismiss = { showAddScheduleDialog = false },
            onSave = { id, day, className, subject, startTime, endTime, room, isAlarm ->
                viewModel.addOrUpdateSchedule(id, day, className, subject, startTime, endTime, room, isAlarm)
                showAddScheduleDialog = false
            }
        )
    }
}

@Composable
fun ScheduleCard(
    schedule: TeachingSchedule,
    onToggleAlarm: () -> Unit,
    onPlayStartAlarm: () -> Unit,
    onPlayEndAlarm: () -> Unit,
    onStartAttendance: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = schedule.dayOfWeek,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "${schedule.startTime} - ${schedule.endTime}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleAlarm, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (schedule.isAlarmEnabled) Icons.Outlined.NotificationsActive else Icons.Outlined.NotificationsOff,
                            contentDescription = "Toggle Alarm",
                            tint = if (schedule.isAlarmEnabled) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", modifier = Modifier.size(20.dp))
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Edit Jadwal") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = { showMenu = false; onEdit() }
                            )
                            DropdownMenuItem(
                                text = { Text("Hapus Jadwal", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                onClick = { showMenu = false; onDelete() }
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = schedule.subject,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Kelas: ${schedule.className} • ${schedule.room}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onStartAttendance,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Isi Absen", fontSize = 12.sp)
                }
            }

            // Bell Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPlayStartAlarm,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bell Waktu Masuk", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onPlayEndAlarm,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bell Waktu Habis", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun AddEditScheduleDialog(
    schedule: TeachingSchedule?,
    allClasses: List<ClassGroup>,
    onDismiss: () -> Unit,
    onSave: (id: Long, day: String, className: String, subject: String, startTime: String, endTime: String, room: String, isAlarm: Boolean) -> Unit
) {
    var dayOfWeek by remember { mutableStateOf(schedule?.dayOfWeek ?: "Senin") }
    var className by remember { mutableStateOf(schedule?.className ?: (allClasses.firstOrNull()?.name ?: "X AKL 1")) }
    var subject by remember { mutableStateOf(schedule?.subject ?: "Akuntansi Keuangan") }
    var startTime by remember { mutableStateOf(schedule?.startTime ?: "07:00") }
    var endTime by remember { mutableStateOf(schedule?.endTime ?: "09:00") }
    var room by remember { mutableStateOf(schedule?.room ?: "Lab AKL") }
    var isAlarmEnabled by remember { mutableStateOf(schedule?.isAlarmEnabled ?: true) }

    val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (schedule == null) "Tambah Jadwal Mengajar" else "Edit Jadwal Mengajar", fontWeight = FontWeight.Bold) },
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
                    Text("Pilih Kelas:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Mata Pelajaran") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

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
