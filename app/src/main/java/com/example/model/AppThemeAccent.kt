package com.example.model

enum class AppThemeAccent(val displayName: String, val primaryHex: Long, val secondaryHex: Long) {
    MONOCHROME_EXECUTIVE("Monochrome B&W", 0xFFFFFFFF, 0xFFE4E4E7),
    STEEL_SILVER("Titanium Silver", 0xFFD4D4D8, 0xFFA1A1AA),
    MINIMAL_OLED("Minimalist OLED", 0xFFFFFFFF, 0xFF71717A),
    GOLD_ACCENT("Monochrome + Gold", 0xFFFFD700, 0xFFE4E4E7)
}

enum class PersonaType(val title: String, val subtitle: String, val iconName: String, val unlockCost: Int = 0) {
    MALA_JAAP("Mala Jaap", "माला जाप - 108 Bead Counter & Dhyan", "SelfImprovement", unlockCost = 0),
    GAMES("Game Zone", "6 Mini Games: Spin, Scratch, Quiz & Coins", "SportsEsports", unlockCost = 0),
    STUDENT("Student Mode", "Study Studio & Syllabus Tracker", "School", unlockCost = 10),
    BUSINESS("Business Mode", "GST Billing & Khata Ledger", "Payments", unlockCost = 10),
    ATHLETE("Athlete Mode", "Biometrics & Pedometer", "FitnessCenter", unlockCost = 10),
    LOOKSMAX("Looksmax Mode", "AI Symmetry & Softmaxing", "AutoAwesome", unlockCost = 10),
    LEADERBOARD("Leaderboard", "High Scores & Top Rankings", "EmojiEvents", unlockCost = 0),
    PROFILE("My Profile", "User Stats, Wallet & Settings", "AccountCircle", unlockCost = 0)
}

