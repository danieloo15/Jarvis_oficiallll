package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.device.DeviceTelemetry
import com.example.device.VoiceMode
import com.example.device.VoiceState
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceBorder
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun TelemetryHeader(
    telemetry: DeviceTelemetry,
    voiceState: VoiceState,
    voiceMode: VoiceMode,
    isAdminActive: Boolean,
    adminTimeFormatted: String,
    onEmergencyStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("telemetry_header"),
        color = JarvisSurface.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisSurfaceBorder),
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: System Title, Device Badge, Emergency Stop
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = when (voiceState) {
                                    VoiceState.IDLE -> JarvisCyan
                                    VoiceState.LISTENING -> JarvisEmerald
                                    VoiceState.THINKING -> JarvisGold
                                    VoiceState.SPEAKING -> JarvisCyan
                                    VoiceState.ERROR -> JarvisRed
                                },
                                shape = CircleShape
                            )
                    )

                    Text(
                        text = "J.A.R.V.I.S.",
                        color = JarvisTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(JarvisSurfaceBorder)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = telemetry.deviceModel,
                            color = JarvisCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                EmergencyStopButton(onEmergencyStop = onEmergencyStop)
            }

            // Row 2: Status chips (Battery, Network, Voice Mode, Admin status)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Battery Pill
                TelemetryChip(
                    icon = Icons.Default.Bolt,
                    label = "${telemetry.batteryPercent}%${if (telemetry.isCharging) " (Cargando)" else ""}",
                    color = if (telemetry.batteryPercent > 20) JarvisEmerald else JarvisRed
                )

                // Network Pill
                TelemetryChip(
                    icon = Icons.Default.Wifi,
                    label = telemetry.networkType,
                    color = JarvisCyan
                )

                // Admin Status Pill if active
                if (isAdminActive) {
                    TelemetryChip(
                        icon = Icons.Default.Security,
                        label = "ADMIN $adminTimeFormatted",
                        color = JarvisGold
                    )
                }

                // Voice Mode Pill
                TelemetryChip(
                    icon = if (voiceMode == VoiceMode.CONTINUOUS_CONVERSATION) Icons.Default.Mic else Icons.Default.Speed,
                    label = if (voiceMode == VoiceMode.CONTINUOUS_CONVERSATION) "Voz Continua" else "Comando",
                    color = if (voiceMode == VoiceMode.CONTINUOUS_CONVERSATION) JarvisEmerald else JarvisTextSecondary
                )
            }
        }
    }
}

@Composable
fun TelemetryChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = label,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
