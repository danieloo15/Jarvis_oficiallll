package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing modular skills of JARVIS.
 */
@Entity(tableName = "skills")
data class SkillEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: String,
    val iconName: String,
    val description: String,
    val isEnabled: Boolean = true,
    val requiredPermissions: String = "", // Comma-separated
    val availableActions: String = "", // JSON or comma list of action names
    val isCustom: Boolean = false
)
