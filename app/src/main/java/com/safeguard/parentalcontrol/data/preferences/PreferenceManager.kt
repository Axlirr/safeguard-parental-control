package com.safeguard.parentalcontrol.data.preferences

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var parentPinHash: String?
        get() = prefs.getString(KEY_PIN_HASH, null)
        set(value) = prefs.edit().putString(KEY_PIN_HASH, value).apply()

    var isChildModeActive: Boolean
        get() = prefs.getBoolean(KEY_CHILD_MODE_ACTIVE, true)
        set(value) = prefs.edit().putBoolean(KEY_CHILD_MODE_ACTIVE, value).apply()

    var isWebFilterEnabled: Boolean
        get() = prefs.getBoolean(KEY_WEB_FILTER_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_WEB_FILTER_ENABLED, value).apply()

    var isAppLimitsEnabled: Boolean
        get() = prefs.getBoolean(KEY_APP_LIMITS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_APP_LIMITS_ENABLED, value).apply()

    var isBedtimeEnabled: Boolean
        get() = prefs.getBoolean(KEY_BEDTIME_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_BEDTIME_ENABLED, value).apply()

    var isAntiTamperEnabled: Boolean
        get() = prefs.getBoolean(KEY_ANTI_TAMPER_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ANTI_TAMPER_ENABLED, value).apply()

    var emergencyContact: String
        get() = prefs.getString(KEY_EMERGENCY_CONTACT, "911") ?: "911"
        set(value) = prefs.edit().putString(KEY_EMERGENCY_CONTACT, value).apply()

    var isSetupCompleted: Boolean
        get() = prefs.getBoolean(KEY_SETUP_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_SETUP_COMPLETED, value).apply()

    fun hasPin(): Boolean = !parentPinHash.isNullOrBlank()

    companion object {
        private const val PREFS_NAME = "safeguard_parental_prefs"
        private const val KEY_PIN_HASH = "parent_pin_hash"
        private const val KEY_CHILD_MODE_ACTIVE = "child_mode_active"
        private const val KEY_WEB_FILTER_ENABLED = "web_filter_enabled"
        private const val KEY_APP_LIMITS_ENABLED = "app_limits_enabled"
        private const val KEY_BEDTIME_ENABLED = "bedtime_enabled"
        private const val KEY_ANTI_TAMPER_ENABLED = "anti_tamper_enabled"
        private const val KEY_EMERGENCY_CONTACT = "emergency_contact"
        private const val KEY_SETUP_COMPLETED = "setup_completed"
    }
}
