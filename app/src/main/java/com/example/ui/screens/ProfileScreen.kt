package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppState
import com.example.model.PersonaType
import com.example.ui.theme.GoldCoinYellow
import com.example.viewmodel.MainViewModel

@Composable
fun ProfileScreen(
    state: AppState,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf(state.userName) }
    var editEmailInput by remember { mutableStateOf(state.userEmail) }
    var editAvatarInput by remember { mutableStateOf(state.avatarEmoji) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 64.dp)
    ) {
        // User Profile Identity Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(state.avatarEmoji, fontSize = 28.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = state.userName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = state.userEmail,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Level ${state.userLevel}  •  ${state.currentLooksmaxTier.tierName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                editNameInput = state.userName
                                editEmailInput = state.userEmail
                                editAvatarInput = state.avatarEmoji
                                showEditProfileDialog = true
                            },
                            modifier = Modifier.testTag("edit_profile_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Real-Time Coins & Wallet Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🪙", fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "${state.coins} COINS",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Lifetime Earned: ${state.totalCoinsEarned} Coins",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = {
                            (context as? Activity)?.let { act ->
                                viewModel.watchRewardedAdForCoin(act)
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp).testTag("profile_watch_ad_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+1 Coin (Ad)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Daily Mode Passes & Unlock Status (10 Coins / Day)
        item {
            Text(
                text = "DAILY MODE PASSES (10 🪙 / DAY)",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(
                        PersonaType.STUDENT,
                        PersonaType.BUSINESS,
                        PersonaType.ATHLETE,
                        PersonaType.LOOKSMAX
                    ).forEachIndexed { index, mode ->
                        val isUnlocked = state.isModeUnlocked(mode)
                        val remainingTime = state.getRemainingDailyUnlockTime(mode)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = mode.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (isUnlocked) "Active • ${remainingTime ?: "24h Pass"}" else "Locked • 10 Coins / Day",
                                    fontSize = 11.sp,
                                    color = if (isUnlocked) Color(0xFF34D399) else Color(0xFF71717A)
                                )
                            }

                            if (!isUnlocked) {
                                Button(
                                    onClick = {
                                        viewModel.unlockModeWithCoins(mode)
                                    },
                                    enabled = state.coins >= mode.unlockCost,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp).testTag("unlock_pass_${mode.name.lowercase()}"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text("Unlock 10 🪙", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF132F20))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("UNLOCKED", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF34D399))
                                }
                            }
                        }

                        if (index < 3) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }

        // Comprehensive Personal Stats
        item {
            Text(
                text = "PERSONAL LIFE STATS",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatRow(label = "Study Deep Focus", value = "${state.studyMinutesLogged} Mins  •  ${state.chapters.count { it.isCompleted }} Chapters Done")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    StatRow(label = "Business Volume", value = "₹ ${String.format("%,.0f", state.invoices.sumOf { it.grandTotal })}  •  ${state.invoices.size} Invoices")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    StatRow(label = "Athlete Pedometer", value = "${state.athleteStats.todaySteps} Steps  •  ${state.athleteStats.activeCaloriesKcal} kcal")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    StatRow(label = "Mala Jaap Chants", value = "${state.malaJaapState.completedMalas} Malas  •  ${state.malaJaapState.totalChantsLifetime} Chants")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    StatRow(label = "Mini Game Record", value = "${state.gamesWon} Won  •  ${state.gamesLost} Lost")
                }
            }
        }

        // System Settings & Push Notifications
        item {
            Text(
                text = "SETTINGS & NOTIFICATIONS",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Dark Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Dark Theme", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("High contrast OLED palette", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = state.isDarkTheme,
                            onCheckedChange = { viewModel.toggleDarkMode() },
                            modifier = Modifier.testTag("profile_dark_mode_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    // Sound Effects
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Sound Effects", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Synthesized audio chimes & beeps", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = state.soundEffectsEnabled,
                            onCheckedChange = { viewModel.toggleSound() }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    // Haptics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Haptic Vibrations", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Mala bead vibrations & tactile clicks", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = state.hapticsEnabled,
                            onCheckedChange = { viewModel.toggleHaptics() }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    // Daily Reward Push Notification Test
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Daily Reward Push Alerts", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Receive streak reminders and coin bonus alerts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = { viewModel.triggerTestNotification(context) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp).testTag("test_push_notification_button")
                        ) {
                            Text("Send Test Alert", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    // Offline Storage & Sync Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Offline Support & Storage", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Active • 100% on-device local database", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    // Modal Edit Profile Dialog
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Player Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editNameInput,
                        onValueChange = { editNameInput = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_name_input")
                    )
                    OutlinedTextField(
                        value = editEmailInput,
                        onValueChange = { editEmailInput = it },
                        label = { Text("Email / Cloud ID") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_email_input")
                    )
                    Text("Select Avatar:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("👑", "🦁", "⚡", "💎", "🔥", "🚀").forEach { emoji ->
                            FilterChip(
                                selected = editAvatarInput == emoji,
                                onClick = { editAvatarInput = emoji },
                                label = { Text(emoji, fontSize = 16.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(editNameInput, editEmailInput, editAvatarInput)
                        showEditProfileDialog = false
                    },
                    modifier = Modifier.testTag("save_profile_button")
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
