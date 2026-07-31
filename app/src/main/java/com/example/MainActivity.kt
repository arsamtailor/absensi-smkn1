package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.model.ClassGroup
import com.example.ui.AttendanceViewModel
import com.example.ui.screens.ClassManagementScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.TakeAttendanceScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest

enum class AppDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Beranda", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    SCHEDULE("Jadwal", Icons.Filled.Schedule, Icons.Outlined.Schedule, "nav_schedule"),
    CLASSES("Kelas & Siswa", Icons.Filled.School, Icons.Outlined.School, "nav_classes"),
    REPORTS("Rekap", Icons.Filled.Assessment, Icons.Outlined.Assessment, "nav_reports"),
    SETTINGS("Pengaturan", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings"),
    TAKE_ATTENDANCE("Input Absensi", Icons.Filled.Home, Icons.Outlined.Home, "nav_attendance")
}

class MainActivity : ComponentActivity() {
    private val viewModel: AttendanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                AttendanceApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AttendanceApp(viewModel: AttendanceViewModel) {
    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val allClasses by viewModel.allClasses.collectAsState()
    val isAppLocked by viewModel.isAppLocked.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    if (isAppLocked) {
        com.example.ui.screens.PinLockScreen(
            onUnlock = { pin -> viewModel.unlockApp(pin) }
        )
        return
    }

    val onStartAttendanceForClass: (ClassGroup) -> Unit = { classGroup ->
        viewModel.selectClassForAttendance(classGroup)
        currentDestination = AppDestination.TAKE_ATTENDANCE
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (currentDestination != AppDestination.TAKE_ATTENDANCE) {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_navigation_bar")
                ) {
                    val items = listOf(
                        AppDestination.HOME,
                        AppDestination.SCHEDULE,
                        AppDestination.CLASSES,
                        AppDestination.REPORTS,
                        AppDestination.SETTINGS
                    )

                    items.forEach { destination ->
                        val isSelected = currentDestination == destination
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentDestination = destination },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.title
                                )
                            },
                            label = {
                                Text(
                                    text = destination.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag(destination.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentDestination,
            modifier = Modifier.padding(innerPadding)
        ) { destination ->
            when (destination) {
                AppDestination.HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onStartTakeAttendance = onStartAttendanceForClass,
                        onEditSession = { session ->
                            viewModel.loadExistingSessionForEdit(session)
                            currentDestination = AppDestination.TAKE_ATTENDANCE
                        },
                        onNavigateToClasses = { currentDestination = AppDestination.CLASSES },
                        onNavigateToReports = { currentDestination = AppDestination.REPORTS },
                        onNavigateToSchedule = { currentDestination = AppDestination.SCHEDULE },
                        onOpenSettings = { currentDestination = AppDestination.SETTINGS }
                    )
                }

                AppDestination.SETTINGS -> {
                    com.example.ui.screens.MasterDataSettingsScreen(
                        viewModel = viewModel,
                        onNavigateToClasses = { currentDestination = AppDestination.CLASSES },
                        onNavigateToSchedule = { currentDestination = AppDestination.SCHEDULE }
                    )
                }

                AppDestination.SCHEDULE -> {
                    ScheduleScreen(
                        viewModel = viewModel,
                        onStartTakeAttendanceForSchedule = { className, subject ->
                            val matchedClass = allClasses.find { it.name.equals(className, ignoreCase = true) }
                            if (matchedClass != null) {
                                viewModel.selectClassForAttendance(matchedClass)
                                viewModel.setAttendanceSubject(subject)
                            } else if (allClasses.isNotEmpty()) {
                                viewModel.selectClassForAttendance(allClasses.first())
                                viewModel.setAttendanceSubject(subject)
                            }
                            currentDestination = AppDestination.TAKE_ATTENDANCE
                        },
                        onOpenSettings = { currentDestination = AppDestination.SETTINGS }
                    )
                }

                AppDestination.CLASSES -> {
                    ClassManagementScreen(
                        viewModel = viewModel,
                        onStartTakeAttendance = onStartAttendanceForClass,
                        onOpenSettings = { currentDestination = AppDestination.SETTINGS }
                    )
                }

                AppDestination.REPORTS -> {
                    ReportsScreen(
                        viewModel = viewModel
                    )
                }

                AppDestination.TAKE_ATTENDANCE -> {
                    TakeAttendanceScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentDestination = AppDestination.HOME },
                        onOpenSettings = { currentDestination = AppDestination.SETTINGS }
                    )
                }
            }
        }
    }

    if (showSettingsDialog) {
        com.example.ui.screens.SettingsSecurityDialog(
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }
}

