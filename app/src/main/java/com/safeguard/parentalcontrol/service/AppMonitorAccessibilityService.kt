package com.safeguard.parentalcontrol.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.safeguard.parentalcontrol.SafeGuardApp
import com.safeguard.parentalcontrol.ui.overlay.BlockedOverlayActivity
import com.safeguard.parentalcontrol.util.BrowserUrlDetector
import com.safeguard.parentalcontrol.util.UsageStatsHelper
import com.safeguard.parentalcontrol.util.WebsiteFilterEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppMonitorAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var lastBlockedTarget: String? = null
    private var lastBlockedTime: Long = 0

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val app = application as? SafeGuardApp ?: return
        val prefs = app.preferenceManager
        val repository = app.repository

        // If Child Mode is turned OFF by parent, do not enforce
        if (!prefs.isChildModeActive) return

        val packageName = event.packageName?.toString() ?: return

        // Skip our own app
        if (packageName == applicationContext.packageName) return

        serviceScope.launch {
            // 1. CHROME & BROWSER WEBSITE MONITORING & FILTERING
            if (prefs.isWebFilterEnabled && BrowserUrlDetector.isBrowserPackage(packageName)) {
                checkAndBlockBrowserUrl(packageName)
            }

            // 2. ANTI-TAMPER: Block Android Settings if enabled
            if (prefs.isAntiTamperEnabled && packageName == "com.android.settings") {
                checkAndBlockSettingsAccess(event)
            }

            // 3. APP-LEVEL BLOCKING (Limits & Bedtime)
            checkAndBlockRestrictedApp(packageName)
        }
    }

    private suspend fun checkAndBlockBrowserUrl(packageName: String) {
        val rootNode = rootInActiveWindow ?: return
        val currentUrl = BrowserUrlDetector.extractUrl(rootNode, packageName) ?: return

        val app = application as SafeGuardApp
        val activeBlockedRules = app.repository.getActiveWebsites()

        val filterResult = WebsiteFilterEngine.evaluateUrl(currentUrl, activeBlockedRules)
        if (filterResult.isBlocked) {
            handleBlockAction(
                target = currentUrl,
                type = "WEBSITE",
                reason = "Blocked Web Content: ${filterResult.category} (${filterResult.matchedPattern})"
            )
        }
    }

    private suspend fun checkAndBlockRestrictedApp(packageName: String) {
        val app = application as SafeGuardApp
        val repository = app.repository
        val prefs = app.preferenceManager

        // Ignore system launchers / dialers / emergency
        if (isExemptSystemPackage(packageName)) return

        // Check Nighttime Curfew / Downtime
        if (prefs.isBedtimeEnabled && repository.isCurfewActive()) {
            handleBlockAction(
                target = packageName,
                type = "APP",
                reason = "Bedtime Downtime Active (Curfew hours)"
            )
            return
        }

        // Check App-Specific Rules
        if (prefs.isAppLimitsEnabled) {
            val rule = repository.getAppRule(packageName)
            if (rule != null) {
                if (rule.isBlocked) {
                    handleBlockAction(
                        target = rule.appName.ifBlank { packageName },
                        type = "APP",
                        reason = "App is restricted by Parent"
                    )
                    return
                }

                // Check Daily Limit
                if (rule.dailyLimitMinutes > 0) {
                    val usageMap = UsageStatsHelper.getTodayUsageMap(applicationContext)
                    val usedMillis = usageMap[packageName] ?: 0L
                    val usedMinutes = (usedMillis / (1000 * 60)).toInt()

                    if (usedMinutes >= rule.dailyLimitMinutes) {
                        handleBlockAction(
                            target = rule.appName.ifBlank { packageName },
                            type = "APP",
                            reason = "Daily Time Limit Exceeded (${rule.dailyLimitMinutes}m limit reached)"
                        )
                        return
                    }
                }
            }
        }
    }

    private fun checkAndBlockSettingsAccess(event: AccessibilityEvent) {
        val textList = event.text.map { it.toString().lowercase() }
        val contentDesc = event.contentDescription?.toString()?.lowercase() ?: ""
        val sensitiveKeywords = listOf("safeguard", "accessibility", "device admin", "uninstall", "force stop", "apps")

        val matches = sensitiveKeywords.any { keyword ->
            textList.any { it.contains(keyword) } || contentDesc.contains(keyword)
        }

        if (matches) {
            handleBlockAction(
                target = "Android Settings",
                type = "SYSTEM",
                reason = "Tampering Protection: Settings access is locked"
            )
        }
    }

    private fun handleBlockAction(target: String, type: String, reason: String) {
        val currentTime = System.currentTimeMillis()
        // Debounce blocks within 1.5 seconds for same target to prevent UI flicker
        if (target == lastBlockedTarget && (currentTime - lastBlockedTime) < 1500) {
            return
        }
        lastBlockedTarget = target
        lastBlockedTime = currentTime

        // Perform Back or Home to immediately get out of restricted content
        performGlobalAction(GLOBAL_ACTION_HOME)

        // Launch Blocker Activity
        val intent = Intent(applicationContext, BlockedOverlayActivity::class.java).apply {
            putExtra(BlockedOverlayActivity.EXTRA_TARGET, target)
            putExtra(BlockedOverlayActivity.EXTRA_TYPE, type)
            putExtra(BlockedOverlayActivity.EXTRA_REASON, reason)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)

        // Log event to database
        serviceScope.launch {
            val app = application as? SafeGuardApp
            app?.repository?.logBlockedEvent(target, type, reason)
        }
    }

    private fun isExemptSystemPackage(packageName: String): Boolean {
        val exempt = listOf(
            "com.android.dialer",
            "com.google.android.dialer",
            "com.samsung.android.dialer",
            "com.android.phone",
            "com.android.server.telecom",
            "com.android.emergency",
            "com.google.android.emergency"
        )
        return exempt.contains(packageName)
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility Service interrupted")
    }

    companion object {
        private const val TAG = "SafeGuardAccessibility"
    }
}
