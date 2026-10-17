package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppState
import com.example.model.PersonaType
import com.example.ui.theme.GoldCoinYellow

@Composable
fun PersonaSelectorBar(
    state: AppState,
    currentPersona: PersonaType,
    onSelectPersona: (PersonaType) -> Unit,
    onLockedModeClicked: (PersonaType) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PersonaType.values().forEach { persona ->
                val isSelected = currentPersona == persona
                val isUnlocked = state.isModeUnlocked(persona)

                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    label = "persona_bg"
                )
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    label = "persona_border"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "persona_text"
                )

                val icon = when (persona) {
                    PersonaType.STUDENT -> Icons.Default.School
                    PersonaType.BUSINESS -> Icons.Default.Payments
                    PersonaType.ATHLETE -> Icons.Default.DirectionsRun
                    PersonaType.LOOKSMAX -> Icons.Default.AutoAwesome
                    PersonaType.MALA_JAAP -> Icons.Default.SelfImprovement
                    PersonaType.GAMES -> Icons.Default.SportsEsports
                    PersonaType.LEADERBOARD -> Icons.Default.EmojiEvents
                    PersonaType.PROFILE -> Icons.Default.AccountCircle
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable {
                            if (isUnlocked) {
                                onSelectPersona(persona)
                            } else {
                                onLockedModeClicked(persona)
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("persona_tab_${persona.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = persona.title,
                            tint = if (!isUnlocked) Color.Gray else textColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = persona.title.replace(" Mode", ""),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (!isUnlocked) Color.Gray else textColor
                        )

                        val remainingTime = state.getRemainingDailyUnlockTime(persona)

                        if (!isUnlocked) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF272733))
                                    .border(0.5.dp, GoldCoinYellow.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text("🔒", fontSize = 9.sp)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${persona.unlockCost} 🪙",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GoldCoinYellow
                                )
                            }
                        } else if (remainingTime != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF132F20))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "⚡ $remainingTime",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
