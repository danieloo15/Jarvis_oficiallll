package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCompact
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.JarvisTaskPlan
import com.example.data.local.entity.ActionHistoryEntity
import com.example.device.DeviceTelemetry
import com.example.device.VoiceMode
import com.example.device.VoiceState
import com.example.ui.components.JarvisOrbCore
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisOrange
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceBorder
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    telemetry: DeviceTelemetry,
    voiceState: VoiceState,
    voiceMode: VoiceMode,
    activeTasks: List<JarvisTaskPlan>?,
    recentHistory: List<ActionHistoryEntity>,
    isMinimalistMode: Boolean,
    onToggleMinimalistMode: (Boolean) -> Unit,
    onStartVoice: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToVision: () -> Unit,
    onQuickAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale("es", "ES")) }
    val currentTime = timeFormat.format(Date())
    val currentDate = dateFormat.format(Date())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Selector Bar (Minimalist / Full)
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(JarvisSurface)
                    .border(1.dp, JarvisSurfaceBorder, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isMinimalistMode) JarvisCyan.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { onToggleMinimalistMode(true) }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ViewCompact,
                        contentDescription = null,
                        tint = if (isMinimalistMode) JarvisCyan else JarvisTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HUD Minimalista",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMinimalistMode) JarvisCyan else JarvisTextMuted
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isMinimalistMode) JarvisCyan.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { onToggleMinimalistMode(false) }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ViewAgenda,
                        contentDescription = null,
                        tint = if (!isMinimalistMode) JarvisCyan else JarvisTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Telemetría Completa",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!isMinimalistMode) JarvisCyan else JarvisTextMuted
                    )
                }
            }
        }

        // Time & System Status
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = currentTime,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisTextPrimary,
                    letterSpacing = 3.sp
                )
                Text(
                    text = currentDate.replaceFirstChar { it.uppercase() },
                    fontSize = 12.sp,
                    color = JarvisTextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Central Holographic Arc Reactor Core Orb
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JarvisOrbCore(
                    voiceState = voiceState,
                    sizeDp = if (isMinimalistMode) 230.dp else 190.dp,
                    onClick = onStartVoice,
                    modifier = Modifier.testTag("dashboard_jarvis_orb")
                )

                // State indicator pill
                val stateText = when (voiceState) {
                    VoiceState.IDLE -> "EN ESPERA // SISTEMAS EN LÍNEA"
                    VoiceState.LISTENING -> "ESCUCHANDO ORDEN..."
                    VoiceState.THINKING -> "PROCESANDO CON INTELIGENCIA ARTIFICIAL..."
                    VoiceState.SPEAKING -> "EJECUTANDO RESPUESTA HABLADA..."
                    VoiceState.ERROR -> "ESTADO DE ALERTA // REVISAR SUBSISTEMAS"
                }
                val stateColor = when (voiceState) {
                    VoiceState.IDLE -> JarvisCyan
                    VoiceState.LISTENING -> JarvisEmerald
                    VoiceState.THINKING -> JarvisGold
                    VoiceState.SPEAKING -> JarvisCyan
                    VoiceState.ERROR -> JarvisRed
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(stateColor.copy(alpha = 0.15f))
                        .border(1.dp, stateColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stateText,
                        color = stateColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Primary Control Buttons (Voice & Conversation)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onStartVoice)
                        .testTag("dashboard_voice_button"),
                    color = JarvisCyan.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hablar con JARVIS",
                            color = JarvisCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onNavigateToChat)
                        .testTag("dashboard_chat_button"),
                    color = JarvisSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisSurfaceBorder),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = JarvisTextPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Abrir Consola",
                            color = JarvisTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Active Agent Tasks Banner if exists
        if (!activeTasks.isNullOrEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, JarvisGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                    color = JarvisSurfaceElevated,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = JarvisGold, modifier = Modifier.size(18.dp))
                            Text(
                                text = "OBJETIVO AGENTE ACTIVO",
                                color = JarvisGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        activeTasks.forEach { task ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (task.isExecuted) Icons.Default.CheckCircle else Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (task.isExecuted) JarvisEmerald else JarvisTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${task.stepNumber}. ${task.title}: ${task.description}",
                                    color = JarvisTextPrimary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Full Mode Extra Widgets
        if (!isMinimalistMode) {
            // Quick Action Triggers
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ACCIONES RÁPIDAS DEL DISPOSITIVO",
                        color = JarvisTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionCard(
                            title = "Música",
                            subtitle = "Spotify",
                            icon = Icons.Default.MusicNote,
                            color = JarvisEmerald,
                            onClick = { onQuickAction("Spotify") },
                            modifier = Modifier.weight(1f)
                        )

                        QuickActionCard(
                            title = "Visión IA",
                            subtitle = "Cámara",
                            icon = Icons.Default.CameraAlt,
                            color = JarvisCyan,
                            onClick = onNavigateToVision,
                            modifier = Modifier.weight(1f)
                        )

                        QuickActionCard(
                            title = "Ajustes",
                            subtitle = "POCO M7",
                            icon = Icons.Default.Tune,
                            color = JarvisGold,
                            onClick = { onQuickAction("Ajustes") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Telemetry & Hardware status cards
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ESTADO DE HARDWARE Y CONEXIONES",
                        color = JarvisTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp)),
                            color = JarvisSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisSurfaceBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Batería POCO", color = JarvisTextSecondary, fontSize = 11.sp)
                                Text("${telemetry.batteryPercent}%", color = JarvisEmerald, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text(if (telemetry.isCharging) "Conectado al cargador" else "En descarga normal", color = JarvisTextMuted, fontSize = 10.sp)
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp)),
                            color = JarvisSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisSurfaceBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Enlace Neural", color = JarvisTextSecondary, fontSize = 11.sp)
                                Text("Gemini 3.5", color = JarvisCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text("Motor Activo", color = JarvisTextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // Recent System Logs Preview
            if (recentHistory.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ÚLTIMA ACTIVIDAD",
                            color = JarvisTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )

                        recentHistory.take(2).forEach { log ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp)),
                                color = JarvisSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisSurfaceBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                when (log.status) {
                                                    "COMPLETED" -> JarvisEmerald
                                                    "PENDING_CONFIRMATION" -> JarvisGold
                                                    "CANCELLED", "EMERGENCY_STOPPED" -> JarvisRed
                                                    else -> JarvisCyan
                                                },
                                                CircleShape
                                            )
                                    )
                                    Column {
                                        Text(log.title, color = JarvisTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Text(log.description, color = JarvisTextMuted, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = JarvisSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisSurfaceBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Text(title, color = JarvisTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = JarvisTextMuted, fontSize = 10.sp)
        }
    }
}
