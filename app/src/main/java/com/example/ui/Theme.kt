package com.example.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Lumina Design System - Color Palette
 *
 * Specific colors:
 * - Background: #F8F9FB (Clean, minimal, premium off-white surface)
 * - Primary: #5B5FEF (Signature vibrant electric indigo)
 */
@Immutable
object LuminaColors {
    // Specific Lumina core colors
    val Background = Color(0xFFF8F9FB)
    val Primary = Color(0xFF5B5FEF)

    // Primary palette shades
    val PrimaryDark = Color(0xFF474BD9)
    val PrimaryLight = Color(0xFFEEF0FD)
    val PrimaryGlow = Color(0x335B5FEF)

    // Neutral Surfaces & Hierarchy
    val Surface = Color(0xFFFFFFFF)
    val CardSurface = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF111827)
    val TextSecondary = Color(0xFF6B7280)
    val TextTertiary = Color(0xFF9CA3AF)
    val BorderSubtle = Color(0xFFE5E7EB)
    val Divider = Color(0xFFF1F3F5)

    // Functional & Status Accents
    val AccentGreen = Color(0xFF10B981)
    val AccentGreenLight = Color(0xFFECFDF5)
    val AccentAmber = Color(0xFFF59E0B)
    val AccentAmberLight = Color(0xFFFEF3C7)
    val AccentRose = Color(0xFFF43F5E)
    val AccentRoseLight = Color(0xFFFFF1F2)
    val AccentViolet = Color(0xFF8B5CF6)
}

/**
 * Lumina Typography Scale
 * Defines the complete typographic hierarchy for the Lumina design system.
 */
@Immutable
object LuminaTypographyScale {
    // Display Scale
    val DisplayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
        color = LuminaColors.TextPrimary
    )

    val DisplayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.4).sp,
        color = LuminaColors.TextPrimary
    )

    val DisplaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.3).sp,
        color = LuminaColors.TextPrimary
    )

    // Headline Scale
    val HeadlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp,
        color = LuminaColors.TextPrimary
    )

    val HeadlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = LuminaColors.TextPrimary
    )

    val HeadlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = LuminaColors.TextPrimary
    )

    // Title Scale
    val TitleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = LuminaColors.TextPrimary
    )

    val TitleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = LuminaColors.TextPrimary
    )

    val TitleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = LuminaColors.TextPrimary
    )

    // Body Scale
    val BodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp,
        color = LuminaColors.TextPrimary
    )

    val BodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = LuminaColors.TextSecondary
    )

    val BodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = LuminaColors.TextSecondary
    )

    // Label Scale
    val LabelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
        color = LuminaColors.TextPrimary
    )

    val LabelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp,
        color = LuminaColors.TextSecondary
    )

    val LabelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.3.sp,
        color = LuminaColors.TextTertiary
    )

    // Commerce-specific Typography
    val PriceLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.3).sp,
        color = LuminaColors.TextPrimary
    )

    val PriceMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        color = LuminaColors.TextPrimary
    )

    val PriceSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        color = LuminaColors.TextPrimary
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
        color = LuminaColors.TextTertiary
    )
}

/**
 * Material 3 Typography conversion
 */
val LuminaMaterialTypography = Typography(
    displayLarge = LuminaTypographyScale.DisplayLarge,
    displayMedium = LuminaTypographyScale.DisplayMedium,
    displaySmall = LuminaTypographyScale.DisplaySmall,
    headlineLarge = LuminaTypographyScale.HeadlineLarge,
    headlineMedium = LuminaTypographyScale.HeadlineMedium,
    headlineSmall = LuminaTypographyScale.HeadlineSmall,
    titleLarge = LuminaTypographyScale.TitleLarge,
    titleMedium = LuminaTypographyScale.TitleMedium,
    titleSmall = LuminaTypographyScale.TitleSmall,
    bodyLarge = LuminaTypographyScale.BodyLarge,
    bodyMedium = LuminaTypographyScale.BodyMedium,
    bodySmall = LuminaTypographyScale.BodySmall,
    labelLarge = LuminaTypographyScale.LabelLarge,
    labelMedium = LuminaTypographyScale.LabelMedium,
    labelSmall = LuminaTypographyScale.LabelSmall
)

/**
 * Lumina Light Color Scheme using Material 3 lightColorScheme.
 *
 * Specific colors:
 * - background: #F8F9FB
 * - primary: #5B5FEF
 * - onPrimary: #FFFFFF (clean white contrast on vibrant indigo)
 * - surface: #FFFFFF (crisp elevated container cards)
 * - onSurface: #111827 (refined deep charcoal for crystal-clear readability)
 */
val LuminaLightColorScheme = lightColorScheme(
    primary = Color(0xFF5B5FEF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEEF0FD),
    onPrimaryContainer = Color(0xFF474BD9),
    secondary = Color(0xFF2D3142),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFF8F9FB),
    onBackground = Color(0xFF111827),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Color(0xFFE5E7EB),
    outlineVariant = Color(0xFFF1F3F5),
    error = Color(0xFFF43F5E),
    onError = Color(0xFFFFFFFF)
)

/**
 * Lumina Color Scheme alias for standard access
 */
val LuminaColorScheme = LuminaLightColorScheme

val LuminaDarkColorScheme = darkColorScheme(
    primary = LuminaColors.Primary,
    onPrimary = Color.White,
    primaryContainer = LuminaColors.PrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = LuminaColors.AccentViolet,
    background = Color(0xFF0F1117),
    surface = Color(0xFF1A1D27),
    onBackground = Color(0xFFF3F4F6),
    onSurface = Color(0xFFF3F4F6),
    surfaceVariant = Color(0xFF242838),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0xFF2D3245)
)

val LocalLuminaColors = staticCompositionLocalOf { LuminaColors }
val LocalLuminaTypography = staticCompositionLocalOf { LuminaTypographyScale }

/**
 * Lumina Design System root theme composable
 */
@Composable
fun LuminaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> LuminaDarkColorScheme
        else -> LuminaLightColorScheme
    }

    CompositionLocalProvider(
        LocalLuminaColors provides LuminaColors,
        LocalLuminaTypography provides LuminaTypographyScale
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LuminaMaterialTypography,
            content = content
        )
    }
}
