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

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = AgriPrimaryDark,
    secondary = AgriSecondaryDark,
    tertiary = AgriTertiaryDark,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color(0xFF1B221B),
    onSecondary = Color(0xFF1B221B),
    onBackground = Color(0xFFF1F8E9),
    onSurface = Color(0xFFF1F8E9)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = AgriPrimary,
    secondary = AgriSecondary,
    tertiary = AgriTertiary,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1B221B),
    onSurface = Color(0xFF1B221B)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    if (darkTheme) DarkColorScheme else LightColorScheme


  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
