package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject

class AppDataRepository(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("apex_life_app_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_APP_STATE_JSON = "key_app_state_json"
    }

    fun loadState(): AppState {
        val jsonString = prefs.getString(KEY_APP_STATE_JSON, null)
        return if (!jsonString.isNullOrEmpty()) {
            try {
                deserializeState(jsonString)
            } catch (_: Exception) {
                getDefaultInitialState()
            }
        } else {
            getDefaultInitialState()
        }
    }

    fun saveState(state: AppState) {
        try {
            val json = serializeState(state)
            prefs.edit().putString(KEY_APP_STATE_JSON, json).apply()
        } catch (_: Exception) {}
    }

    fun exportStateJson(state: AppState): String {
        return serializeState(state)
    }

    fun importStateJson(jsonString: String): Result<AppState> {
        return try {
            val state = deserializeState(jsonString)
            saveState(state)
            Result.success(state)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getDefaultInitialState(): AppState {
        return AppState(
            coins = 25,
            totalCoinsEarned = 25,
            unlockedModes = setOf(
                PersonaType.MALA_JAAP.name,
                PersonaType.GAMES.name,
                PersonaType.LEADERBOARD.name,
                PersonaType.PROFILE.name
            ),
            dailyUnlockedUntil = emptyMap(),
            userName = "Varun Chouhan",
            userEmail = "chouhanvarun535@gmail.com",
            avatarEmoji = "👑",
            totalXp = 0,
            streakDays = 1,
            studyMinutesLogged = 0,
            chapters = emptyList(),
            timetable = emptyList(),
            flashcards = emptyList(),
            invoices = emptyList(),
            khataEntries = emptyList(),
            inventory = emptyList(),
            athleteStats = AthleteStats(
                todaySteps = 0,
                stepGoal = 10000,
                distanceKm = 0.0,
                activeCaloriesKcal = 0,
                sleepHours = 0.0,
                restingHeartRateBpm = 0
            ),
            workouts = emptyList(),
            softmaxingRoutine = SoftmaxingRoutine(),
            latestMetrics = FacialMetrics(0, 0, CanthalTilt.NEUTRAL, 0, 0),
            scanHistory = emptyList(),
            userSelectedPhotoUri = null,
            malaJaapState = MalaJaapState(
                currentBead = 0,
                completedMalas = 0,
                totalChantsToday = 0,
                totalChantsLifetime = 0L,
                targetMalas = 1
            ),
            leaderboardState = generateInitialLeaderboard(25, "Varun Chouhan")
        )
    }

    fun generateInitialLeaderboard(userCoins: Int, userName: String): LeaderboardState {
        val sampleOtherPlayers = listOf(
            LeaderboardPlayer(1, "p_1", "Aarav Sharma", 165, 8, "🦁"),
            LeaderboardPlayer(2, "p_2", "Pooja Patel", 120, 6, "✨"),
            LeaderboardPlayer(3, "p_3", "Rohan Verma", 95, 5, "⚡"),
            LeaderboardPlayer(4, "p_4", "Ananya Iyer", 82, 5, "💎"),
            LeaderboardPlayer(5, "p_5", "Vikramaditya Singh", 64, 4, "🎯"),
            LeaderboardPlayer(6, "p_6", "Sneha Kulkarni", 50, 4, "🌸"),
            LeaderboardPlayer(7, "p_7", "Kabir Das", 42, 3, "📿"),
            LeaderboardPlayer(8, "p_8", "Divya Malhotra", 35, 3, "🔥"),
            LeaderboardPlayer(9, "p_9", "Arjun Mehra", 28, 2, "🚀"),
            LeaderboardPlayer(10, "p_10", "Neha Gupta", 20, 2, "⭐"),
            LeaderboardPlayer(11, "p_11", "Rahul Jain", 15, 1, "🧘")
        )

        val cleanUserName = if (userName.isBlank() || userName.contains("Apex Player") || userName.contains("Apex Warrior")) {
            "Varun Chouhan"
        } else {
            userName.replace(" (You)", "").trim()
        }

        // Combine with user and sort descending by coins
        val currentUser = LeaderboardPlayer(
            rank = 0,
            id = "current_user",
            name = "$cleanUserName (You)",
            coins = userCoins,
            level = (userCoins / 15).coerceAtLeast(1),
            avatarEmoji = "👑",
            isCurrentUser = true
        )

        val allPlayers = (sampleOtherPlayers + currentUser).sortedByDescending { it.coins }
        val rankedList = allPlayers.mapIndexed { index, player ->
            player.copy(rank = index + 1)
        }
        val userRank = rankedList.firstOrNull { it.isCurrentUser }?.rank ?: 1

        return LeaderboardState(
            currentSeasonName = "Season October 2026",
            daysUntilMonthlyReset = 14,
            topPlayers = rankedList,
            userRank = userRank,
            totalCoinsRanked = userCoins
        )
    }

    private fun serializeState(state: AppState): String {
        val root = JSONObject()
        root.put("currentPersona", state.currentPersona.name)
        root.put("themeAccent", state.themeAccent.name)
        root.put("isDarkTheme", state.isDarkTheme)
        root.put("soundEffectsEnabled", state.soundEffectsEnabled)
        root.put("hapticsEnabled", state.hapticsEnabled)

        // Profile & Coins
        root.put("userName", state.userName)
        root.put("userEmail", state.userEmail)
        root.put("avatarEmoji", state.avatarEmoji)
        root.put("coins", state.coins)
        root.put("totalCoinsEarned", state.totalCoinsEarned)
        root.put("gamesWon", state.gamesWon)
        root.put("gamesLost", state.gamesLost)

        val unlModesArr = JSONArray()
        state.unlockedModes.forEach { unlModesArr.put(it) }
        root.put("unlockedModes", unlModesArr)

        val dailyObj = JSONObject()
        state.dailyUnlockedUntil.forEach { (mode, expiry) ->
            dailyObj.put(mode, expiry)
        }
        root.put("dailyUnlockedUntil", dailyObj)

        root.put("totalXp", state.totalXp)
        root.put("streakDays", state.streakDays)
        root.put("lastRewardDayNumber", state.lastRewardDayNumber)
        root.put("studyMinutesLogged", state.studyMinutesLogged)
        root.put("businessName", state.businessName)
        root.put("businessUpiId", state.businessUpiId)
        root.put("userSelectedPhotoUri", state.userSelectedPhotoUri ?: "")

        // Mala Jaap
        val malaObj = JSONObject()
        malaObj.put("currentBead", state.malaJaapState.currentBead)
        malaObj.put("completedMalas", state.malaJaapState.completedMalas)
        malaObj.put("totalChantsToday", state.malaJaapState.totalChantsToday)
        malaObj.put("totalChantsLifetime", state.malaJaapState.totalChantsLifetime)
        malaObj.put("targetMalas", state.malaJaapState.targetMalas)
        malaObj.put("selectedMantraId", state.malaJaapState.selectedMantraId)
        malaObj.put("isVibrationEnabled", state.malaJaapState.isVibrationEnabled)
        malaObj.put("isSoundChimeEnabled", state.malaJaapState.isSoundChimeEnabled)
        malaObj.put("dailyJaapStreak", state.malaJaapState.dailyJaapStreak)
        root.put("malaJaapState", malaObj)

        // Chapters
        val chArr = JSONArray()
        state.chapters.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("subjectName", c.subjectName)
            obj.put("chapterTitle", c.chapterTitle)
            obj.put("notes", c.notes)
            obj.put("isCompleted", c.isCompleted)
            obj.put("completedAt", c.completedAt)
            chArr.put(obj)
        }
        root.put("chapters", chArr)

        // Invoices
        val invArr = JSONArray()
        state.invoices.forEach { inv ->
            val obj = JSONObject()
            obj.put("invoiceNumber", inv.invoiceNumber)
            obj.put("customerName", inv.customerName)
            obj.put("customerPhone", inv.customerPhone)
            obj.put("gstin", inv.gstin)
            obj.put("dateEpochMs", inv.dateEpochMs)
            obj.put("isPaid", inv.isPaid)
            val itemsArr = JSONArray()
            inv.items.forEach { itm ->
                val itmObj = JSONObject()
                itmObj.put("description", itm.description)
                itmObj.put("quantity", itm.quantity)
                itmObj.put("rate", itm.rate)
                itmObj.put("gstRatePercent", itm.gstRatePercent)
                itemsArr.put(itmObj)
            }
            obj.put("items", itemsArr)
            invArr.put(obj)
        }
        root.put("invoices", invArr)

        // Khata
        val khArr = JSONArray()
        state.khataEntries.forEach { k ->
            val obj = JSONObject()
            obj.put("id", k.id)
            obj.put("partyName", k.partyName)
            obj.put("partyPhone", k.partyPhone)
            obj.put("amount", k.amount)
            obj.put("type", k.type.name)
            obj.put("note", k.note)
            obj.put("dateEpochMs", k.dateEpochMs)
            khArr.put(obj)
        }
        root.put("khataEntries", khArr)

        // Inventory
        val invenArr = JSONArray()
        state.inventory.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("category", p.category)
            obj.put("quantity", p.quantity)
            obj.put("minThresholdAlert", p.minThresholdAlert)
            obj.put("costPrice", p.costPrice)
            obj.put("sellingPrice", p.sellingPrice)
            invenArr.put(obj)
        }
        root.put("inventory", invenArr)

        // Athlete stats
        val athObj = JSONObject()
        athObj.put("todaySteps", state.athleteStats.todaySteps)
        athObj.put("stepGoal", state.athleteStats.stepGoal)
        athObj.put("distanceKm", state.athleteStats.distanceKm)
        athObj.put("activeCaloriesKcal", state.athleteStats.activeCaloriesKcal)
        athObj.put("sleepHours", state.athleteStats.sleepHours)
        athObj.put("restingHeartRateBpm", state.athleteStats.restingHeartRateBpm)
        root.put("athleteStats", athObj)

        // Workouts
        val wArr = JSONArray()
        state.workouts.forEach { w ->
            val obj = JSONObject()
            obj.put("id", w.id)
            obj.put("sportType", w.sportType.name)
            obj.put("durationMinutes", w.durationMinutes)
            obj.put("caloriesBurned", w.caloriesBurned)
            obj.put("distanceKm", w.distanceKm)
            obj.put("notes", w.notes)
            obj.put("timestamp", w.timestamp)
            wArr.put(obj)
        }
        root.put("workouts", wArr)

        // Softmaxing
        val smObj = JSONObject()
        smObj.put("morningSkincareDone", state.softmaxingRoutine.morningSkincareDone)
        smObj.put("nightSkincareDone", state.softmaxingRoutine.nightSkincareDone)
        smObj.put("spfApplied", state.softmaxingRoutine.spfApplied)
        smObj.put("mewingMinutesLogged", state.softmaxingRoutine.mewingMinutesLogged)
        smObj.put("mewingTargetMinutes", state.softmaxingRoutine.mewingTargetMinutes)
        smObj.put("iceFacialColdShowerDone", state.softmaxingRoutine.iceFacialColdShowerDone)
        smObj.put("waterGlassesLogged", state.softmaxingRoutine.waterGlassesLogged)
        smObj.put("waterGlassesTarget", state.softmaxingRoutine.waterGlassesTarget)
        smObj.put("postureExercisesDone", state.softmaxingRoutine.postureExercisesDone)
        root.put("softmaxingRoutine", smObj)

        // Metrics
        val mObj = JSONObject()
        mObj.put("symmetryPercent", state.latestMetrics.symmetryPercent)
        mObj.put("jawlineDefinitionPercent", state.latestMetrics.jawlineDefinitionPercent)
        mObj.put("canthalTilt", state.latestMetrics.canthalTilt.name)
        mObj.put("goldenRatioMatchPercent", state.latestMetrics.goldenRatioMatchPercent)
        mObj.put("skinQualityPercent", state.latestMetrics.skinQualityPercent)
        root.put("latestMetrics", mObj)

        return root.toString(2)
    }

    private fun deserializeState(jsonStr: String): AppState {
        val root = JSONObject(jsonStr)
        val persona = try {
            PersonaType.valueOf(root.optString("currentPersona", PersonaType.STUDENT.name))
        } catch (_: Exception) { PersonaType.STUDENT }
        val theme = try {
            AppThemeAccent.valueOf(root.optString("themeAccent", AppThemeAccent.MONOCHROME_EXECUTIVE.name))
        } catch (_: Exception) { AppThemeAccent.MONOCHROME_EXECUTIVE }
        val isDark = root.optBoolean("isDarkTheme", true)
        val sound = root.optBoolean("soundEffectsEnabled", true)
        val haptics = root.optBoolean("hapticsEnabled", true)

        val rawUserName = root.optString("userName", "Varun Chouhan")
        val userName = if (rawUserName.isBlank() || rawUserName == "Apex Player" || rawUserName == "Apex Warrior") "Varun Chouhan" else rawUserName
        val rawEmail = root.optString("userEmail", "chouhanvarun535@gmail.com")
        val userEmail = if (rawEmail.contains("apexlife") || rawEmail.contains("player@")) "chouhanvarun535@gmail.com" else rawEmail
        val avatarEmoji = root.optString("avatarEmoji", "👑")
        val coins = root.optInt("coins", 25)
        val totalCoinsEarned = root.optInt("totalCoinsEarned", coins)
        val gamesWon = root.optInt("gamesWon", 0)
        val gamesLost = root.optInt("gamesLost", 0)

        val unlModes = mutableSetOf<String>()
        val unlArr = root.optJSONArray("unlockedModes")
        if (unlArr != null) {
            for (i in 0 until unlArr.length()) unlModes.add(unlArr.getString(i))
        } else {
            unlModes.addAll(listOf(PersonaType.MALA_JAAP.name, PersonaType.GAMES.name, PersonaType.LEADERBOARD.name, PersonaType.PROFILE.name))
        }

        val dailyMap = mutableMapOf<String, Long>()
        val dailyObj = root.optJSONObject("dailyUnlockedUntil")
        if (dailyObj != null) {
            val keys = dailyObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                dailyMap[k] = dailyObj.optLong(k, 0L)
            }
        }

        val totalXp = root.optInt("totalXp", 0)
        val streak = root.optInt("streakDays", 1)
        val lastDay = root.optLong("lastRewardDayNumber", 0L)
        val studyMins = root.optInt("studyMinutesLogged", 0)
        val bName = root.optString("businessName", "My Business")
        val bUpi = root.optString("businessUpiId", "merchant@upi")
        val photoUri = root.optString("userSelectedPhotoUri", "").ifEmpty { null }

        // Mala Jaap
        val malaObj = root.optJSONObject("malaJaapState")
        val malaState = if (malaObj != null) {
            MalaJaapState(
                currentBead = malaObj.optInt("currentBead", 0),
                completedMalas = malaObj.optInt("completedMalas", 0),
                totalChantsToday = malaObj.optInt("totalChantsToday", 0),
                totalChantsLifetime = malaObj.optLong("totalChantsLifetime", 0L),
                targetMalas = malaObj.optInt("targetMalas", 1),
                selectedMantraId = malaObj.optString("selectedMantraId", "om_namah_shivaya"),
                isVibrationEnabled = malaObj.optBoolean("isVibrationEnabled", true),
                isSoundChimeEnabled = malaObj.optBoolean("isSoundChimeEnabled", true),
                dailyJaapStreak = malaObj.optInt("dailyJaapStreak", 0)
            )
        } else MalaJaapState()

        // Chapters
        val chapters = mutableListOf<SyllabusChapter>()
        val chArr = root.optJSONArray("chapters")
        if (chArr != null) {
            for (i in 0 until chArr.length()) {
                val o = chArr.getJSONObject(i)
                chapters.add(
                    SyllabusChapter(
                        id = o.optString("id", "c$i"),
                        subjectName = o.optString("subjectName", "General"),
                        chapterTitle = o.optString("chapterTitle", "Chapter"),
                        notes = o.optString("notes", ""),
                        isCompleted = o.optBoolean("isCompleted", false),
                        completedAt = o.optLong("completedAt", 0L)
                    )
                )
            }
        }

        // Invoices
        val invoices = mutableListOf<Invoice>()
        val invArr = root.optJSONArray("invoices")
        if (invArr != null) {
            for (i in 0 until invArr.length()) {
                val o = invArr.getJSONObject(i)
                val items = mutableListOf<InvoiceLineItem>()
                val itmArr = o.optJSONArray("items")
                if (itmArr != null) {
                    for (j in 0 until itmArr.length()) {
                        val io = itmArr.getJSONObject(j)
                        items.add(
                            InvoiceLineItem(
                                description = io.optString("description", "Item"),
                                quantity = io.optDouble("quantity", 1.0),
                                rate = io.optDouble("rate", 0.0),
                                gstRatePercent = io.optDouble("gstRatePercent", 18.0)
                            )
                        )
                    }
                }
                invoices.add(
                    Invoice(
                        invoiceNumber = o.optString("invoiceNumber", "INV-001"),
                        customerName = o.optString("customerName", "Customer"),
                        customerPhone = o.optString("customerPhone", ""),
                        gstin = o.optString("gstin", ""),
                        dateEpochMs = o.optLong("dateEpochMs", System.currentTimeMillis()),
                        items = items,
                        isPaid = o.optBoolean("isPaid", true)
                    )
                )
            }
        }

        // Khata
        val khata = mutableListOf<KhataEntry>()
        val khArr = root.optJSONArray("khataEntries")
        if (khArr != null) {
            for (i in 0 until khArr.length()) {
                val o = khArr.getJSONObject(i)
                khata.add(
                    KhataEntry(
                        id = o.optString("id", "k$i"),
                        partyName = o.optString("partyName", "Party"),
                        partyPhone = o.optString("partyPhone", ""),
                        amount = o.optDouble("amount", 0.0),
                        type = try { KhataType.valueOf(o.optString("type")) } catch (_: Exception) { KhataType.GAVE_UDHAAR },
                        note = o.optString("note", ""),
                        dateEpochMs = o.optLong("dateEpochMs", System.currentTimeMillis())
                    )
                )
            }
        }

        // Inventory
        val inventory = mutableListOf<InventoryItem>()
        val inArr = root.optJSONArray("inventory")
        if (inArr != null) {
            for (i in 0 until inArr.length()) {
                val o = inArr.getJSONObject(i)
                inventory.add(
                    InventoryItem(
                        id = o.optString("id", "p$i"),
                        name = o.optString("name", "Product"),
                        category = o.optString("category", "General"),
                        quantity = o.optInt("quantity", 0),
                        minThresholdAlert = o.optInt("minThresholdAlert", 5),
                        costPrice = o.optDouble("costPrice", 0.0),
                        sellingPrice = o.optDouble("sellingPrice", 0.0)
                    )
                )
            }
        }

        // Athlete stats
        val athObj = root.optJSONObject("athleteStats")
        val athleteStats = if (athObj != null) {
            AthleteStats(
                todaySteps = athObj.optInt("todaySteps", 0),
                stepGoal = athObj.optInt("stepGoal", 10000),
                distanceKm = athObj.optDouble("distanceKm", 0.0),
                activeCaloriesKcal = athObj.optInt("activeCaloriesKcal", 0),
                sleepHours = athObj.optDouble("sleepHours", 0.0),
                restingHeartRateBpm = athObj.optInt("restingHeartRateBpm", 0)
            )
        } else AthleteStats(0, 10000, 0.0, 0, 0.0, 0)

        // Workouts
        val workouts = mutableListOf<WorkoutSession>()
        val wArr = root.optJSONArray("workouts")
        if (wArr != null) {
            for (i in 0 until wArr.length()) {
                val o = wArr.getJSONObject(i)
                workouts.add(
                    WorkoutSession(
                        id = o.optString("id", "w$i"),
                        sportType = try { SportType.valueOf(o.optString("sportType")) } catch (_: Exception) { SportType.RUNNING },
                        durationMinutes = o.optInt("durationMinutes", 0),
                        caloriesBurned = o.optInt("caloriesBurned", 0),
                        distanceKm = o.optDouble("distanceKm", 0.0),
                        notes = o.optString("notes", ""),
                        timestamp = o.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        }

        // Softmaxing
        val smObj = root.optJSONObject("softmaxingRoutine")
        val routine = if (smObj != null) {
            SoftmaxingRoutine(
                morningSkincareDone = smObj.optBoolean("morningSkincareDone", false),
                nightSkincareDone = smObj.optBoolean("nightSkincareDone", false),
                spfApplied = smObj.optBoolean("spfApplied", false),
                mewingMinutesLogged = smObj.optInt("mewingMinutesLogged", 0),
                mewingTargetMinutes = smObj.optInt("mewingTargetMinutes", 30),
                iceFacialColdShowerDone = smObj.optBoolean("iceFacialColdShowerDone", false),
                waterGlassesLogged = smObj.optInt("waterGlassesLogged", 0),
                waterGlassesTarget = smObj.optInt("waterGlassesTarget", 8),
                postureExercisesDone = smObj.optBoolean("postureExercisesDone", false)
            )
        } else SoftmaxingRoutine()

        // Metrics
        val mObj = root.optJSONObject("latestMetrics")
        val metrics = if (mObj != null) {
            FacialMetrics(
                symmetryPercent = mObj.optInt("symmetryPercent", 0),
                jawlineDefinitionPercent = mObj.optInt("jawlineDefinitionPercent", 0),
                canthalTilt = try { CanthalTilt.valueOf(mObj.optString("canthalTilt")) } catch (_: Exception) { CanthalTilt.NEUTRAL },
                goldenRatioMatchPercent = mObj.optInt("goldenRatioMatchPercent", 0),
                skinQualityPercent = mObj.optInt("skinQualityPercent", 0)
            )
        } else FacialMetrics(0, 0, CanthalTilt.NEUTRAL, 0, 0)

        val initialLeaderboard = generateInitialLeaderboard(coins, userName)

        return AppState(
            currentPersona = persona,
            themeAccent = theme,
            isDarkTheme = isDark,
            soundEffectsEnabled = sound,
            hapticsEnabled = haptics,
            userName = userName,
            userEmail = userEmail,
            avatarEmoji = avatarEmoji,
            coins = coins,
            totalCoinsEarned = totalCoinsEarned,
            unlockedModes = unlModes,
            dailyUnlockedUntil = dailyMap,
            gamesWon = gamesWon,
            gamesLost = gamesLost,
            totalXp = totalXp,
            streakDays = streak,
            lastRewardDayNumber = lastDay,
            studyMinutesLogged = studyMins,
            businessName = bName,
            businessUpiId = bUpi,
            chapters = chapters,
            timetable = emptyList(),
            flashcards = emptyList(),
            invoices = invoices,
            khataEntries = khata,
            inventory = inventory,
            athleteStats = athleteStats,
            workouts = workouts,
            sorenessMap = mapOf("Chest" to SorenessLevel.FRESH),
            softmaxingRoutine = routine,
            latestMetrics = metrics,
            userSelectedPhotoUri = photoUri,
            malaJaapState = malaState,
            leaderboardState = initialLeaderboard
        )
    }
}
