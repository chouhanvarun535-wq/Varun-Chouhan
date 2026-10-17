package com.example.model

data class AppState(
    // App & Persona Preferences
    val currentPersona: PersonaType = PersonaType.STUDENT,
    val themeAccent: AppThemeAccent = AppThemeAccent.MONOCHROME_EXECUTIVE,
    val isDarkTheme: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,

    // User Profile
    val userName: String = "Varun Chouhan",
    val userEmail: String = "chouhanvarun535@gmail.com",
    val avatarEmoji: String = "👑",

    // Real-Time Coins Economy & Daily Modes Unlocking (10 Coins / Day)
    val coins: Int = 25, // Initial balance to unlock modes and test immediately
    val totalCoinsEarned: Int = 25,
    val unlockedModes: Set<String> = setOf(
        PersonaType.MALA_JAAP.name,
        PersonaType.GAMES.name,
        PersonaType.LEADERBOARD.name,
        PersonaType.PROFILE.name
    ),
    // Map of PersonaType.name to expiration epoch timestamp (ms) for daily unlock passes
    val dailyUnlockedUntil: Map<String, Long> = emptyMap(),

    // Universal Gamification
    val totalXp: Int = 0,
    val streakDays: Int = 0,
    val lastRewardDayNumber: Long = 0L,
    val unlockedBadges: List<String> = emptyList(),
    val gamesWon: Int = 0,
    val gamesLost: Int = 0,

    // Mala Jaap (108 Bead Spiritual Chanting)
    val malaJaapState: MalaJaapState = MalaJaapState(),

    // Leaderboard
    val leaderboardState: LeaderboardState = LeaderboardState(),

    // Student Mode
    val studyMinutesLogged: Int = 0,
    val chapters: List<SyllabusChapter> = emptyList(),
    val timetable: List<TimetableSlot> = emptyList(),
    val flashcards: List<Flashcard> = emptyList(),

    // Business Mode
    val businessName: String = "My Business",
    val businessUpiId: String = "merchant@upi",
    val invoices: List<Invoice> = emptyList(),
    val khataEntries: List<KhataEntry> = emptyList(),
    val inventory: List<InventoryItem> = emptyList(),

    // Athlete Mode
    val athleteStats: AthleteStats = AthleteStats(),
    val workouts: List<WorkoutSession> = emptyList(),
    val sorenessMap: Map<String, SorenessLevel> = mapOf(
        "Chest" to SorenessLevel.FRESH,
        "Shoulders" to SorenessLevel.FRESH,
        "Biceps & Triceps" to SorenessLevel.FRESH,
        "Core & Abs" to SorenessLevel.FRESH,
        "Quads" to SorenessLevel.FRESH,
        "Hamstrings" to SorenessLevel.FRESH,
        "Back & Lats" to SorenessLevel.FRESH,
        "Calves" to SorenessLevel.FRESH
    ),

    // Looksmaxer Mode
    val softmaxingRoutine: SoftmaxingRoutine = SoftmaxingRoutine(),
    val latestMetrics: FacialMetrics = FacialMetrics(),
    val scanHistory: List<FaceScanRecord> = emptyList(),
    val userSelectedPhotoUri: String? = null
) {
    val userLevel: Int get() = (totalXp / 150) + 1
    val currentLevelProgress: Float get() = ((totalXp % 150) / 150f).coerceIn(0f, 1f)
    val xpForNextLevel: Int get() = 150 - (totalXp % 150)

    val currentLooksmaxTier: LooksmaxTier
        get() = when {
            totalXp >= LooksmaxTier.APEX_CHAD.minXp -> LooksmaxTier.APEX_CHAD
            totalXp >= LooksmaxTier.DIAMOND.minXp -> LooksmaxTier.DIAMOND
            totalXp >= LooksmaxTier.GOLD.minXp -> LooksmaxTier.GOLD
            totalXp >= LooksmaxTier.SILVER.minXp -> LooksmaxTier.SILVER
            else -> LooksmaxTier.BRONZE
        }

    fun isModeUnlocked(persona: PersonaType, currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        if (persona.unlockCost == 0) return true
        val expiry = dailyUnlockedUntil[persona.name] ?: 0L
        if (expiry > currentTimeMs) return true
        return unlockedModes.contains(persona.name)
    }

    fun getRemainingDailyUnlockTime(persona: PersonaType, currentTimeMs: Long = System.currentTimeMillis()): String? {
        val expiry = dailyUnlockedUntil[persona.name] ?: return null
        val diffMs = expiry - currentTimeMs
        if (diffMs <= 0) return null
        val hours = diffMs / (1000 * 60 * 60)
        val mins = (diffMs / (1000 * 60)) % 60
        return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    }
}
