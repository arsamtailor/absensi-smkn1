package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AttendanceViewModel
import com.example.util.BackupManager
import kotlinx.coroutines.launch

@Composable
fun SettingsSecurityDialog(
    viewModel: AttendanceViewModel,
    onDismiss: () -> Unit
) {
    val savedPin by viewModel.savedPin.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showPinChangeDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Penyimpanan & Keamanan Data", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Storage Info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("Penyimpanan Jangka Panjang (SQLite Room)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text(
                            "• Data disimpan 100% lokal di HP Guru secara permanen & terenkripsi Sandbox Android.\n" +
                            "• Tidak membutuhkan internet, hemat kuota & aman dari kebocoran cloud.\n" +
                            "• Ekspor CSV/Excel & Backup JSON secara berkala ke Google Drive / WhatsApp.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Section 2: Backup & Restore
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Cadangan & Pulihkan Data", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val json = viewModel.generateBackupJson()
                                BackupManager.shareBackupText(context, json)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("backup_data_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cadangkan Data (Backup JSON)")
                    }

                    OutlinedButton(
                        onClick = { showRestoreDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("restore_data_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pulihkan Data dari Backup")
                    }
                }

                HorizontalDivider()

                // Section 3: App Security PIN
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Keamanan PIN Guru", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                if (savedPin.isBlank()) "PIN belum aktif (Semua orang dapat akses)" else "PIN Aktif (Hanya Guru yang tahu PIN)",
                                fontSize = 12.sp,
                                color = if (savedPin.isBlank()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }

                        Icon(
                            imageVector = if (savedPin.isBlank()) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (savedPin.isBlank()) Color.Gray else MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = { showPinChangeDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("set_pin_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = if (savedPin.isBlank()) ButtonDefaults.buttonColors() else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (savedPin.isBlank()) "Aktifkan Kunci PIN" else "Ubah / Matikan PIN")
                    }
                }

                // Section 4: Developer Info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text("Aplikasi Presensi Siswa", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Developed by arsam © 2026", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Versi 1.0.0 • Hak Cipta Dilindungi", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Selesai")
            }
        }
    )

    if (showPinChangeDialog) {
        ChangePinDialog(
            currentPin = savedPin,
            onDismiss = { showPinChangeDialog = false },
            onSavePin = { newPin ->
                viewModel.setSecurityPin(newPin)
                showPinChangeDialog = false
            }
        )
    }

    if (showRestoreDialog) {
        RestoreBackupDialog(
            onDismiss = { showRestoreDialog = false },
            onRestore = { jsonString ->
                viewModel.restoreBackup(jsonString) {
                    showRestoreDialog = false
                }
            }
        )
    }
}

@Composable
fun ChangePinDialog(
    currentPin: String,
    onDismiss: () -> Unit,
    onSavePin: (String) -> Unit
) {
    var pinInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atur PIN Keamanan Aplikasi", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Masukkan 4 digit PIN baru untuk mencegah siswa mengubah data absensi tanpa izin Guru.", fontSize = 13.sp)

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 6) pinInput = it },
                    label = { Text("PIN Keamanan (Contoh: 1234)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (currentPin.isNotBlank()) {
                    TextButton(
                        onClick = { onSavePin("") },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Matikan Kunci PIN")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSavePin(pinInput.trim()) },
                enabled = pinInput.length >= 4
            ) {
                Text("Simpan PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun RestoreBackupDialog(
    onDismiss: () -> Unit,
    onRestore: (String) -> Unit
) {
    var jsonInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pulihkan Data (Paste JSON Backup)", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Tempel (paste) teks file backup JSON yang sebelumnya Anda cadangkan:", fontSize = 13.sp)

                OutlinedTextField(
                    value = jsonInput,
                    onValueChange = { jsonInput = it },
                    label = { Text("Teks JSON Backup") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onRestore(jsonInput.trim()) },
                enabled = jsonInput.isNotBlank()
            ) {
                Text("Pulihkan Sekarang")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun PinLockScreen(
    onUnlock: (String) -> Boolean
) {
    var pinText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text("Aplikasi Terkunci", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(
                    "Masukkan PIN Keamanan Guru untuk membuka aplikasi Presensi SMKN 1 Cirinten.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = pinText,
                    onValueChange = {
                        pinText = it
                        errorMessage = null
                    },
                    label = { Text("PIN Guru") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("pin_unlock_input")
                )

                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        val ok = onUnlock(pinText)
                        if (!ok) {
                            errorMessage = "PIN Salah. Silakan coba lagi."
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("pin_unlock_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Buka Aplikasi", fontSize = 15.sp)
                }
            }
        }
    }
}
