package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Connected services (Gemini, Spotify, WhatsApp, Gmail, Calendar, SmartHome, etc.)
 */
@Entity(tableName = "connections")
data class ConnectionEntity(
    @PrimaryKey
    val serviceId: String,
    val name: String,
    val isConnected: Boolean,
    val accountInfo: String = "",
    val statusDescription: String = "",
    val permissionsGranted: Boolean = true,
    val iconName: String = ""
)
