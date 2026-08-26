package com.example.device

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.Settings
import android.widget.Toast

data class DeviceTelemetry(
    val deviceModel: String = "POCO M7 4G",
    val batteryPercent: Int = 85,
    val isCharging: Boolean = false,
    val isWifiConnected: Boolean = true,
    val networkType: String = "Wi-Fi (5 GHz)",
    val volumeLevel: Int = 70,
    val currentApp: String = "JARVIS OS"
)

class DeviceController(private val context: Context) {

    fun getDeviceTelemetry(): DeviceTelemetry {
        // 1. Read Battery
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 85
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 85
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        // 2. Read Network
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = cm?.activeNetwork
        val caps = cm?.getNetworkCapabilities(network)
        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val isCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        val netType = when {
            isWifi -> "Wi-Fi Enlazado"
            isCellular -> "4G LTE (POCO)"
            else -> "Modo Local"
        }

        // 3. Read Volume
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val currentVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 7
        val maxVol = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        val volPct = (currentVol * 100) / (if (maxVol > 0) maxVol else 15)

        return DeviceTelemetry(
            deviceModel = "POCO M7 4G",
            batteryPercent = batteryPct,
            isCharging = isCharging,
            isWifiConnected = isWifi || isCellular,
            networkType = netType,
            volumeLevel = volPct,
            currentApp = "JARVIS Suite"
        )
    }

    fun openApplication(appName: String): Boolean {
        val lower = appName.lowercase()
        val packageName = when {
            lower.contains("spotify") || lower.contains("música") -> "com.spotify.music"
            lower.contains("whatsapp") -> "com.whatsapp"
            lower.contains("youtube") -> "com.google.android.youtube"
            lower.contains("mapas") || lower.contains("maps") -> "com.google.android.apps.maps"
            lower.contains("gmail") || lower.contains("correo") -> "com.google.android.gm"
            lower.contains("cámara") || lower.contains("camara") -> "com.android.camera"
            lower.contains("ajustes") || lower.contains("configuración") -> "com.android.settings"
            else -> null
        }

        if (packageName != null) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return true
            }
        }

        // Fallback standard Intents
        return when {
            lower.contains("ajustes") || lower.contains("configuración") -> {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                true
            }
            lower.contains("contactos") -> {
                val intent = Intent(Intent.ACTION_VIEW, ContactsContractUri()).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                    true
                } catch (e: Exception) {
                    false
                }
            }
            lower.contains("reloj") || lower.contains("alarma") || lower.contains("temporizador") -> {
                val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                    true
                } catch (e: Exception) {
                    false
                }
            }
            else -> {
                // Try searching in web or generic intent
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$appName")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(webIntent)
                    true
                } catch (e: Exception) {
                    Toast.makeText(context, "No se encontró el paquete para $appName", Toast.LENGTH_SHORT).show()
                    false
                }
            }
        }
    }

    private fun ContactsContractUri(): Uri = Uri.parse("content://contacts/people")

    fun openDialer(phoneNumberOrName: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${Uri.encode(phoneNumberOrName)}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir el marcador telefónico", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendWhatsAppMessage(contactOrPhone: String, message: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_TEXT, message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback generic SMS or share intent
            val genericIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(Intent.createChooser(genericIntent, "Enviar mensaje a $contactOrPhone").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (ex: Exception) {
                Toast.makeText(context, "No se pudo enviar el mensaje", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setMusicVolume(targetPercent: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetVol = ((targetPercent.coerceIn(0, 100) / 100f) * maxVol).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, AudioManager.FLAG_SHOW_UI)
    }

    fun openSystemSettings(action: String = Settings.ACTION_SETTINGS) {
        val intent = Intent(action).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Ajuste no disponible directamente", Toast.LENGTH_SHORT).show()
        }
    }
}
