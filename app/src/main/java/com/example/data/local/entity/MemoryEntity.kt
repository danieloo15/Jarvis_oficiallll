package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing long term user memories, preferences, facts, and projects.
 */
@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: String, // "Personal", "Preference", "Project", "CustomCommand", "General"
    val tags: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
