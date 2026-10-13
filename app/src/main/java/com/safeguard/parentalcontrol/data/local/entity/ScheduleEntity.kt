package com.safeguard.parentalcontrol.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val startHour: Int, // 0 - 23
    val startMinute: Int, // 0 - 59
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeek: String = "1,2,3,4,5,6,7", // Comma-separated day-of-week indices (1 = Sunday ... 7 = Saturday)
    val isEnabled: Boolean = true
)
