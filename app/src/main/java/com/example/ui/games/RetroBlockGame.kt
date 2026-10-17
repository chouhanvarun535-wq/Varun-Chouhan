package com.example.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppState
import com.example.ui.theme.GoldCoinYellow
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

// Tetromino definitions
enum class TetrominoShape(val blocks: List<Pair<Int, Int>>, val color: Color) {
    I(listOf(0 to 0, 1 to 0, 2 to 0, 3 to 0), Color(0xFF00E5FF)),
    O(listOf(0 to 0, 1 to 0, 0 to 1, 1 to 1), Color(0xFFFFD700)),
    T(listOf(1 to 0, 0 to 1, 1 to 1, 2 to 1), Color(0xFFBA68C8)),
    S(listOf(1 to 0, 2 to 0, 0 to 1, 1 to 1), Color(0xFF00E676)),
    Z(listOf(0 to 0, 1 to 0, 1 to 1, 2 to 1), Color(0xFFFF5252)),
    J(listOf(0 to 0, 0 to 1, 1 to 1, 2 to 1), Color(0xFF448AFF)),
    L(listOf(2 to 0, 0 to 1, 1 to 1, 2 to 1), Color(0xFFFF9100))
}

data class ActivePiece(
    val shape: TetrominoShape,
    var x: Int,
    var y: Int,
    var blocks: List<Pair<Int, Int>>
)

