package com.safeguard.parentalcontrol

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.safeguard.parentalcontrol.data.local.AppDatabase
import com.safeguard.parentalcontrol.data.preferences.PreferenceManager
import com.safeguard.parentalcontrol.data.repository.ParentalControlRepository

class SafeGuardApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var preferenceManager: PreferenceManager
        private set

    lateinit var repository: ParentalControlRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getDatabase(this)
        preferenceManager = PreferenceManager(this)
        repository = ParentalControlRepository(database, preferenceManager)

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                GUARDIAN_CHANNEL_ID,
                getString(R.string.guardian_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.guardian_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val GUARDIAN_CHANNEL_ID = "guardian_service_channel"
        lateinit var instance: SafeGuardApp
            private set
    }
}
