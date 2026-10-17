package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppState
import com.example.model.DEFAULT_MANTRAS
import com.example.ui.theme.GoldCoinYellow
import com.example.viewmodel.MainViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MalaJaapScreen(
    state: AppState,
    viewModel: MainViewModel
) {
    val mala = state.malaJaapState
    val currentMantra = DEFAULT_MANTRAS.firstOrNull { it.id == mala.selectedMantraId } ?: DEFAULT_MANTRAS[0]
    var showMantraDialog by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val beadScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(),
        label = "bead_scale"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 64.dp)
    ) {
        // Hero / Jaap Overview Card
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MALA JAAP (माला जाप)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "108 Sacred Beads Counter with Haptic Touch",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(text = "🕉️", fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Current Bead", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${mala.currentBead} / 108", fontSize = 16.sp, fontWeight = FontWeight.Black, color = GoldCoinYellow)
                        }
                        Column {
                            Text("Completed Malas", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${mala.completedMalas}", fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }
                        Column {
                            Text("Lifetime Chants", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${mala.totalChantsLifetime}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { (mala.currentBead / 108f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = GoldCoinYellow,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // Active Mantra Selector Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMantraDialog = true }
                    .testTag("select_mantra_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE CHANT / MANTRA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Change", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentMantra.devanagari,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentMantra.englishChant,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // TACTILE BEAD ROSARY & COUNTING INTERFACE
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TAP ROSARY TO COUNT BEAD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Circular Rosary of Beads with Center Tappable Guru Bead
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .testTag("bead_circle_container"),
                        contentAlignment = Alignment.Center
                    ) {
                        // Draw outer circle of 27 visible miniature beads
                        val beadColor = if (state.isDarkTheme) Color.White else Color.Black
                        val inactiveColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        val totalVisibleBeads = 27
                        val activeQuarterIndex = (mala.currentBead % totalVisibleBeads)

                        Canvas(modifier = Modifier.size(220.dp)) {
                            val radius = size.width / 2f - 12.dp.toPx()
                            val center = Offset(size.width / 2f, size.height / 2f)

                            for (i in 0 until totalVisibleBeads) {
                                val angleRad = Math.toRadians((i * (360.0 / totalVisibleBeads)) - 90.0)
                                val x = center.x + radius * cos(angleRad).toFloat()
                                val y = center.y + radius * sin(angleRad).toFloat()
                                val isBeadActive = i <= activeQuarterIndex

                                drawCircle(
                                    color = if (isBeadActive) beadColor else inactiveColor,
                                    radius = if (isBeadActive) 7.dp.toPx() else 4.dp.toPx(),
                                    center = Offset(x, y)
                                )
                            }
                        }

                        // Central Tap Button (Large Bead)
                        Surface(
                            modifier = Modifier
                                .size(140.dp)
                                .scale(beadScale)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    viewModel.incrementMalaBead()
                                }
                                .border(2.5.dp, if (state.isDarkTheme) Color.White else Color.Black, CircleShape)
                                .testTag("tap_bead_button"),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shadowElevation = 8.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                MaterialTheme.colorScheme.surface
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${mala.currentBead}",
                                        fontSize = 38.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (state.isDarkTheme) Color.White else Color.Black
                                    )
                                    Text(
                                        text = "/ 108 BEADS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "TAP CHANT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📳 Haptic Vibration: Active on each bead",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.resetCurrentMala() },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("reset_mala_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Mala", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { viewModel.incrementMalaBead() },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("count_bead_action_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+1 Bead Count", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Mala Rewards Info Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🪙", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Complete 1 Mala (108 Beads) = +1 Coin & +50 XP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Chant with dhyan and mindfulness to unlock higher life tiers and coins.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Mantra Selection Dialog
    if (showMantraDialog) {
        AlertDialog(
            onDismissRequest = { showMantraDialog = false },
            title = { Text("Select Chanting Mantra", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(DEFAULT_MANTRAS.size) { idx ->
                        val m = DEFAULT_MANTRAS[idx]
                        val isSel = m.id == mala.selectedMantraId
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.selectMantra(m.id)
                                    showMantraDialog = false
                                }
                                .border(
                                    width = if (isSel) 1.5.dp else 1.dp,
                                    color = if (isSel) GoldCoinYellow else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(m.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(m.devanagari, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMantraDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
