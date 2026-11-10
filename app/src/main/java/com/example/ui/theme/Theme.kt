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

private val DarkColorScheme = darkColorScheme(
    primary = NovaBlueAccent,
    onPrimary = Color.White,
    primaryContainer = NovaBlueDeep,
    onPrimaryContainer = Color.White,
    secondary = NovaCyanAccent,
    onSecondary = NovaNavyDark,
    secondaryContainer = NovaNavySurface,
    onSecondaryContainer = Color.White,
    tertiary = NovaGoldAccent,
    onTertiary = NovaNavyDark,
    background = NovaBgDark,
    onBackground = NovaTextPrimaryDark,
    surface = NovaSurfaceDark,
    onSurface = NovaTextPrimaryDark,
    surfaceVariant = NovaSurfaceVariantDark,
    onSurfaceVariant = NovaTextSecondaryDark,
    outline = NovaBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = NovaBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = NovaNavyDark,
    secondary = NovaBlueDeep,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = NovaNavyDark,
    tertiary = NovaGoldAccent,
    onTertiary = NovaNavyDark,
    background = NovaBgLight,
    onBackground = NovaTextPrimaryLight,
    surface = NovaSurfaceLight,
    onSurface = NovaTextPrimaryLight,
    surfaceVariant = NovaSurfaceVariantLight,
    onSurfaceVariant = NovaTextSecondaryLight,
    outline = NovaBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to preserve our executive brand aesthetic
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
