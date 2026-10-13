package com.safeguard.parentalcontrol.ui.schedule

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
import com.safeguard.parentalcontrol.data.local.entity.ScheduleEntity
import com.safeguard.parentalcontrol.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as SafeGuardApp
    val repository = app.repository
    val prefs = app.preferenceManager
    val coroutineScope = rememberCoroutineScope()

    val schedules by repository.allSchedulesFlow.collectAsState(initial = emptyList())
    var isBedtimeActive by remember { mutableStateOf(prefs.isBedtimeEnabled) }

    var showAddDialog by remember { mutableStateOf(false) }
    var scheduleTitle by remember { mutableStateOf("Night Downtime") }
    var startHour by remember { mutableStateOf("21") }
    var startMinute by remember { mutableStateOf("30") }
    var endHour by remember { mutableStateOf("06") }
    var endMinute by remember { mutableStateOf("30") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bedtime & Curfews", fontWeight = FontWeight.Bold) },
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
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Schedule")
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
            // Master Switch
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
                                text = "Enforce Curfew Schedules",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Locks non-essential apps during scheduled sleep or study hours.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF64748B)
                            )
                        }

                        Switch(
                            checked = isBedtimeActive,
                            onCheckedChange = { active ->
                                isBedtimeActive = active
                                prefs.isBedtimeEnabled = active
                            }
                        )
                    }
                }
            }

            // Schedule Cards
            items(schedules, key = { it.id }) { schedule ->
                ScheduleCard(
                    schedule = schedule,
                    onToggle = { enabled ->
                        coroutineScope.launch {
                            repository.toggleSchedule(schedule.id, enabled)
                        }
                    },
                    onDelete = {
                        coroutineScope.launch {
                            repository.removeSchedule(schedule.id)
                            Toast.makeText(context, "Removed schedule", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Add Downtime Schedule", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = scheduleTitle,
                        onValueChange = { scheduleTitle = it },
                        label = { Text("Schedule Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Start Time (24h format):", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startHour,
                            onValueChange = { startHour = it },
                            label = { Text("HH") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = startMinute,
                            onValueChange = { startMinute = it },
                            label = { Text("MM") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("End Time (24h format):", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = endHour,
                            onValueChange = { endHour = it },
                            label = { Text("HH") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endMinute,
                            onValueChange = { endMinute = it },
                            label = { Text("MM") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sH = startHour.toIntOrNull() ?: 21
                        val sM = startMinute.toIntOrNull() ?: 0
                        val eH = endHour.toIntOrNull() ?: 7
                        val eM = endMinute.toIntOrNull() ?: 0

                        coroutineScope.launch {
                            repository.saveSchedule(
                                ScheduleEntity(
                                    title = scheduleTitle.ifBlank { "Curfew" },
                                    startHour = sH,
                                    startMinute = sM,
                                    endHour = eH,
                                    endMinute = eM,
                                    daysOfWeek = "1,2,3,4,5,6,7",
                                    isEnabled = true
                                )
                            )
                            showAddDialog = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Schedule")
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
fun ScheduleCard(
    schedule: ScheduleEntity,
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
                imageVector = Icons.Default.Bedtime,
                contentDescription = null,
                tint = if (schedule.isEnabled) PrimaryBlue else Color(0xFF94A3B8),
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(2.dp))
                val timeRange = "%02d:%02d - %02d:%02d".format(
                    schedule.startHour,
                    schedule.startMinute,
                    schedule.endHour,
                    schedule.endMinute
                )
                Text(
                    text = "Downtime: $timeRange (Daily)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B)
                )
            }

            Switch(
                checked = schedule.isEnabled,
                onCheckedChange = onToggle
            )

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
