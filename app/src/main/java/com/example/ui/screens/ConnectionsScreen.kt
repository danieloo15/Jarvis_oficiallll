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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.ConnectionEntity
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceBorder
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun ConnectionsScreen(
    connections: List<ConnectionEntity>,
    onToggleConnection: (ConnectionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedConnectionForDetail by remember { mutableStateOf<ConnectionEntity?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    Text(
                        text = "ENLACES DE SERVICIO",
                        color = JarvisCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Conexiones & Ecosistema",
                        color = JarvisTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            items(connections, key = { it.serviceId }) { conn ->
                ConnectionCardItem(
                    connection = conn,
                    onToggle = { onToggleConnection(conn) },
                    onClick = { selectedConnectionForDetail = conn }
                )
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    selectedConnectionForDetail?.let { conn ->
        ConnectionDetailDialog(
            connection = conn,
            onDismiss = { selectedConnectionForDetail = null },
            onToggle = { onToggleConnection(conn) }
        )
    }
}

@Composable
fun ConnectionCardItem(
    connection: ConnectionEntity,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("connection_card_${connection.serviceId}"),
        color = JarvisSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (connection.isConnected) JarvisCyan.copy(alpha = 0.35f) else JarvisSurfaceBorder
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val (icon, iconColor) = getConnectionIconAndColor(connection.serviceId)

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = connection.name,
                        color = JarvisTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (connection.isConnected) JarvisEmerald.copy(alpha = 0.15f) else JarvisRed.copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = if (connection.isConnected) "CONECTADO" else "DESVINCULADO",
                            color = if (connection.isConnected) JarvisEmerald else JarvisRed,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Text(
                    text = connection.statusDescription,
                    color = JarvisTextMuted,
                    fontSize = 12.sp
                )
            }

            Switch(
                checked = connection.isConnected,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = JarvisCyan,
                    uncheckedThumbColor = JarvisTextMuted,
                    uncheckedTrackColor = JarvisSurface
                )
            )
        }
    }
}

@Composable
fun ConnectionDetailDialog(
    connection: ConnectionEntity,
    onDismiss: () -> Unit,
    onToggle: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, JarvisCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
            color = JarvisSurfaceElevated,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val (icon, iconColor) = getConnectionIconAndColor(connection.serviceId)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(iconColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                    }

                    Column {
                        Text(connection.name, color = JarvisTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (connection.isConnected) "🟢 Enlace Operativo" else "🔴 Fuera de línea",
                            color = if (connection.isConnected) JarvisEmerald else JarvisRed,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(JarvisSurface, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Estado del Enlace:", color = JarvisCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(connection.statusDescription, color = JarvisTextPrimary, fontSize = 12.sp)
                        Text("Información de Cuenta: ${connection.accountInfo}", color = JarvisTextMuted, fontSize = 11.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onToggle()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (connection.isConnected) JarvisRed else JarvisEmerald)
                    ) {
                        Text(if (connection.isConnected) "Desconectar" else "Conectar", color = if (connection.isConnected) JarvisRed else JarvisEmerald)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color.Black)
                    ) {
                        Text("Aceptar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun getConnectionIconAndColor(id: String): Pair<ImageVector, Color> {
    return when (id) {
        "gemini" -> Pair(Icons.Default.Psychology, JarvisCyan)
        "spotify" -> Pair(Icons.Default.MusicNote, JarvisEmerald)
        "whatsapp" -> Pair(Icons.Default.Chat, Color(0xFF25D366))
        "gmail" -> Pair(Icons.Default.Email, JarvisRed)
        "calendar" -> Pair(Icons.Default.CalendarMonth, JarvisBlue)
        "poco_m7" -> Pair(Icons.Default.Smartphone, JarvisGold)
        else -> Pair(Icons.Default.Home, JarvisCyan)
    }
}
