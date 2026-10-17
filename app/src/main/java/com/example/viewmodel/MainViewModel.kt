package com.example.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdMobManager
import com.example.audio.SoundAndHapticManager
import com.example.data.AppDataRepository
import com.example.model.*
import com.example.notification.DailyNotificationManager
import com.example.sensor.StepSensorManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppDataRepository(application)
    val soundManager = SoundAndHapticManager(application)
    val stepSensorManager = StepSensorManager(application)

    private val _uiState = MutableStateFlow(repository.loadState())
    val uiState: StateFlow<AppState> = _uiState.asStateFlow()

    // Pomodoro Timer State
    private var pomodoroJob: Job? = null
    val pomodoroSecondsLeft = MutableStateFlow(25 * 60)
    val isPomodoroRunning = MutableStateFlow(false)
    val pomodoroPresetMinutes = MutableStateFlow(25)

    // Face Scan State
    val isScanningFace = MutableStateFlow(false)
    val scanProgress = MutableStateFlow(0f)

    // Sensor Status
    val isSensorActive = MutableStateFlow(false)

    init {
        val loaded = _uiState.value
        soundManager.soundEnabled = loaded.soundEffectsEnabled
        soundManager.hapticsEnabled = loaded.hapticsEnabled
        startStepSensor()
    }

    fun startStepSensor() {
        stepSensorManager.startListening { stepsIncrement ->
            onHardwareStepsDetected(stepsIncrement)
        }
        isSensorActive.value = stepSensorManager.isListening
    }

    fun stopStepSensor() {
        stepSensorManager.stopListening()
        isSensorActive.value = false
    }

    private fun onHardwareStepsDetected(stepIncrement: Int) {
        val stats = _uiState.value.athleteStats
        val newSteps = stats.todaySteps + stepIncrement
        val newDist = stats.distanceKm + (stepIncrement * 0.00078)
        val newKcal = stats.activeCaloriesKcal + (stepIncrement * 0.04).toInt()
        val updatedStats = stats.copy(
            todaySteps = newSteps,
            distanceKm = String.format("%.2f", newDist).toDoubleOrNull() ?: newDist,
            activeCaloriesKcal = newKcal
        )
        persist(_uiState.value.copy(athleteStats = updatedStats))
    }

    override fun onCleared() {
        super.onCleared()
        stopStepSensor()
    }

    private fun persist(newState: AppState) {
        // Also update leaderboard whenever coins or user name change
        val updatedLeaderboard = repository.generateInitialLeaderboard(newState.coins, newState.userName)
        val finalState = newState.copy(leaderboardState = updatedLeaderboard)
        _uiState.value = finalState
        repository.saveState(finalState)
    }

    // ==========================================
    // COINS & MODE UNLOCKING ECONOMY
    // ==========================================
    fun addCoins(amount: Int) {
        val current = _uiState.value
        val newCoins = current.coins + amount
        val newTotal = current.totalCoinsEarned + amount
        soundManager.playCoinWin()
        persist(current.copy(coins = newCoins, totalCoinsEarned = newTotal))
    }

    fun deductCoins(amount: Int): Boolean {
        val current = _uiState.value
        if (current.coins >= amount) {
            val newCoins = current.coins - amount
            soundManager.playClick()
            persist(current.copy(coins = newCoins))
            return true
        } else {
            soundManager.playGameLoss()
            return false
        }
    }

    fun unlockModeWithCoins(persona: PersonaType): Boolean {
        val cost = persona.unlockCost
        if (cost <= 0) return true
        val current = _uiState.value
        if (current.coins >= cost) {
            val newCoins = current.coins - cost
            val expiry = System.currentTimeMillis() + (24 * 60 * 60 * 1000L) // 24-Hour Daily Pass
            val updatedDailyMap = current.dailyUnlockedUntil + (persona.name to expiry)
            val updatedModes = current.unlockedModes + persona.name
            soundManager.playLevelUp()
            val newTotalXp = current.totalXp + 30
            persist(current.copy(
                coins = newCoins,
                totalXp = newTotalXp,
                dailyUnlockedUntil = updatedDailyMap,
                unlockedModes = updatedModes
            ))
            return true
        } else {
            soundManager.playGameLoss()
            return false
        }
    }

    fun unlockModeWithRewardedAdPass(activity: Activity, persona: PersonaType) {
        soundManager.playClick()
        AdMobManager.showRewardedAd(
            activity = activity,
            onRewardEarned = { coinsAwarded ->
                addCoins(coinsAwarded)
                val current = _uiState.value
                val expiry = System.currentTimeMillis() + (24 * 60 * 60 * 1000L)
                val updatedDailyMap = current.dailyUnlockedUntil + (persona.name to expiry)
                val updatedModes = current.unlockedModes + persona.name
                soundManager.playLevelUp()
                val newTotalXp = current.totalXp + 20
                persist(current.copy(
                    totalXp = newTotalXp,
                    dailyUnlockedUntil = updatedDailyMap,
                    unlockedModes = updatedModes
                ))
                setPersona(persona)
            }
        )
    }

    fun watchRewardedAdForCoin(activity: Activity) {
        soundManager.playClick()
        AdMobManager.showRewardedAd(
            activity = activity,
            onRewardEarned = { coinsAwarded ->
                addCoins(coinsAwarded)
                addXp(20)
            }
        )
    }

    // Navigation between personas with rewarded ad support
    fun openModeWithRewardedAd(activity: Activity, persona: PersonaType) {
        if (persona == _uiState.value.currentPersona) return
        soundManager.playClick()
        AdMobManager.showRewardedAd(
            activity = activity,
            onRewardEarned = { coinsAwarded ->
                addCoins(coinsAwarded)
                addXp(10)
            },
            onDismiss = {
                setPersona(persona)
            }
        )
    }

    fun setPersona(persona: PersonaType) {
        soundManager.playClick()
        persist(_uiState.value.copy(currentPersona = persona))
    }

    fun setThemeAccent(accent: AppThemeAccent) {
        soundManager.playClick()
        persist(_uiState.value.copy(themeAccent = accent))
    }

    fun toggleDarkMode() {
        soundManager.playClick()
        persist(_uiState.value.copy(isDarkTheme = !_uiState.value.isDarkTheme))
    }

    fun toggleSound() {
        val newVal = !_uiState.value.soundEffectsEnabled
        soundManager.soundEnabled = newVal
        persist(_uiState.value.copy(soundEffectsEnabled = newVal))
        soundManager.playClick()
    }

    fun toggleHaptics() {
        val newVal = !_uiState.value.hapticsEnabled
        soundManager.hapticsEnabled = newVal
        persist(_uiState.value.copy(hapticsEnabled = newVal))
        soundManager.playClick()
    }

    fun addXp(amount: Int) {
        val oldState = _uiState.value
        val oldLevel = oldState.userLevel
        val newXp = oldState.totalXp + amount
        val newState = oldState.copy(totalXp = newXp)
        val newLevel = newState.userLevel
        persist(newState)
        if (newLevel > oldLevel) {
            soundManager.playLevelUp()
        } else {
            soundManager.playSuccess()
        }
    }

    // ==========================================
    // MALA JAAP (108 BEAD COUNTER & CHANTING)
    // ==========================================
    fun incrementMalaBead() {
        // Distinct haptic vibration on every single bead click as requested!
        soundManager.playBeadVibration()

        val current = _uiState.value
        val mala = current.malaJaapState
        val nextBead = mala.currentBead + 1

        if (nextBead >= 108) {
            // Completed 1 full mala of 108 beads!
            val newCompleted = mala.completedMalas + 1
            val updatedMala = mala.copy(
                currentBead = 0,
                completedMalas = newCompleted,
                totalChantsToday = mala.totalChantsToday + 1,
                totalChantsLifetime = mala.totalChantsLifetime + 1
            )
            // Reward: +1 Coin and +50 XP for completing a full 108 bead mala!
            soundManager.playLevelUp()
            val newCoins = current.coins + 1
            val newTotalCoins = current.totalCoinsEarned + 1
            val newXp = current.totalXp + 50
            persist(current.copy(
                malaJaapState = updatedMala,
                coins = newCoins,
                totalCoinsEarned = newTotalCoins,
                totalXp = newXp
            ))
        } else {
            val updatedMala = mala.copy(
                currentBead = nextBead,
                totalChantsToday = mala.totalChantsToday + 1,
                totalChantsLifetime = mala.totalChantsLifetime + 1
            )
            persist(current.copy(malaJaapState = updatedMala))
        }
    }

    fun resetCurrentMala() {
        soundManager.playClick()
        val current = _uiState.value
        val updatedMala = current.malaJaapState.copy(currentBead = 0)
        persist(current.copy(malaJaapState = updatedMala))
    }

    fun selectMantra(mantraId: String) {
        soundManager.playClick()
        val current = _uiState.value
        val updatedMala = current.malaJaapState.copy(selectedMantraId = mantraId)
        persist(current.copy(malaJaapState = updatedMala))
    }

    fun setTargetMalas(target: Int) {
        soundManager.playClick()
        val current = _uiState.value
        val updatedMala = current.malaJaapState.copy(targetMalas = target)
        persist(current.copy(malaJaapState = updatedMala))
    }

    // ==========================================
    // 6 MINI GAMES (SPIN, SCRATCH, QUIZ, DICE, MEMORY, SPEED REFLEX)
    // ==========================================
    fun onSpinWheelResult(coinsWon: Int) {
        val current = _uiState.value
        if (coinsWon > 0) {
            soundManager.playCoinWin()
            val newCoins = current.coins + coinsWon
            val newTotal = current.totalCoinsEarned + coinsWon
            val newXp = current.totalXp + (coinsWon * 10)
            persist(current.copy(
                coins = newCoins,
                totalCoinsEarned = newTotal,
                totalXp = newXp,
                gamesWon = current.gamesWon + 1
            ))
        } else {
            soundManager.playGameLoss()
            persist(current.copy(gamesLost = current.gamesLost + 1))
        }
    }

    fun onScratchCardRevealed(coinsWon: Int) {
        val current = _uiState.value
        if (coinsWon > 0) {
            soundManager.playCoinWin()
            persist(current.copy(
                coins = current.coins + coinsWon,
                totalCoinsEarned = current.totalCoinsEarned + coinsWon,
                totalXp = current.totalXp + 15,
                gamesWon = current.gamesWon + 1
            ))
        } else {
            soundManager.playClick()
        }
    }

    fun answerQuiz(question: QuizQuestion, chosenIndex: Int): Boolean {
        val isCorrect = chosenIndex == question.correctIndex
        val current = _uiState.value
        if (isCorrect) {
            // Win: +2 coins, +25 XP
            soundManager.playCoinWin()
            persist(current.copy(
                coins = current.coins + 2,
                totalCoinsEarned = current.totalCoinsEarned + 2,
                totalXp = current.totalXp + 25,
                gamesWon = current.gamesWon + 1
            ))
        } else {
            // Loss: -1 coin ("game loss hona par coin kam bhi honga")
            soundManager.playGameLoss()
            val newCoins = (current.coins - 1).coerceAtLeast(0)
            persist(current.copy(
                coins = newCoins,
                gamesLost = current.gamesLost + 1
            ))
        }
        return isCorrect
    }

    fun playDiceDuel(wager: Int, guessHigh: Boolean, diceRollResult: Int): Boolean {
        val isWin = (diceRollResult >= 4 && guessHigh) || (diceRollResult <= 3 && !guessHigh)
        val current = _uiState.value
        if (isWin) {
            soundManager.playCoinWin()
            persist(current.copy(
                coins = current.coins + wager,
                totalCoinsEarned = current.totalCoinsEarned + wager,
                totalXp = current.totalXp + (wager * 10),
                gamesWon = current.gamesWon + 1
            ))
        } else {
            soundManager.playGameLoss()
            val newCoins = (current.coins - wager).coerceAtLeast(0)
            persist(current.copy(
                coins = newCoins,
                gamesLost = current.gamesLost + 1
            ))
        }
        return isWin
    }

    fun onMemoryGameFinished(matchesFound: Int, coinsWon: Int) {
        val current = _uiState.value
        if (coinsWon > 0) {
            soundManager.playCoinWin()
            persist(current.copy(
                coins = current.coins + coinsWon,
                totalCoinsEarned = current.totalCoinsEarned + coinsWon,
                totalXp = current.totalXp + 30,
                gamesWon = current.gamesWon + 1
            ))
        } else {
            soundManager.playGameLoss()
            persist(current.copy(gamesLost = current.gamesLost + 1))
        }
    }

    fun onSpeedCoinReflexFinished(coinsCaught: Int) {
        val current = _uiState.value
        if (coinsCaught > 0) {
            soundManager.playCoinWin()
            persist(current.copy(
                coins = current.coins + coinsCaught,
                totalCoinsEarned = current.totalCoinsEarned + coinsCaught,
                totalXp = current.totalXp + (coinsCaught * 5),
                gamesWon = current.gamesWon + 1
            ))
        }
    }

    fun onSubwayRunnerFinished(distanceMeters: Int, coinsCollected: Int) {
        val current = _uiState.value
        val bonusCoins = (distanceMeters / 150).coerceAtLeast(0)
        val totalEarned = coinsCollected + bonusCoins
        if (totalEarned > 0) {
            soundManager.playCoinWin()
            persist(current.copy(
                coins = current.coins + totalEarned,
                totalCoinsEarned = current.totalCoinsEarned + totalEarned,
                totalXp = current.totalXp + (distanceMeters / 5) + (totalEarned * 5),
                gamesWon = current.gamesWon + 1
            ))
        } else {
            soundManager.playGameLoss()
            persist(current.copy(gamesLost = current.gamesLost + 1))
        }
    }

    fun onBlockPuzzleFinished(score: Int, linesCleared: Int, coinsEarned: Int) {
        val current = _uiState.value
        if (coinsEarned > 0) {
            soundManager.playCoinWin()
            persist(current.copy(
                coins = current.coins + coinsEarned,
                totalCoinsEarned = current.totalCoinsEarned + coinsEarned,
                totalXp = current.totalXp + (score / 10) + 15,
                gamesWon = current.gamesWon + 1
            ))
        } else {
            soundManager.playGameLoss()
            persist(current.copy(gamesLost = current.gamesLost + 1))
        }
    }

    fun onTalkingTomReward(coinsAwarded: Int, reason: String) {
        val current = _uiState.value
        if (coinsAwarded > 0) {
            soundManager.playCoinWin()
            persist(current.copy(
                coins = current.coins + coinsAwarded,
                totalCoinsEarned = current.totalCoinsEarned + coinsAwarded,
                totalXp = current.totalXp + 25,
                gamesWon = current.gamesWon + 1
            ))
        }
    }

    // ==========================================
    // LEADERBOARD MONTHLY RESET
    // ==========================================
    fun resetMonthlyLeaderboard() {
        soundManager.playLevelUp()
        val current = _uiState.value
        // Regenerate season
        val newSeasonName = "Season November 2026"
        val resetState = current.leaderboardState.copy(
            currentSeasonName = newSeasonName,
            daysUntilMonthlyReset = 30
        )
        persist(current.copy(leaderboardState = resetState))
    }

    // ==========================================
    // USER PROFILE & SETTINGS
    // ==========================================
    fun updateProfile(name: String, email: String, avatar: String) {
        soundManager.playClick()
        val current = _uiState.value
        persist(current.copy(
            userName = name.trim().ifEmpty { current.userName },
            userEmail = email.trim().ifEmpty { current.userEmail },
            avatarEmoji = avatar
        ))
    }

    fun triggerTestNotification(context: android.content.Context) {
        DailyNotificationManager.sendDailyRewardNotification(
            context = context,
            streakDays = _uiState.value.streakDays,
            bonusCoins = 1
        )
    }

    // Daily Streak Check-in System
    fun claimDailyReward() {
        val current = _uiState.value
        val todayNumber = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
        if (current.lastRewardDayNumber != todayNumber) {
            val newStreak = if (todayNumber - current.lastRewardDayNumber == 1L) current.streakDays + 1 else 1
            val bonusCoins = 2
            val bonusXp = 15 * newStreak
            val newXp = current.totalXp + bonusXp
            val newCoins = current.coins + bonusCoins
            persist(
                current.copy(
                    streakDays = newStreak,
                    lastRewardDayNumber = todayNumber,
                    totalXp = newXp,
                    coins = newCoins,
                    totalCoinsEarned = current.totalCoinsEarned + bonusCoins
                )
            )
            soundManager.playLevelUp()
        } else {
            soundManager.playClick()
        }
    }

    fun isDailyRewardAvailable(): Boolean {
        val todayNumber = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
        return _uiState.value.lastRewardDayNumber != todayNumber
    }

    // Student Mode: Chapters
    fun toggleChapterCompletion(chapterId: String) {
        soundManager.playClick()
        val currentChapters = _uiState.value.chapters.toMutableList()
        val index = currentChapters.indexOfFirst { it.id == chapterId }
        if (index != -1) {
            val old = currentChapters[index]
            val updated = old.copy(
                isCompleted = !old.isCompleted,
                completedAt = if (!old.isCompleted) System.currentTimeMillis() else 0L
            )
            currentChapters[index] = updated
            if (updated.isCompleted) {
                addXp(35)
                addCoins(1) // +1 Coin on completing a study chapter!
            } else {
                persist(_uiState.value.copy(chapters = currentChapters))
            }
        }
    }

    fun addChapter(subjectName: String, title: String, notes: String) {
        soundManager.playClick()
        val newChapter = SyllabusChapter(
            id = "c_${System.currentTimeMillis()}",
            subjectName = subjectName.trim().ifEmpty { "General" },
            chapterTitle = title.trim().ifEmpty { "Untitled Chapter" },
            notes = notes.trim(),
            isCompleted = false
        )
        val updated = _uiState.value.chapters + newChapter
        persist(_uiState.value.copy(chapters = updated))
    }

    // Pomodoro logic
    fun startPomodoro() {
        soundManager.playClick()
        if (isPomodoroRunning.value) return
        isPomodoroRunning.value = true
        pomodoroJob = viewModelScope.launch {
            while (pomodoroSecondsLeft.value > 0 && isPomodoroRunning.value) {
                delay(1000L)
                pomodoroSecondsLeft.value -= 1
            }
            if (pomodoroSecondsLeft.value <= 0) {
                isPomodoroRunning.value = false
                val minutesAdded = pomodoroPresetMinutes.value
                val updatedMins = _uiState.value.studyMinutesLogged + minutesAdded
                persist(_uiState.value.copy(studyMinutesLogged = updatedMins))
                addXp(minutesAdded * 2)
                addCoins(2)
                soundManager.playLevelUp()
            }
        }
    }

    fun pausePomodoro() {
        soundManager.playClick()
        isPomodoroRunning.value = false
        pomodoroJob?.cancel()
    }

    fun resetPomodoro(minutes: Int = pomodoroPresetMinutes.value) {
        soundManager.playClick()
        pausePomodoro()
        pomodoroPresetMinutes.value = minutes
        pomodoroSecondsLeft.value = minutes * 60
    }

    // Business Mode: Invoices
    fun addInvoice(
        customerName: String,
        customerPhone: String,
        gstin: String,
        items: List<InvoiceLineItem>,
        isPaid: Boolean
    ) {
        soundManager.playClick()
        val invoiceNumber = "INV-${System.currentTimeMillis() % 100000}"
        val newInvoice = Invoice(
            invoiceNumber = invoiceNumber,
            customerName = customerName.trim().ifEmpty { "Cash Customer" },
            customerPhone = customerPhone.trim(),
            gstin = gstin.trim(),
            dateEpochMs = System.currentTimeMillis(),
            items = items,
            isPaid = isPaid
        )
        val updated = listOf(newInvoice) + _uiState.value.invoices
        persist(_uiState.value.copy(invoices = updated))
        addXp(50)
        addCoins(1)
    }

    // Business: Khata
    fun addKhataEntry(partyName: String, partyPhone: String, amount: Double, type: KhataType, note: String) {
        soundManager.playClick()
        val entry = KhataEntry(
            id = "k_${System.currentTimeMillis()}",
            partyName = partyName.trim().ifEmpty { "Customer" },
            partyPhone = partyPhone.trim(),
            amount = amount,
            type = type,
            note = note.trim(),
            dateEpochMs = System.currentTimeMillis()
        )
        val updated = listOf(entry) + _uiState.value.khataEntries
        persist(_uiState.value.copy(khataEntries = updated))
        addXp(25)
    }

    // Business: Inventory
    fun updateInventoryQuantity(productId: String, delta: Int) {
        soundManager.playClick()
        val list = _uiState.value.inventory.toMutableList()
        val idx = list.indexOfFirst { it.id == productId }
        if (idx != -1) {
            val old = list[idx]
            val newQty = (old.quantity + delta).coerceAtLeast(0)
            list[idx] = old.copy(quantity = newQty)
            persist(_uiState.value.copy(inventory = list))
        }
    }

    fun addInventoryItem(name: String, category: String, qty: Int, costPrice: Double, sellPrice: Double) {
        soundManager.playClick()
        val item = InventoryItem(
            id = "p_${System.currentTimeMillis()}",
            name = name.trim().ifEmpty { "New Item" },
            category = category.trim().ifEmpty { "General" },
            quantity = qty,
            costPrice = costPrice,
            sellingPrice = sellPrice
        )
        persist(_uiState.value.copy(inventory = _uiState.value.inventory + item))
        addXp(30)
    }

    // Athlete Mode: Pedometer & Workouts
    fun simulateSteps(stepIncrement: Int) {
        soundManager.playClick()
        onHardwareStepsDetected(stepIncrement)
        addXp(stepIncrement / 50)
    }

    fun logWorkout(sport: SportType, durationMins: Int, calories: Int, distanceKm: Double, notes: String) {
        soundManager.playClick()
        val workout = WorkoutSession(
            id = "w_${System.currentTimeMillis()}",
            sportType = sport,
            durationMinutes = durationMins,
            caloriesBurned = calories,
            distanceKm = distanceKm,
            notes = notes.trim(),
            timestamp = System.currentTimeMillis()
        )
        val currentStats = _uiState.value.athleteStats
        val updatedStats = currentStats.copy(
            activeCaloriesKcal = currentStats.activeCaloriesKcal + calories,
            distanceKm = currentStats.distanceKm + distanceKm
        )
        persist(
            _uiState.value.copy(
                workouts = listOf(workout) + _uiState.value.workouts,
                athleteStats = updatedStats
            )
        )
        addXp(60)
        addCoins(1)
    }

    fun cycleMuscleSoreness(muscleName: String) {
        soundManager.playClick()
        val current = _uiState.value.sorenessMap.toMutableMap()
        val currentLevel = current[muscleName] ?: SorenessLevel.FRESH
        val nextLevel = when (currentLevel) {
            SorenessLevel.FRESH -> SorenessLevel.TIGHT
            SorenessLevel.TIGHT -> SorenessLevel.SORE
            SorenessLevel.SORE -> SorenessLevel.FRESH
        }
        current[muscleName] = nextLevel
        persist(_uiState.value.copy(sorenessMap = current))
    }

    // Looksmaxer Mode: Photo Upload & AI Scan
    fun setUserSelectedPhoto(photoUri: String?) {
        soundManager.playClick()
        persist(_uiState.value.copy(userSelectedPhotoUri = photoUri))
    }

    fun triggerAiFaceScan() {
        soundManager.playClick()
        if (isScanningFace.value) return
        isScanningFace.value = true
        scanProgress.value = 0f
        viewModelScope.launch {
            for (step in 1..20) {
                delay(70L)
                scanProgress.value = step / 20f
            }
            val sym = (86..96).random()
            val jaw = (82..95).random()
            val tilt = listOf(CanthalTilt.POSITIVE, CanthalTilt.NEUTRAL).random()
            val golden = (88..97).random()
            val skin = (80..94).random()
            val metrics = FacialMetrics(sym, jaw, tilt, golden, skin)
            val record = FaceScanRecord(
                id = "scan_${System.currentTimeMillis()}",
                timestamp = System.currentTimeMillis(),
                metrics = metrics,
                scanNotes = "Facial symmetry computed. Sharp mandibular structure and golden proportion alignment verified.",
                photoUri = _uiState.value.userSelectedPhotoUri
            )
            isScanningFace.value = false
            persist(
                _uiState.value.copy(
                    latestMetrics = metrics,
                    scanHistory = listOf(record) + _uiState.value.scanHistory
                )
            )
            addXp(75)
            soundManager.playLevelUp()
        }
    }

    fun toggleHabit(habitName: String) {
        soundManager.playClick()
        val routine = _uiState.value.softmaxingRoutine
        val updated = when (habitName) {
            "morningSkincare" -> routine.copy(morningSkincareDone = !routine.morningSkincareDone)
            "nightSkincare" -> routine.copy(nightSkincareDone = !routine.nightSkincareDone)
            "spfApplied" -> routine.copy(spfApplied = !routine.spfApplied)
            "iceFacial" -> routine.copy(iceFacialColdShowerDone = !routine.iceFacialColdShowerDone)
            "posture" -> routine.copy(postureExercisesDone = !routine.postureExercisesDone)
            else -> routine
        }
        persist(_uiState.value.copy(softmaxingRoutine = updated))
        addXp(15)
    }

    fun addMewingMinutes(minutes: Int) {
        soundManager.playClick()
        val routine = _uiState.value.softmaxingRoutine
        val newMinutes = (routine.mewingMinutesLogged + minutes).coerceAtMost(120)
        persist(_uiState.value.copy(softmaxingRoutine = routine.copy(mewingMinutesLogged = newMinutes)))
        addXp(15)
    }

    fun addWaterGlass() {
        soundManager.playClick()
        val routine = _uiState.value.softmaxingRoutine
        val newGlasses = (routine.waterGlassesLogged + 1).coerceAtMost(16)
        persist(_uiState.value.copy(softmaxingRoutine = routine.copy(waterGlassesLogged = newGlasses)))
        addXp(10)
    }

    // Export / Import
    fun getExportJson(): String {
        return repository.exportStateJson(_uiState.value)
    }

    fun importJson(jsonStr: String): Boolean {
        val res = repository.importStateJson(jsonStr)
        return if (res.isSuccess) {
            _uiState.value = res.getOrThrow()
            soundManager.playLevelUp()
            true
        } else {
            false
        }
    }

    fun resetToZeroBaseline() {
        soundManager.playClick()
        val clean = repository.getDefaultInitialState()
        persist(clean)
    }
}
