package com.example.ui.theme

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
    primary = Color(0xFF2196F3),
    onPrimary = Color.White,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF141416),
    surface = Color(0xFF1F1F24),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF24242B),
    onSurfaceVariant = Color(0xFFC4C4C4),
    outline = Color(0xFF2E2E36)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF2196F3),
    onPrimary = Color.White,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFF6F8FA),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF141416),
    onSurface = Color(0xFF141416),
    surfaceVariant = Color(0xFFECEFF1),
    onSurfaceVariant = Color(0xFF5E6066),
    outline = Color(0xFFDFE2E6)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
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
