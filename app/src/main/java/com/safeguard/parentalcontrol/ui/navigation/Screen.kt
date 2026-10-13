package com.safeguard.parentalcontrol.ui.navigation

sealed class Screen(val route: String) {
    object Permissions : Screen("permissions")
    object Dashboard : Screen("dashboard")
    object WebFilter : Screen("web_filter")
    object AppLimits : Screen("app_limits")
    object Schedule : Screen("schedule")
    object Settings : Screen("settings")
}
