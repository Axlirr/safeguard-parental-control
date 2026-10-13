package com.safeguard.parentalcontrol.data.local.dao

import androidx.room.*
import com.safeguard.parentalcontrol.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules ORDER BY startHour ASC")
    fun getAllSchedulesFlow(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE isEnabled = 1")
    suspend fun getActiveSchedules(): List<ScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(schedule: ScheduleEntity): Long

    @Delete
    suspend fun delete(schedule: ScheduleEntity)

    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE schedules SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateStatus(id: Int, isEnabled: Boolean)
}
