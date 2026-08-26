package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Historical log of every action executed by JARVIS with status and undo capability.
 */
@Entity(tableName = "action_history")
data class ActionHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val actionType: String, // "System", "App", "Message", "Call", "Automation", "AgentPlan", "Memory", "Vision"
    val status: String, // "COMPLETED", "PENDING_CONFIRMATION", "CANCELLED", "FAILED", "EMERGENCY_STOPPED"
    val details: String = "",
    val canUndo: Boolean = false,
    val undoPayload: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
