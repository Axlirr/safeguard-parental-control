package com.safeguard.parentalcontrol.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_logs")
data class BlockedLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val target: String,
    val type: String, // "WEBSITE" or "APP"
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)
