package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AttendanceStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label, icon) = getStatusConfig(status)

    Surface(
        color = bgColor,
        contentColor = textColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun getStatusConfig(status: String): StatusConfig {
    return when (status.uppercase()) {
        "HADIR" -> StatusConfig(StatusHadirBg, StatusHadirGreen, "Hadir", Icons.Default.CheckCircle)
        "IZIN" -> StatusConfig(StatusIzinBg, StatusIzinBlue, "Izin", Icons.Default.Info)
        "SAKIT" -> StatusConfig(StatusSakitBg, StatusSakitAmber, "Sakit", Icons.Default.LocalHospital)
        "ALPA" -> StatusConfig(StatusAlpaBg, StatusAlpaRed, "Alpa", Icons.Default.Cancel)
        else -> StatusConfig(Color.LightGray, Color.DarkGray, status, Icons.Default.Info)
    }
}

private data class StatusConfig(
    val bgColor: Color,
    val textColor: Color,
    val label: String,
    val icon: ImageVector
)

@Composable
fun StatusSelectorGroup(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val statuses = listOf(
            StatusOption("HADIR", "Hadir", "H", StatusHadirGreen, StatusHadirBg),
            StatusOption("IZIN", "Izin", "I", StatusIzinBlue, StatusIzinBg),
            StatusOption("SAKIT", "Sakit", "S", StatusSakitAmber, StatusSakitBg),
            StatusOption("ALPA", "Alpa", "A", StatusAlpaRed, StatusAlpaBg)
        )

        statuses.forEach { option ->
            val isSelected = selectedStatus.equals(option.code, ignoreCase = true)

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onStatusSelected(option.code) },
                color = if (isSelected) option.brandColor else option.bgColor.copy(alpha = 0.5f),
                contentColor = if (isSelected) Color.White else option.brandColor,
                tonalElevation = if (isSelected) 3.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) Color.White else option.brandColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = option.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private data class StatusOption(
    val code: String,
    val label: String,
    val letter: String,
    val brandColor: Color,
    val bgColor: Color
)
