package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppState
import com.example.viewmodel.MainViewModel

@Composable
fun StudentScreen(
    state: AppState,
    viewModel: MainViewModel
) {
    val pomodoroSeconds by viewModel.pomodoroSecondsLeft.collectAsState()
    val isRunning by viewModel.isPomodoroRunning.collectAsState()
    val presetMinutes by viewModel.pomodoroPresetMinutes.collectAsState()

    var newSubjectInput by remember { mutableStateOf("") }
    var newChapterInput by remember { mutableStateOf("") }
    var newNotesInput by remember { mutableStateOf("") }
    var showAddChapterDialog by remember { mutableStateOf(false) }

    val completedChapters = state.chapters.count { it.isCompleted }
    val totalChapters = state.chapters.size
    val progressPercent = if (totalChapters > 0) (completedChapters.toFloat() / totalChapters) else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 64.dp)
    ) {
        // Section: Hero / Overview
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
                                text = "STUDENT STUDIO",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Syllabus tracking, deep focus & exam mastery",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(text = "🎓", fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Deep Work", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${state.studyMinutesLogged} Mins", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Syllabus Completed", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$completedChapters / $totalChapters", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Daily Streak", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${state.streakDays}d", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // Section: Live Pomodoro Focus Stopwatch
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "POMODORO FOCUS TIMER",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(15, 25, 45).forEach { mins ->
                                FilterChip(
                                    selected = presetMinutes == mins,
                                    onClick = { viewModel.resetPomodoro(mins) },
                                    label = { Text("${mins}m", fontSize = 10.sp) },
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val minutes = pomodoroSeconds / 60
                    val seconds = pomodoroSeconds % 60
                    val timeString = String.format("%02d:%02d", minutes, seconds)
                    Text(
                        text = timeString,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (isRunning) viewModel.pausePomodoro() else viewModel.startPomodoro()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("pomodoro_toggle_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isRunning) "PAUSE" else "START FOCUS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetPomodoro() },
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("pomodoro_reset_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Section: Syllabus Tracker
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SYLLABUS & CHAPTERS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { showAddChapterDialog = true },
                    modifier = Modifier.height(34.dp).testTag("add_chapter_dialog_button"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Chapter", fontSize = 11.sp)
                }
            }
        }

        if (state.chapters.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No chapters logged yet. Tap 'Add Chapter' above to log your first syllabus chapter and earn +35 XP & +1 Coin!",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(state.chapters, key = { it.id }) { chapter ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleChapterCompletion(chapter.id) }
                        .testTag("chapter_item_${chapter.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = chapter.isCompleted,
                            onCheckedChange = { viewModel.toggleChapterCompletion(chapter.id) },
                            modifier = Modifier.testTag("checkbox_${chapter.id}")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = chapter.subjectName.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (chapter.isCompleted) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "✔ Completed (+35 XP, +1 Coin)",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = chapter.chapterTitle,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            if (chapter.notes.isNotEmpty()) {
                                Text(
                                    text = chapter.notes,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog to Add Chapter
    if (showAddChapterDialog) {
        AlertDialog(
            onDismissRequest = { showAddChapterDialog = false },
            title = { Text("Log Syllabus Chapter", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newSubjectInput,
                        onValueChange = { newSubjectInput = it },
                        label = { Text("Subject (e.g. Physics, Law)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_subject_name")
                    )
                    OutlinedTextField(
                        value = newChapterInput,
                        onValueChange = { newChapterInput = it },
                        label = { Text("Chapter Title") },
                        modifier = Modifier.fillMaxWidth().testTag("input_chapter_title")
                    )
                    OutlinedTextField(
                        value = newNotesInput,
                        onValueChange = { newNotesInput = it },
                        label = { Text("Important Formulas / Summary") },
                        modifier = Modifier.fillMaxWidth().testTag("input_chapter_notes")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newChapterInput.isNotBlank()) {
                            viewModel.addChapter(newSubjectInput, newChapterInput, newNotesInput)
                            newSubjectInput = ""
                            newChapterInput = ""
                            newNotesInput = ""
                            showAddChapterDialog = false
                        }
                    },
                    modifier = Modifier.testTag("submit_add_chapter_button")
                ) {
                    Text("Add to Syllabus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddChapterDialog = false }) { Text("Cancel") }
            }
        )
    }
}
