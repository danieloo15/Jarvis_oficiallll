package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AutomationDao
import com.example.data.local.dao.ConnectionDao
import com.example.data.local.dao.CustomCommandDao
import com.example.data.local.dao.HistoryDao
import com.example.data.local.dao.MemoryDao
import com.example.data.local.dao.SkillDao
import com.example.data.local.entity.ActionHistoryEntity
import com.example.data.local.entity.AutomationEntity
import com.example.data.local.entity.ConnectionEntity
import com.example.data.local.entity.CustomCommandEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.SkillEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MemoryEntity::class,
        SkillEntity::class,
        AutomationEntity::class,
        ActionHistoryEntity::class,
        ConnectionEntity::class,
        CustomCommandEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun skillDao(): SkillDao
    abstract fun automationDao(): AutomationDao
    abstract fun historyDao(): HistoryDao
    abstract fun connectionDao(): ConnectionDao
    abstract fun customCommandDao(): CustomCommandDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisDatabase? = null

        fun getInstance(context: Context): JarvisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisDatabase::class.java,
                    "jarvis_master.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            populateInitialData(getInstance(context))
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(database: JarvisDatabase) {
            // Seed default Skills
            val defaultSkills = listOf(
                SkillEntity("music", "Música", "Multimedia", "music", "Control de reproducción y Spotify", true, "None", "Play, Pause, Open Spotify, Next Track"),
                SkillEntity("whatsapp", "WhatsApp", "Mensajería", "chat", "Preparación y envío de mensajes", true, "Contacts", "Prepare Message, Send WhatsApp"),
                SkillEntity("camera", "Cámara & Visión", "Visión", "camera", "Captura y análisis visual con IA", true, "Camera", "Take Photo, Scan Screen, Describe View"),
                SkillEntity("internet", "Internet & Búsqueda", "Información", "public", "Búsqueda web en tiempo real", true, "Internet", "Search Web, Get News, Weather"),
                SkillEntity("files", "Gestor de Archivos", "Sistema", "folder", "Búsqueda universal de fotos y docs", true, "Storage", "Find Photos, Open Documents"),
                SkillEntity("automations", "Automatizaciones", "Productividad", "bolt", "Motor de rutinas por disparadores", true, "None", "Run Routine, Schedule Task"),
                SkillEntity("ai_brain", "Cerebro Multimodelo", "IA", "psychology", "Razonamiento profundo con Gemini", true, "Internet", "Chat, Complex Reasoning, Planning"),
                SkillEntity("settings", "Ajustes del Sistema", "Sistema", "settings", "Control de volumen, brillo y DND", true, "ModifyAudio", "Toggle DND, Set Volume, Open Settings"),
                SkillEntity("calls", "Llamadas", "Telefonía", "call", "Realizar y gestionar llamadas", true, "CallPhone", "Make Call, Dial Number"),
                SkillEntity("contacts", "Contactos", "Telefonía", "contacts", "Búsqueda y gestión de contactos", true, "Contacts", "Search Contact, View Contact"),
                SkillEntity("notifications", "Notificaciones", "Sistema", "notifications", "Lectura y filtrado de avisos", true, "Notifications", "Read Notifications, Filter Apps")
            )
            database.skillDao().insertAll(defaultSkills)

            // Seed default Connections
            val defaultConnections = listOf(
                ConnectionEntity("gemini", "Google Gemini", true, "API Clave Activa", "Modelo: Gemini 3.5 Flash", true, "psychology"),
                ConnectionEntity("spotify", "Spotify", true, "Integrado vía Intent/App", "Listo para reproducir música", true, "music"),
                ConnectionEntity("whatsapp", "WhatsApp", true, "Integrado vía Intent/Direct", "Preparación asistida", true, "chat"),
                ConnectionEntity("gmail", "Gmail", false, "No vinculado", "Requiere autenticación", false, "mail"),
                ConnectionEntity("calendar", "Google Calendar", false, "No vinculado", "Requiere sincronización", false, "calendar"),
                ConnectionEntity("poco_m7", "POCO M7 4G (Este Dispositivo)", true, "Sensor Suite Activa", "Telemetría y Control Local", true, "smartphone")
            )
            database.connectionDao().insertAll(defaultConnections)

            // Seed default Memories
            val defaultMemories = listOf(
                MemoryEntity(
                    title = "Dispositivo Principal",
                    content = "El usuario utiliza un smartphone POCO M7 4G con Android.",
                    category = "Personal",
                    tags = "dispositivo, poco, hardware"
                ),
                MemoryEntity(
                    title = "Estilo de Asistente",
                    content = "JARVIS debe responder con cortesía británica, concisión, elegancia y precisión técnica.",
                    category = "Preference",
                    tags = "personalidad, tono, estilo"
                ),
                MemoryEntity(
                    title = "Protocolo de Seguridad",
                    content = "Las acciones sensibles como envío de mensajes, eliminación de datos o llamadas deben pasar por confirmación.",
                    category = "Preference",
                    tags = "seguridad, confirmacion"
                )
            )
            defaultMemories.forEach { database.memoryDao().insert(it) }

            // Seed default Automations
            val defaultAutomations = listOf(
                AutomationEntity(
                    name = "Modo Noche",
                    triggerType = "Time",
                    triggerCondition = "Después de las 22:00",
                    secondCondition = "Al llegar a casa",
                    actionsList = "Activar No Molestar, Bajar brillo, Silenciar alertas",
                    isEnabled = true
                ),
                AutomationEntity(
                    name = "Modo Estudio",
                    triggerType = "AppOpen",
                    triggerCondition = "Abrir app de estudio",
                    secondCondition = "Temporizador 45 min",
                    actionsList = "Activar No Molestar, Temporizador 45 min, Música de concentración",
                    isEnabled = true
                )
            )
            database.automationDao().insertAll(defaultAutomations)

            // Seed default Custom Commands
            val defaultCommands = listOf(
                CustomCommandEntity(
                    triggerPhrase = "Modo cine",
                    description = "Configuración óptima para ver películas",
                    actionsList = "Activar No Molestar, Bajar brillo al 15%, Ajustar volumen multimedia al 70%"
                ),
                CustomCommandEntity(
                    triggerPhrase = "Modo trabajo",
                    description = "Concentración máxima para proyectos",
                    actionsList = "Silenciar notificaciones, Abrir navegador, Registrar sesión"
                )
            )
            database.customCommandDao().insertAll(defaultCommands)

            // Seed initial Action History entry
            database.historyDao().insert(
                ActionHistoryEntity(
                    title = "Inicialización del Sistema",
                    description = "JARVIS activado con éxito en POCO M7 4G. Todos los subsistemas en línea.",
                    actionType = "System",
                    status = "COMPLETED",
                    details = "Cerebro IA, Memoria Local y Telemetría listos."
                )
            )
        }
    }
}
