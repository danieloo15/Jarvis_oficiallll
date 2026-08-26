package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Automations: Trigger -> Conditions -> Actions
 */
@Entity(tableName = "automations")
data class AutomationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val triggerType: String, // "Time", "Battery", "Wifi", "Location", "AppOpen"
    val triggerCondition: String, // e.g. "22:00", "<20%", "Home_WiFi"
    val secondCondition: String = "", // e.g. "Llegar a casa"
    val actionsList: String, // Comma separated actions or JSON
    val isEnabled: Boolean = true,
    val lastExecutedTime: Long = 0,
    val executionCount: Int = 0
)
