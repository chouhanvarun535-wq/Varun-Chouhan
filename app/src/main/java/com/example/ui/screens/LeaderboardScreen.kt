package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.model.AppState
import com.example.model.LeaderboardPlayer
import com.example.ui.theme.GoldCoinYellow
import com.example.viewmodel.MainViewModel

@Composable
fun LeaderboardScreen(
    state: AppState,
    viewModel: MainViewModel
) {
    val leaderboard = state.leaderboardState
    val topPlayers = leaderboard.topPlayers
    val top1 = topPlayers.getOrNull(0)
    val top2 = topPlayers.getOrNull(1)
    val top3 = topPlayers.getOrNull(2)

    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("👑 ALL-TIME TOP", "📿 SADHANA MASTERS", "⚡ RISING STARS")

    val displayedPlayers = remember(topPlayers, selectedTab) {
        when (selectedTab) {
            1 -> topPlayers.filter { it.level >= 4 }
            2 -> topPlayers.sortedBy { it.coins }
            else -> topPlayers
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 64.dp)
    ) {
        // Season & Reset Banner
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111116)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E38)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF181822), Color(0xFF111116))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = GoldCoinYellow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "GLOBAL MASTERY LEADERBOARD",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${leaderboard.currentSeasonName}  •  Reset in ${leaderboard.daysUntilMonthlyReset} days",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFA1A1AA)
                            )
                        }
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = GoldCoinYellow,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Real-time scores from Jap Mala, Mini Games & daily accomplishments.",
                            fontSize = 11.sp,
                            color = Color(0xFF71717A),
                            modifier = Modifier.weight(1f),
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.resetMonthlyLeaderboard() },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3F3F4E)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("monthly_reset_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Monthly Reset", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // TOP 3 PODIUM DISPLAY
        if (top1 != null && top2 != null && top3 != null) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111116)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E38)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TOP SADHAKS PODIUM",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            color = Color(0xFF71717A)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Rank 2 (Silver)
                            PodiumColumn(
                                player = top2,
                                medal = "🥈",
                                rankNum = 2,
                                heightDp = 100,
                                borderColor = Color(0xFFE2E8F0),
                                pedestalBg = Brush.verticalGradient(listOf(Color(0xFF24242E), Color(0xFF15151B)))
                            )

                            // Rank 1 (Gold - Center & Tallest)
                            PodiumColumn(
                                player = top1,
                                medal = "🥇",
                                rankNum = 1,
                                heightDp = 130,
                                isWinner = true,
                                borderColor = Color(0xFFFFD700),
                                pedestalBg = Brush.verticalGradient(listOf(Color(0xFF382F16), Color(0xFF1C180E)))
                            )

                            // Rank 3 (Bronze)
                            PodiumColumn(
                                player = top3,
                                medal = "🥉",
                                rankNum = 3,
                                heightDp = 85,
                                borderColor = Color(0xFFD97706),
                                pedestalBg = Brush.verticalGradient(listOf(Color(0xFF291E16), Color(0xFF16110D)))
                            )
                        }
                    }
                }
            }
        }

        // YOUR CURRENT LIVE RANK BADGE
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A24)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFFFFF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_rank_badge")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.5.dp, GoldCoinYellow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#${leaderboard.userRank}",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${state.userName} (You)",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF22C55E).copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("LIVE", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF4ADE80))
                                }
                            }
                            Text(
                                text = "Level ${state.userLevel}  •  ${state.currentLooksmaxTier.tierName}",
                                fontSize = 11.sp,
                                color = Color(0xFFA1A1AA)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "🪙 ${state.coins}",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = GoldCoinYellow
                        )
                        Text(
                            text = "${state.totalXp} XP",
                            fontSize = 10.sp,
                            color = Color(0xFF71717A)
                        )
                    }
                }
            }
        }

        // FILTER TABS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabTitles.forEachIndexed { index, title ->
                    val isTabSelected = selectedTab == index
                    FilterChip(
                        selected = isTabSelected,
                        onClick = { selectedTab = index },
                        label = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (isTabSelected) FontWeight.Black else FontWeight.SemiBold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White,
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF16161D),
                            labelColor = Color(0xFFA1A1AA)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isTabSelected) Color.White else Color(0xFF2E2E38),
                            enabled = true,
                            selected = isTabSelected
                        )
                    )
                }
            }
        }

        // ALL PLAYERS LIST
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RANKED PRACTITIONERS (${displayedPlayers.size})",
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = Color(0xFF71717A)
                )
                Text(
                    text = "COINS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color(0xFF71717A)
                )
            }
        }

        items(displayedPlayers, key = { it.id }) { player ->
            val isUser = player.isCurrentUser
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) Color(0xFF1C1C26) else Color(0xFF121217)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isUser) 1.5.dp else 1.dp,
                    color = if (isUser) Color.White else Color(0xFF262630)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "#${player.rank}",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            modifier = Modifier.width(34.dp),
                            color = when (player.rank) {
                                1 -> GoldCoinYellow
                                2 -> Color(0xFFE2E8F0)
                                3 -> Color(0xFFD97706)
                                else -> Color(0xFF71717A)
                            }
                        )

                        Text(player.avatarEmoji, fontSize = 22.sp)

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = player.name,
                                    fontWeight = if (isUser) FontWeight.Black else FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isUser) Color.White else Color(0xFFE4E4E7)
                                )
                                if (isUser) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color.White)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("YOU", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                    }
                                }
                            }

                            val playerBadge = when {
                                player.level >= 7 -> "Grandmaster Sadhak"
                                player.level >= 5 -> "Focus Elite"
                                player.level >= 3 -> "Dedicated Practitioner"
                                else -> "Rising Seeker"
                            }
                            Text(
                                text = "Level ${player.level}  •  $playerBadge",
                                fontSize = 10.sp,
                                color = Color(0xFFA1A1AA)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🪙 ${player.coins}",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = GoldCoinYellow
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PodiumColumn(
    player: LeaderboardPlayer,
    medal: String,
    rankNum: Int,
    heightDp: Int,
    borderColor: Color,
    pedestalBg: Brush,
    isWinner: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(96.dp)
    ) {
        Text(text = medal, fontSize = 22.sp)
        Text(text = player.avatarEmoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = player.name.replace(" (You)", ""),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            text = "🪙 ${player.coins}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = GoldCoinYellow
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Luxury Pedestal
        Box(
            modifier = Modifier
                .width(82.dp)
                .height(heightDp.dp)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .background(pedestalBg)
                .border(1.5.dp, borderColor, RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "#$rankNum",
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = borderColor
                )
                if (isWinner) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = GoldCoinYellow,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
