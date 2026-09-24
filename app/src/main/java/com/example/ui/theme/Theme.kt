package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Lumina Design System Color Palette
 *
 * Specific brand colors:
 * - Background: #F8F9FB (Clean, minimal, premium off-white surface)
 * - Primary: #5B5FEF (Vibrant signature electric indigo)
 */
object LuminaPalette {
    // Brand Primary Palette
    val Primary = Color(0xFF5B5FEF)
    val PrimaryDark = Color(0xFF474BD9)
    val PrimaryLight = Color(0xFFEEF0FD)
    val PrimaryGlow = Color(0x335B5FEF)

    // Neutral Surfaces & Text
    val Background = Color(0xFFF8F9FB)
    val Surface = Color(0xFFFFFFFF)
    val CardSurface = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF111827)
    val TextSecondary = Color(0xFF6B7280)
    val TextTertiary = Color(0xFF9CA3AF)
    val BorderSubtle = Color(0xFFE5E7EB)
    val Divider = Color(0xFFF1F3F5)

    // Functional & Status Accents
    val AccentAmber = Color(0xFFF59E0B)
    val AccentGreen = Color(0xFF10B981)
    val AccentGreenLight = Color(0xFFECFDF5)
    val AccentRose = Color(0xFFF43F5E)
    val AccentRoseLight = Color(0xFFFFF1F2)
    val AccentViolet = Color(0xFF8B5CF6)
}

/**
 * Lumina Typography Constants
 * Defines consistent typography constants and font hierarchy across the entire app.
 */
object LuminaTypography {
    // Hero & Large Displays
    val HeroDisplay = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp,
        color = LuminaPalette.TextPrimary
    )

    // Headlines
    val HeadlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp,
        color = LuminaPalette.TextPrimary
    )

    val HeadlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = LuminaPalette.TextPrimary
    )

    // Titles
    val TitleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = LuminaPalette.TextPrimary
    )

    val TitleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = LuminaPalette.TextPrimary
    )

    val TitleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = LuminaPalette.TextPrimary
    )

    // Body text
    val BodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp,
        color = LuminaPalette.TextPrimary
    )

    val BodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = LuminaPalette.TextSecondary
    )

    val BodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = LuminaPalette.TextSecondary
    )

    // Labels & Action Buttons
    val LabelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
        color = LuminaPalette.TextPrimary
    )

    val LabelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp,
        color = LuminaPalette.TextSecondary
    )

    val LabelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.3.sp,
        color = LuminaPalette.TextTertiary
    )

    // Commercial & E-commerce Specific Styles
    val PriceLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.3).sp,
        color = LuminaPalette.TextPrimary
    )

    val PriceMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        color = LuminaPalette.TextPrimary
    )

    val PriceSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        color = LuminaPalette.TextPrimary
    )

    val BadgeText = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.4.sp
    )

    val Caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        color = LuminaPalette.TextTertiary
    )
}

// Convenient top-level typography constants
val LuminaHeroDisplay = LuminaTypography.HeroDisplay
val LuminaHeadlineLarge = LuminaTypography.HeadlineLarge
val LuminaHeadlineMedium = LuminaTypography.HeadlineMedium
val LuminaTitleLarge = LuminaTypography.TitleLarge
val LuminaTitleMedium = LuminaTypography.TitleMedium
val LuminaTitleSmall = LuminaTypography.TitleSmall
val LuminaBodyLarge = LuminaTypography.BodyLarge
val LuminaBodyMedium = LuminaTypography.BodyMedium
val LuminaBodySmall = LuminaTypography.BodySmall
val LuminaLabelLarge = LuminaTypography.LabelLarge
val LuminaLabelMedium = LuminaTypography.LabelMedium
val LuminaLabelSmall = LuminaTypography.LabelSmall
val LuminaPriceLarge = LuminaTypography.PriceLarge
val LuminaPriceMedium = LuminaTypography.PriceMedium
val LuminaPriceSmall = LuminaTypography.PriceSmall
val LuminaBadgeText = LuminaTypography.BadgeText
val LuminaCaption = LuminaTypography.Caption

/**
 * Creates Material 3 Typography initialized with Lumina design tokens
 */
fun createMaterialTypography(): Typography = Typography(
    displayLarge = LuminaTypography.HeroDisplay,
    headlineLarge = LuminaTypography.HeadlineLarge,
    headlineMedium = LuminaTypography.HeadlineMedium,
    titleLarge = LuminaTypography.TitleLarge,
    titleMedium = LuminaTypography.TitleMedium,
    titleSmall = LuminaTypography.TitleSmall,
    bodyLarge = LuminaTypography.BodyLarge,
    bodyMedium = LuminaTypography.BodyMedium,
    bodySmall = LuminaTypography.BodySmall,
    labelLarge = LuminaTypography.LabelLarge,
    labelMedium = LuminaTypography.LabelMedium,
    labelSmall = LuminaTypography.LabelSmall
)

private val DarkColorScheme = darkColorScheme(
    primary = LuminaPalette.Primary,
    onPrimary = Color.White,
    primaryContainer = LuminaPalette.PrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = LuminaPalette.AccentViolet,
    background = Color(0xFF0F1117),
    surface = Color(0xFF1A1D27),
    onBackground = Color(0xFFF3F4F6),
    onSurface = Color(0xFFF3F4F6),
    surfaceVariant = Color(0xFF242838),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0xFF2D3245)
)

val LightColorScheme = lightColorScheme(
    primary = LuminaPalette.Primary,         // #5B5FEF
    onPrimary = Color.White,
    primaryContainer = LuminaPalette.PrimaryLight,  // #EEF0FD
    onPrimaryContainer = LuminaPalette.PrimaryDark,
    secondary = Color(0xFF2D3142),
    onSecondary = Color.White,
    background = LuminaPalette.Background,   // #F8F9FB
    surface = LuminaPalette.Surface,         // #FFFFFF
    onBackground = LuminaPalette.TextPrimary,
    onSurface = LuminaPalette.TextPrimary,
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = LuminaPalette.TextSecondary,
    outline = LuminaPalette.BorderSubtle,
    outlineVariant = LuminaPalette.Divider,
    error = LuminaPalette.AccentRose
)

val LuminaColorScheme = LightColorScheme
val LuminaLightColorScheme = LightColorScheme

/**
 * Lumina Design System Theme Provider
 */
@Composable
fun LuminaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent Lumina signature #5B5FEF styling by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Backward-compatible alias for existing app callers
 */
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = LuminaTheme(
    darkTheme = darkTheme,
    dynamicColor = dynamicColor,
    content = content
)
