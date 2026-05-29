package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryYellow,
    secondary = SecondaryGold,
    tertiary = AccentCyan,
    background = DarkNavyBg,
    surface = DarkCardSurface,
    onPrimary = DarkNavyBg,
    onSecondary = DarkNavyBg,
    onTertiary = LightAccentText,
    onBackground = LightAccentText,
    onSurface = LightAccentText,
    primaryContainer = DarkCardSurface,
    onPrimaryContainer = SecondaryGold
  )

private val LightColorScheme = DarkColorScheme // Default to unified luxury dark theme as requested

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme as requested by branding requirements
  dynamicColor: Boolean = false, // Use our custom construction color palette instead of dynamic wallpapers
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
