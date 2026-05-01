package com.trm.warsawtransportmap

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import warsawtransportmap.composeapp.generated.resources.Res
import warsawtransportmap.composeapp.generated.resources.inter_variable
import warsawtransportmap.composeapp.generated.resources.outfit_variable

@Composable
internal fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
    typography = appTypography(),
    content = content,
  )
}

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF0061A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF535F70),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD7E3F7),
    onSecondaryContainer = Color(0xFF101C2B),
    tertiary = Color(0xFF006874),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF97F0FF),
    onTertiaryContainer = Color(0xFF001F24),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF8F4F0),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFF8F4F0),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDFE2EB),
    onSurfaceVariant = Color(0xFF43474E),
    outline = Color(0xFF73777F),
    outlineVariant = Color(0xFFC3C7CF),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF2F3033),
    inverseOnSurface = Color(0xFFF1F0F4),
    inversePrimary = Color(0xFF9ECAFF),
    surfaceTint = Color(0xFF0061A4),
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFF9ECAFF),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFFBBC7DB),
    onSecondary = Color(0xFF253140),
    secondaryContainer = Color(0xFF3B4858),
    onSecondaryContainer = Color(0xFFD7E3F7),
    tertiary = Color(0xFF4FD8EB),
    onTertiary = Color(0xFF00363D),
    tertiaryContainer = Color(0xFF004F58),
    onTertiaryContainer = Color(0xFF97F0FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF161618),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF161618),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF43474E),
    onSurfaceVariant = Color(0xFFC3C7CF),
    outline = Color(0xFF8D9199),
    outlineVariant = Color(0xFF43474E),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE2E2E6),
    inverseOnSurface = Color(0xFF2F3033),
    inversePrimary = Color(0xFF0061A4),
    surfaceTint = Color(0xFF9ECAFF),
  )

@Composable
private fun appTypography(): Typography {
  val outfitFontFamily =
    FontFamily(
      Font(Res.font.outfit_variable, FontWeight.Normal),
      Font(Res.font.outfit_variable, FontWeight.Medium),
      Font(Res.font.outfit_variable, FontWeight.SemiBold),
      Font(Res.font.outfit_variable, FontWeight.Bold),
    )
  val interFontFamily =
    FontFamily(
      Font(Res.font.inter_variable, FontWeight.Normal),
      Font(Res.font.inter_variable, FontWeight.Medium),
    )
  val defaultTypography = Typography()
  return Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = outfitFontFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = outfitFontFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = outfitFontFamily),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = outfitFontFamily),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = outfitFontFamily),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = outfitFontFamily),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = outfitFontFamily),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = outfitFontFamily),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = outfitFontFamily),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = interFontFamily),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = interFontFamily),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = interFontFamily),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = interFontFamily),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = interFontFamily),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = interFontFamily),
  )
}
