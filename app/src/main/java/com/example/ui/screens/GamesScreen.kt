package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppState
import com.example.model.MiniGameType
import com.example.model.QuizQuestion
import com.example.model.SAMPLE_QUIZ_QUESTIONS
import com.example.ui.games.RetroBlockGame
import com.example.ui.games.SubwayRunnerGame
import com.example.ui.games.TalkingTomGame
import com.example.ui.theme.GoldCoinYellow
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GamesScreen(
    state: AppState,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedGame by remember { mutableStateOf<MiniGameType?>(null) }

    // Sub-game: Spin Wheel State
    val spinRotation = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var spinResultText by remember { mutableStateOf<String?>(null) }

    // Sub-game: Scratch Card State
    var isScratched by remember { mutableStateOf(false) }
    var scratchCoinsWon by remember { mutableIntStateOf(2) }

    // Sub-game: Quiz State
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var quizSelectedOption by remember { mutableStateOf<Int?>(null) }
    var quizAnswerFeedback by remember { mutableStateOf<String?>(null) }

    // Sub-game: Dice Roll State
    var diceNumber by remember { mutableIntStateOf(4) }
    var isRollingDice by remember { mutableStateOf(false) }
    var diceDuelFeedback by remember { mutableStateOf<String?>(null) }

    // Sub-game: Memory Match State
    val cardSymbols = remember { listOf("👑", "💎", "⚡", "🦁", "👑", "💎", "⚡", "🦁").shuffled() }
    val revealedCards = remember { mutableStateListOf(false, false, false, false, false, false, false, false) }
    val matchedCards = remember { mutableStateListOf(false, false, false, false, false, false, false, false) }
    var firstFlippedIndex by remember { mutableStateOf<Int?>(null) }
    var memoryGameOver by remember { mutableStateOf(false) }

    // Sub-game: Speed Reflex State
    var reflexSecondsLeft by remember { mutableIntStateOf(10) }
    var isReflexActive by remember { mutableStateOf(false) }
    var reflexScore by remember { mutableIntStateOf(0) }
    var coinOffsetX by remember { mutableFloatStateOf(0.5f) }
    var coinOffsetY by remember { mutableFloatStateOf(0.5f) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 64.dp)
    ) {
        // Coin Wallet & Header Card
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
                                text = "GAME ARENA (9 GAMES)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Win coins to unlock premium life modes! Loss reduces coins.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(text = "🎮", fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🪙", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${state.coins} COINS",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GoldCoinYellow
                                )
                                Text(
                                    text = "Won: ${state.gamesWon} | Lost: ${state.gamesLost}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Watch Rewarded Ad button for instant coin
                        Button(
                            onClick = {
                                (context as? Activity)?.let { act ->
                                    viewModel.watchRewardedAdForCoin(act)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(40.dp).testTag("watch_rewarded_video_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+1 Free Coin", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Active Game Container or 6 Games Grid
        if (selectedGame != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedGame!!.title.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedButton(
                        onClick = { selectedGame = null },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp).testTag("close_game_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("All Games", fontSize = 11.sp)
                    }
                }
            }

            // BLOCKBUSTER 1: SUBWAY TRACK RUNNER
            if (selectedGame == MiniGameType.SUBWAY_SURFER) {
                item {
                    SubwayRunnerGame(
                        state = state,
                        viewModel = viewModel,
                        onBack = { selectedGame = null }
                    )
                }
            }

            // BLOCKBUSTER 2: TALKING TOM CAT
            if (selectedGame == MiniGameType.TALKING_TOM) {
                item {
                    TalkingTomGame(
                        state = state,
                        viewModel = viewModel,
                        onBack = { selectedGame = null }
                    )
                }
            }

            // BLOCKBUSTER 3: RETRO BLOCK STACKER
            if (selectedGame == MiniGameType.BLOCK_PUZZLE) {
                item {
                    RetroBlockGame(
                        state = state,
                        viewModel = viewModel,
                        onBack = { selectedGame = null }
                    )
                }
            }

            // GAME 4: SPIN THE WHEEL
            if (selectedGame == MiniGameType.SPIN_WHEEL) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("SPIN THE LUCKY COIN WHEEL", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(14.dp))

                            // Wheel graphic with pointer
                            Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                                Canvas(
                                    modifier = Modifier
                                        .size(190.dp)
                                        .rotate(spinRotation.value)
                                ) {
                                    val radius = size.width / 2f
                                    val center = Offset(radius, radius)
                                    val segments = 6
                                    val colors = listOf(
                                        Color(0xFFFFD700), Color(0xFF00E676), Color(0xFFFF5252),
                                        Color(0xFF00E5FF), Color(0xFFFF9800), Color(0xFF9C27B0)
                                    )
                                    for (i in 0 until segments) {
                                        val startAngle = i * (360f / segments)
                                        drawArc(
                                            color = colors[i % colors.size],
                                            startAngle = startAngle,
                                            sweepAngle = 360f / segments,
                                            useCenter = true
                                        )
                                    }
                                    drawCircle(color = Color.Black, radius = 22.dp.toPx(), center = center)
                                }
                                // Center Wheel icon
                                Text("🪙", fontSize = 22.sp)

                                // Top Pointer Indicator
                                Text(
                                    text = "▼",
                                    fontSize = 20.sp,
                                    color = Color.White,
                                    modifier = Modifier.align(Alignment.TopCenter)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (spinResultText != null) {
                                Text(
                                    text = spinResultText!!,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = GoldCoinYellow
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            Button(
                                onClick = {
                                    if (!isSpinning) {
                                        isSpinning = true
                                        spinResultText = null
                                        coroutineScope.launch {
                                            val rewards = listOf(1, 2, 0, 3, 1, 5)
                                            val rewardIndex = Random.nextInt(rewards.size)
                                            val targetReward = rewards[rewardIndex]
                                            val fullRotations = 5 * 360f
                                            val finalAngle = fullRotations + (rewardIndex * 60f)

                                            spinRotation.animateTo(
                                                targetValue = spinRotation.value + finalAngle,
                                                animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing)
                                            )
                                            isSpinning = false
                                            viewModel.onSpinWheelResult(targetReward)
                                            spinResultText = if (targetReward > 0) "🎉 Jackpot! You won +$targetReward Coins!" else "Better luck next time! (0 Coins)"
                                        }
                                    }
                                },
                                enabled = !isSpinning,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("spin_wheel_button")
                            ) {
                                Text(if (isSpinning) "SPINNING..." else "SPIN FOR COINS", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // GAME 2: SCRATCH CARD
            if (selectedGame == MiniGameType.SCRATCH_CARD) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("SCRATCH & REVEAL COIN CARD", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        if (!isScratched) {
                                            isScratched = true
                                            viewModel.onScratchCardRevealed(scratchCoinsWon)
                                        }
                                    }
                                    .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                    .testTag("scratch_card_surface"),
                                color = if (isScratched) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (!isScratched) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("🎫", fontSize = 36.sp)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("TAP TO SCRATCH & REVEAL", fontWeight = FontWeight.Black, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("🎉", fontSize = 36.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("YOU WON +$scratchCoinsWon COINS & +15 XP!", fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (isScratched) {
                                Button(
                                    onClick = {
                                        isScratched = false
                                        scratchCoinsWon = Random.nextInt(1, 4)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("new_scratch_card_button")
                                ) {
                                    Text("Scratch Another Card", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // GAME 3: KNOWLEDGE QUIZ
            if (selectedGame == MiniGameType.QUIZ_TRIVIA) {
                item {
                    val q = SAMPLE_QUIZ_QUESTIONS[currentQuestionIndex % SAMPLE_QUIZ_QUESTIONS.size]
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("QUESTION ${currentQuestionIndex + 1} / ${SAMPLE_QUIZ_QUESTIONS.size}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("+2 Coins | -1 on Wrong", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldCoinYellow)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(q.question, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp)

                            Spacer(modifier = Modifier.height(14.dp))

                            q.options.forEachIndexed { idx, opt ->
                                val isChosen = quizSelectedOption == idx
                                val isCorrect = idx == q.correctIndex
                                val bgColor = when {
                                    quizSelectedOption == null -> MaterialTheme.colorScheme.surfaceVariant
                                    isCorrect -> Color(0xFF1B5E20)
                                    isChosen -> Color(0xFFB71C1C)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable(enabled = quizSelectedOption == null) {
                                            quizSelectedOption = idx
                                            val win = viewModel.answerQuiz(q, idx)
                                            quizAnswerFeedback = if (win) "✅ Correct! +2 Coins earned!" else "❌ Wrong answer! -1 Coin lost."
                                        }
                                        .testTag("quiz_option_$idx"),
                                    color = bgColor
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text("${'A' + idx}. ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(opt, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }

                            if (quizAnswerFeedback != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(quizAnswerFeedback!!, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(q.explanation, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        quizSelectedOption = null
                                        quizAnswerFeedback = null
                                        currentQuestionIndex = (currentQuestionIndex + 1) % SAMPLE_QUIZ_QUESTIONS.size
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("next_quiz_button")
                                ) {
                                    Text("Next Question", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // GAME 4: HIGH / LOW DICE ROLL
            if (selectedGame == MiniGameType.DICE_ROLL) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("DICE DUEL (HIGH OR LOW)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Guess whether roll will be Low (1-3) or High (4-6). Wager: 1 Coin", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(16.dp))

                            // Dice Face Display
                            Surface(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val diceEmoji = when (diceNumber) {
                                        1 -> "⚀"
                                        2 -> "⚁"
                                        3 -> "⚂"
                                        4 -> "⚃"
                                        5 -> "⚄"
                                        else -> "⚅"
                                    }
                                    Text(text = diceEmoji, fontSize = 48.sp)
                                }
                            }

                            if (diceDuelFeedback != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(diceDuelFeedback!!, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (state.coins >= 1 && !isRollingDice) {
                                            isRollingDice = true
                                            coroutineScope.launch {
                                                repeat(6) {
                                                    diceNumber = Random.nextInt(1, 7)
                                                    delay(60L)
                                                }
                                                val finalRoll = Random.nextInt(1, 7)
                                                diceNumber = finalRoll
                                                isRollingDice = false
                                                val win = viewModel.playDiceDuel(1, guessHigh = false, finalRoll)
                                                diceDuelFeedback = if (win) "🎉 Low Won! Rolled $finalRoll (+1 Coin)" else "❌ Lost! Rolled $finalRoll (-1 Coin)"
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(46.dp).testTag("dice_bet_low_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    enabled = state.coins >= 1 && !isRollingDice
                                ) {
                                    Text("Bet Low (1-3)", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        if (state.coins >= 1 && !isRollingDice) {
                                            isRollingDice = true
                                            coroutineScope.launch {
                                                repeat(6) {
                                                    diceNumber = Random.nextInt(1, 7)
                                                    delay(60L)
                                                }
                                                val finalRoll = Random.nextInt(1, 7)
                                                diceNumber = finalRoll
                                                isRollingDice = false
                                                val win = viewModel.playDiceDuel(1, guessHigh = true, finalRoll)
                                                diceDuelFeedback = if (win) "🎉 High Won! Rolled $finalRoll (+1 Coin)" else "❌ Lost! Rolled $finalRoll (-1 Coin)"
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(46.dp).testTag("dice_bet_high_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    enabled = state.coins >= 1 && !isRollingDice
                                ) {
                                    Text("Bet High (4-6)", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // GAME 5: MEMORY MATCH
            if (selectedGame == MiniGameType.MEMORY_CARDS) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("MEMORY CARD MATCH (MATCH ALL PAIRS)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Match all 4 pairs to win +3 Coins & +30 XP!", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(14.dp))

                            // 8 Cards Grid (2 rows of 4)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (i in 0..3) {
                                    val isFaceUp = revealedCards[i] || matchedCards[i]
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(60.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                if (!revealedCards[i] && !matchedCards[i] && firstFlippedIndex != i) {
                                                    revealedCards[i] = true
                                                    if (firstFlippedIndex == null) {
                                                        firstFlippedIndex = i
                                                    } else {
                                                        val prev = firstFlippedIndex!!
                                                        firstFlippedIndex = null
                                                        if (cardSymbols[prev] == cardSymbols[i]) {
                                                            matchedCards[prev] = true
                                                            matchedCards[i] = true
                                                            if (matchedCards.all { it }) {
                                                                memoryGameOver = true
                                                                viewModel.onMemoryGameFinished(4, 3)
                                                            }
                                                        } else {
                                                            coroutineScope.launch {
                                                                delay(600L)
                                                                revealedCards[prev] = false
                                                                revealedCards[i] = false
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                                        color = if (isFaceUp) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(if (isFaceUp) cardSymbols[i] else "❓", fontSize = 20.sp)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (i in 4..7) {
                                    val isFaceUp = revealedCards[i] || matchedCards[i]
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(60.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                if (!revealedCards[i] && !matchedCards[i] && firstFlippedIndex != i) {
                                                    revealedCards[i] = true
                                                    if (firstFlippedIndex == null) {
                                                        firstFlippedIndex = i
                                                    } else {
                                                        val prev = firstFlippedIndex!!
                                                        firstFlippedIndex = null
                                                        if (cardSymbols[prev] == cardSymbols[i]) {
                                                            matchedCards[prev] = true
                                                            matchedCards[i] = true
                                                            if (matchedCards.all { it }) {
                                                                memoryGameOver = true
                                                                viewModel.onMemoryGameFinished(4, 3)
                                                            }
                                                        } else {
                                                            coroutineScope.launch {
                                                                delay(600L)
                                                                revealedCards[prev] = false
                                                                revealedCards[i] = false
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                                        color = if (isFaceUp) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(if (isFaceUp) cardSymbols[i] else "❓", fontSize = 20.sp)
                                        }
                                    }
                                }
                            }

                            if (memoryGameOver) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text("🎉 Congratulations! All pairs matched! (+3 Coins)", fontWeight = FontWeight.Bold, color = GoldCoinYellow, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // GAME 6: SPEED COIN REFLEX
            if (selectedGame == MiniGameType.TAP_REFLEX) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("SPEED COIN REFLEX", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Tap the golden coin as many times as you can in 10s!", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("⏱️ Time Left: ${reflexSecondsLeft}s", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("🪙 Caught: $reflexScore", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GoldCoinYellow)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF161618))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isReflexActive) {
                                    // Pop-up coin
                                    Box(
                                        modifier = Modifier
                                            .offset(x = (coinOffsetX * 180 - 90).dp, y = (coinOffsetY * 120 - 60).dp)
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(GoldCoinYellow)
                                            .clickable {
                                                reflexScore += 1
                                                viewModel.soundManager.playBeadVibration()
                                                coinOffsetX = Random.nextFloat()
                                                coinOffsetY = Random.nextFloat()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🪙", fontSize = 24.sp)
                                    }
                                } else {
                                    Text(
                                        if (reflexSecondsLeft == 0) "Time's up! You caught $reflexScore coins!" else "Press START below to begin",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    isReflexActive = true
                                    reflexScore = 0
                                    reflexSecondsLeft = 10
                                    coroutineScope.launch {
                                        while (reflexSecondsLeft > 0) {
                                            delay(1000L)
                                            reflexSecondsLeft -= 1
                                        }
                                        isReflexActive = false
                                        viewModel.onSpeedCoinReflexFinished(reflexScore)
                                    }
                                },
                                enabled = !isReflexActive,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(44.dp).testTag("start_reflex_button")
                            ) {
                                Text(if (isReflexActive) "CATCH THE COINS!" else "START 10-SEC CHALLENGE", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // SHOW GRID OF 6 MINI GAMES
            item {
                Text(
                    text = "SELECT A GAME TO PLAY",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(MiniGameType.values().size) { idx ->
                val game = MiniGameType.values()[idx]
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedGame = game }
                        .testTag("game_item_${game.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (game) {
                            MiniGameType.SUBWAY_SURFER -> {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(10.dp))
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.subway_runner_hero),
                                        contentDescription = "Subway Surfer",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            MiniGameType.TALKING_TOM -> {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(10.dp))
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.talking_tom_avatar),
                                        contentDescription = "Talking Tom",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF1E1E28)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(game.iconEmoji, fontSize = 24.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(game.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (game == MiniGameType.SUBWAY_SURFER || game == MiniGameType.TALKING_TOM || game == MiniGameType.BLOCK_PUZZLE) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFFFF3D00).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFFF3D00))
                                    ) {
                                        Text(
                                            text = "FEATURED",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFFFF3D00),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(game.subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
