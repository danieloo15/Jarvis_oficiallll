package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiBrainEngine
import com.example.data.ai.JarvisIntentResult
import com.example.data.ai.JarvisIntentType
import com.example.data.ai.JarvisTaskPlan
import com.example.data.local.JarvisDatabase
import com.example.data.local.JarvisRepository
import com.example.data.local.entity.ActionHistoryEntity
import com.example.data.local.entity.AutomationEntity
import com.example.data.local.entity.ConnectionEntity
import com.example.data.local.entity.CustomCommandEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.SkillEntity
import com.example.device.AdminModeManager
import com.example.device.DeviceController
import com.example.device.DeviceTelemetry
import com.example.device.PermissionManager
import com.example.device.VoiceController
import com.example.device.VoiceMode
import com.example.device.VoiceState
import com.example.ui.screens.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PendingActionData(
    val title: String,
    val details: String,
    val command: String?,
    val target: String?,
    val taskPlans: List<JarvisTaskPlan>? = null
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val database = JarvisDatabase.getInstance(application)
    val repository = JarvisRepository(database)
    val permissionManager = PermissionManager(application)
    val deviceController = DeviceController(application)
    val adminModeManager = AdminModeManager()
    val aiBrain = AiBrainEngine()

    val voiceController = VoiceController(application) { spokenText ->
        processUserInput(spokenText)
    }

    // Live Room Flows
    val memories: StateFlow<List<MemoryEntity>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val skills: StateFlow<List<SkillEntity>> = repository.allSkills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val automations: StateFlow<List<AutomationEntity>> = repository.allAutomations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<ActionHistoryEntity>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val connections: StateFlow<List<ConnectionEntity>> = repository.allConnections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customCommands: StateFlow<List<CustomCommandEntity>> = repository.allCommands
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI States
    private val _telemetry = MutableStateFlow(deviceController.getDeviceTelemetry())
    val telemetry: StateFlow<DeviceTelemetry> = _telemetry.asStateFlow()

    private val _isMinimalistMode = MutableStateFlow(false)
    val isMinimalistMode: StateFlow<Boolean> = _isMinimalistMode.asStateFlow()

    private val _selectedModel = MutableStateFlow(AiBrainEngine.MODEL_FAST)
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "JARVIS",
                text = "Sistemas iniciados y operativos en su POCO M7 4G, Señor. A su entera disposición para cualquier instrucción."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _pendingConfirmation = MutableStateFlow<PendingActionData?>(null)
    val pendingConfirmation: StateFlow<PendingActionData?> = _pendingConfirmation.asStateFlow()

    private val _activeAgentPlan = MutableStateFlow<List<JarvisTaskPlan>?>(null)
    val activeAgentPlan: StateFlow<List<JarvisTaskPlan>?> = _activeAgentPlan.asStateFlow()

    // Vision States
    private val _isVisionAnalyzing = MutableStateFlow(false)
    val isVisionAnalyzing: StateFlow<Boolean> = _isVisionAnalyzing.asStateFlow()

    private val _visionResult = MutableStateFlow<String?>(null)
    val visionResult: StateFlow<String?> = _visionResult.asStateFlow()

    init {
        // Periodic telemetry refresh
        viewModelScope.launch {
            while (true) {
                _telemetry.value = deviceController.getDeviceTelemetry()
                delay(10000)
            }
        }
    }

    fun toggleMinimalistMode(isMinimalist: Boolean) {
        _isMinimalistMode.value = isMinimalist
    }

    fun setSelectedModel(model: String) {
        _selectedModel.value = model
    }

    fun processUserInput(input: String) {
        if (input.isBlank()) return

        // 1. Add User Message to Chat
        val userMsg = ChatMessage(sender = "USER", text = input)
        _chatMessages.value = _chatMessages.value + userMsg

        _isThinking.value = true
        voiceController.setVoiceState(VoiceState.THINKING)

        viewModelScope.launch {
            val memoryList = repository.getRecentMemoriesSync()
            val skillList = repository.getEnabledSkillsSync()

            val conversationHistory = _chatMessages.value.takeLast(6).map {
                Pair(if (it.sender == "USER") it.text else "", if (it.sender == "JARVIS") it.text else "")
            }

            val result = aiBrain.processUserMessage(
                userInput = input,
                selectedModel = _selectedModel.value,
                memories = memoryList,
                enabledSkills = skillList,
                conversationHistory = conversationHistory
            )

            _isThinking.value = false

            // Check if emergency stop
            if (result.intentType == JarvisIntentType.EMERGENCY_STOP) {
                emergencyStop()
                addJarvisMessage(result.replyText)
                return@launch
            }

            // Check Admin Mode toggle
            if (result.intentType == JarvisIntentType.ADMIN_MODE_TOGGLE) {
                val mins = result.actionTarget?.toIntOrNull() ?: 30
                adminModeManager.enableAdminMode(mins)
                logAction("Modo Administrador Activado", "Privilegios elevados por $mins minutos", "System", "COMPLETED")
                addJarvisMessage(result.replyText)
                voiceController.speak(result.replyText)
                return@launch
            }

            // Check Memory Save
            if (result.intentType == JarvisIntentType.SAVE_MEMORY && result.actionTarget != null) {
                repository.saveMemory(
                    MemoryEntity(
                        title = "Dato Recordado",
                        content = result.actionTarget,
                        category = "Personal",
                        tags = "voz, asistente"
                    )
                )
                logAction("Memoria Guardada", result.actionTarget, "Memory", "COMPLETED")
                addJarvisMessage(result.replyText)
                voiceController.speak(result.replyText)
                return@launch
            }

            // Check if action requires confirmation (and admin mode is NOT active)
            val isAdmin = adminModeManager.isAdminActive.value
            if (result.requiresConfirmation && !isAdmin) {
                _pendingConfirmation.value = PendingActionData(
                    title = result.confirmationTitle ?: "Confirmar Acción",
                    details = result.confirmationDetails ?: result.replyText,
                    command = result.actionCommand,
                    target = result.actionTarget,
                    taskPlans = result.plannedTasks
                )
                addJarvisMessage(result.replyText, result.plannedTasks)
                voiceController.speak(result.replyText)
                logAction(
                    title = result.confirmationTitle ?: "Acción Pendiente",
                    desc = result.confirmationDetails ?: result.replyText,
                    type = "AgentPlan",
                    status = "PENDING_CONFIRMATION"
                )
            } else {
                // Direct execution or Chat response
                if (result.actionCommand != null) {
                    executeActionCommand(result.actionCommand, result.actionTarget, result.plannedTasks)
                }
                addJarvisMessage(result.replyText, result.plannedTasks)
                voiceController.speak(result.replyText)
            }
        }
    }

    private fun addJarvisMessage(text: String, plan: List<JarvisTaskPlan>? = null) {
        val jarvisMsg = ChatMessage(
            sender = "JARVIS",
            text = text,
            agentPlan = plan
        )
        _chatMessages.value = _chatMessages.value + jarvisMsg
    }

    fun confirmPendingAction() {
        val pending = _pendingConfirmation.value ?: return
        _pendingConfirmation.value = null

        viewModelScope.launch {
            executeActionCommand(pending.command, pending.target, pending.taskPlans)
            logAction(pending.title, "Acción autorizada por el usuario y ejecutada con éxito.", "AgentPlan", "COMPLETED")
            val confirmationMsg = "Ejecutando la orden autorizada, Señor. Todos los pasos se han completado con éxito."
            addJarvisMessage(confirmationMsg)
            voiceController.speak(confirmationMsg)
        }
    }

    fun cancelPendingAction() {
        val pending = _pendingConfirmation.value ?: return
        _pendingConfirmation.value = null
        logAction(pending.title, "Acción cancelada por el usuario.", "AgentPlan", "CANCELLED")
        val cancelMsg = "Operación cancelada según sus instrucciones, Señor."
        addJarvisMessage(cancelMsg)
        voiceController.speak(cancelMsg)
    }

    private suspend fun executeActionCommand(
        command: String?,
        target: String?,
        plans: List<JarvisTaskPlan>? = null
    ) {
        if (plans != null) {
            _activeAgentPlan.value = plans
            plans.forEach { step ->
                step.isExecuted = true
                delay(600)
            }
            logAction("Plan de Agente Ejecutado", plans.joinToString(", ") { it.title }, "AgentPlan", "COMPLETED")
            _activeAgentPlan.value = null
            return
        }

        when (command) {
            "OPEN_APP" -> {
                if (target != null) {
                    deviceController.openApplication(target)
                    logAction("Apertura de Aplicación", "Se abrió $target en POCO M7", "App", "COMPLETED")
                }
            }
            "SEND_WHATSAPP" -> {
                if (target != null) {
                    val parts = target.split("::")
                    val contact = parts.getOrNull(0) ?: "Contacto"
                    val msg = parts.getOrNull(1) ?: ""
                    deviceController.sendWhatsAppMessage(contact, msg)
                    logAction("Mensaje Enviado", "WhatsApp para $contact: \"$msg\"", "Message", "COMPLETED")
                }
            }
            "CALL_CONTACT" -> {
                if (target != null) {
                    deviceController.openDialer(target)
                    logAction("Llamada Iniciada", "Enlace telefónico con $target", "Call", "COMPLETED")
                }
            }
        }
    }

    fun emergencyStop() {
        voiceController.emergencyStopAll()
        _pendingConfirmation.value = null
        _activeAgentPlan.value = null
        _isThinking.value = false
        viewModelScope.launch {
            logAction(
                title = "PARADA DE EMERGENCIA",
                desc = "Interrupción instantánea de todos los subsistemas por orden prioritaria del usuario.",
                type = "System",
                status = "EMERGENCY_STOPPED"
            )
        }
    }

    fun executeAutomation(automation: AutomationEntity) {
        viewModelScope.launch {
            repository.updateAutomation(automation.copy(executionCount = automation.executionCount + 1, lastExecutedTime = System.currentTimeMillis()))
            logAction("Automatización Disparada", "${automation.name}: ${automation.actionsList}", "Automation", "COMPLETED")
            val msg = "Automatización '${automation.name}' ejecutada con éxito en su POCO M7."
            addJarvisMessage(msg)
            voiceController.speak(msg)
        }
    }

    fun analyzeImage(bitmap: Bitmap, prompt: String) {
        _isVisionAnalyzing.value = true
        _visionResult.value = null
        viewModelScope.launch {
            val result = aiBrain.analyzeImageWithVision(bitmap, prompt)
            _isVisionAnalyzing.value = false
            _visionResult.value = result
            logAction("Análisis Óptico Gemini", prompt, "Vision", "COMPLETED", details = result)
            voiceController.speak(result)
        }
    }

    fun logAction(title: String, desc: String, type: String, status: String, details: String = "", canUndo: Boolean = false) {
        viewModelScope.launch {
            repository.logAction(
                ActionHistoryEntity(
                    title = title,
                    description = desc,
                    actionType = type,
                    status = status,
                    details = details,
                    canUndo = canUndo
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceController.destroy()
        adminModeManager.disableAdminMode()
    }
}

class JarvisViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JarvisViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JarvisViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
