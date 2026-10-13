package com.safeguard.parentalcontrol.util

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import java.util.Calendar

object UsageStatsHelper {

    data class InstalledAppInfo(
        val packageName: String,
        val appName: String,
        val isSystemApp: Boolean
    )

    fun getInstalledUserApps(context: Context): List<InstalledAppInfo> {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val result = mutableListOf<InstalledAppInfo>()

        val myPackage = context.packageName

        for (app in packages) {
            // Exclude our own app from being blocked
            if (app.packageName == myPackage) continue

            val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val appName = try {
                pm.getApplicationLabel(app).toString()
            } catch (e: Exception) {
                app.packageName
            }

            result.add(
                InstalledAppInfo(
                    packageName = app.packageName,
                    appName = appName,
                    isSystemApp = isSystem
                )
            )
        }

        return result.sortedBy { it.appName.lowercase() }
    }

    fun getTodayUsageMap(context: Context): Map<String, Long> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyMap()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val stats: List<UsageStats> = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: emptyList()

        val usageMap = mutableMapOf<String, Long>()
        for (usage in stats) {
            val totalTimeInForeground = usage.totalTimeInForeground
            if (totalTimeInForeground > 0) {
                val current = usageMap[usage.packageName] ?: 0L
                usageMap[usage.packageName] = current + totalTimeInForeground
            }
        }
        return usageMap
    }
}
