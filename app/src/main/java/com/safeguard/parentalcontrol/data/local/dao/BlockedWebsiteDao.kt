package com.safeguard.parentalcontrol.data.local.dao

import androidx.room.*
import com.safeguard.parentalcontrol.data.local.entity.BlockedWebsiteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedWebsiteDao {
    @Query("SELECT * FROM blocked_websites ORDER BY id DESC")
    fun getAllWebsitesFlow(): Flow<List<BlockedWebsiteEntity>>

    @Query("SELECT * FROM blocked_websites WHERE isEnabled = 1")
    suspend fun getActiveWebsites(): List<BlockedWebsiteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(website: BlockedWebsiteEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(websites: List<BlockedWebsiteEntity>)

    @Update
    suspend fun update(website: BlockedWebsiteEntity)

    @Delete
    suspend fun delete(website: BlockedWebsiteEntity)

    @Query("DELETE FROM blocked_websites WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE blocked_websites SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateStatus(id: Long, isEnabled: Boolean)
}
