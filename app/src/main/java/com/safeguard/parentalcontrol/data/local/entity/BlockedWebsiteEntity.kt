package com.safeguard.parentalcontrol.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_websites")
data class BlockedWebsiteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val domainPattern: String,
    val category: String = "Custom",
    val isEnabled: Boolean = true
)
