package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.PersonaType
import com.example.ui.theme.GoldCoinYellow

@Composable
fun ModeLockedDialog(
    persona: PersonaType,
    currentCoins: Int,
    onUnlockWithCoins: () -> Unit,
    onNavigateToGames: () -> Unit,
    onWatchAdForCoins: () -> Unit,
    onUnlockWithAdPass: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val canAfford = currentCoins >= persona.unlockCost

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF0F0F14),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF3F3F4E)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("mode_locked_dialog")
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF181822),
                                Color(0xFF0F0F14)
                            )
                        )
                    )
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Luxury Emblem Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF242430))
                        .border(1.5.dp, Color(0xFF525266), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = GoldCoinYellow,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "DAILY PASS REQUIRED",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = persona.title.uppercase(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldCoinYellow,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Daily unlock cost: 10 🪙 Coins\nGrants full 24-hour uninterrupted access to all tools, tracking & features.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFA1A1AA),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Wallet Status Pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1E28))
                        .border(1.dp, Color(0xFF3F3F4E), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🪙", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("YOUR BALANCE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF71717A))
                                Text("$currentCoins Coins", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("DAILY PASS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF71717A))
                            Text("10 Coins / 24h", fontSize = 13.sp, fontWeight = FontWeight.Black, color = GoldCoinYellow)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action 1: Unlock with 10 Coins
                if (canAfford) {
                    Button(
                        onClick = {
                            onUnlockWithCoins()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("unlock_with_coins_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unlock 24h Pass with 10 Coins", fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Action 2: Unlock with Rewarded Ad Video (Instant 24H Pass)
                OutlinedButton(
                    onClick = {
                        onUnlockWithAdPass()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("watch_ad_unlock_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF525266))
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = GoldCoinYellow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("📺 Unlock Today Free with Video Ad", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action 3: Go to Games to Earn Coins
                OutlinedButton(
                    onClick = {
                        onNavigateToGames()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("play_games_to_earn_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3F3F4E))
                ) {
                    Text("🎮 Play Mini-Games to Win Coins", color = Color(0xFFD4D4D8), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color(0xFF71717A), fontSize = 12.sp)
                }
            }
        }
    }
}
