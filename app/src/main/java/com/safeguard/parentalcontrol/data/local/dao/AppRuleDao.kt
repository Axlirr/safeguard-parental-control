package com.safeguard.parentalcontrol.data.local.dao

import androidx.room.*
import com.safeguard.parentalcontrol.data.local.entity.AppRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppRuleDao {
    @Query("SELECT * FROM app_rules ORDER BY appName ASC")
    fun getAllRulesFlow(): Flow<List<AppRuleEntity>>

    @Query("SELECT * FROM app_rules")
    suspend fun getAllRules(): List<AppRuleEntity>

    @Query("SELECT * FROM app_rules WHERE packageName = :packageName LIMIT 1")
    suspend fun getRuleByPackage(packageName: String): AppRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(rule: AppRuleEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(rules: List<AppRuleEntity>)

    @Query("UPDATE app_rules SET isBlocked = :isBlocked WHERE packageName = :packageName")
    suspend fun updateBlockedStatus(packageName: String, isBlocked: Boolean)

    @Query("UPDATE app_rules SET dailyLimitMinutes = :limitMinutes WHERE packageName = :packageName")
    suspend fun updateDailyLimit(packageName: String, limitMinutes: Int)

    @Query("UPDATE app_rules SET usedTodayMinutes = :usedMinutes WHERE packageName = :packageName")
    suspend fun updateUsage(packageName: String, usedMinutes: Int)

    @Query("UPDATE app_rules SET usedTodayMinutes = 0")
    suspend fun resetDailyUsage()

    @Delete
    suspend fun delete(rule: AppRuleEntity)
}
