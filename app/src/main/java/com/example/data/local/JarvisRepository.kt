package com.example.data.local

import com.example.data.local.entity.ActionHistoryEntity
import com.example.data.local.entity.AutomationEntity
import com.example.data.local.entity.ConnectionEntity
import com.example.data.local.entity.CustomCommandEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.SkillEntity
import kotlinx.coroutines.flow.Flow

class JarvisRepository(private val database: JarvisDatabase) {

    // Memories
    val allMemories: Flow<List<MemoryEntity>> = database.memoryDao().getAllMemories()
    fun searchMemories(query: String): Flow<List<MemoryEntity>> = database.memoryDao().searchMemories(query)
    fun getMemoriesByCategory(cat: String): Flow<List<MemoryEntity>> = database.memoryDao().getMemoriesByCategory(cat)
    suspend fun getRecentMemoriesSync(): List<MemoryEntity> = database.memoryDao().getRecentMemoriesSync()
    suspend fun saveMemory(memory: MemoryEntity): Long = database.memoryDao().insert(memory)
    suspend fun updateMemory(memory: MemoryEntity) = database.memoryDao().update(memory)
    suspend fun deleteMemory(id: Long) = database.memoryDao().deleteById(id)
    suspend fun clearAllMemories() = database.memoryDao().clearAll()

    // Skills
    val allSkills: Flow<List<SkillEntity>> = database.skillDao().getAllSkills()
    val enabledSkills: Flow<List<SkillEntity>> = database.skillDao().getEnabledSkills()
    suspend fun getEnabledSkillsSync(): List<SkillEntity> = database.skillDao().getEnabledSkillsSync()
    suspend fun getSkillById(id: String): SkillEntity? = database.skillDao().getSkillById(id)
    suspend fun saveSkill(skill: SkillEntity) = database.skillDao().insertOrUpdate(skill)
    suspend fun updateSkill(skill: SkillEntity) = database.skillDao().update(skill)
    suspend fun deleteSkill(id: String) = database.skillDao().deleteById(id)

    // Automations
    val allAutomations: Flow<List<AutomationEntity>> = database.automationDao().getAllAutomations()
    suspend fun saveAutomation(auto: AutomationEntity): Long = database.automationDao().insert(auto)
    suspend fun updateAutomation(auto: AutomationEntity) = database.automationDao().update(auto)
    suspend fun deleteAutomation(id: Long) = database.automationDao().deleteById(id)

    // History
    val allHistory: Flow<List<ActionHistoryEntity>> = database.historyDao().getAllHistory()
    fun searchHistory(query: String): Flow<List<ActionHistoryEntity>> = database.historyDao().searchHistory(query)
    suspend fun logAction(action: ActionHistoryEntity): Long = database.historyDao().insert(action)
    suspend fun updateAction(action: ActionHistoryEntity) = database.historyDao().update(action)
    suspend fun deleteHistoryItem(id: Long) = database.historyDao().deleteById(id)
    suspend fun clearAllHistory() = database.historyDao().clearAll()

    // Connections
    val allConnections: Flow<List<ConnectionEntity>> = database.connectionDao().getAllConnections()
    suspend fun updateConnection(conn: ConnectionEntity) = database.connectionDao().update(conn)
    suspend fun saveConnection(conn: ConnectionEntity) = database.connectionDao().insertOrUpdate(conn)

    // Custom Commands
    val allCommands: Flow<List<CustomCommandEntity>> = database.customCommandDao().getAllCommands()
    suspend fun getEnabledCommandsSync(): List<CustomCommandEntity> = database.customCommandDao().getEnabledCommandsSync()
    suspend fun saveCommand(cmd: CustomCommandEntity): Long = database.customCommandDao().insert(cmd)
    suspend fun updateCommand(cmd: CustomCommandEntity) = database.customCommandDao().update(cmd)
    suspend fun deleteCommand(id: Long) = database.customCommandDao().deleteById(id)
}
