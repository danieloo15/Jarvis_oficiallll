package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ActionHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM action_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ActionHistoryEntity>>

    @Query("SELECT * FROM action_history WHERE status = :status ORDER BY timestamp DESC")
    fun getHistoryByStatus(status: String): Flow<List<ActionHistoryEntity>>

    @Query("SELECT * FROM action_history WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchHistory(query: String): Flow<List<ActionHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: ActionHistoryEntity): Long

    @Update
    suspend fun update(history: ActionHistoryEntity)

    @Query("DELETE FROM action_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM action_history")
    suspend fun clearAll()
}
