package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DalefonDarkColorScheme =
  darkColorScheme(
    primary = DalefonPurpleBright,
    onPrimary = Color.White,
    primaryContainer = DalefonPurpleDark,
    onPrimaryContainer = DalefonTextWhite,
    secondary = DalefonPurpleLight,
    onSecondary = Color.Black,
    secondaryContainer = DalefonDarkSurfaceVariant,
    onSecondaryContainer = DalefonTextWhite,
    tertiary = DalefonPurpleNeon,
    background = DalefonDarkBg,
    onBackground = DalefonTextWhite,
    surface = DalefonDarkSurface,
    onSurface = DalefonTextWhite,
    surfaceVariant = DalefonDarkSurfaceVariant,
    onSurfaceVariant = DalefonTextSubtle,
  )

private val DalefonLightColorScheme =
  lightColorScheme(
    primary = DalefonPurplePrimary,
    onPrimary = Color.White,
    primaryContainer = DalefonPurpleLight,
    onPrimaryContainer = DalefonDarkSurface,
    secondary = DalefonPurpleDark,
    onSecondary = Color.White,
    tertiary = DalefonPurpleNeon,
    background = Color(0xFFF9F7FD),
    onBackground = Color(0xFF1E1035),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1E1035),
    surfaceVariant = Color(0xFFEDE9FE),
    onSurfaceVariant = Color(0xFF4C1D95),
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to sleek Siri-inspired dark purple aesthetic
  dynamicColor: Boolean = false, // Keep Dalefon brand purple identity
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DalefonDarkColorScheme else DalefonLightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

