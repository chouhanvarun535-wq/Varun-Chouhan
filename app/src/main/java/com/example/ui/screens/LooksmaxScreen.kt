package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.AppState
import com.example.viewmodel.MainViewModel

@Composable
fun LooksmaxScreen(
    state: AppState,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val isScanning by viewModel.isScanningFace.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val metrics = state.latestMetrics
    val routine = state.softmaxingRoutine
    val tier = state.currentLooksmaxTier
    val photoUri = state.userSelectedPhotoUri

    // Zero-permission Android Photo Picker for selfie upload
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setUserSelectedPhoto(uri.toString())
        }
    }

    var splitPosition by remember { mutableFloatStateOf(0.5f) }
    var selectedTab by remember { mutableStateOf(0) } // 0: AI Scanner, 1: Photo Comparator, 2: Softmaxing Habits

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 64.dp)
    ) {
        // Section: Tier & Rating Banner
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tier.badge, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tier.tierName.uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "Facial Symmetry & Golden Ratio Architecture",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${String.format("%.1f", metrics.overallRating)}/10",
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Symmetry", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${metrics.symmetryPercent}%", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Jawline", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${metrics.jawlineDefinitionPercent}%", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Golden Ratio", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${metrics.goldenRatioMatchPercent}%", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Skin Quality", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${metrics.skinQualityPercent}%", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Subtabs
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                edgePadding = 0.dp
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("AI Face Scan", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Photo Comparator", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Softmaxing Habits", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // TAB 0: AI SCANNER & PHOTO UPLOAD
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
                                    text = "FACIAL MESH SCANNER",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier.height(32.dp).testTag("upload_selfie_button"),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (photoUri != null) "Change Photo" else "Upload Photo", fontSize = 10.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF141414))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (photoUri != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(photoUri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "User Uploaded Selfie",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                val overlayColor = MaterialTheme.colorScheme.primary
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height

                                    drawLine(
                                        color = Color.White.copy(alpha = 0.35f),
                                        start = Offset(w * 0.5f, h * 0.08f),
                                        end = Offset(w * 0.5f, h * 0.92f),
                                        strokeWidth = 1f
                                    )
                                    drawOval(
                                        color = Color.White.copy(alpha = 0.4f),
                                        topLeft = Offset(w * 0.28f, h * 0.12f),
                                        size = androidx.compose.ui.geometry.Size(w * 0.44f, h * 0.74f),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                                    )
                                    drawLine(
                                        color = overlayColor,
                                        start = Offset(w * 0.32f, h * 0.62f),
                                        end = Offset(w * 0.5f, h * 0.86f),
                                        strokeWidth = 2f
                                    )
                                    drawLine(
                                        color = overlayColor,
                                        start = Offset(w * 0.68f, h * 0.62f),
                                        end = Offset(w * 0.5f, h * 0.86f),
                                        strokeWidth = 2f
                                    )

                                    if (isScanning) {
                                        val scanY = h * scanProgress
                                        drawLine(
                                            color = Color.White,
                                            start = Offset(0f, scanY),
                                            end = Offset(w, scanY),
                                            strokeWidth = 3f
                                        )
                                    }
                                }

                                if (isScanning) {
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.75f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(
                                            text = "ANALYZING FACIAL SYMMETRY... ${(scanProgress * 100).toInt()}%",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                } else if (photoUri == null) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "TAP 'UPLOAD PHOTO' TO SELECT SELFIE",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.LightGray
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Upload a front-facing selfie to analyze symmetry & jawline",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { viewModel.triggerAiFaceScan() },
                                enabled = !isScanning,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("run_face_scan_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (isScanning) "ANALYZING..." else "EXECUTE FACIAL ANALYSIS (+75 XP)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Metrics Breakdown
                item {
                    Text(
                        text = "METRIC ANALYSIS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Facial Symmetry", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${metrics.symmetryPercent}%", fontWeight = FontWeight.Black, fontSize = 18.sp)
                                Text("Left/Right proportion", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Jawline Masseter", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${metrics.jawlineDefinitionPercent}%", fontWeight = FontWeight.Black, fontSize = 18.sp)
                                Text("Mandibular angle", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Canthal Tilt", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(metrics.canthalTilt.label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Eye axis slope", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Skin Quality Index", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${metrics.skinQualityPercent}%", fontWeight = FontWeight.Black, fontSize = 18.sp)
                                Text("Texture & smoothness", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            1 -> {
                // TAB 1: ADVANCED PROGRESS PHOTO COMPARATOR
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
                                    text = "PHOTO COMPARATOR",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Pick Photo", fontSize = 10.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Drag slider to compare baseline day 0 vs today's progress:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF141414))
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, _ ->
                                            val newPos = change.position.x / size.width.toFloat()
                                            splitPosition = newPos.coerceIn(0.05f, 0.95f)
                                        }
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(splitPosition)
                                        .background(Color(0xFF222222)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("BEFORE", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                        Text("Baseline", fontSize = 10.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text("👤", fontSize = 48.sp)
                                        Text("Soft Angle", fontSize = 10.sp, color = Color.LightGray)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(1f - splitPosition)
                                        .align(Alignment.CenterEnd)
                                        .background(Color(0xFF333333)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (photoUri != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context).data(photoUri).crossfade(true).build(),
                                            contentDescription = "Uploaded Selfie",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("AFTER", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                            Text("Today", fontSize = 10.sp, color = Color.Gray)
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text("👑", fontSize = 48.sp)
                                            Text("Sharp Definition", fontSize = 10.sp, color = Color.White)
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(2.dp)
                                        .align(Alignment.CenterStart)
                                        .offset(x = (splitPosition * 300).dp)
                                        .background(Color.White)
                                )

                                Surface(
                                    color = Color.Black.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = "◀ SLIDE TO COMPARE ▶",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // TAB 2: SOFTMAXING HABITS
                item {
                    Text(
                        text = "DAILY SOFTMAXING ROUTINE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleHabit("morningSkincare") }
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = routine.morningSkincareDone,
                                onCheckedChange = { viewModel.toggleHabit("morningSkincare") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Morning Gentle Cleanser & Moisturizer", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("+15 XP • Barrier hydration", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleHabit("spfApplied") }
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = routine.spfApplied,
                                onCheckedChange = { viewModel.toggleHabit("spfApplied") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Broad Spectrum SPF 50+ Sunscreen", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("+15 XP • Anti-aging protection", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleHabit("iceFacial") }
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = routine.iceFacialColdShowerDone,
                                onCheckedChange = { viewModel.toggleHabit("iceFacial") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Ice Facial Bowl / Cold Shower", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("+15 XP • Depuff face & tighten skin", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                item {
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
                                Text("Tongue Posture & Mewing", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Logged: ${routine.mewingMinutesLogged} / 30 mins", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = { viewModel.addMewingMinutes(10) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("+10m", fontSize = 10.sp)
                            }
                        }
                    }
                }

                item {
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
                                Text("Hydro-Maxxing Water Intake", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Logged: ${routine.waterGlassesLogged} / 8 Glasses", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = { viewModel.addWaterGlass() },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("+1 Glass", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
