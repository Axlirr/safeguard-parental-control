package com.safeguard.parentalcontrol.ui.applimits

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safeguard.parentalcontrol.SafeGuardApp
import com.safeguard.parentalcontrol.data.local.entity.AppRuleEntity
import com.safeguard.parentalcontrol.ui.theme.ErrorRed
import com.safeguard.parentalcontrol.ui.theme.PrimaryBlue
import com.safeguard.parentalcontrol.util.UsageStatsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLimitsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as SafeGuardApp
    val repository = app.repository
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var installedApps by remember { mutableStateOf<List<UsageStatsHelper.InstalledAppInfo>>(emptyList()) }
    var appRulesMap by remember { mutableStateOf<Map<String, AppRuleEntity>>(emptyMap()) }
    var usageMinutesMap by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load installed apps and rules
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val apps = UsageStatsHelper.getInstalledUserApps(context)
            val usage = UsageStatsHelper.getTodayUsageMap(context).mapValues { (it.value / (1000 * 60)).toInt() }
            val existingRules = app.database.appRuleDao().getAllRules().associateBy { it.packageName }

            withContext(Dispatchers.Main) {
                installedApps = apps
                usageMinutesMap = usage
                appRulesMap = existingRules
                isLoading = false
            }
        }
    }

    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Limits & Blocking", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF0F172A)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search installed applications...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { appInfo ->
                        val rule = appRulesMap[appInfo.packageName]
                        val isBlocked = rule?.isBlocked ?: false
                        val dailyLimit = rule?.dailyLimitMinutes ?: 0
                        val usedToday = usageMinutesMap[appInfo.packageName] ?: 0

                        AppLimitCard(
                            appInfo = appInfo,
                            isBlocked = isBlocked,
                            dailyLimitMinutes = dailyLimit,
                            usedTodayMinutes = usedToday,
                            onToggleBlock = { blocked ->
                                coroutineScope.launch {
                                    val updatedRule = AppRuleEntity(
                                        packageName = appInfo.packageName,
                                        appName = appInfo.appName,
                                        isBlocked = blocked,
                                        dailyLimitMinutes = dailyLimit,
                                        usedTodayMinutes = usedToday
                                    )
                                    repository.saveAppRule(updatedRule)
                                    appRulesMap = appRulesMap + (appInfo.packageName to updatedRule)
                                }
                            },
                            onLimitChanged = { newLimit ->
                                coroutineScope.launch {
                                    val updatedRule = AppRuleEntity(
                                        packageName = appInfo.packageName,
                                        appName = appInfo.appName,
                                        isBlocked = isBlocked,
                                        dailyLimitMinutes = newLimit,
                                        usedTodayMinutes = usedToday
                                    )
                                    repository.saveAppRule(updatedRule)
                                    appRulesMap = appRulesMap + (appInfo.packageName to updatedRule)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppLimitCard(
    appInfo: UsageStatsHelper.InstalledAppInfo,
    isBlocked: Boolean,
    dailyLimitMinutes: Int,
    usedTodayMinutes: Int,
    onToggleBlock: (Boolean) -> Unit,
    onLimitChanged: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appInfo.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Used today: ${usedTodayMinutes}m | ${if (isBlocked) "Blocked" else if (dailyLimitMinutes > 0) "Limit: ${dailyLimitMinutes}m" else "Unlimited"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isBlocked) ErrorRed else Color(0xFF64748B)
                    )
                }

                // Completely Block Switch
                Column(horizontalAlignment = Alignment.End) {
                    Text("Block App", fontSize = 11.sp, color = Color(0xFF64748B))
                    Switch(
                        checked = isBlocked,
                        onCheckedChange = onToggleBlock
                    )
                }
            }

            if (!isBlocked) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Daily Time Allowance: ${if (dailyLimitMinutes == 0) "Unlimited" else "$dailyLimitMinutes mins"}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155)
                )

                Slider(
                    value = dailyLimitMinutes.toFloat(),
                    onValueChange = { onLimitChanged(it.toInt()) },
                    valueRange = 0f..180f,
                    steps = 11, // 0, 15, 30, 45, 60, 75, 90, 105, 120, 135, 150, 165, 180
                    colors = SliderDefaults.colors(
                        thumbColor = PrimaryBlue,
                        activeTrackColor = PrimaryBlue
                    )
                )
            }
        }
    }
}
