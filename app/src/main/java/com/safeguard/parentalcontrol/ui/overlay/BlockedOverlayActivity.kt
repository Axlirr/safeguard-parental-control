package com.safeguard.parentalcontrol.ui.overlay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safeguard.parentalcontrol.SafeGuardApp
import com.safeguard.parentalcontrol.ui.security.PinDialog
import com.safeguard.parentalcontrol.ui.theme.ErrorRed
import com.safeguard.parentalcontrol.ui.theme.SafeGuardTheme
import com.safeguard.parentalcontrol.util.SecurityHelper

class BlockedOverlayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val target = intent.getStringExtra(EXTRA_TARGET) ?: "Restricted Content"
        val type = intent.getStringExtra(EXTRA_TYPE) ?: "CONTENT"
        val reason = intent.getStringExtra(EXTRA_REASON) ?: "Restricted by Parental Controls"

        val app = application as SafeGuardApp
        val prefs = app.preferenceManager

        setContent {
            SafeGuardTheme {
                var showPinDialog by remember { mutableStateOf(false) }
                var pinError by remember { mutableStateOf<String?>(null) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xE60F172A))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Blocker Icon
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(ErrorRed.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = "Blocked",
                                    tint = ErrorRed,
                                    modifier = Modifier.size(42.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (type == "WEBSITE") "Website Blocked" else "App Restricted",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = target,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    maxLines = 2
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = reason,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF64748B)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Action Buttons
                            Button(
                                onClick = {
                                    // Go Home
                                    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                        addCategory(Intent.CATEGORY_HOME)
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    startActivity(homeIntent)
                                    finish()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                            ) {
                                Text("Return to Home Screen")
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = { showPinDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Parent Unlock")
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            TextButton(
                                onClick = {
                                    val emergencyNumber = prefs.emergencyContact
                                    val callIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$emergencyNumber")
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    startActivity(callIntent)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Emergency Contact (${prefs.emergencyContact})",
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                if (showPinDialog) {
                    PinDialog(
                        title = "Parent Unlock",
                        subtitle = "Enter parent PIN to temporarily unlock or bypass restriction.",
                        errorMessage = pinError,
                        onDismiss = {
                            showPinDialog = false
                            pinError = null
                        },
                        onPinSubmitted = { enteredPin ->
                            if (SecurityHelper.verifyPin(enteredPin, prefs.parentPinHash)) {
                                Toast.makeText(
                                    applicationContext,
                                    "Restriction bypassed by Parent",
                                    Toast.LENGTH_SHORT
                                ).show()
                                showPinDialog = false
                                finish()
                            } else {
                                pinError = "Incorrect PIN. Please try again."
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        // Prevent child from simply pressing hardware back button to dismiss the overlay
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    companion object {
        const val EXTRA_TARGET = "extra_target"
        const val EXTRA_TYPE = "extra_type"
        const val EXTRA_REASON = "extra_reason"
    }
}
