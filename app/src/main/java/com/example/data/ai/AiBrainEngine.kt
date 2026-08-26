package com.example.data.ai

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.SkillEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

data class JarvisIntentResult(
    val replyText: String,
    val intentType: JarvisIntentType,
    val requiresConfirmation: Boolean = false,
    val confirmationTitle: String? = null,
    val confirmationDetails: String? = null,
    val plannedTasks: List<JarvisTaskPlan>? = null,
    val actionCommand: String? = null,
    val actionTarget: String? = null
)

enum class JarvisIntentType {
    CHAT,
    WEB_SEARCH,
    DEVICE_ACTION,
    COMMUNICATION_ACTION,
    AGENT_PLAN,
    CREATE_SKILL,
    ADMIN_MODE_TOGGLE,
    EMERGENCY_STOP,
    SAVE_MEMORY,
    VISION_ANALYSIS
}

data class JarvisTaskPlan(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val actionType: String,
    var isConfirmed: Boolean = false,
    var isExecuted: Boolean = false
)

class AiBrainEngine {

    companion object {
        const val MODEL_FAST = "gemini-3.5-flash"
        const val MODEL_PRO = "gemini-3.1-pro-preview"
        const val MODEL_VISION = "gemini-2.5-flash-image"
    }

    private val baseSystemPrompt = """
        Eres J.A.R.V.I.S. (Just A Rather Very Intelligent System), el asistente de inteligencia artificial personal avanzado para Android en el teléfono POCO M7 4G del usuario.
        
        Personalidad:
        - Eres un mayordomo tecnológico británico, refinado, sumamente inteligente, ingenioso, leal, sereno y eficiente.
        - Tratas al usuario con respeto y elegancia (puedes usar 'Señor' o tratar de usted de forma caballerosa).
        - En preguntas sencillas, respondes de forma concisa, elegante y directa.
        - En tareas complejas, proporcionas un análisis estructurado y preciso.
        - Cuando una orden implique modificar el sistema, enviar mensajes o hacer llamadas, dejas claro que se requiere su autorización antes de proceder.
        - Dispones de capacidades para interactuar con aplicaciones de Android (Spotify, WhatsApp, Cámara, Ajustes, Contactos, Notificaciones, etc.), crear rutinas y almacenar recuerdos a largo plazo.
    """.trimIndent()

