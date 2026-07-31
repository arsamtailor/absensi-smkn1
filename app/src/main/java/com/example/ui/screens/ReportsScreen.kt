package com.example.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClassGroup
import com.example.data.model.StudentSummary
import com.example.ui.AttendanceViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val allClasses by viewModel.allClasses.collectAsState()
    val reportSelectedClassId by viewModel.reportSelectedClassId.collectAsState()
    val reportPeriod by viewModel.reportPeriod.collectAsState()
    val reportSelectedSubject by viewModel.reportSelectedSubject.collectAsState()
    val summaries by viewModel.reportSummaries.collectAsState()

    val context = LocalContext.current

    val allSchedules by viewModel.allSchedules.collectAsState()
    val selectedClassGroup = allClasses.find { it.id == reportSelectedClassId }

    val teacherName by viewModel.teacherName.collectAsState()
    val teacherNip by viewModel.teacherNip.collectAsState()
    val schoolName by viewModel.schoolName.collectAsState()
    val rawTeacherSubject by viewModel.teacherSubject.collectAsState()
    val activeAcademicYear by viewModel.activeAcademicYear.collectAsState()
    val activeSemester by viewModel.activeSemester.collectAsState()

    val teacherSubjects = remember(rawTeacherSubject, allSchedules, selectedClassGroup) {
        val classScheduleSubjects = if (selectedClassGroup != null) {
            allSchedules.filter { it.className.equals(selectedClassGroup.name, ignoreCase = true) }
                .map { it.subject.trim() }
                .filter { it.isNotBlank() }
        } else emptyList()

        val allScheduleSubjects = allSchedules.map { it.subject.trim() }.filter { it.isNotBlank() }
        val masterTeacherSubjects = viewModel.getTeacherSubjectList()

        (classScheduleSubjects + allScheduleSubjects + masterTeacherSubjects)
            .distinct()
    }

    LaunchedEffect(allClasses) {
        if (reportSelectedClassId == null && allClasses.isNotEmpty()) {
            viewModel.loadReportForClass(allClasses.first().id)
        }
    }

    var dropdownExpanded by remember { mutableStateOf(false) }
    var showAiAnalysisDialog by remember { mutableStateOf(false) }
    var showOnlyCriticalAlpa by remember { mutableStateOf(false) }

    val displayedSummaries = remember(summaries, showOnlyCriticalAlpa) {
        if (showOnlyCriticalAlpa) summaries.filter { it.totalAlpa >= 3 } else summaries
    }

    val criticalCount = remember(summaries) { summaries.count { it.totalAlpa >= 3 } }

    val classAvgRate = remember(summaries) {
        if (summaries.isEmpty()) 0f
        else summaries.map { it.attendancePercentage }.average().toFloat()
    }

    val atRiskStudents = remember(summaries) {
        summaries.filter { it.totalAlpa >= 2 || it.attendancePercentage < 80f }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Rekap & Laporan Absensi", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("${schoolName.ifBlank { "SMKN 1 Cirinten" }} • TA $activeAcademicYear ($activeSemester)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    if (selectedClassGroup != null && summaries.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                exportReportCsv(context, selectedClassGroup, summaries, reportPeriod.label, teacherName, teacherNip, schoolName)
                            },
                            modifier = Modifier.testTag("export_csv_btn")
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Ekspor CSV / Excel")
                        }
                        IconButton(
                            onClick = {
                                shareReportText(context, selectedClassGroup, summaries, reportPeriod.label, teacherName, teacherNip, schoolName)
                            },
                            modifier = Modifier.testTag("share_report_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Bagikan Laporan")
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Class Dropdown Selector & Period Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    OutlinedCard(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text(
                                        text = selectedClassGroup?.name ?: "Pilih Kelas",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    if (selectedClassGroup != null) {
                                        Text(
                                            text = "Jurusan: ${selectedClassGroup.major} • TA: ${selectedClassGroup.academicYear}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        allClasses.forEach { classGroup ->
                            DropdownMenuItem(
                                text = { Text("${classGroup.name} (${classGroup.major})", fontWeight = FontWeight.Medium) },
                                onClick = {
                                    viewModel.loadReportForClass(classGroup.id)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Filter Periode Waktu & Mata Pelajaran (Dropdown side-by-side agar hemat tempat)
                var periodDropdownExpanded by remember { mutableStateOf(false) }
                var subjectDropdownExpanded by remember { mutableStateOf(false) }
                val availableSubjectFilters = remember(teacherSubjects) {
                    listOf("Semua Mapel") + teacherSubjects
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Dropdown Periode Waktu
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedCard(
                            onClick = { periodDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
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
                                        text = "Periode Waktu",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = reportPeriod.label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = periodDropdownExpanded,
                            onDismissRequest = { periodDropdownExpanded = false }
                        ) {
                            com.example.ui.ReportPeriod.values().forEach { period ->
                                DropdownMenuItem(
                                    text = { Text(period.label, fontSize = 13.sp) },
                                    onClick = {
                                        viewModel.setReportPeriod(period)
                                        periodDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Dropdown Mata Pelajaran
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedCard(
                            onClick = { subjectDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
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
                                        text = "Mata Pelajaran",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = reportSelectedSubject,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = subjectDropdownExpanded,
                            onDismissRequest = { subjectDropdownExpanded = false }
                        ) {
                            availableSubjectFilters.forEach { subFilter ->
                                DropdownMenuItem(
                                    text = { Text(subFilter, fontSize = 13.sp) },
                                    onClick = {
                                        viewModel.setReportSubjectFilter(subFilter)
                                        subjectDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Filter Khusus Siswa Alpa >= 3x
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = showOnlyCriticalAlpa,
                        onClick = { showOnlyCriticalAlpa = !showOnlyCriticalAlpa },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (showOnlyCriticalAlpa) Color.White else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "🚨 Siswa Alpa ≥ 3x ($criticalCount Siswa)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.error,
                            selectedLabelColor = Color.White
                        )
                    )

                    if (criticalCount > 0) {
                        Text(
                            text = "$criticalCount Perlu SP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (selectedClassGroup == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada kelas terdaftar.")
                }
            } else if (summaries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada data presensi atau siswa di kelas ${selectedClassGroup.name}.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // Smart AI Insight Banner Button
                    item {
                        Card(
                            onClick = { showAiAnalysisDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
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
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                                        }
                                    }
                                    Column {
                                        Text("🤖 Analisis Pintar AI Kelas", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(
                                            if (atRiskStudents.isNotEmpty())
                                                "⚠️ ${atRiskStudents.size} Siswa perlu perhatian khusus Guru/BK"
                                            else
                                                "✅ Kehadiran kelas sangat baik",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Button(
                                    onClick = { showAiAnalysisDialog = true },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Text("Lihat Analisis", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Class Overview Metric Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityOutline)
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
                                    Column {
                                        Text(
                                            text = "Tingkat Kehadiran Kelas",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${String.format("%.1f", classAvgRate)}%",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                                                 Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { exportReportCsv(context, selectedClassGroup, summaries, reportPeriod.label, teacherName, teacherNip, schoolName) },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("CSV", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = { shareReportText(context, selectedClassGroup, summaries, reportPeriod.label, teacherName, teacherNip, schoolName) },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Bagikan", fontSize = 12.sp)
                                        }
                                    }          }
                                }

                                LinearProgressIndicator(
                                    progress = { (classAvgRate / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                val totalSessions = summaries.firstOrNull()?.totalSessions ?: 0
                                Text(
                                    text = "Berdasarkan $totalSessions sesi presensi untuk ${summaries.size} siswa.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (showOnlyCriticalAlpa) "Siswa Alpa ≥ 3x (${displayedSummaries.size} Siswa)" else "Detail Rekap Siswa (${displayedSummaries.size} Siswa)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    if (displayedSummaries.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (showOnlyCriticalAlpa) "Tidak ada siswa di kelas ini dengan Alpa ≥ 3 kali (Semua siswa aman)." else "Belum ada rekap data siswa.",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(displayedSummaries) { summary ->
                            StudentSummaryRowCard(
                                summary = summary,
                                className = selectedClassGroup.name,
                                teacherName = teacherName,
                                teacherNip = teacherNip,
                                schoolName = schoolName
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAiAnalysisDialog && selectedClassGroup != null) {
        AiAnalysisDialog(
            classGroup = selectedClassGroup,
            periodLabel = reportPeriod.label,
            summaries = summaries,
            classAvgRate = classAvgRate,
            atRiskStudents = atRiskStudents,
            onDismiss = { showAiAnalysisDialog = false },
            onShareAnalysis = { analysisText ->
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, analysisText)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Kirim Hasil Analisis AI"))
            }
        )
    }
}

@Composable
fun StudentSummaryRowCard(
    summary: StudentSummary,
    className: String = "",
    teacherName: String = "",
    teacherNip: String = "",
    schoolName: String = ""
) {
    val isCriticalAlpa = summary.totalAlpa >= 3
    val isAtRisk = summary.totalAlpa >= 2 || summary.attendancePercentage < 80f
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCriticalAlpa) StatusAlpaBg.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isCriticalAlpa) 1.5.dp else 1.dp,
            if (isCriticalAlpa) StatusAlpaRed else if (isAtRisk) StatusAlpaRed.copy(alpha = 0.6f) else HighDensityOutline
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = summary.student.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        if (isCriticalAlpa) {
                            Surface(
                                color = StatusAlpaRed,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (summary.totalAlpa >= 6) "🚨 SP 3 (Panggilan Ortu)" else if (summary.totalAlpa >= 4) "⚠️ SP 2 (Alpa ${summary.totalAlpa}x)" else "⚠️ SP 1 (Alpa 3x)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else if (isAtRisk) {
                            Surface(
                                color = StatusAlpaBg,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "⚠️ Perlu Perhatian",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusAlpaRed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "NISN: ${if (summary.student.nisn.isBlank()) "-" else summary.student.nisn}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = if (isCriticalAlpa) StatusAlpaRed else if (isAtRisk) StatusAlpaBg else MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${String.format("%.0f", summary.attendancePercentage)}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isCriticalAlpa) Color.White else if (isAtRisk) StatusAlpaRed else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Stats breakdown chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportChip("Hadir: ${summary.totalHadir}", StatusHadirGreen, StatusHadirBg, Modifier.weight(1f))
                ReportChip("Izin: ${summary.totalIzin}", StatusIzinBlue, StatusIzinBg, Modifier.weight(1f))
                ReportChip("Sakit: ${summary.totalSakit}", StatusSakitAmber, StatusSakitBg, Modifier.weight(1f))
                ReportChip("Alpa: ${summary.totalAlpa}", StatusAlpaRed, StatusAlpaBg, Modifier.weight(1f))
            }

            if (isCriticalAlpa) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🚨 Siswa Alpa ≥ 3x! Perlu Surat Peringatan.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusAlpaRed,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            val info = com.example.data.model.CriticalStudentAlpaInfo(
                                summary = summary,
                                className = className,
                                classMajor = ""
                            )
                            sendWarningLetterViaWhatsApp(context, info, teacherName, teacherNip, schoolName)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusAlpaRed,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kirim SP (WA)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ReportChip(text: String, color: Color, bgColor: Color, modifier: Modifier = Modifier) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun AiAnalysisDialog(
    classGroup: ClassGroup,
    periodLabel: String,
    summaries: List<StudentSummary>,
    classAvgRate: Float,
    atRiskStudents: List<StudentSummary>,
    onDismiss: () -> Unit,
    onShareAnalysis: (String) -> Unit
) {
    val totalStudents = summaries.size
    val totalSessions = summaries.firstOrNull()?.totalSessions ?: 0
    val topStudents = summaries.filter { it.attendancePercentage >= 100f }

    val analysisText = remember(classGroup, periodLabel, summaries) {
        val sb = java.lang.StringBuilder()
        sb.append("🤖 *RINGKASAN ANALISIS AI PRESENSI - SMKN 1 CIRINTEN*\n")
        sb.append("Kelas: ${classGroup.name} (${classGroup.major})\n")
        sb.append("Periode Evaluasi: $periodLabel\n")
        sb.append("Tingkat Kehadiran Kelas: ${String.format("%.1f", classAvgRate)}%\n")
        sb.append("Total Sesi: $totalSessions Sesi | Total Siswa: $totalStudents Siswa\n")
        sb.append("--------------------------------------------------\n\n")

        if (atRiskStudents.isNotEmpty()) {
            sb.append("⚠️ *SISWA BERISIKO (PERLU PERHATIAN WALI KELAS/BK):*\n")
            atRiskStudents.forEach { summary ->
                val reason = if (summary.totalAlpa >= 2) "${summary.totalAlpa}x Tanpa Keterangan (Alpa)" else "Kehadiran Rendah (${String.format("%.0f", summary.attendancePercentage)}%)"
                sb.append("• ${summary.student.name}: $reason\n")
                sb.append("  👉 Saran Tindakan: Kirim pemberitahuan orang tua / konseling BK.\n")
            }
            sb.append("\n")
        } else {
            sb.append("✅ *STATUS KEHADIRAN:* Sangat baik! Tidak ada siswa dengan Alpa berlebih pada periode ini.\n\n")
        }

        if (topStudents.isNotEmpty()) {
            sb.append("🌟 *SISWA RAJIN (100% KEHADIRAN):*\n")
            topStudents.take(5).forEach { summary ->
                sb.append("• ${summary.student.name}\n")
            }
            if (topStudents.size > 5) {
                sb.append("  dan ${topStudents.size - 5} siswa lainnya.\n")
            }
            sb.append("\n")
        }

        sb.append("📌 *KESIMPULAN GURU:*\n")
        sb.append(
            if (classAvgRate >= 85f)
                "Kedisiplinan kelas ${classGroup.name} terpantau stabil dan kondusif."
            else
                "Diperlukan pembinaan wali kelas untuk menekan angka ketidakhadiran."
        )

        sb.toString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Text("Analisis AI Kehadiran Kelas", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Hasil Evaluasi Otomatis AI:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("• Rata-rata Kehadiran Kelas: ${String.format("%.1f", classAvgRate)}%", fontSize = 13.sp)
                            Text("• Siswa Berisiko (Alpa/Rendah): ${atRiskStudents.size} Siswa", fontSize = 13.sp, color = if (atRiskStudents.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                            Text("• Siswa Sempurna (100%): ${topStudents.size} Siswa", fontSize = 13.sp)
                        }
                    }
                }

                if (atRiskStudents.isNotEmpty()) {
                    item {
                        Text("⚠️ Siswa Perlu Perhatian Wali Kelas:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                    }
                    items(atRiskStudents) { summary ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(summary.student.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Alpa: ${summary.totalAlpa}x | Hadir: ${String.format("%.0f", summary.attendancePercentage)}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                                Text("💡 Rekomendasi: Pemanggilan Wali Murid / Peringatan BK", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onShareAnalysis(analysisText) }) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Kirim Laporan AI")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

private fun exportReportCsv(
    context: Context,
    classGroup: ClassGroup,
    summaries: List<StudentSummary>,
    periodLabel: String,
    teacherName: String,
    teacherNip: String,
    schoolName: String
) {
    val totalSessions = summaries.firstOrNull()?.totalSessions ?: 0
    val sb = java.lang.StringBuilder()
    sb.append("REKAP PRESENSI $schoolName\n")
    sb.append("Guru Pengampu,\"$teacherName\"\n")
    if (teacherNip.isNotBlank()) {
        sb.append("NIP Guru,\"$teacherNip\"\n")
    }
    sb.append("Kelas,${classGroup.name}\n")
    sb.append("Jurusan,${classGroup.major}\n")
    sb.append("Periode,$periodLabel\n")
    sb.append("Tahun Ajaran,${classGroup.academicYear}\n")
    sb.append("Total Sesi,$totalSessions Sesi\n\n")

    sb.append("No,NISN,Nama Siswa,Hadir,Izin,Sakit,Alpa,Persentase Hadir (%),Catatan Evaluasi\n")

    summaries.forEachIndexed { index, summary ->
        val statusRisk = if (summary.totalAlpa >= 2) "PERLU PERHATIAN WALI KELAS (Alpa >= 2)"
        else if (summary.attendancePercentage < 80f) "KEHADIRAN DI BAWAH TARGET ( < 80% )"
        else "NORMAL / BAIK"

        sb.append("${index + 1},\"${summary.student.nisn}\",\"${summary.student.name}\",${summary.totalHadir},${summary.totalIzin},${summary.totalSakit},${summary.totalAlpa},${String.format("%.1f", summary.attendancePercentage)},\"$statusRisk\"\n")
    }

    sb.append("\nMengetahui,\n")
    sb.append("Guru Pengampu / Wali Kelas,\n\n\n")
    sb.append("\"$teacherName\"\n")
    if (teacherNip.isNotBlank()) {
        sb.append("NIP. $teacherNip\n")
    }

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        putExtra(Intent.EXTRA_TITLE, "Rekap_Presensi_${classGroup.name.replace(" ", "_")}.csv")
        type = "text/csv"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Ekspor / Simpan File CSV Excel")
    context.startActivity(shareIntent)
}

private fun shareReportText(
    context: Context,
    classGroup: ClassGroup,
    summaries: List<StudentSummary>,
    periodLabel: String,
    teacherName: String,
    teacherNip: String,
    schoolName: String
) {
    val totalSessions = summaries.firstOrNull()?.totalSessions ?: 0
    val sb = StringBuilder()
    sb.append("🏫 *REKAP PRESENSI $schoolName*\n")
    sb.append("Guru: $teacherName${if (teacherNip.isNotBlank()) " (NIP: $teacherNip)" else ""}\n")
    sb.append("Kelas: ${classGroup.name} (${classGroup.major})\n")
    sb.append("Periode: $periodLabel\n")
    sb.append("Tahun Ajaran: ${classGroup.academicYear}\n")
    sb.append("Total Sesi Presensi: $totalSessions Sesi\n")
    sb.append("----------------------------------\n\n")

    summaries.forEachIndexed { index, summary ->
        sb.append("${index + 1}. ${summary.student.name}\n")
        sb.append("   - Hadir: ${summary.totalHadir} | Izin: ${summary.totalIzin} | Sakit: ${summary.totalSakit} | Alpa: ${summary.totalAlpa}\n")
        sb.append("   - Persentase Hadir: ${String.format("%.1f", summary.attendancePercentage)}%\n\n")
    }

    sb.append("Salam,\n")
    sb.append("*$teacherName*\n")
    if (teacherNip.isNotBlank()) {
        sb.append("NIP. $teacherNip\n")
    }
    sb.append("Laporan resmi $schoolName.")

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Bagikan Rekap Presensi $schoolName")
    context.startActivity(shareIntent)
}
