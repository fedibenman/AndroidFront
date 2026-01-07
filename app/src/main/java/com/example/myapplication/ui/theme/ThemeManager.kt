package com.example.myapplication.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ThemeManager handles dark/light theme switching and persistence
 * Similar to the SwiftUI ThemeManager
 */
class ThemeManager(private val context: Context) {
    
    companion object {
        private const val PREF_NAME = "theme_preferences"
        private const val KEY_IS_DARK_MODE = "is_dark_mode"
        
        @Volatile
        private var INSTANCE: ThemeManager? = null
        
        fun getInstance(context: Context): ThemeManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemeManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    private val _isDarkModeFlow = MutableStateFlow(
        sharedPreferences.getBoolean(KEY_IS_DARK_MODE, false)
    )
    
    // StateFlow for collecting in Composables
    val isDarkMode: StateFlow<Boolean> = _isDarkModeFlow.asStateFlow()
    
    // Direct value access
    val isDarkModeValue: Boolean
        get() = _isDarkModeFlow.value
    
    fun toggleTheme() {
        val newValue = !_isDarkModeFlow.value
        _isDarkModeFlow.value = newValue
        sharedPreferences.edit {
            putBoolean(KEY_IS_DARK_MODE, newValue)
        }
    }
    
    fun setDarkMode(darkMode: Boolean) {
        if (_isDarkModeFlow.value != darkMode) {
            _isDarkModeFlow.value = darkMode
            sharedPreferences.edit {
                putBoolean(KEY_IS_DARK_MODE, darkMode)
            }
        }
    }
}

// Composition local for ThemeManager
val LocalThemeManager = staticCompositionLocalOf<ThemeManager> {
    error("No ThemeManager provided")
}

// Extended color scheme for dark theme
val DarkColorSchemeExtended = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF1A1A1A),
    surface = Color(0xFF2D2D2D),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

// Extended color scheme for light theme
val LightColorSchemeExtended = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)
