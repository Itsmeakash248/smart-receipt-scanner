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
    primary = Blue600,
    secondary = Slate500,
    tertiary = Green700,
    background = Color(0xFF0F172A), // Slate900 when dark
    surface = Color(0xFF1E293B), // Slate800 when dark
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = Blue900,
    onPrimaryContainer = Blue100,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Slate400,
    outline = Slate600
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Blue600,
    secondary = Slate600,
    tertiary = Green700,
    background = Slate50,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Slate900,
    onSurface = Slate900,
    primaryContainer = Blue100,
    onPrimaryContainer = Blue900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate500,
    outline = Slate200
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled to enforce branding of Professional Polish theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
