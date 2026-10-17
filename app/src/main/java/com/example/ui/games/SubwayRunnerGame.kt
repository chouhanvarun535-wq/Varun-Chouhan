package com.example.ui.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppState
import com.example.ui.theme.GoldCoinYellow
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

enum class ObstacleType {
    TRAIN,
    LOW_BARRIER,
    HIGH_SIGN,
    COIN,
    MAGNET_POWERUP
}

data class RunnerObstacle(
    val id: Long,
    val lane: Int, // -1, 0, 1
    var yPos: Float, // 0.0 (far distance) to 1.0 (player pos)
    val type: ObstacleType
)

@Composable
fun SubwayRunnerGame(
    state: AppState,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // Game Running State
    var isPlaying by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var distanceMeters by remember { mutableIntStateOf(0) }
    var coinsCollectedThisRun by remember { mutableIntStateOf(0) }
    var bestDistance by remember { mutableIntStateOf(240) }

    // Player Lane (-1 = Left, 0 = Center, 1 = Right)
    var currentLane by remember { mutableIntStateOf(0) }
    val animatedLaneX = remember { Animatable(0f) }

    // Jumping / Sliding State
    var isJumping by remember { mutableStateOf(false) }
    var isSliding by remember { mutableStateOf(false) }
    val jumpAnimY = remember { Animatable(0f) }

    // Magnet powerup
    var magnetSecondsLeft by remember { mutableIntStateOf(0) }

    // Active obstacles
    val obstacles = remember { mutableStateListOf<RunnerObstacle>() }
    var nextObstacleId by remember { mutableLongStateOf(1L) }

    // Track scroll offset for visual speed
    var trackScrollOffset by remember { mutableFloatStateOf(0f) }

    // Function to handle lane switch
    fun switchLane(targetLane: Int) {
        val clamped = targetLane.coerceIn(-1, 1)
        if (clamped != currentLane) {
            currentLane = clamped
            viewModel.soundManager.playBeadVibration()
            coroutineScope.launch {
                animatedLaneX.animateTo(clamped.toFloat(), tween(120, easing = LinearEasing))
            }
        }
    }

    // Function to jump
    fun performJump() {
        if (!isJumping && !isSliding) {
            isJumping = true
            viewModel.soundManager.playButtonClickSound()
            coroutineScope.launch {
                jumpAnimY.animateTo(-65f, tween(260))
                jumpAnimY.animateTo(0f, tween(220))
                isJumping = false
            }
        }
    }

    // Function to slide
    fun performSlide() {
        if (!isSliding && !isJumping) {
            isSliding = true
            viewModel.soundManager.playButtonClickSound()
            coroutineScope.launch {
                delay(450L)
                isSliding = false
            }
        }
    }

    // Game loop
    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect

        obstacles.clear()
        distanceMeters = 0
        coinsCollectedThisRun = 0
        isGameOver = false
        currentLane = 0
        animatedLaneX.snapTo(0f)
        var spawnTimer = 0

        while (isPlaying && !isGameOver) {
            delay(30L)
            trackScrollOffset = (trackScrollOffset + 0.05f) % 1.0f
            distanceMeters += 1

            // Spawn obstacles
            spawnTimer++
            if (spawnTimer >= 32) {
                spawnTimer = 0
                val spawnLane = Random.nextInt(-1, 2)
                val typeRoll = Random.nextInt(100)
                val type = when {
                    typeRoll < 35 -> ObstacleType.COIN
                    typeRoll < 55 -> ObstacleType.LOW_BARRIER
                    typeRoll < 80 -> ObstacleType.TRAIN
                    typeRoll < 92 -> ObstacleType.HIGH_SIGN
                    else -> ObstacleType.MAGNET_POWERUP
                }

                // If coin, spawn a line of 3 coins
                if (type == ObstacleType.COIN) {
                    obstacles.add(RunnerObstacle(nextObstacleId++, spawnLane, -0.1f, ObstacleType.COIN))
                    obstacles.add(RunnerObstacle(nextObstacleId++, spawnLane, -0.25f, ObstacleType.COIN))
                    obstacles.add(RunnerObstacle(nextObstacleId++, spawnLane, -0.4f, ObstacleType.COIN))
                } else {
                    obstacles.add(RunnerObstacle(nextObstacleId++, spawnLane, -0.1f, type))
                }
            }

            // Powerup countdown
            if (magnetSecondsLeft > 0 && distanceMeters % 30 == 0) {
                magnetSecondsLeft -= 1
            }

            // Move obstacles forward
            val speed = 0.024f + (distanceMeters / 15000f).coerceAtMost(0.02f)
            val toRemove = mutableListOf<RunnerObstacle>()

            for (obs in obstacles) {
                obs.yPos += speed

                // Magnet effect attracts coins across lanes
                if (magnetSecondsLeft > 0 && obs.type == ObstacleType.COIN && obs.yPos in 0.4f..0.9f) {
                    // Collect magnetically
                    if (abs(obs.lane - currentLane) <= 1) {
                        coinsCollectedThisRun += 1
                        viewModel.soundManager.playBeadVibration()
                        toRemove.add(obs)
                        continue
                    }
                }

                // Collision detection with player at yPos ~ 0.82f
                if (obs.yPos in 0.76f..0.88f) {
                    if (obs.lane == currentLane) {
                        when (obs.type) {
                            ObstacleType.COIN -> {
                                coinsCollectedThisRun += 1
                                viewModel.soundManager.playBeadVibration()
                                toRemove.add(obs)
                            }
                            ObstacleType.MAGNET_POWERUP -> {
                                magnetSecondsLeft = 12
                                viewModel.soundManager.playLevelUp()
                                toRemove.add(obs)
                            }
                            ObstacleType.LOW_BARRIER -> {
                                if (!isJumping) {
                                    // Crash
                                    isGameOver = true
                                }
                            }
                            ObstacleType.HIGH_SIGN -> {
                                if (!isSliding) {
                                    // Crash
                                    isGameOver = true
                                }
                            }
                            ObstacleType.TRAIN -> {
                                // Trains are fatal unless jumped from above or dodged
                                isGameOver = true
                            }
                        }
                    }
                }

                // Remove out of screen
                if (obs.yPos > 1.1f) {
                    toRemove.add(obs)
                }
            }
            obstacles.removeAll(toRemove)

            if (isGameOver) {
                isPlaying = false
                if (distanceMeters > bestDistance) {
                    bestDistance = distanceMeters
                }
                viewModel.onSubwayRunnerFinished(distanceMeters, coinsCollectedThisRun)
            }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F14)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E38)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subway_runner_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏃", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "SUBWAY TRACK RUNNER",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Dodge Trains • Jump Barriers • Collect Coins",
                            fontSize = 10.sp,
                            color = Color(0xFFAAAAAA)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🪙", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$coinsCollectedThisRun",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = GoldCoinYellow
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${distanceMeters}m",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Track Canvas / Game Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0A0A0E))
                    .border(1.dp, Color(0xFF242430), RoundedCornerShape(14.dp))
                    .pointerInput(isPlaying) {
                        if (!isPlaying) return@pointerInput
                        var totalDragX = 0f
                        var totalDragY = 0f
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                totalDragX += dragAmount.x
                                totalDragY += dragAmount.y
                            },
                            onDragEnd = {
                                if (abs(totalDragX) > abs(totalDragY)) {
                                    if (totalDragX < -30f) switchLane(currentLane - 1)
                                    else if (totalDragX > 30f) switchLane(currentLane + 1)
                                } else {
                                    if (totalDragY < -30f) performJump()
                                    else if (totalDragY > 30f) performSlide()
                                }
                                totalDragX = 0f
                                totalDragY = 0f
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Subway Perspective Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val horizonY = h * 0.15f
                    val trackBottomWidth = w * 0.92f
                    val trackTopWidth = w * 0.32f
                    val centerX = w / 2f

                    // Tunnel / Sky background gradient
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF0D0D18), Color(0xFF161622)),
                            startY = 0f,
                            endY = horizonY
                        ),
                        topLeft = Offset(0f, 0f),
                        size = Size(w, horizonY)
                    )

                    // Track bed gravel
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF1A1A22), Color(0xFF0F0F16)),
                            startY = horizonY,
                            endY = h
                        ),
                        topLeft = Offset(0f, horizonY),
                        size = Size(w, h - horizonY)
                    )

                    // Rails (3 lanes = 4 rails)
                    for (i in 0..3) {
                        val frac = i / 3f
                        val topX = (centerX - trackTopWidth / 2f) + trackTopWidth * frac
                        val bottomX = (centerX - trackBottomWidth / 2f) + trackBottomWidth * frac

                        drawLine(
                            color = Color(0xFF757588),
                            start = Offset(topX, horizonY),
                            end = Offset(bottomX, h),
                            strokeWidth = 3f + (bottomX / w) * 2f
                        )
                    }

                    // Railway wooden ties (horizontal sleepers scrolling)
                    val numSleepers = 14
                    for (s in 0 until numSleepers) {
                        val progress = ((s / numSleepers.toFloat()) + trackScrollOffset) % 1.0f
                        // Perspective scaling
                        val y = horizonY + (h - horizonY) * (progress * progress)
                        val curWidth = trackTopWidth + (trackBottomWidth - trackTopWidth) * progress
                        val leftX = centerX - curWidth / 2f
                        val rightX = centerX + curWidth / 2f

                        drawLine(
                            color = Color(0xFF32323E),
                            start = Offset(leftX, y),
                            end = Offset(rightX, y),
                            strokeWidth = 2f + progress * 5f
                        )
                    }

                    // Overhead Tunnel Neon Lights
                    val lightCount = 6
                    for (l in 0 until lightCount) {
                        val lProgress = ((l / lightCount.toFloat()) + trackScrollOffset) % 1.0f
                        val ly = horizonY * lProgress
                        drawCircle(
                            color = Color(0xFF00E5FF).copy(alpha = 0.4f * (1f - lProgress)),
                            radius = 4f + lProgress * 6f,
                            center = Offset(centerX, ly)
                        )
                    }
                }

                // Render dynamic obstacles
                Box(modifier = Modifier.fillMaxSize()) {
                    obstacles.forEach { obs ->
                        if (obs.yPos in 0.0f..1.05f) {
                            val laneFrac = (obs.lane + 1) / 2f // 0f, 0.5f, 1f
                            val progress = obs.yPos.coerceIn(0f, 1f)

                            // Perspective calculation
                            val curTrackWidth = 110f + (280f - 110f) * progress
                            val xOffsetDp = (-curTrackWidth / 2f + curTrackWidth * laneFrac).dp
                            val yOffsetDp = (50f + 250f * (progress * progress)).dp
                            val scale = 0.5f + (progress * 0.7f)

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(x = xOffsetDp, y = yOffsetDp)
                            ) {
                                when (obs.type) {
                                    ObstacleType.COIN -> {
                                        Text(
                                            text = "🪙",
                                            fontSize = (18 * scale).sp
                                        )
                                    }
                                    ObstacleType.MAGNET_POWERUP -> {
                                        Text(
                                            text = "🧲",
                                            fontSize = (22 * scale).sp
                                        )
                                    }
                                    ObstacleType.LOW_BARRIER -> {
                                        Surface(
                                            color = Color(0xFFFF5252),
                                            shape = RoundedCornerShape(4.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                                            modifier = Modifier.size((44 * scale).dp, (18 * scale).dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("🚧", fontSize = (12 * scale).sp)
                                            }
                                        }
                                    }
                                    ObstacleType.HIGH_SIGN -> {
                                        Surface(
                                            color = Color(0xFFFFAB00),
                                            shape = RoundedCornerShape(4.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
                                            modifier = Modifier.size((50 * scale).dp, (24 * scale).dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("⚠️ SLIDE", fontSize = (9 * scale).sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                        }
                                    }
                                    ObstacleType.TRAIN -> {
                                        Surface(
                                            color = Color(0xFF1E2230),
                                            shape = RoundedCornerShape(6.dp),
                                            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00E5FF)),
                                            modifier = Modifier.size((52 * scale).dp, (70 * scale).dp)
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text("🚇", fontSize = (22 * scale).sp)
                                                Text("SUBWAY", fontSize = (7 * scale).sp, fontWeight = FontWeight.Black, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Runner Character Player Sprite
                    val playerXOffset = (animatedLaneX.value * 95f).dp
                    val playerYOffset = (250f + jumpAnimY.value).dp

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(x = playerXOffset, y = playerYOffset),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Sliding crouching or Jump indicator
                            if (isSliding) {
                                Text("💨", fontSize = 18.sp)
                                Surface(
                                    color = Color(0xFF00E5FF),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.size(36.dp, 20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("🛹", fontSize = 14.sp)
                                    }
                                }
                            } else {
                                if (isJumping) {
                                    Text("⚡", fontSize = 14.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF22222E))
                                        .border(2.dp, if (magnetSecondsLeft > 0) Color(0xFFFFD700) else Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.subway_runner_hero),
                                        contentDescription = "Runner Hero",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Text(if (isJumping) "👟 AIR" else "🛹", fontSize = 14.sp)
                            }
                        }
                    }

                    // Active powerup badge
                    if (magnetSecondsLeft > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E1E28),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldCoinYellow),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🧲", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("MAGNET: ${magnetSecondsLeft}s", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldCoinYellow)
                            }
                        }
                    }
                }

                // Start Overlay or Game Over Modal
                if (!isPlaying && !isGameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xCC08080C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color(0xFF00E5FF), CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.subway_runner_hero),
                                    contentDescription = "Subway Surfer",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "READY TO SURF?",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Swipe or tap buttons: Left/Right to turn, UP to Jump, DOWN to Slide!",
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp,
                                color = Color(0xFFAAAAAA),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { isPlaying = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(44.dp).testTag("start_subway_run_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("START RUNNING", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                if (isGameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xDD120808)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("💥 CRASHED!", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFFFF5252))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Distance: ${distanceMeters}m", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Text("Coins Collected: $coinsCollectedThisRun 🪙", fontSize = 12.sp, color = GoldCoinYellow)
                            Text("Best Record: ${bestDistance}m", fontSize = 11.sp, color = Color(0xFFAAAAAA))

                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { isPlaying = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("PLAY AGAIN", fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = onBack,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("EXIT", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tactile On-Screen Game Controls (Jump, Slide, Left, Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Turn
                IconButton(
                    onClick = { if (isPlaying) switchLane(currentLane - 1) },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1E28))
                        .border(1.dp, Color(0xFF38384A), RoundedCornerShape(12.dp))
                        .testTag("runner_left_button")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Turn Left", tint = Color.White)
                }

                // Center Jump & Slide buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { if (isPlaying) performJump() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E3E)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(44.dp).testTag("runner_jump_button")
                    ) {
                        Text("⬆ JUMP", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    }

                    Button(
                        onClick = { if (isPlaying) performSlide() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E3E)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(44.dp).testTag("runner_slide_button")
                    ) {
                        Text("⬇ SLIDE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    }
                }

                // Right Turn
                IconButton(
                    onClick = { if (isPlaying) switchLane(currentLane + 1) },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1E28))
                        .border(1.dp, Color(0xFF38384A), RoundedCornerShape(12.dp))
                        .testTag("runner_right_button")
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Turn Right", tint = Color.White)
                }
            }
        }
    }
}
