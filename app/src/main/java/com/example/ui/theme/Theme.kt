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
    primary = NeonCyan,
    secondary = ElectricBlue,
    tertiary = NeonTeal,
    background = DarkNavyBackground,
    surface = DarkNavySurface,
    onPrimary = DarkNavyBackground,
    onSecondary = TextWhite,
    onTertiary = DarkNavyBackground,
    onBackground = TextWhite,
    onSurface = TextWhite,
    outline = GlassBorder
  )

private val LightColorScheme = DarkColorScheme // Keep it consistently dark and premium as requested!

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force Dark theme for premium feeling!
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve our brand palette
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
