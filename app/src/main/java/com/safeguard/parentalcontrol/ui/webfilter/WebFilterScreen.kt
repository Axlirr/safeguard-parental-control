package com.safeguard.parentalcontrol.ui.webfilter

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
import com.safeguard.parentalcontrol.data.local.entity.BlockedWebsiteEntity
import com.safeguard.parentalcontrol.ui.theme.ErrorRed
import com.safeguard.parentalcontrol.ui.theme.PrimaryBlue
import com.safeguard.parentalcontrol.ui.theme.SafeGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebFilterScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as SafeGuardApp
    val repository = app.repository
    val prefs = app.preferenceManager
    val coroutineScope = rememberCoroutineScope()

    val blockedWebsites by repository.allWebsitesFlow.collectAsState(initial = emptyList())
    var isFilterActive by remember { mutableStateOf(prefs.isWebFilterEnabled) }

    var showAddDialog by remember { mutableStateOf(false) }
    var domainInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Custom") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chrome Website Blocker", fontWeight = FontWeight.Bold) },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Block Website")
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
            // Master Switch Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Safe Browsing Enforcement",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Inspects Google Chrome URL bar and blocks restricted domains instantly.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF64748B)
                            )
                        }

                        Switch(
                            checked = isFilterActive,
                            onCheckedChange = { active ->
                                isFilterActive = active
                                prefs.isWebFilterEnabled = active
                            }
                        )
                    }
                }
            }

            // Quick Info Banner
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEFF6FF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "SafeGuard automatically monitors Google Chrome, Microsoft Edge, and Firefox address bars.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1E40AF)
                        )
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Blocked Domains (${blockedWebsites.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                }
            }

            // Blocked Website Items
            items(blockedWebsites, key = { it.id }) { website ->
                BlockedWebsiteItem(
                    website = website,
                    onToggle = { enabled ->
                        coroutineScope.launch {
                            repository.toggleWebsite(website.id, enabled)
                        }
                    },
                    onDelete = {
                        coroutineScope.launch {
                            repository.removeWebsite(website.id)
                            Toast.makeText(context, "Removed ${website.domainPattern}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            if (blockedWebsites.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No websites configured yet.\nTap '+' to add a website to block.",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }

    // Add Website Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Block a Website in Chrome",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Enter a website domain or keyword to block in Google Chrome (e.g. reddit.com or gambling):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = domainInput,
                        onValueChange = { domainInput = it },
                        label = { Text("Website Domain (e.g. tiktok.com)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Category:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Custom", "Social", "Adult", "Gaming").forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (domainInput.isNotBlank()) {
                            coroutineScope.launch {
                                repository.addBlockedWebsite(domainInput, selectedCategory)
                                domainInput = ""
                                showAddDialog = false
                                Toast.makeText(context, "Website added to blocklist", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    enabled = domainInput.isNotBlank()
                ) {
                    Text("Add to Blocklist")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BlockedWebsiteItem(
    website: BlockedWebsiteEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PublicOff,
                contentDescription = null,
                tint = if (website.isEnabled) ErrorRed else Color(0xFF94A3B8),
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = website.domainPattern,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Category: ${website.category}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF64748B)
                )
            }

            Switch(
                checked = website.isEnabled,
                onCheckedChange = onToggle
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = Color(0xFF94A3B8)
                )
            }
        }
    }
}
