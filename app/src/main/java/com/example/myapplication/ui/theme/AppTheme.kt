package com.example.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Light Theme Colors
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4A90E2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF1565C0),
    secondary = Color(0xFFFFC107),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFFF9C4),
    onSecondaryContainer = Color(0xFFF57F17),
    tertiary = Color(0xFF4CAF50),
    onTertiary = Color.White,
    error = Color(0xFFE53935),
    onError = Color.White,
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF212121),
    surface = Color.White,
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF424242)
)

// Dark Theme Colors
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1976D2),
    onPrimaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFFFFD54F),
    onSecondary = Color(0xFF5D4037),
    secondaryContainer = Color(0xFFF57C00),
    onSecondaryContainer = Color(0xFFFFE082),
    tertiary = Color(0xFF81C784),
    onTertiary = Color(0xFF1B5E20),
    error = Color(0xFFEF5350),
    onError = Color(0xFFFFEBEE),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFBDBDBD)
)

@Composable
fun AppTheme(
    themeManager: ThemeManager = ThemeManager.getInstance(LocalContext.current),
    content: @Composable () -> Unit
) {
    val isDarkMode by themeManager.isDarkMode.collectAsState()
    
    val colorScheme = if (isDarkMode) DarkColorScheme else LightColorScheme
    
    CompositionLocalProvider(LocalThemeManager provides themeManager) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
