package com.example.model

enum class SportType(val title: String) {
    RUNNING("Endurance Run"),
    SPRINT("Sprint & Speed"),
    GYM_LIFTING("Hypertrophy / Lift"),
    CYCLING("Cycling Cadence"),
    FOOTBALL("Field & Agility")
}

data class WorkoutSession(
    val id: String,
    val sportType: SportType,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val distanceKm: Double = 0.0,
    val notes: String = "",
    val timestamp: Long
)

enum class SorenessLevel(val label: String, val colorHex: Long) {
    FRESH("Fresh", 0xFFFFFFFF),
    TIGHT("Tight", 0xFF888888),
    SORE("Sore", 0xFF444444)
}

data class AthleteStats(
    val todaySteps: Int = 0,
    val stepGoal: Int = 10000,
    val distanceKm: Double = 0.0,
    val activeCaloriesKcal: Int = 0,
    val sleepHours: Double = 0.0,
    val restingHeartRateBpm: Int = 0
) {
    val readinessScore: Int
        get() {
            if (todaySteps == 0 && sleepHours == 0.0 && restingHeartRateBpm == 0) {
                return 0
            }
            val sleepFactor = (sleepHours / 8.0 * 50).coerceIn(10.0, 50.0)
            val loadFactor = if (todaySteps > 18000) 25.0 else 40.0
            val hrFactor = if (restingHeartRateBpm in 50..65) 10.0 else 5.0
            return (sleepFactor + loadFactor + hrFactor).toInt().coerceIn(1, 100)
        }

    val readinessStatus: String
        get() = when {
            readinessScore == 0 -> "Baseline 0 (Log activity to calculate)"
            readinessScore >= 80 -> "Optimal (100% Push Permitted)"
            readinessScore >= 60 -> "Moderate (Standard Volume)"
            else -> "Active Recovery Recommended"
        }
}
