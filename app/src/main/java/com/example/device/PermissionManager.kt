package com.example.device

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

enum class PermissionState {
    GRANTED,
    DENIED,
    REQUIRES_SETUP
}

data class JarvisPermissionInfo(
    val id: String,
    val name: String,
    val iconName: String,
    val status: PermissionState,
    val description: String,
    val whyNeeded: String,
    val androidPermission: String? = null
)

class PermissionManager(private val context: Context) {

    fun getAllPermissions(): List<JarvisPermissionInfo> {
        val audioStatus = checkPermission(Manifest.permission.RECORD_AUDIO)
        val cameraStatus = checkPermission(Manifest.permission.CAMERA)
        val contactsStatus = checkPermission(Manifest.permission.READ_CONTACTS)
        val callStatus = checkPermission(Manifest.permission.CALL_PHONE)
        val notifStatus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermission(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            PermissionState.GRANTED
        }
        val locationStatus = checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)

        return listOf(
            JarvisPermissionInfo(
                id = "audio",
                name = "Micrófono & Voz",
                iconName = "mic",
                status = audioStatus,
                description = "Reconocimiento de voz y comandos directos",
                whyNeeded = "Permite hablar con JARVIS y activar el modo conversación continua.",
                androidPermission = Manifest.permission.RECORD_AUDIO
            ),
            JarvisPermissionInfo(
                id = "camera",
                name = "Cámara & Visión",
                iconName = "camera",
                status = cameraStatus,
                description = "Captura de entorno y análisis visual con Gemini",
                whyNeeded = "Permite a JARVIS responder preguntas sobre lo que estás viendo mediante el sensor óptico.",
                androidPermission = Manifest.permission.CAMERA
            ),
            JarvisPermissionInfo(
                id = "contacts",
                name = "Contactos",
                iconName = "contacts",
                status = contactsStatus,
                description = "Consulta y preparación de llamadas o WhatsApp",
                whyNeeded = "Permite a JARVIS buscar los números de tus contactos para preparar mensajes o llamadas.",
                androidPermission = Manifest.permission.READ_CONTACTS
            ),
            JarvisPermissionInfo(
                id = "calls",
                name = "Llamadas Telefónicas",
                iconName = "call",
                status = callStatus,
                description = "Enlace directo con la app de llamadas del POCO",
                whyNeeded = "Permite iniciar llamadas telefónicas cuando confirmes la orden.",
                androidPermission = Manifest.permission.CALL_PHONE
            ),
            JarvisPermissionInfo(
                id = "notifications",
                name = "Notificaciones",
                iconName = "notifications",
                status = notifStatus,
                description = "Avisos de estado y lectura de alertas importantes",
                whyNeeded = "Muestra recordatorios de tareas en segundo plano y estado del asistente.",
                androidPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else null
            ),
            JarvisPermissionInfo(
                id = "location",
                name = "Ubicación",
                iconName = "location",
                status = locationStatus,
                description = "Automatizaciones basadas en estar en casa o fuera",
                whyNeeded = "Permite activar rutinas inteligentes como 'Modo Noche al llegar a casa'.",
                androidPermission = Manifest.permission.ACCESS_FINE_LOCATION
            ),
            JarvisPermissionInfo(
                id = "accessibility",
                name = "Servicio de Accesibilidad",
                iconName = "accessibility",
                status = PermissionState.REQUIRES_SETUP,
                description = "Automatización avanzada e interacción con pantalla",
                whyNeeded = "Mecanismo oficial de Android para interactuar con botones o leer contenido en apps compatibles.",
                androidPermission = null
            )
        )
    }

    private fun checkPermission(permission: String): PermissionState {
        return when (ContextCompat.checkSelfPermission(context, permission)) {
            PackageManager.PERMISSION_GRANTED -> PermissionState.GRANTED
            else -> PermissionState.DENIED
        }
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            openAppSettings()
        }
    }
}
