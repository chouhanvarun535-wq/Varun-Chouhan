package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.viewmodel.MainViewModel

@Composable
fun AthleteScreen(
    state: AppState,
    viewModel: MainViewModel
) {
    val stats = state.athleteStats
    val isSensorActive by viewModel.isSensorActive.collectAsState()
    var showWorkoutDialog by remember { mutableStateOf(false) }

    // Workout dialog state
    var selectedSport by remember { mutableStateOf(SportType.RUNNING) }
    var durationMinsInput by remember { mutableStateOf("30") }
    var caloriesInput by remember { mutableStateOf("250") }
    var distanceKmInput by remember { mutableStateOf("4.2") }
    var notesInput by remember { mutableStateOf("") }

    val stepProgress = if (stats.stepGoal > 0) (stats.todaySteps.toFloat() / stats.stepGoal).coerceIn(0f, 1f) else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 64.dp)
    ) {
        // Section: Sensor Telemetry & Step Counter
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
                                text = "ATHLETE TELEMETRY",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 1.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isSensorActive) Color(0xFF4CAF50) else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSensorActive) "Hardware Motion Sensor Active" else "Sensor Paused",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(text = "🏃‍♂️", fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Step Counter Metric Display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "${stats.todaySteps}",
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "STEPS TODAY (Goal: ${stats.stepGoal})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${stats.distanceKm} km",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${stats.activeCaloriesKcal} kcal burned",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { stepProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Step Log / Simulate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(100, 500, 1000).forEach { inc ->
                            OutlinedButton(
                                onClick = { viewModel.simulateSteps(inc) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .testTag("step_increment_${inc}"),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("+$inc Steps", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Section: Daily Readiness Score
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DAILY READINESS SCORE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stats.readinessStatus,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Sleep: ${stats.sleepHours}h • RHR: ${stats.restingHeartRateBpm} bpm",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${stats.readinessScore}%",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Section: Muscle & Soreness Interactive Heatmap
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
                        Text(
                            text = "MUSCLE & SORENESS HEATMAP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("⚪ Fresh", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("🔘 Tight", fontSize = 9.sp, color = Color.Gray)
                            Text("⚫ Sore", fontSize = 9.sp, color = Color.DarkGray)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap any muscle group to toggle recovery state",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val muscleList = state.sorenessMap.keys.toList()
                    val rows = muscleList.chunked(2)
                    rows.forEach { rowMuscles ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowMuscles.forEach { muscle ->
                                val level = state.sorenessMap[muscle] ?: SorenessLevel.FRESH
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.cycleMuscleSoreness(muscle) }
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                        .testTag("muscle_toggle_${muscle.lowercase().replace(" ", "_").replace("&", "_")}"),
                                    color = when (level) {
                                        SorenessLevel.FRESH -> MaterialTheme.colorScheme.surface
                                        SorenessLevel.TIGHT -> MaterialTheme.colorScheme.surfaceVariant
                                        SorenessLevel.SORE -> MaterialTheme.colorScheme.primaryContainer
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(muscle, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Text(level.label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Sport-Specific Workout Logger
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WORKOUT SESSIONS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = { showWorkoutDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp).testTag("log_workout_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Workout (+60 XP, +1 Coin)", fontSize = 11.sp)
                }
            }
        }

        if (state.workouts.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No workouts logged yet. Tap 'Log Workout' to record a session and build endurance.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(state.workouts, key = { it.id }) { w ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(w.sportType.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                "${w.durationMinutes} mins • ${w.caloriesBurned} kcal ${if (w.distanceKm > 0) "• " + w.distanceKm + " km" else ""}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text("+60 XP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Modal Dialog: Log Workout
    if (showWorkoutDialog) {
        AlertDialog(
            onDismissRequest = { showWorkoutDialog = false },
            title = { Text("Log Workout Session", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Sport:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    SportType.values().forEach { sport ->
                        FilterChip(
                            selected = selectedSport == sport,
                            onClick = { selectedSport = sport },
                            label = { Text(sport.title, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = durationMinsInput,
                            onValueChange = { durationMinsInput = it },
                            label = { Text("Duration (mins)") },
                            modifier = Modifier.weight(1f).testTag("input_workout_duration")
                        )
                        OutlinedTextField(
                            value = caloriesInput,
                            onValueChange = { caloriesInput = it },
                            label = { Text("Kcal") },
                            modifier = Modifier.weight(1f).testTag("input_workout_calories")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val dur = durationMinsInput.toIntOrNull() ?: 0
                        val cal = caloriesInput.toIntOrNull() ?: 0
                        val dist = distanceKmInput.toDoubleOrNull() ?: 0.0
                        viewModel.logWorkout(selectedSport, dur, cal, dist, notesInput)
                        showWorkoutDialog = false
                    },
                    modifier = Modifier.testTag("submit_log_workout_button")
                ) {
                    Text("Save & Claim XP")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWorkoutDialog = false }) { Text("Cancel") }
            }
        )
    }
}
