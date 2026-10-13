package com.safeguard.parentalcontrol.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.safeguard.parentalcontrol.ui.theme.PrimaryBlue
import com.safeguard.parentalcontrol.ui.theme.SafeGreen
import com.safeguard.parentalcontrol.util.PermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    onAllPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current

    var isAccessibilityGranted by remember { mutableStateOf(PermissionHelper.isAccessibilityEnabled(context)) }
    var isUsageGranted by remember { mutableStateOf(PermissionHelper.isUsageAccessGranted(context)) }
    var isOverlayGranted by remember { mutableStateOf(PermissionHelper.isOverlayGranted(context)) }
    var isDeviceAdminGranted by remember { mutableStateOf(PermissionHelper.isDeviceAdminActive(context)) }

    // Re-check on resume / button clicks
    fun refreshPermissions() {
        isAccessibilityGranted = PermissionHelper.isAccessibilityEnabled(context)
        isUsageGranted = PermissionHelper.isUsageAccessGranted(context)
        isOverlayGranted = PermissionHelper.isOverlayGranted(context)
        isDeviceAdminGranted = PermissionHelper.isDeviceAdminActive(context)
    }

    val canProceed = isAccessibilityGranted && isUsageGranted && isOverlayGranted

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parental Setup Permissions", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF0F172A)
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        refreshPermissions()
                        if (canProceed) {
                            onAllPermissionsGranted()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    enabled = canProceed,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (canProceed) "Continue to Parent Dashboard" else "Grant Required Permissions Above",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Device Guardian Permissions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "SafeGuard needs system access to inspect Chrome web addresses, enforce limits, and protect rules from being bypassed.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }
            }

            // 1. Accessibility Service (Crucial for Chrome)
            item {
                PermissionCard(
                    title = "1. Accessibility Service (Required)",
                    description = "Required to inspect web addresses in Google Chrome and automatically block restricted apps and inappropriate content.",
                    icon = Icons.Default.Web,
                    isGranted = isAccessibilityGranted,
                    onGrantClick = {
                        PermissionHelper.openAccessibilitySettings(context)
                    }
                )
            }

            // 2. Usage Access
            item {
                PermissionCard(
                    title = "2. Usage Access (Required)",
                    description = "Allows SafeGuard to monitor daily screen time and enforce per-app time quotas.",
                    icon = Icons.Default.Timer,
                    isGranted = isUsageGranted,
                    onGrantClick = {
                        PermissionHelper.openUsageAccessSettings(context)
                    }
                )
            }

            // 3. Draw Over Other Apps
            item {
                PermissionCard(
                    title = "3. Overlay Blocker (Required)",
                    description = "Enables displaying the full-screen parental lock when a blocked website or app is opened.",
                    icon = Icons.Default.Layers,
                    isGranted = isOverlayGranted,
                    onGrantClick = {
                        PermissionHelper.openOverlaySettings(context)
                    }
                )
            }

            // 4. Device Administrator (Anti-Tamper)
            item {
                PermissionCard(
                    title = "4. Anti-Uninstall Protection (Recommended)",
                    description = "Prevents children from easily uninstalling SafeGuard via device settings without your authorization.",
                    icon = Icons.Default.Security,
                    isGranted = isDeviceAdminGranted,
                    onGrantClick = {
                        PermissionHelper.requestDeviceAdmin(context)
                    }
                )
            }
        }
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    onGrantClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isGranted) SafeGreen.copy(alpha = 0.15f)
                            else PrimaryBlue.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) SafeGreen else PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isGranted) "Permission Active" else "Action Required",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isGranted) SafeGreen else Color(0xFFE65100),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (isGranted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Granted",
                        tint = SafeGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF64748B)
            )

            if (!isGranted) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onGrantClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Enable in Settings")
                }
            }
        }
    }
}