@Composable
fun RetroBlockGame(
    state: AppState,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val cols = 10
    val rows = 18

    // Board grid representation (0 = empty, Color = occupied)
    val board = remember { mutableStateListOf<Color?>().apply { repeat(cols * rows) { add(null) } } }

    var score by remember { mutableIntStateOf(0) }
    var linesClearedTotal by remember { mutableIntStateOf(0) }
    var coinsWonThisSession by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }

    var currentPiece by remember { mutableStateOf<ActivePiece?>(null) }
    var nextPieceShape by remember { mutableStateOf(TetrominoShape.values().random()) }
    var holdPieceShape by remember { mutableStateOf<TetrominoShape?>(null) }
    var canHoldThisTurn by remember { mutableStateOf(true) }

    fun getIndex(x: Int, y: Int) = y * cols + x

    fun isValidPosition(piece: ActivePiece, offsetX: Int = 0, offsetY: Int = 0, testBlocks: List<Pair<Int, Int>> = piece.blocks): Boolean {
        for ((bx, by) in testBlocks) {
            val targetX = piece.x + bx + offsetX
            val targetY = piece.y + by + offsetY
            if (targetX < 0 || targetX >= cols || targetY >= rows) return false
            if (targetY >= 0 && board[getIndex(targetX, targetY)] != null) return false
        }
        return true
    }

    fun spawnNewPiece() {
        val shape = nextPieceShape
        nextPieceShape = TetrominoShape.values().random()
        val newPiece = ActivePiece(
            shape = shape,
            x = (cols / 2) - 2,
            y = 0,
            blocks = shape.blocks
        )
        if (!isValidPosition(newPiece)) {
            // Cannot spawn -> Game Over
            isGameOver = true
            isPlaying = false
            viewModel.onBlockPuzzleFinished(score, linesClearedTotal, coinsWonThisSession)
        } else {
            currentPiece = newPiece
            canHoldThisTurn = true
        }
    }

    fun lockCurrentPiece() {
        val piece = currentPiece ?: return
        for ((bx, by) in piece.blocks) {
            val tx = piece.x + bx
            val ty = piece.y + by
            if (ty in 0 until rows && tx in 0 until cols) {
                board[getIndex(tx, ty)] = piece.shape.color
            }
        }
        viewModel.soundManager.playBeadVibration()

        // Check line clears
        var cleared = 0
        for (r in rows - 1 downTo 0) {
            var full = true
            for (c in 0 until cols) {
                if (board[getIndex(c, r)] == null) {
                    full = false
                    break
                }
            }
            if (full) {
                cleared++
                // Shift everything above down
                for (aboveR in r downTo 1) {
                    for (c in 0 until cols) {
                        board[getIndex(c, aboveR)] = board[getIndex(c, aboveR - 1)]
                    }
                }
                for (c in 0 until cols) {
                    board[getIndex(c, 0)] = null
                }
            }
        }

        if (cleared > 0) {
            linesClearedTotal += cleared
            val points = when (cleared) {
                1 -> 100
                2 -> 300
                3 -> 500
                else -> 800
            }
            score += points
            val coinsEarned = if (cleared >= 4) 4 else cleared
            coinsWonThisSession += coinsEarned
            viewModel.soundManager.playCoinWin()
        }

        currentPiece = null
        spawnNewPiece()
    }

    fun moveLeft() {
        val piece = currentPiece ?: return
        if (isValidPosition(piece, offsetX = -1)) {
            piece.x -= 1
            viewModel.soundManager.playButtonClickSound()
        }
    }

    fun moveRight() {
        val piece = currentPiece ?: return
        if (isValidPosition(piece, offsetX = 1)) {
            piece.x += 1
            viewModel.soundManager.playButtonClickSound()
        }
    }

    fun rotatePiece() {
        val piece = currentPiece ?: return
        // Rotate 90 degrees: (x, y) -> (-y, x)
        val rotated = piece.blocks.map { (bx, by) ->
            Pair(2 - by, bx)
        }
        if (isValidPosition(piece, testBlocks = rotated)) {
            piece.blocks = rotated
            viewModel.soundManager.playButtonClickSound()
        }
    }

    fun softDrop() {
        val piece = currentPiece ?: return
        if (isValidPosition(piece, offsetY = 1)) {
            piece.y += 1
            score += 1
        } else {
            lockCurrentPiece()
        }
    }

    fun hardDrop() {
        val piece = currentPiece ?: return
        var dropDistance = 0
        while (isValidPosition(piece, offsetY = dropDistance + 1)) {
            dropDistance++
        }
        piece.y += dropDistance
        score += dropDistance * 2
        lockCurrentPiece()
    }

    fun holdCurrentPiece() {
        if (!canHoldThisTurn) return
        val current = currentPiece ?: return
        val currentShape = current.shape
        if (holdPieceShape == null) {
            holdPieceShape = currentShape
            currentPiece = null
            spawnNewPiece()
        } else {
            val toSwap = holdPieceShape!!
            holdPieceShape = currentShape
            currentPiece = ActivePiece(toSwap, (cols / 2) - 2, 0, toSwap.blocks)
        }
        canHoldThisTurn = false
        viewModel.soundManager.playButtonClickSound()
    }

    fun startNewGame() {
        board.clear()
        repeat(cols * rows) { board.add(null) }
        score = 0
        linesClearedTotal = 0
        coinsWonThisSession = 0
        isGameOver = false
        isPaused = false
        currentPiece = null
        holdPieceShape = null
        nextPieceShape = TetrominoShape.values().random()
        isPlaying = true
        spawnNewPiece()
    }

    // Auto-fall ticker
    LaunchedEffect(isPlaying, isPaused, isGameOver) {
        if (!isPlaying || isPaused || isGameOver) return@LaunchedEffect
        while (isPlaying && !isPaused && !isGameOver) {
            val speed = (650L - (linesClearedTotal * 20L)).coerceAtLeast(180L)
            delay(speed)
            softDrop()
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F14)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E38)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("retro_block_game_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🧱", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "RETRO BLOCK STACKER",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Clear Lines • Earn Coins • Build Combos",
                            fontSize = 10.sp,
                            color = Color(0xFFAAAAAA)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🪙", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$coinsWonThisSession",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = GoldCoinYellow
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Score: $score",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Playfield Row: Hold Box | Grid | Next Box & Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Top
            ) {
                // Left Column: Hold Box & Lines
                Column(
                    modifier = Modifier.width(60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("HOLD", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFFAAAAAA))
                    Surface(
                        color = Color(0xFF14141E),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2D2D3E)),
                        modifier = Modifier
                            .size(54.dp)
                            .clickable { if (isPlaying) holdCurrentPiece() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            holdPieceShape?.let { shape ->
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(shape.color)
                                )
                            } ?: Text("—", color = Color(0xFF555566), fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("LINES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFAAAAAA))
                    Text("$linesClearedTotal", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.White)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Center Grid (10 cols x 18 rows)
                Box(
                    modifier = Modifier
                        .size(180.dp, 324.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF09090D))
                        .border(1.5.dp, Color(0xFF2C2C3A), RoundedCornerShape(8.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cellW = size.width / cols
                        val cellH = size.height / rows

                        // Subtle grid lines
                        for (c in 1 until cols) {
                            drawLine(
                                color = Color(0xFF161622),
                                start = Offset(c * cellW, 0f),
                                end = Offset(c * cellW, size.height),
                                strokeWidth = 1f
                            )
                        }
                        for (r in 1 until rows) {
                            drawLine(
                                color = Color(0xFF161622),
                                start = Offset(0f, r * cellH),
                                end = Offset(size.width, r * cellH),
                                strokeWidth = 1f
                            )
                        }

                        // Draw locked blocks
                        for (y in 0 until rows) {
                            for (x in 0 until cols) {
                                val blockColor = board[y * cols + x]
                                if (blockColor != null) {
                                    drawRect(
                                        color = blockColor,
                                        topLeft = Offset(x * cellW + 1f, y * cellH + 1f),
                                        size = Size(cellW - 2f, cellH - 2f)
                                    )
                                }
                            }
                        }

                        // Draw falling active piece
                        currentPiece?.let { piece ->
                            for ((bx, by) in piece.blocks) {
                                val px = piece.x + bx
                                val py = piece.y + by
                                if (py in 0 until rows && px in 0 until cols) {
                                    drawRect(
                                        color = piece.shape.color,
                                        topLeft = Offset(px * cellW + 1f, py * cellH + 1f),
                                        size = Size(cellW - 2f, cellH - 2f)
                                    )
                                }
                            }
                        }
                    }

                    // Start/Game Over Overlay
                    if (!isPlaying && !isGameOver) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xCC000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = { startNewGame() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("start_block_game_button")
                            ) {
                                Text("PLAY BLOCKS", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    if (isGameOver) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xDD1A0A0A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("GAME OVER", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFFFF5252))
                                Text("Score: $score", fontSize = 11.sp, color = Color.White)
                                Text("Coins: +$coinsWonThisSession 🪙", fontSize = 11.sp, color = GoldCoinYellow)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { startNewGame() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("AGAIN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right Column: Next Piece Preview & Pause
                Column(
                    modifier = Modifier.width(60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("NEXT", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFFAAAAAA))
                    Surface(
                        color = Color(0xFF14141E),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2D2D3E)),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(nextPieceShape.color)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    IconButton(
                        onClick = { if (isPlaying) isPaused = !isPaused },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Control Buttons: Left, Rotate, Soft Drop, Hard Drop, Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (isPlaying && !isPaused) moveLeft() },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1A1A26))
                        .border(1.dp, Color(0xFF333346), RoundedCornerShape(10.dp))
                        .testTag("block_left_button")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Move Left", tint = Color.White)
                }

                IconButton(
                    onClick = { if (isPlaying && !isPaused) rotatePiece() },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF262638))
                        .border(1.dp, Color(0xFF44445E), RoundedCornerShape(10.dp))
                        .testTag("block_rotate_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Rotate", tint = Color.White)
                }

                IconButton(
                    onClick = { if (isPlaying && !isPaused) softDrop() },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1A1A26))
                        .border(1.dp, Color(0xFF333346), RoundedCornerShape(10.dp))
                        .testTag("block_soft_drop_button")
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Soft Drop", tint = Color.White)
                }

                IconButton(
                    onClick = { if (isPlaying && !isPaused) hardDrop() },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF332020))
                        .border(1.dp, Color(0xFF663333), RoundedCornerShape(10.dp))
                        .testTag("block_hard_drop_button")
                ) {
                    Text("⚡", fontSize = 18.sp)
                }

                IconButton(
                    onClick = { if (isPlaying && !isPaused) moveRight() },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1A1A26))
                        .border(1.dp, Color(0xFF333346), RoundedCornerShape(10.dp))
                        .testTag("block_right_button")
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Move Right", tint = Color.White)
                }
            }
        }
    }
}
