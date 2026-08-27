package com.example.ui

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.local.entity.AutomationEntity
import com.example.data.local.entity.SkillEntity
import com.example.ui.components.ActionConfirmationDialog
import com.example.ui.components.TelemetryHeader
import com.example.ui.screens.AutomationsScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ConnectionsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SkillsScreen
import com.example.ui.screens.VisionScreen
import com.example.ui.theme.JarvisBackground
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
import com.example.viewmodel.JarvisViewModel
import kotlinx.coroutines.launch

enum class JarvisNavTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Inicio", Icons.Default.Speed),
    CHAT("Consola", Icons.Default.Chat),
    SKILLS("Habilidades", Icons.Default.Bolt),
    AUTOMATIONS("Rutinas", Icons.Default.Schedule),
    MEMORY("Memoria", Icons.Default.Psychology),
    CONNECTIONS("Enlaces", Icons.Default.Link),
    HISTORY("Historial", Icons.Default.History),
    SECURITY("Seguridad", Icons.Default.Security),
    VISION("Visión", Icons.Default.Visibility),
    SETTINGS("Ajustes", Icons.Default.Settings)
}

@Composable
fun MainScreen(viewModel: JarvisViewModel) {
    val coroutineScope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(JarvisNavTab.DASHBOARD) }

    // State collections
    val telemetry by viewModel.telemetry.collectAsState()
    val isMinimalistMode by viewModel.isMinimalistMode.collectAsState()
    val voiceState by viewModel.voiceController.voiceState.collectAsState()
    val voiceMode by viewModel.voiceController.voiceMode.collectAsState()
    val isHotwordEnabled by viewModel.voiceController.isHotwordEnabled.collectAsState()
    val voicePitch by viewModel.voiceController.voicePitch.collectAsState()
    val voiceVolume by viewModel.voiceController.voiceVolume.collectAsState()
    val isAdminActive by viewModel.adminModeManager.isAdminActive.collectAsState()
    val adminTimeFormatted = viewModel.adminModeManager.formatRemainingTime()

    val chatMessages by viewModel.chatMessages.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()
    val pendingConfirmation by viewModel.pendingConfirmation.collectAsState()
    val activeAgentPlan by viewModel.activeAgentPlan.collectAsState()

    val memories by viewModel.memories.collectAsState()
    val skills by viewModel.skills.collectAsState()
    val automations by viewModel.automations.collectAsState()
    val history by viewModel.history.collectAsState()
    val connections by viewModel.connections.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()

    val isVisionAnalyzing by viewModel.isVisionAnalyzing.collectAsState()
    val visionResult by viewModel.visionResult.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground),
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                TelemetryHeader(
                    telemetry = telemetry,
                    voiceState = voiceState,
                    voiceMode = voiceMode,
                    isAdminActive = isAdminActive,
                    adminTimeFormatted = adminTimeFormatted,
                    onEmergencyStop = { viewModel.emergencyStop() }
                )

                // High-Tech Horizontal Navigation Selector
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = JarvisSurface.copy(alpha = 0.98f),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, JarvisSurfaceBorder)
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(JarvisNavTab.values()) { tab ->
                            val isSelected = currentTab == tab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) JarvisCyan.copy(alpha = 0.18f) else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (isSelected) JarvisCyan else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { currentTab = tab }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("nav_tab_${tab.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) JarvisCyan else JarvisTextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = tab.title,
                                        color = if (isSelected) JarvisCyan else JarvisTextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = JarvisBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                JarvisNavTab.DASHBOARD -> DashboardScreen(
                    telemetry = telemetry,
                    voiceState = voiceState,
                    voiceMode = voiceMode,
                    activeTasks = activeAgentPlan,
                    recentHistory = history,
                    isMinimalistMode = isMinimalistMode,
                    onToggleMinimalistMode = { viewModel.toggleMinimalistMode(it) },
                    onStartVoice = { viewModel.voiceController.startListening() },
                    onNavigateToChat = { currentTab = JarvisNavTab.CHAT },
                    onNavigateToVision = { currentTab = JarvisNavTab.VISION },
                    onQuickAction = { app -> viewModel.deviceController.openApplication(app) }
                )

                JarvisNavTab.CHAT -> ChatScreen(
                    messages = chatMessages,
                    isThinking = isThinking,
                    voiceState = voiceState,
                    voiceMode = voiceMode,
                    onSendMessage = { viewModel.processUserInput(it) },
                    onStartVoice = {
    val currentState = viewModel.voiceController.voiceState.value

    when (currentState) {
        VoiceState.IDLE,
        VoiceState.ERROR -> {
            viewModel.voiceController.startListening()
        }

        VoiceState.LISTENING -> {
            viewModel.voiceController.stopListening()
        }

        VoiceState.THINKING -> {
            viewModel.voiceController.stopListening()
        }

        VoiceState.SPEAKING -> {
            viewModel.voiceController.stopSpeaking()
        }
    }
},
                    onStopVoice = { viewModel.voiceController.stopListening() },
                    onToggleVoiceMode = { viewModel.voiceController.toggleVoiceMode() },
                    onSpeakText = { viewModel.voiceController.speak(it) },
                    onConfirmPlan = { viewModel.confirmPendingAction() },
                    onCancelPlan = { viewModel.cancelPendingAction() }
                )

                JarvisNavTab.SKILLS -> SkillsScreen(
                    skills = skills,
                    onToggleSkill = { skill ->
                        coroutineScope.launch {
                            viewModel.repository.updateSkill(skill.copy(isEnabled = !skill.isEnabled))
                        }
                    },
                    onCreateSkill = { name, category, trigger, actions ->
                        coroutineScope.launch {
                            val newSkill = SkillEntity(
                                id = "skill_${System.currentTimeMillis()}",
                                name = name,
                                category = category,
                                iconName = "bolt",
                                description = "Disparador: $trigger. Acciones: $actions",
                                isEnabled = true,
                                availableActions = actions,
                                isCustom = true
                            )
                            viewModel.repository.saveSkill(newSkill)
                            viewModel.logAction("Nueva Habilidad Creada", name, "System", "COMPLETED")
                        }
                    },
                    onDeleteSkill = { skillId ->
                        coroutineScope.launch {
                            viewModel.repository.deleteSkill(skillId)
                        }
                    }
                )

                JarvisNavTab.AUTOMATIONS -> AutomationsScreen(
                    automations = automations,
                    onToggleAutomation = { auto ->
                        coroutineScope.launch {
                            viewModel.repository.updateAutomation(auto.copy(isEnabled = !auto.isEnabled))
                        }
                    },
                    onCreateAutomation = { name, trigType, trigCond, secCond, acts ->
                        coroutineScope.launch {
                            val newAuto = AutomationEntity(
                                name = name,
                                triggerType = trigType,
                                triggerCondition = trigCond,
                                secondCondition = secCond,
                                actionsList = acts,
                                isEnabled = true
                            )
                            viewModel.repository.saveAutomation(newAuto)
                            viewModel.logAction("Automatización Creada", name, "Automation", "COMPLETED")
                        }
                    },
                    onExecuteAutomation = { auto ->
                        viewModel.executeAutomation(auto)
                    },
                    onDeleteAutomation = { id ->
                        coroutineScope.launch {
                            viewModel.repository.deleteAutomation(id)
                        }
                    }
                )

                JarvisNavTab.MEMORY -> MemoryScreen(
                    memories = memories,
                    onSaveMemory = { title, content, cat, tags ->
                        coroutineScope.launch {
                            viewModel.repository.saveMemory(
                                com.example.data.local.entity.MemoryEntity(
                                    title = title,
                                    content = content,
                                    category = cat,
                                    tags = tags
                                )
                            )
                            viewModel.logAction("Recuerdo Almacenado", title, "Memory", "COMPLETED")
                        }
                    },
                    onDeleteMemory = { id ->
                        coroutineScope.launch {
                            viewModel.repository.deleteMemory(id)
                        }
                    },
                    onClearAllMemories = {
                        coroutineScope.launch {
                            viewModel.repository.clearAllMemories()
                            viewModel.logAction("Memoria Purgada", "Todos los recuerdos eliminados", "Memory", "COMPLETED")
                        }
                    }
                )

                JarvisNavTab.CONNECTIONS -> ConnectionsScreen(
                    connections = connections,
                    onToggleConnection = { conn ->
                        coroutineScope.launch {
                            viewModel.repository.updateConnection(conn.copy(isConnected = !conn.isConnected))
                        }
                    }
                )

                JarvisNavTab.HISTORY -> HistoryScreen(
                    historyList = history,
                    onClearAllHistory = {
                        coroutineScope.launch {
                            viewModel.repository.clearAllHistory()
                        }
                    },
                    onUndoAction = { action ->
                        viewModel.logAction("Acción Deshecha", action.title, "System", "COMPLETED")
                    }
                )

                JarvisNavTab.SECURITY -> PermissionsScreen(
                    permissions = viewModel.permissionManager.getAllPermissions(),
                    onOpenSettings = { viewModel.permissionManager.openAppSettings() },
                    onOpenAccessibilitySettings = { viewModel.permissionManager.openAccessibilitySettings() }
                )

                JarvisNavTab.VISION -> VisionScreen(
                    isAnalyzing = isVisionAnalyzing,
                    analysisResult = visionResult,
                    onAnalyzeImage = { bmp, prompt ->
                        viewModel.analyzeImage(bmp, prompt)
                    }
                )

                JarvisNavTab.SETTINGS -> SettingsScreen(
                    selectedModel = selectedModel,
                    onSelectModel = { viewModel.setSelectedModel(it) },
                    isAdminActive = isAdminActive,
                    adminTimeFormatted = adminTimeFormatted,
                    onEnableAdmin = { mins -> viewModel.adminModeManager.enableAdminMode(mins) },
                    onDisableAdmin = { viewModel.adminModeManager.disableAdminMode() },
                    isHotwordEnabled = isHotwordEnabled,
                    onToggleHotword = { viewModel.voiceController.toggleHotword() },
                    pitch = voicePitch,
                    onPitchChange = { viewModel.voiceController.setPitch(it) },
                    volume = voiceVolume,
                    onVolumeChange = { viewModel.voiceController.setVolume(it) }
                )
            }

            // Pending Action Confirmation Modal
            pendingConfirmation?.let { pending ->
                ActionConfirmationDialog(
                    title = pending.title,
                    details = pending.details,
                    onConfirm = { viewModel.confirmPendingAction() },
                    onDismiss = { viewModel.cancelPendingAction() }
                )
            }
        }
    }
}
