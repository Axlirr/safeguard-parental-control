package com.safeguard.parentalcontrol.data.repository

import com.safeguard.parentalcontrol.data.local.AppDatabase
import com.safeguard.parentalcontrol.data.local.entity.*
import com.safeguard.parentalcontrol.data.preferences.PreferenceManager
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ParentalControlRepository(
    private val database: AppDatabase,
    val preferenceManager: PreferenceManager
) {
    val allRulesFlow: Flow<List<AppRuleEntity>> = database.appRuleDao().getAllRulesFlow()
    val allWebsitesFlow: Flow<List<BlockedWebsiteEntity>> = database.blockedWebsiteDao().getAllWebsitesFlow()
    val recentLogsFlow: Flow<List<BlockedLogEntity>> = database.blockedLogDao().getRecentLogsFlow()
    val allSchedulesFlow: Flow<List<ScheduleEntity>> = database.scheduleDao().getAllSchedulesFlow()

    suspend fun getAppRule(packageName: String): AppRuleEntity? =
        database.appRuleDao().getRuleByPackage(packageName)

    suspend fun updateAppBlocked(packageName: String, isBlocked: Boolean) =
        database.appRuleDao().updateBlockedStatus(packageName, isBlocked)

    suspend fun updateAppLimit(packageName: String, limitMinutes: Int) =
        database.appRuleDao().updateDailyLimit(packageName, limitMinutes)

    suspend fun saveAppRule(rule: AppRuleEntity) =
        database.appRuleDao().insertOrUpdate(rule)

    suspend fun addBlockedWebsite(domain: String, category: String = "Custom") {
        val cleanDomain = domain.lowercase().trim()
            .replace("https://", "")
            .replace("http://", "")
            .replace("www.", "")
            .split("/")[0]

        database.blockedWebsiteDao().insert(
            BlockedWebsiteEntity(
                domainPattern = cleanDomain,
                category = category,
                isEnabled = true
            )
        )
    }

    suspend fun toggleWebsite(id: Long, isEnabled: Boolean) =
        database.blockedWebsiteDao().updateStatus(id, isEnabled)

    suspend fun removeWebsite(id: Long) =
        database.blockedWebsiteDao().deleteById(id)

    suspend fun getActiveWebsites(): List<BlockedWebsiteEntity> =
        database.blockedWebsiteDao().getActiveWebsites()

    suspend fun logBlockedEvent(target: String, type: String, reason: String) {
        database.blockedLogDao().insertLog(
            BlockedLogEntity(
                target = target,
                type = type,
                reason = reason,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearLogs() = database.blockedLogDao().clearLogs()

    suspend fun saveSchedule(schedule: ScheduleEntity) =
        database.scheduleDao().insertOrUpdate(schedule)

    suspend fun toggleSchedule(id: Int, isEnabled: Boolean) =
        database.scheduleDao().updateStatus(id, isEnabled)

    suspend fun removeSchedule(id: Int) =
        database.scheduleDao().deleteById(id)

    suspend fun isCurfewActive(): Boolean {
        if (!preferenceManager.isBedtimeEnabled) return false

        val activeSchedules = database.scheduleDao().getActiveSchedules()
        if (activeSchedules.isEmpty()) return false

        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_WEEK) // 1 = Sunday ... 7 = Saturday
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentMinutesOfDay = currentHour * 60 + currentMinute

        for (schedule in activeSchedules) {
            val daysList = schedule.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
            if (daysList.contains(currentDay)) {
                val startMinutesOfDay = schedule.startHour * 60 + schedule.startMinute
                val endMinutesOfDay = schedule.endHour * 60 + schedule.endMinute

                if (startMinutesOfDay <= endMinutesOfDay) {
                    // Normal range (e.g. 09:00 to 17:00)
                    if (currentMinutesOfDay in startMinutesOfDay..endMinutesOfDay) return true
                } else {
                    // Overnight range (e.g. 21:00 to 07:00)
                    if (currentMinutesOfDay >= startMinutesOfDay || currentMinutesOfDay <= endMinutesOfDay) return true
                }
            }
        }
        return false
    }
}
