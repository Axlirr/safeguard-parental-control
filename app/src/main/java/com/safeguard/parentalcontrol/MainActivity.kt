package com.safeguard.parentalcontrol

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.safeguard.parentalcontrol.service.GuardianForegroundService
import com.safeguard.parentalcontrol.ui.applimits.AppLimitsScreen
import com.safeguard.parentalcontrol.ui.dashboard.DashboardScreen
import com.safeguard.parentalcontrol.ui.navigation.Screen
import com.safeguard.parentalcontrol.ui.onboarding.PermissionsScreen
import com.safeguard.parentalcontrol.ui.schedule.ScheduleScreen
import com.safeguard.parentalcontrol.ui.security.PinDialog
import com.safeguard.parentalcontrol.ui.theme.BackgroundLight
import com.safeguard.parentalcontrol.ui.theme.SafeGuardTheme
import com.safeguard.parentalcontrol.ui.webfilter.WebFilterScreen
import com.safeguard.parentalcontrol.util.PermissionHelper
import com.safeguard.parentalcontrol.util.SecurityHelper

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as SafeGuardApp
        val prefs = app.preferenceManager

        // Start Guardian Background Service
        startGuardianService()

        setContent {
            SafeGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundLight
                ) {
                    val navController = rememberNavController()

                    // Check if initial permissions or PIN are needed
                    val hasPin = prefs.hasPin()
                    val hasPermissions = PermissionHelper.isAccessibilityEnabled(this) &&
                            PermissionHelper.isUsageAccessGranted(this) &&
                            PermissionHelper.isOverlayGranted(this)

                    var isUnlocked by remember { mutableStateOf(!hasPin) }
                    var showFirstTimePinSetup by remember { mutableStateOf(!hasPin) }
                    var showUnlockPinDialog by remember { mutableStateOf(hasPin && !isUnlocked) }
                    var pinError by remember { mutableStateOf<String?>(null) }

                    // Initial Route logic
                    val startDestination = if (!hasPermissions) {
                        Screen.Permissions.route
                    } else {
                        Screen.Dashboard.route
                    }

                    if (showFirstTimePinSetup) {
                        PinDialog(
                            title = "Set Master Parent PIN",
                            subtitle = "Create a 4-to-6 digit PIN to protect your parental control settings from being changed by children.",
                            isSettingNewPin = true,
                            errorMessage = pinError,
                            onDismiss = {
                                // Default pin if skipped
                                prefs.parentPinHash = SecurityHelper.hashPin("1234")
                                showFirstTimePinSetup = false
                                isUnlocked = true
                            },
                            onPinSubmitted = { newPin ->
                                prefs.parentPinHash = SecurityHelper.hashPin(newPin)
                                showFirstTimePinSetup = false
                                isUnlocked = true
                                Toast.makeText(applicationContext, "Parent PIN configured successfully", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else if (showUnlockPinDialog && !isUnlocked) {
                        PinDialog(
                            title = "SafeGuard Parent Access",
                            subtitle = "Enter parent security PIN to open control center.",
                            errorMessage = pinError,
                            onDismiss = {
                                finish() // Exit if PIN entry cancelled
                            },
                            onPinSubmitted = { enteredPin ->
                                if (SecurityHelper.verifyPin(enteredPin, prefs.parentPinHash)) {
                                    isUnlocked = true
                                    showUnlockPinDialog = false
                                    pinError = null
                                } else {
                                    pinError = "Incorrect PIN. Please try again."
                                }
                            }
                        )
                    }

                    if (isUnlocked) {
                        NavHost(
                            navController = navController,
                            startDestination = startDestination
                        ) {
                            composable(Screen.Permissions.route) {
                                PermissionsScreen(
                                    onAllPermissionsGranted = {
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(Screen.Permissions.route) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable(Screen.Dashboard.route) {
                                DashboardScreen(
                                    onNavigateToWebFilter = { navController.navigate(Screen.WebFilter.route) },
                                    onNavigateToAppLimits = { navController.navigate(Screen.AppLimits.route) },
                                    onNavigateToSchedule = { navController.navigate(Screen.Schedule.route) },
                                    onNavigateToPermissions = { navController.navigate(Screen.Permissions.route) }
                                )
                            }

                            composable(Screen.WebFilter.route) {
                                WebFilterScreen(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            composable(Screen.AppLimits.route) {
                                AppLimitsScreen(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            composable(Screen.Schedule.route) {
                                ScheduleScreen(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startGuardianService() {
        try {
            val serviceIntent = Intent(this, GuardianForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
