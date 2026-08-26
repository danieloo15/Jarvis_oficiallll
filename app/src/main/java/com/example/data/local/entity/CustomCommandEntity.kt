package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Custom commands configured by voice or UI (e.g. "Modo cine" -> DND + low brightness + open app)
 */
@Entity(tableName = "custom_commands")
data class CustomCommandEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val triggerPhrase: String,
    val description: String,
    val actionsList: String, // Comma separated actions
    val isEnabled: Boolean = true,
    val createdTime: Long = System.currentTimeMillis()
)
