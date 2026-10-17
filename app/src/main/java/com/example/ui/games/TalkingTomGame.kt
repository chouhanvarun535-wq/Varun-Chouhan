package com.example.ui.games

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.GoldCoinYellow
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

enum class TomMood {
    NORMAL,
    HAPPY_PURR,
    LAUGHING,
    DIZZY,
    FIRE_BREATHING,
    EATING,
    FRIGHTENED,
    DANCING,
    SLEEPING
}

@Composable
fun TalkingTomGame(
    state: AppState,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Text To Speech with high pitch squeaky cat voice
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
            }
        }
        ttsEngine = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    // Tom Pet Attributes
    var mood by remember { mutableStateOf(TomMood.NORMAL) }
    var hungerMeter by remember { mutableIntStateOf(75) } // 0 to 100
    var happinessMeter by remember { mutableIntStateOf(85) } // 0 to 100
    var isSleeping by remember { mutableStateOf(false) }
    var speechBubbleText by remember { mutableStateOf<String?>("Meow! Tap me, feed me or talk to me!") }
    var customInputText by remember { mutableStateOf("") }
    var showTalkInput by remember { mutableStateOf(false) }

    // Animated transforms for Tom
    val tomScale = remember { Animatable(1f) }
    val tomRotation = remember { Animatable(0f) }
    val tomOffsetY = remember { Animatable(0f) }

    // Function to speak in squeaky Tom cat voice
    fun speakTom(text: String) {
        speechBubbleText = text
        if (isTtsReady && ttsEngine != null) {
            try {
                ttsEngine?.setPitch(1.85f) // High-pitched squeaky cartoon cat pitch
                ttsEngine?.setSpeechRate(1.22f) // Lively fast cartoon pace
                ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TOM_VOICE")
            } catch (_: Exception) {}
        }
        viewModel.soundManager.playBeadVibration()

        // Tom talking mouth & bounce animation
        coroutineScope.launch {
            repeat(3) {
                tomOffsetY.animateTo(-10f, tween(100))
                tomOffsetY.animateTo(0f, tween(100))
            }
        }
    }

    // Trigger funny reactions
    fun triggerReaction(newMood: TomMood, soundEffect: () -> Unit, speechText: String, durationMs: Long = 1800L) {
        if (isSleeping && newMood != TomMood.SLEEPING) {
            isSleeping = false
        }
        mood = newMood
        speechBubbleText = speechText
        soundEffect()

        coroutineScope.launch {
            when (newMood) {
                TomMood.DIZZY -> {
                    tomRotation.animateTo(20f, tween(150))
                    tomRotation.animateTo(-20f, tween(150))
                    tomRotation.animateTo(0f, tween(150))
                }
                TomMood.LAUGHING -> {
                    repeat(4) {
                        tomScale.animateTo(1.08f, tween(90))
                        tomScale.animateTo(1f, tween(90))
                    }
                }
                TomMood.FRIGHTENED -> {
                    tomOffsetY.animateTo(-45f, tween(120))
                    tomOffsetY.animateTo(0f, tween(200))
                }
                TomMood.HAPPY_PURR -> {
                    repeat(3) {
                        tomScale.animateTo(1.05f, tween(160))
                        tomScale.animateTo(1f, tween(160))
                    }
                }
                TomMood.DANCING -> {
                    repeat(5) {
                        tomRotation.animateTo(12f, tween(120))
                        tomRotation.animateTo(-12f, tween(120))
                    }
                    tomRotation.animateTo(0f, tween(100))
                }
                else -> {}
            }
            delay(durationMs)
            if (mood == newMood && !isSleeping) {
                mood = TomMood.NORMAL
            }
        }
    }

    // Check coin reward when full care
    fun checkCareReward() {
        if (hungerMeter >= 95 && happinessMeter >= 95) {
            viewModel.onTalkingTomReward(3, "Tom is super happy and well-fed!")
            speechBubbleText = "Thank you! Here are +3 Gold Coins for you! 🪙"
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSleeping) Color(0xFF07070B) else Color(0xFF101017)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E3E)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("talking_tom_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🐱", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "TALKING TOM CAT",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Voice Mimic • Petting • Feeding • Pranks",
                            fontSize = 10.sp,
                            color = Color(0xFFAAAAAA)
                        )
                    }
                }

                // Sleep Light Switch Button
                IconButton(
                    onClick = {
                        isSleeping = !isSleeping
                        if (isSleeping) {
                            mood = TomMood.SLEEPING
                            speechBubbleText = "Zzz... Good night! Snoozing..."
                            viewModel.soundManager.playButtonClickSound()
                        } else {
                            mood = TomMood.NORMAL
                            speechBubbleText = "Yawn! Tom is awake! Good morning!"
                            speakTom("Good morning, let's play!")
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isSleeping) Color(0xFF1A1A2A) else Color(0xFFFFD700))
                ) {
                    Text(if (isSleeping) "🌙" else "💡", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hunger & Happiness Progress Bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("HUNGER", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFAAAAAA))
                        Text("$hungerMeter%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    LinearProgressIndicator(
                        progress = { hungerMeter / 100f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF00E676),
                        trackColor = Color(0xFF222230),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("HAPPINESS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFAAAAAA))
                        Text("$happinessMeter%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    LinearProgressIndicator(
                        progress = { happinessMeter / 100f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFFFF4081),
                        trackColor = Color(0xFF222230),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tom Character Stage Room
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = if (isSleeping)
                                listOf(Color(0xFF0A0A12), Color(0xFF050508))
                            else
                                listOf(Color(0xFF181826), Color(0xFF0E0E18))
                        )
                    )
                    .border(1.dp, Color(0xFF2A2A3C), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Speech Bubble
                speechBubbleText?.let { bubble ->
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp, start = 16.dp, end = 16.dp)
                    ) {
                        Text(
                            text = bubble,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Tom Avatar Sprite & Touch Targets
                Box(
                    modifier = Modifier
                        .offset(y = tomOffsetY.value.dp)
                        .scale(tomScale.value)
                        .rotate(tomRotation.value)
                        .size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.talking_tom_avatar),
                        contentDescription = "Talking Tom",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Mood Overlays
                    when (mood) {
                        TomMood.DIZZY -> {
                            Text("💫😵💫", fontSize = 28.sp, modifier = Modifier.align(Alignment.TopCenter))
                        }
                        TomMood.HAPPY_PURR -> {
                            Text("❤️😻❤️", fontSize = 26.sp, modifier = Modifier.align(Alignment.TopCenter))
                        }
                        TomMood.LAUGHING -> {
                            Text("😂😹", fontSize = 28.sp, modifier = Modifier.align(Alignment.TopCenter))
                        }
                        TomMood.FIRE_BREATHING -> {
                            Text("🔥🌶️🔥", fontSize = 34.sp, modifier = Modifier.align(Alignment.Center))
                        }
                        TomMood.FRIGHTENED -> {
                            Text("😱⚡", fontSize = 28.sp, modifier = Modifier.align(Alignment.TopCenter))
                        }
                        TomMood.DANCING -> {
                            Text("🪩🕺🎵", fontSize = 24.sp, modifier = Modifier.align(Alignment.TopCenter))
                        }
                        TomMood.SLEEPING -> {
                            Text("💤 Zzz...", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.align(Alignment.TopCenter))
                        }
                        else -> {}
                    }

                    // Interactive Touch Zones on Tom's Body
                    // 1. Head Zone (Tap for dizzy spin)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .size(100.dp, 60.dp)
                            .clickable {
                                triggerReaction(
                                    TomMood.DIZZY,
                                    { viewModel.soundManager.playBeadVibration() },
                                    "Ouch! My head is spinning! 💫"
                                )
                                happinessMeter = (happinessMeter - 5).coerceAtLeast(0)
                            }
                            .testTag("tom_head_touch")
                    )

                    // 2. Belly Zone (Tap for laugh, pet for purr)
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(90.dp, 70.dp)
                            .clickable {
                                triggerReaction(
                                    TomMood.HAPPY_PURR,
                                    { viewModel.soundManager.playCoinWin() },
                                    "Purrrr... I love when you pet my belly! ❤️"
                                )
                                happinessMeter = (happinessMeter + 10).coerceAtMost(100)
                                checkCareReward()
                            }
                            .testTag("tom_belly_touch")
                    )

                    // 3. Feet Zone (Tap to trip/jump)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .size(90.dp, 40.dp)
                            .clickable {
                                triggerReaction(
                                    TomMood.LAUGHING,
                                    { viewModel.soundManager.playButtonClickSound() },
                                    "Hehehe! That tickles my paws! 🐾"
                                )
                                happinessMeter = (happinessMeter + 5).coerceAtMost(100)
                            }
                            .testTag("tom_feet_touch")
                    )
                }

                // Voice Recording / Talk Indicator
                if (showTalkInput) {
                    Surface(
                        color = Color(0xEE161622),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3E3E54)),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customInputText,
                                onValueChange = { customInputText = it },
                                placeholder = { Text("Type something for Tom to say...", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f).height(48.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = Color.White),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    if (customInputText.isNotBlank()) {
                                        speakTom(customInputText)
                                        customInputText = ""
                                        showTalkInput = false
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                                modifier = Modifier.height(44.dp).testTag("tom_speak_submit")
                            ) {
                                Text("SAY", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Tabs / Interaction Bar
            // 1. Voice Mimic Quick Speech Phrases
            Text("TALK WITH TOM (VOICE MIMIC)", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFAAAAAA))
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { speakTom("Namaste! Kaise ho aap?") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(34.dp).testTag("tom_phrase_1")
                ) {
                    Text("Namaste!", fontSize = 10.sp, maxLines = 1)
                }
                OutlinedButton(
                    onClick = { speakTom("Give me cold milk please, meow!") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(34.dp).testTag("tom_phrase_2")
                ) {
                    Text("Give Milk 🥛", fontSize = 10.sp, maxLines = 1)
                }
                OutlinedButton(
                    onClick = { speakTom("Apex Life is the best app in the world!") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(34.dp).testTag("tom_phrase_3")
                ) {
                    Text("Apex Life 👑", fontSize = 10.sp, maxLines = 1)
                }
                IconButton(
                    onClick = { showTalkInput = !showTalkInput },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF262638))
                        .testTag("tom_toggle_custom_talk")
                ) {
                    Icon(Icons.Default.Keyboard, contentDescription = "Custom Talk", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Feeding Kitchen (Milk, Pizza, Hot Chili, Cake, Ice Cream)
            Text("FEED TOM SNACKS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFAAAAAA))
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Milk
                Button(
                    onClick = {
                        triggerReaction(
                            TomMood.HAPPY_PURR,
                            { viewModel.soundManager.playCoinWin() },
                            "Slurp slurp! Fresh cold milk is so tasty! 🥛"
                        )
                        hungerMeter = (hungerMeter + 20).coerceAtMost(100)
                        happinessMeter = (happinessMeter + 10).coerceAtMost(100)
                        checkCareReward()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E2C)),
                    modifier = Modifier.height(40.dp).testTag("feed_milk_button")
                ) {
                    Text("🥛 Milk", fontSize = 11.sp, color = Color.White)
                }

                // Pizza
                Button(
                    onClick = {
                        triggerReaction(
                            TomMood.EATING,
                            { viewModel.soundManager.playCoinWin() },
                            "Nom nom nom! Delicious cheesy pizza! 🍕"
                        )
                        hungerMeter = (hungerMeter + 25).coerceAtMost(100)
                        happinessMeter = (happinessMeter + 15).coerceAtMost(100)
                        checkCareReward()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E2C)),
                    modifier = Modifier.height(40.dp).testTag("feed_pizza_button")
                ) {
                    Text("🍕 Pizza", fontSize = 11.sp, color = Color.White)
                }

                // Red Chili (Fire Breathing)
                Button(
                    onClick = {
                        triggerReaction(
                            TomMood.FIRE_BREATHING,
                            { viewModel.soundManager.playLevelUp() },
                            "AHHH! HOT CHILI PEPPER!! FIREEEE! 🔥🌶️"
                        )
                        hungerMeter = (hungerMeter + 10).coerceAtMost(100)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF331414)),
                    modifier = Modifier.height(40.dp).testTag("feed_chili_button")
                ) {
                    Text("🌶️ Chili", fontSize = 11.sp, color = Color(0xFFFF5252))
                }

                // Cake
                Button(
                    onClick = {
                        triggerReaction(
                            TomMood.HAPPY_PURR,
                            { viewModel.soundManager.playCoinWin() },
                            "Yay! Sweet birthday cake! Party time! 🍰"
                        )
                        hungerMeter = (hungerMeter + 20).coerceAtMost(100)
                        happinessMeter = (happinessMeter + 20).coerceAtMost(100)
                        checkCareReward()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E2C)),
                    modifier = Modifier.height(40.dp).testTag("feed_cake_button")
                ) {
                    Text("🍰 Cake", fontSize = 11.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Funny Gags & Pranks
            Text("FUNNY PRANKS & ACTIONS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFAAAAAA))
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Fart gag
                Button(
                    onClick = {
                        triggerReaction(
                            TomMood.LAUGHING,
                            { viewModel.soundManager.playBeadVibration() },
                            "Pfffft! 💨 Who did that?! Haha eww!"
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22222E)),
                    modifier = Modifier.weight(1f).height(38.dp).testTag("tom_prank_fart")
                ) {
                    Text("💨 Fart", fontSize = 11.sp, color = Color.White)
                }

                // Paper Bag Pop
                Button(
                    onClick = {
                        triggerReaction(
                            TomMood.FRIGHTENED,
                            { viewModel.soundManager.playLevelUp() },
                            "POP!! 🎈 Aah! You scared me to the moon!"
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22222E)),
                    modifier = Modifier.weight(1f).height(38.dp).testTag("tom_prank_pop")
                ) {
                    Text("🎈 Pop Bag", fontSize = 11.sp, color = Color.White)
                }

                // Disco Dance
                Button(
                    onClick = {
                        triggerReaction(
                            TomMood.DANCING,
                            { viewModel.soundManager.playLevelUp() },
                            "Let's groove! Dance party time! 🪩🕺"
                        )
                        happinessMeter = (happinessMeter + 25).coerceAtMost(100)
                        checkCareReward()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C1E3C)),
                    modifier = Modifier.weight(1f).height(38.dp).testTag("tom_disco_dance")
                ) {
                    Text("🪩 Disco", fontSize = 11.sp, color = Color(0xFFE040FB))
                }
            }
        }
    }
}
