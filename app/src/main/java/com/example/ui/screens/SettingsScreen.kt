package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiBrainEngine
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCyan
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

@Composable
fun SettingsScreen(
    selectedModel: String,
    onSelectModel: (String) -> Unit,
    isAdminActive: Boolean,
    adminTimeFormatted: String,
    onEnableAdmin: (Int) -> Unit,
    onDisableAdmin: () -> Unit,
    isHotwordEnabled: Boolean,
    onToggleHotword: () -> Unit,
    pitch: Float,
    onPitchChange: (Float) -> Unit,
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var personalityStyle by remember { mutableStateOf("Mayordomo Británico") }
    var selectedAdminDuration by remember { mutableIntStateOf(30) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        // Title
        Column {
            Text(
                text = "CONFIGURACIÓN DEL SISTEMA",
                color = JarvisCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Parámetros de JARVIS",
                color = JarvisTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Section 1: AI Engine & Model
        SettingsSectionCard(title = "MOTOR NEURONAL & MODELOS IA", icon = Icons.Default.Psychology) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Seleccione el modelo de Google Gemini:",
                    color = JarvisTextSecondary,
                    fontSize = 12.sp
                )

                val models = listOf(
                    Triple(AiBrainEngine.MODEL_FAST, "Gemini 3.5 Flash", "Baja latencia, ideal para respuestas rápidas y comandos"),
                    Triple(AiBrainEngine.MODEL_PRO, "Gemini 3.1 Pro", "Razonamiento profundo y planificación avanzada de agentes"),
                    Triple(AiBrainEngine.MODEL_VISION, "Gemini 2.5 Vision", "Análisis multimodal de imágenes e inspección óptica")
                )

                models.forEach { (modelKey, name, desc) ->
                    val isSelected = selectedModel == modelKey
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectModel(modelKey) },
                        color = if (isSelected) JarvisCyan.copy(alpha = 0.15f) else JarvisSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) JarvisCyan else JarvisSurfaceBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(if (isSelected) JarvisCyan else JarvisTextMuted, CircleShape)
                            )
                            Column {
                                Text(name, color = JarvisTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(desc, color = JarvisTextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Admin Mode (Elevated Privileges)
        SettingsSectionCard(title = "MODO ADMINISTRADOR (CONTROL TOTAL)", icon = Icons.Default.Security) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Otorga a JARVIS permisos temporales para ejecutar acciones complejas sin pedir confirmación por cada paso. Se revertirá automáticamente al finalizar el tiempo.",
                    color = JarvisTextSecondary,
                    fontSize = 11.sp
                )

                if (isAdminActive) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = JarvisGold.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("MODO ADMIN ACTIVO", color = JarvisGold, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                Text("Tiempo restante: $adminTimeFormatted", color = JarvisTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onDisableAdmin,
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisRed, contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Desactivar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(10, 30, 60).forEach { mins ->
                            val isSelected = selectedAdminDuration == mins
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) JarvisGold.copy(alpha = 0.2f) else JarvisSurface)
                                    .border(1.dp, if (isSelected) JarvisGold else JarvisSurfaceBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedAdminDuration = mins }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$mins min",
                                    color = if (isSelected) JarvisGold else JarvisTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { onEnableAdmin(selectedAdminDuration) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("enable_admin_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Activar Modo Administrador ($selectedAdminDuration min)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 3: Voice & Personality
        SettingsSectionCard(title = "VOZ & PERSONALIDAD", icon = Icons.Default.GraphicEq) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Hotword Activation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Activación por 'Hey JARVIS'", color = JarvisTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Detección de voz en segundo plano", color = JarvisTextMuted, fontSize = 10.sp)
                    }

                    Switch(
                        checked = isHotwordEnabled,
                        onCheckedChange = { onToggleHotword() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = JarvisCyan
                        )
                    )
                }

                // Voice Pitch Slider
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tono de Voz (Elegancia):", color = JarvisTextSecondary, fontSize = 11.sp)
                        Text(String.format("%.2f", pitch), color = JarvisCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = pitch,
                        onValueChange = onPitchChange,
                        valueRange = 0.5f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisCyan,
                            activeTrackColor = JarvisCyan,
                            inactiveTrackColor = JarvisSurfaceBorder
                        )
                    )
                }

                // Volume Slider
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Volumen de Síntesis:", color = JarvisTextSecondary, fontSize = 11.sp)
                        Text("${(volume * 100).toInt()}%", color = JarvisCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = volume,
                        onValueChange = onVolumeChange,
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisCyan,
                            activeTrackColor = JarvisCyan,
                            inactiveTrackColor = JarvisSurfaceBorder
                        )
                    )
                }
            }
        }

        // Section 4: Device & System Info
        SettingsSectionCard(title = "INFORMACIÓN DEL TERMINAL", icon = Icons.Default.Info) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Dispositivo: POCO M7 4G (Exclusivo)", color = JarvisTextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                Text("Arquitectura: Modular Jetpack Compose + Room DB", color = JarvisTextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text("Versión JARVIS OS: 2.0.0 Core Build", color = JarvisCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        color = JarvisSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisSurfaceBorder),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(18.dp))
                Text(
                    text = title,
                    color = JarvisCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            content()
        }
    }
}
