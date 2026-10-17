package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.AppThemeAccent

@Composable
fun ApexLifeTheme(
    accent: AppThemeAccent = AppThemeAccent.MONOCHROME_EXECUTIVE,
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val (primaryColor, secondaryColor) = getMonochromeAccentColors(accent, darkTheme)
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color.White,
            onPrimary = Color.Black,
            primaryContainer = Color(0xFF18181B),
            onPrimaryContainer = Color.White,
            secondary = Color(0xFFD4D4D8),
            onSecondary = Color.Black,
            secondaryContainer = Color(0xFF27272A),
            onSecondaryContainer = Color(0xFFA1A1AA),
            background = PitchBlack,
            onBackground = TextPureWhite,
            surface = PitchBlack,
            onSurface = TextPureWhite,
            surfaceVariant = DarkCardSurface,
            onSurfaceVariant = TextSilver,
            outline = DarkBorder,
            error = Color(0xFFEF4444)
        )
    } else {
        lightColorScheme(
            primary = Color.Black,
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE4E4E7),
            onPrimaryContainer = Color.Black,
            secondary = Color(0xFF27272A),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFF4F4F5),
            onSecondaryContainer = Color(0xFF52525B),
            background = PureWhite,
            onBackground = Color.Black,
            surface = PureWhite,
            onSurface = Color.Black,
            surfaceVariant = LightCardSurface,
            onSurfaceVariant = TextCharcoalMuted,
            outline = LightBorder,
            error = Color(0xFFDC2626)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
