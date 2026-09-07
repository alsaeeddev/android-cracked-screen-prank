package com.alsaeeddev.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
    darkColorScheme(
        primary = PolishDarkPrimary,
        onPrimary = PolishDarkBackground,
        primaryContainer = PolishDarkPrimaryContainer,
        onPrimaryContainer = PolishDarkPrimary,
        secondary = PolishDarkPrimary,
        onSecondary = PolishDarkBackground,
        background = PolishDarkBackground,
        onBackground = PolishDarkOnSurface,
        surface = PolishDarkSurface,
        onSurface = PolishDarkOnSurface,
        surfaceVariant = PolishDarkCard,
        onSurfaceVariant = PolishDarkOnSurfaceVariant,
        outline = PolishDarkBorder
    )

private val LightColorScheme =
    lightColorScheme(
        primary = PolishPrimary,
        onPrimary = Color.White,
        primaryContainer = PolishPrimaryContainer,
        onPrimaryContainer = PolishOnPrimaryContainer,
        secondary = PolishPrimary,
        onSecondary = Color.White,
        background = PolishBackgroundLight,
        onBackground = PolishOnSurfaceLight,
        surface = PolishSurfaceLight,
        onSurface = PolishOnSurfaceLight,
        surfaceVariant = PolishCardLight,
        onSurfaceVariant = PolishOnSurfaceVariantLight,
        outline = PolishBorderLight
    )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Emphasize the curated Professional Polish design theme
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
