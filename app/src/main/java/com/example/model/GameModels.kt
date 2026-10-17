package com.example.model

enum class MiniGameType(val title: String, val subtitle: String, val iconEmoji: String) {
    SUBWAY_SURFER("Subway Track Runner", "3-Lane endless runner: Dodge trains & collect coins", "🏃"),
    TALKING_TOM("Talking Tom Cat", "Voice mimic, petting, snacks, funny pranks & sleep", "🐱"),
    BLOCK_PUZZLE("Retro Block Stacker", "Drop falling blocks, clear lines & score combos", "🧱"),
    SPIN_WHEEL("Spin the Wheel", "Lucky spin for coin jackpot", "🎡"),
    SCRATCH_CARD("Scratch Card", "Scratch & reveal instant rewards", "🎫"),
    QUIZ_TRIVIA("Knowledge Quiz", "Test brainpower: Win coins, lose on wrong", "🧠"),
    DICE_ROLL("High / Low Dice", "Wager coins and roll the duel", "🎲"),
    MEMORY_CARDS("Memory Match", "Flip cards & find matching pairs", "🃏"),
    TAP_REFLEX("Speed Coin Reflex", "Catch falling gold coins in 10s", "⚡")
}

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

val SAMPLE_QUIZ_QUESTIONS = listOf(
    QuizQuestion(
        id = "q1",
        question = "What is considered the master gland of the human endocrine system?",
        options = listOf("Thyroid", "Pituitary Gland", "Adrenal", "Pancreas"),
        correctIndex = 1,
        explanation = "The pituitary gland produces critical hormones regulating growth and metabolism."
    ),
    QuizQuestion(
        id = "q2",
        question = "Which Indian emperor embraced Buddhism after the Kalinga war?",
        options = listOf("Chandragupta Maurya", "Samudragupta", "Ashoka the Great", "Harshavardhana"),
        correctIndex = 2,
        explanation = "Emperor Ashoka dedicated his life to Dhamma and non-violence after witnessing the Kalinga war."
    ),
    QuizQuestion(
        id = "q3",
        question = "In physics, what is the unit of electrical resistance?",
        options = listOf("Volt", "Ampere", "Ohm", "Watt"),
        correctIndex = 2,
        explanation = "Electrical resistance is measured in Ohms (Ω), named after Georg Simon Ohm."
    ),
    QuizQuestion(
        id = "q4",
        question = "Which nutrient is the body's primary and most efficient source of brain energy?",
        options = listOf("Glucose / Carbohydrates", "Dietary Fiber", "Trans Fat", "Creatine"),
        correctIndex = 0,
        explanation = "Glucose is the essential fuel cross-transmitted across the blood-brain barrier for cognitive function."
    ),
    QuizQuestion(
        id = "q5",
        question = "What does GST stand for in Indian business taxation?",
        options = listOf("General Service Tax", "Goods and Services Tax", "Government Sales Tariff", "Gross Standard Tax"),
        correctIndex = 1,
        explanation = "GST stands for Goods and Services Tax, implemented nationwide in India on July 1, 2017."
    ),
    QuizQuestion(
        id = "q6",
        question = "In traditional Jap Mala, how many counting beads are on a complete rosary cord?",
        options = listOf("54", "101", "108", "121"),
        correctIndex = 2,
        explanation = "108 represents the sacred cosmic alignment, distance ratios, and nadi intersections in Vedic tradition."
    )
)

data class LeaderboardPlayer(
    val rank: Int,
    val id: String,
    val name: String,
    val coins: Int,
    val level: Int,
    val avatarEmoji: String,
    val isCurrentUser: Boolean = false
)

data class LeaderboardState(
    val currentSeasonName: String = "Season October 2026",
    val daysUntilMonthlyReset: Int = 14,
    val topPlayers: List<LeaderboardPlayer> = emptyList(),
    val userRank: Int = 1,
    val totalCoinsRanked: Int = 10
)
