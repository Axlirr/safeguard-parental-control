package com.safeguard.parentalcontrol.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_rules")
data class AppRuleEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val isBlocked: Boolean = false,
    val dailyLimitMinutes: Int = 0, // 0 = no limit
    val usedTodayMinutes: Int = 0,
    val category: String = "General"
)
