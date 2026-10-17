package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.model.AppThemeAccent

// Pure True Black OLED & Crisp Monochrome Palette
val PitchBlack = Color(0xFF000000)
val DarkCharcoal = Color(0xFF101012)
val DarkCardSurface = Color(0xFF18181B)
val DarkBorder = Color(0xFF2E2E33)
val TextPureWhite = Color(0xFFFFFFFF)
val TextSilver = Color(0xFFA1A1AA)
val TextMutedGray = Color(0xFF71717A)

// Pure Light Monochrome Palette
val PureWhite = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFFAFAFA)
val LightCardSurface = Color(0xFFF4F4F5)
val LightBorder = Color(0xFFE4E4E7)
val TextJetBlack = Color(0xFF09090B)
val TextCharcoalMuted = Color(0xFF52525B)

// Subtle Coin & Accent Accouterment
val GoldCoinYellow = Color(0xFFFFD700)

fun getMonochromeAccentColors(accent: AppThemeAccent, isDark: Boolean): Pair<Color, Color> {
    return if (isDark) {
        when (accent) {
            AppThemeAccent.MONOCHROME_EXECUTIVE -> Pair(Color(0xFFFFFFFF), Color(0xFFE4E4E7))
            AppThemeAccent.STEEL_SILVER -> Pair(Color(0xFFD4D4D8), Color(0xFFA1A1AA))
            AppThemeAccent.MINIMAL_OLED -> Pair(Color(0xFFFFFFFF), Color(0xFF71717A))
            AppThemeAccent.GOLD_ACCENT -> Pair(Color(0xFFFFD700), Color(0xFFE4E4E7))
        }
    } else {
        when (accent) {
            AppThemeAccent.MONOCHROME_EXECUTIVE -> Pair(Color(0xFF000000), Color(0xFF27272A))
            AppThemeAccent.STEEL_SILVER -> Pair(Color(0xFF27272A), Color(0xFF52525B))
            AppThemeAccent.MINIMAL_OLED -> Pair(Color(0xFF09090B), Color(0xFF3F3F46))
            AppThemeAccent.GOLD_ACCENT -> Pair(Color(0xFF1A1A1A), Color(0xFFC5A000))
        }
    }
}
