package com.safeguard.parentalcontrol.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.safeguard.parentalcontrol.data.local.dao.*
import com.safeguard.parentalcontrol.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AppRuleEntity::class,
        BlockedWebsiteEntity::class,
        BlockedLogEntity::class,
        ScheduleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appRuleDao(): AppRuleDao
    abstract fun blockedWebsiteDao(): BlockedWebsiteDao
    abstract fun blockedLogDao(): BlockedLogDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "safeguard_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDefaultData(database)
                    }
                }
            }

            private suspend fun populateDefaultData(database: AppDatabase) {
                // Default Blocked Domains for Chrome & Browsers
                val defaultWebsites = listOf(
                    BlockedWebsiteEntity(domainPattern = "pornhub.com", category = "Adult"),
                    BlockedWebsiteEntity(domainPattern = "xvideos.com", category = "Adult"),
                    BlockedWebsiteEntity(domainPattern = "redtube.com", category = "Adult"),
                    BlockedWebsiteEntity(domainPattern = "onlyfans.com", category = "Adult"),
                    BlockedWebsiteEntity(domainPattern = "chaturbate.com", category = "Adult"),
                    BlockedWebsiteEntity(domainPattern = "bet365.com", category = "Gambling"),
                    BlockedWebsiteEntity(domainPattern = "stake.com", category = "Gambling"),
                    BlockedWebsiteEntity(domainPattern = "draftkings.com", category = "Gambling"),
                    BlockedWebsiteEntity(domainPattern = "tiktok.com", category = "Social Media"),
                    BlockedWebsiteEntity(domainPattern = "omegle.com", category = "Unsafe Chat")
                )
                database.blockedWebsiteDao().insertAll(defaultWebsites)

                // Default Bedtime Schedule: 9:30 PM to 6:30 AM every night
                val defaultBedtime = ScheduleEntity(
                    title = "Nighttime Curfew",
                    startHour = 21,
                    startMinute = 30,
                    endHour = 6,
                    endMinute = 30,
                    daysOfWeek = "1,2,3,4,5,6,7",
                    isEnabled = true
                )
                database.scheduleDao().insertOrUpdate(defaultBedtime)
            }
        }
    }
}
