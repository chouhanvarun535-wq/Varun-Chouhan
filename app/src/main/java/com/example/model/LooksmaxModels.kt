package com.example.model

enum class CanthalTilt(val label: String) {
    NEUTRAL("Neutral (0°)"),
    POSITIVE("Positive (+3° Aesthetic)"),
    NEGATIVE("Negative (-2°)")
}

data class FacialMetrics(
    val symmetryPercent: Int = 0,
    val jawlineDefinitionPercent: Int = 0,
    val canthalTilt: CanthalTilt = CanthalTilt.NEUTRAL,
    val goldenRatioMatchPercent: Int = 0,
    val skinQualityPercent: Int = 0
) {
    val overallRating: Double
        get() = if (symmetryPercent == 0 && jawlineDefinitionPercent == 0 && goldenRatioMatchPercent == 0 && skinQualityPercent == 0) {
            0.0
        } else {
            (symmetryPercent + jawlineDefinitionPercent + goldenRatioMatchPercent + skinQualityPercent) / 40.0
        }
}

data class FaceScanRecord(
    val id: String,
    val timestamp: Long,
    val metrics: FacialMetrics,
    val scanNotes: String,
    val photoUri: String? = null
)

data class SoftmaxingRoutine(
    val morningSkincareDone: Boolean = false,
    val nightSkincareDone: Boolean = false,
    val spfApplied: Boolean = false,
    val mewingMinutesLogged: Int = 0,
    val mewingTargetMinutes: Int = 30,
    val iceFacialColdShowerDone: Boolean = false,
    val waterGlassesLogged: Int = 0,
    val waterGlassesTarget: Int = 8,
    val postureExercisesDone: Boolean = false
) {
    val completedHabitCount: Int
        get() = (if (morningSkincareDone) 1 else 0) +
                (if (nightSkincareDone) 1 else 0) +
                (if (spfApplied) 1 else 0) +
                (if (mewingMinutesLogged >= mewingTargetMinutes) 1 else 0) +
                (if (iceFacialColdShowerDone) 1 else 0) +
                (if (waterGlassesLogged >= waterGlassesTarget) 1 else 0) +
                (if (postureExercisesDone) 1 else 0)

    val totalHabitsCount: Int = 7
    val progressFraction: Float get() = if (totalHabitsCount > 0) completedHabitCount.toFloat() / totalHabitsCount else 0f
}

enum class LooksmaxTier(val tierName: String, val minXp: Int, val badge: String) {
    BRONZE("Bronze Recruit", 0, "🥉"),
    SILVER("Silver Aesthetic", 250, "🥈"),
    GOLD("Gold Refined", 600, "🥇"),
    DIAMOND("Diamond Sculpted", 1200, "💎"),
    APEX_CHAD("Apex Chad", 2500, "👑")
}
