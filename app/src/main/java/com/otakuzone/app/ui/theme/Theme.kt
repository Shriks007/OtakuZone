package com.otakuzone.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Anikage uses Rubik font — we use system sans-serif as fallback (Rubik not bundled)
// If desired, add rubik-latin.woff2 to assets and load via custom font provider
val RubikFamily = FontFamily.Default  // Fallback; replace with Rubik if font file added

val OtakuTypography = androidx.compose.material3.Typography(
    // Display — hero titles, spotlight
    displayLarge = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        color = Fg
    ),
    displayMedium = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        color = Fg
    ),
    displaySmall = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        color = Fg
    ),
    // Headlines — section titles
    headlineLarge = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        color = Fg
    ),
    headlineMedium = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = Fg
    ),
    headlineSmall = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = Fg
    ),
    // Title — card titles, list items
    titleLarge = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = Fg
    ),
    titleMedium = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = Fg
    ),
    titleSmall = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = Fg
    ),
    // Body
    bodyLarge = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = Fg
    ),
    bodyMedium = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = FgMuted
    ),
    bodySmall = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        color = FgMuted
    ),
    // Labels
    labelLarge = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        color = Fg
    ),
    labelMedium = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        color = FgMuted
    ),
    labelSmall = TextStyle(
        fontFamily = RubikFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        color = FgMuted
    )
)

private val AnikageDarkColorScheme = darkColorScheme(
    primary            = AccentWhite,
    onPrimary          = AccentFg,
    primaryContainer   = Accent200,
    onPrimaryContainer = Fg,

    secondary          = Accent600,
    onSecondary        = AccentFg,
    secondaryContainer = Accent300,
    onSecondaryContainer = Fg,

    tertiary           = Warning,
    onTertiary         = Color(0xFF101010),

    background         = Background,
    onBackground       = Fg,

    surface            = Surface,
    onSurface          = Fg,
    surfaceVariant     = SurfaceCard,
    onSurfaceVariant   = FgMuted,

    surfaceContainer       = SurfaceCard,
    surfaceContainerHigh   = SurfaceElevated,
    surfaceContainerHighest = SurfaceInput,

    outline            = BorderColor,
    outlineVariant     = DividerColor,

    error              = Danger,
    onError            = Fg,
    errorContainer     = Color(0xFF3B0A0A),
    onErrorContainer   = Color(0xFFFFDAD6),

    inverseSurface     = Fg,
    inverseOnSurface   = Background,
    inversePrimary     = Color(0xFF0A0A0A),

    scrim              = Color(0xCC000000)
)

@Composable
fun OtakuZoneTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AnikageDarkColorScheme,
        typography = OtakuTypography,
        content = content
    )
}