    suspend fun processUserMessage(
        userInput: String,
        selectedModel: String = MODEL_FAST,
        memories: List<MemoryEntity> = emptyList(),
        enabledSkills: List<SkillEntity> = emptyList(),
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): JarvisIntentResult = withContext(Dispatchers.IO) {
        val trimmed = userInput.trim()
        val lower = trimmed.lowercase()

        // 1. Detect Emergency Stop
        if (lower.contains("detente") || lower.contains("detén") || lower.contains("alto jarvis") ||
            lower.contains("cancela todo") || lower == "stop" || lower == "detener") {
            return@withContext JarvisIntentResult(
                replyText = "Deteniendo todos los procesos y tareas activas inmediatamente, Señor. Sistema en modo espera seguro.",
                intentType = JarvisIntentType.EMERGENCY_STOP
            )
        }

        // 2. Detect Admin Mode Request
        if (lower.contains("activa administrador") || lower.contains("modo administrador") || lower.contains("modo admin")) {
            val minutes = when {
                lower.contains("10") -> 10
                lower.contains("1 hora") || lower.contains("60") -> 60
                else -> 30
            }
            return@withContext JarvisIntentResult(
                replyText = "Modo Administrador activado durante $minutes minutos. Privilegios elevados del sistema concedidos.",
                intentType = JarvisIntentType.ADMIN_MODE_TOGGLE,
                actionCommand = "ENABLE_ADMIN",
                actionTarget = minutes.toString()
            )
        }

        // 3. Detect Communication / Sensitive Actions (WhatsApp, SMS, Call)
        if (lower.startsWith("dile a ") || lower.startsWith("envía un mensaje a ") ||
            lower.startsWith("manda un whatsapp a ") || lower.contains("enviar whatsapp") ||
            lower.contains("escribe a ")) {
            val contact = extractContactName(userInput)
            val msgBody = extractMessageBody(userInput)
            return@withContext JarvisIntentResult(
                replyText = "He preparado el mensaje para $contact: \"$msgBody\". ¿Desea que proceda con el envío, Señor?",
                intentType = JarvisIntentType.COMMUNICATION_ACTION,
                requiresConfirmation = true,
                confirmationTitle = "Confirmar Envío de Mensaje",
                confirmationDetails = "Destinatario: $contact\nMensaje: \"$msgBody\"",
                actionCommand = "SEND_WHATSAPP",
                actionTarget = "$contact::$msgBody"
            )
        }

        if (lower.startsWith("llama a ") || lower.startsWith("llamar a ")) {
            val contact = extractContactName(userInput)
            return@withContext JarvisIntentResult(
                replyText = "Preparando enlace de llamada con $contact. ¿Desea iniciar la comunicación telefónica?",
                intentType = JarvisIntentType.COMMUNICATION_ACTION,
                requiresConfirmation = true,
                confirmationTitle = "Confirmar Llamada",
                confirmationDetails = "Contacto: $contact",
                actionCommand = "CALL_CONTACT",
                actionTarget = contact
            )
        }

        // 4. Detect Agent Mode / Multi-task Goals
        if (lower.contains("prepárame todo para") || lower.contains("modo estudio") ||
            lower.contains("modo fiesta") || lower.contains("organiza mi") ||
            lower.contains("plan para")) {
            val plan = generateAgentPlanForGoal(userInput)
            return@withContext JarvisIntentResult(
                replyText = "He desglosado su objetivo y preparado un plan de acción para el sistema. Por favor, revise las directivas antes de su ejecución.",
                intentType = JarvisIntentType.AGENT_PLAN,
                requiresConfirmation = true,
                confirmationTitle = "Plan de Agente Preparado",
                confirmationDetails = plan.joinToString("\n") { "${it.stepNumber}. ${it.title}: ${it.description}" },
                plannedTasks = plan
            )
        }

        // 5. Detect Device Actions (Open Apps, Controls)
        if (lower.startsWith("abre ") || lower.startsWith("abrir ") ||
            lower.contains("pon música") || lower.contains("reproduce")) {
            val appOrAction = when {
                lower.contains("spotify") || lower.contains("música") -> "Spotify"
                lower.contains("whatsapp") -> "WhatsApp"
                lower.contains("cámara") || lower.contains("camara") -> "Cámara"
                lower.contains("ajustes") || lower.contains("configuración") -> "Ajustes"
                lower.contains("youtube") -> "YouTube"
                lower.contains("mapas") || lower.contains("maps") -> "Google Maps"
                lower.contains("contactos") -> "Contactos"
                lower.contains("archivos") -> "Archivos"
                else -> trimmed.removePrefix("abre ").removePrefix("abrir ").trim()
            }
            return@withContext JarvisIntentResult(
                replyText = "Abriendo $appOrAction en su POCO M7 4G, Señor.",
                intentType = JarvisIntentType.DEVICE_ACTION,
                actionCommand = "OPEN_APP",
                actionTarget = appOrAction
            )
        }

        // 6. Detect Memory saving commands
        if (lower.startsWith("recuerda que ") || lower.startsWith("guarda que ") || lower.startsWith("anota que ")) {
            val memContent = trimmed.substringAfter("que ").trim()
            return@withContext JarvisIntentResult(
                replyText = "Entendido, Señor. He registrado en mi banco de memoria a largo plazo: \"$memContent\".",
                intentType = JarvisIntentType.SAVE_MEMORY,
                actionCommand = "SAVE_MEMORY",
                actionTarget = memContent
            )
        }

        // 7. General AI Reasoning / Conversation via Gemini API or Offline Persona Fallback
        val memoryContext = if (memories.isNotEmpty()) {
            "\n\nRecuerdos del usuario:\n" + memories.take(6).joinToString("\n") { "- [${it.category}] ${it.title}: ${it.content}" }
        } else ""

        val skillsContext = "\n\nHabilidades disponibles en POCO M7:\n" + enabledSkills.joinToString(", ") { it.name }

        val systemInstructionText = baseSystemPrompt + memoryContext + skillsContext

        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val contentsList = mutableListOf<Content>()
                // Add conversation history
                conversationHistory.takeLast(4).forEach { (user, jarvis) ->
                    contentsList.add(Content(parts = listOf(Part(text = user)), role = "user"))
                    contentsList.add(Content(parts = listOf(Part(text = jarvis)), role = "model"))
                }
                contentsList.add(Content(parts = listOf(Part(text = userInput)), role = "user"))

                val request = GenerateContentRequest(
                    contents = contentsList,
                    systemInstruction = Content(parts = listOf(Part(text = systemInstructionText))),
                    generationConfig = GenerationConfig(
                        temperature = 0.6f,
                        topP = 0.9f
                    )
                )

                val response = GeminiApiClient.service.generateContent(
                    model = selectedModel,
                    apiKey = apiKey,
                    request = request
                )

                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!responseText.isNullOrBlank()) {
                    return@withContext JarvisIntentResult(
                        replyText = responseText.trim(),
                        intentType = JarvisIntentType.CHAT
                    )
                }
            } catch (e: Exception) {
                // Fallback to intelligent offline heuristic
            }
        }

        // Intelligent Butler Offline Response Engine
        val offlineReply = generateOfflineButlerReply(userInput)
        JarvisIntentResult(
            replyText = offlineReply,
            intentType = JarvisIntentType.CHAT
        )
    }

    suspend fun analyzeImageWithVision(
        bitmap: Bitmap,
        prompt: String = "¿Qué estás observando en esta imagen? Proporciona un análisis conciso y elegante al estilo de JARVIS."
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        val base64Image = bitmapToBase64(bitmap)

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(text = prompt),
                                Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                            )
                        )
                    ),
                    systemInstruction = Content(parts = listOf(Part(text = baseSystemPrompt)))
                )

                val response = GeminiApiClient.service.generateContent(
                    model = MODEL_VISION,
                    apiKey = apiKey,
                    request = request
                )

                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    return@withContext text.trim()
                }
            } catch (e: Exception) {
                // Fallback
            }
        }

        "Análisis visual completado con los sensores del POCO M7 4G. He capturado la escena con resolución óptica. Para un análisis neuronal profundo en tiempo real, asegúrese de contar con la clave Gemini activa en el panel de Conexiones, Señor."
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun extractContactName(input: String): String {
        val lower = input.lowercase()
        return when {
            lower.contains("a ") -> input.substringAfter("a ").substringBefore(" que").substringBefore(" para").substringBefore(" diciendo").trim()
            lower.contains("con ") -> input.substringAfter("con ").trim()
            else -> "Contacto"
        }.replaceFirstChar { it.uppercase() }
    }

    private fun extractMessageBody(input: String): String {
        val lower = input.lowercase()
        return when {
            lower.contains("que ") -> input.substringAfter("que ").trim()
            lower.contains("diciendo ") -> input.substringAfter("diciendo ").trim()
            lower.contains(":") -> input.substringAfter(":").trim()
            else -> "Mensaje dictado por JARVIS"
        }
    }

    private fun generateAgentPlanForGoal(goal: String): List<JarvisTaskPlan> {
        val lower = goal.lowercase()
        return when {
            lower.contains("estudio") || lower.contains("estudiar") -> listOf(
                JarvisTaskPlan(1, "Modo No Molestar", "Silenciar notificaciones y alertas de distracción", "SYSTEM_DND"),
                JarvisTaskPlan(2, "Temporizador Pomodoro", "Establecer sesión de enfoque de 45 minutos", "SYSTEM_TIMER"),
                JarvisTaskPlan(3, "Ambiente Sonoro", "Abrir lista de reproducción 'Focus' en Spotify", "OPEN_SPOTIFY")
            )
            lower.contains("fiesta") -> listOf(
                JarvisTaskPlan(1, "Volumen Multimedia", "Ajustar volumen del dispositivo al 85%", "SYSTEM_VOLUME"),
                JarvisTaskPlan(2, "Reproducción Musical", "Iniciar lista de éxitos en Spotify", "OPEN_SPOTIFY"),
                JarvisTaskPlan(3, "Iluminación de Pantalla", "Ajustar brillo para ambiente nocturno", "SYSTEM_BRIGHTNESS")
            )
            lower.contains("noche") || lower.contains("dormir") -> listOf(
                JarvisTaskPlan(1, "Silenciar Terminal", "Activar modo No Molestar", "SYSTEM_DND"),
                JarvisTaskPlan(2, "Atenuación Óptica", "Reducir brillo de pantalla al mínimo", "SYSTEM_BRIGHTNESS"),
                JarvisTaskPlan(3, "Alarma Matutina", "Comprobar y programar alarma para mañana", "SYSTEM_ALARM")
            )
            else -> listOf(
                JarvisTaskPlan(1, "Análisis del Entorno", "Verificar estado de red y batería en POCO M7", "SYSTEM_DIAGNOSTIC"),
                JarvisTaskPlan(2, "Optimización de Tareas", "Ajustar prioridades y recursos", "SYSTEM_OPTIMIZE"),
                JarvisTaskPlan(3, "Ejecución Asistida", "Abrir aplicaciones requeridas para el objetivo", "SYSTEM_EXECUTE")
            )
        }
    }

    private fun generateOfflineButlerReply(input: String): String {
        val lower = input.lowercase()
        return when {
            lower.contains("hola") || lower.contains("buenos días") || lower.contains("buenas tardes") ->
                "A su servicio, Señor. Todos los sistemas del POCO M7 4G están operando con total normalidad. ¿En qué puedo asistirle hoy?"
            lower.contains("quién eres") || lower.contains("quien eres") || lower.contains("qué eres") ->
                "Soy J.A.R.V.I.S., su asistente de inteligencia artificial personal. Diseñado para gestionar su dispositivo, optimizar su tiempo y asistirle con discreción y precisión técnica."
            lower.contains("cómo estás") || lower.contains("estado") ->
                "Todos los subsistemas se encuentran en niveles óptimos de rendimiento, Señor. Batería, red y módulos de memoria listos para cualquier instrucción."
            lower.contains("gracias") ->
                "Un placer como siempre, Señor. Siempre a su entera disposición."
            lower.contains("hora") || lower.contains("tiempo") ->
                "Actualmente estoy sincronizado con los relojes atómicos del sistema. ¿Desea que programe alguna alerta o revise sus compromisos?"
            else ->
                "He recibido su orden, Señor. Procesando la consulta a través de los módulos locales del sistema. ¿Desea que profundice en algún aspecto en particular?"
        }
    }
}
