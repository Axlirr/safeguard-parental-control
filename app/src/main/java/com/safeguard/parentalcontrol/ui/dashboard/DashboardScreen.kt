package com.safeguard.parentalcontrol.ui.dashboard

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safeguard.parentalcontrol.SafeGuardApp
import com.safeguard.parentalcontrol.data.local.entity.BlockedLogEntity
import com.safeguard.parentalcontrol.ui.security.PinDialog
import com.safeguard.parentalcontrol.ui.theme.*
import com.safeguard.parentalcontrol.util.SecurityHelper
import com.safeguard.parentalcontrol.util.UsageStatsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToWebFilter: () -> Unit,
    onNavigateToAppLimits: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToPermissions: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as SafeGuardApp
    val repository = app.repository
    val prefs = app.preferenceManager
    val coroutineScope = rememberCoroutineScope()

    var isChildModeActive by remember { mutableStateOf(prefs.isChildModeActive) }
    var showPinDialogForModeToggle by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf<String?>(null) }

    val recentLogs by repository.recentLogsFlow.collectAsState(initial = emptyList())
    var totalScreenTimeMinutes by remember { mutableStateOf(0) }

    // Load today's total screen time
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val usageMap = UsageStatsHelper.getTodayUsageMap(context)
            val totalMillis = usageMap.values.sum()
            withContext(Dispatchers.Main) {
                totalScreenTimeMinutes = (totalMillis / (1000 * 60)).toInt()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("SafeGuard Parent Center", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF0F172A)
                ),
                actions = {
                    IconButton(onClick = onNavigateToPermissions) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = "Permissions")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Master Child Mode Protection Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isChildModeActive) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isChildModeActive) SafeGreen else ErrorRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isChildModeActive) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isChildModeActive) "Child Mode Active" else "Parent Mode (Protection Paused)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isChildModeActive) Color(0xFF166534) else Color(0xFF991B1B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isChildModeActive) "Websites & app rules enforced" else "Restrictions temporarily suspended",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                        }

                        Switch(
                            checked = isChildModeActive,
                            onCheckedChange = { showPinDialogForModeToggle = true }
                        )
                    }
                }
            }

            // Quick Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Screen Time",
                        value = "${totalScreenTimeMinutes / 60}h ${totalScreenTimeMinutes % 60}m",
                        subtitle = "Today's usage",
                        icon = Icons.Default.Schedule,
                        iconColor = PrimaryBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Blocks Today",
                        value = "${recentLogs.size}",
                        subtitle = "Intercepted",
                        icon = Icons.Default.Block,
                        iconColor = ErrorRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Navigation Actions Grid
            item {
                Text(
                    text = "Controls & Rules",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
            }

            item {
                ControlCard(
                    title = "Chrome & Browser Web Blocker",
                    description = "Block inappropriate domains, adult content, and custom websites in Chrome.",
                    icon = Icons.Default.Language,
                    iconColor = PrimaryBlue,
                    onClick = onNavigateToWebFilter
                )
            }

            item {
                ControlCard(
                    title = "App Limits & Quotas",
                    description = "Set daily time limits for apps like games or completely restrict specific apps.",
                    icon = Icons.Default.Apps,
                    iconColor = SecondaryTeal,
                    onClick = onNavigateToAppLimits
                )
            }

            item {
                ControlCard(
                    title = "Bedtime & Curfews",
                    description = "Set downtime schedules during sleep or study hours to lock non-essential apps.",
                    icon = Icons.Default.Bedtime,
                    iconColor = Color(0xFF6B21A8),
                    onClick = onNavigateToSchedule
                )
            }

            // Recent Blocked Events Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Interceptions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )

                    if (recentLogs.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    repository.clearLogs()
                                }
                            }
                        ) {
                            Text("Clear", color = Color(0xFF64748B), fontSize = 13.sp)
                        }
                    }
                }
            }

            if (recentLogs.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No blocked attempts recorded yet.\nProtection is active in background.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF94A3B8),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(recentLogs.take(10), key = { it.id }) { log ->
                    LogItemCard(log = log)
                }
            }
        }
    }

    // PIN Dialog for Mode Toggle
    if (showPinDialogForModeToggle) {
        PinDialog(
            title = if (isChildModeActive) "Disable Child Mode?" else "Enable Child Mode?",
            subtitle = "Enter parent PIN to modify protection state.",
            errorMessage = pinError,
            onDismiss = {
                showPinDialogForModeToggle = false
                pinError = null
            },
            onPinSubmitted = { enteredPin ->
                if (SecurityHelper.verifyPin(enteredPin, prefs.parentPinHash)) {
                    val newState = !isChildModeActive
                    isChildModeActive = newState
                    prefs.isChildModeActive = newState
                    showPinDialogForModeToggle = false
                    pinError = null
                } else {
                    pinError = "Invalid PIN. Please try again."
                }
            }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF64748B)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun ControlCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun LogItemCard(log: BlockedLogEntity) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (log.type == "WEBSITE") Icons.Default.PublicOff else Icons.Default.Block,
                contentDescription = null,
                tint = ErrorRed,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.target,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = log.reason,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )
            }
            Text(
                text = DateFormat.format("HH:mm", Date(log.timestamp)).toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
