package com.safeguard.parentalcontrol.data.local.dao

import androidx.room.*
import com.safeguard.parentalcontrol.data.local.entity.BlockedLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedLogDao {
    @Query("SELECT * FROM blocked_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLogsFlow(): Flow<List<BlockedLogEntity>>

    @Insert
    suspend fun insertLog(log: BlockedLogEntity)

    @Query("DELETE FROM blocked_logs")
    suspend fun clearLogs()

    @Query("SELECT COUNT(*) FROM blocked_logs WHERE timestamp > :sinceTimestamp")
    suspend fun getBlockedCountSince(sinceTimestamp: Long): Int
}
