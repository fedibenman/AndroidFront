package com.example.myapplication.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import kotlinx.coroutines.delay

/**
 * Animated theme toggle button similar to the SwiftUI implementation
 */
@Composable
fun AnimatedThemeToggle(
    modifier: Modifier = Modifier,
    onThemeToggle: (() -> Unit)? = null
) {
    val themeManager = LocalThemeManager.current
    val isDarkMode = themeManager.isDarkMode
    
    // Animation state for the toggle button
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    
    // Icon position animation
    val iconOffset = remember { Animatable(if (isDarkMode) 12.dp.value else -12.dp.value) }
    
    LaunchedEffect(isDarkMode) {
        // Animate rotation when theme changes
        rotation.animateTo(
            targetValue = if (isDarkMode) 360f else 0f,
            animationSpec = tween(500)
        )
        
        // Animate icon position
        iconOffset.animateTo(
            targetValue = if (isDarkMode) -12.dp.value else 12.dp.value,
            animationSpec = tween(300)
        )
        
        // Scale animation
        scale.animateTo(1.1f, tween(100))
        delay(100)
        scale.animateTo(1f, tween(100))
    }
    
    Box(
        modifier = modifier
            .size(60.dp)
            .clip(CircleShape)
            .shadow(8.dp, CircleShape)
            .clickable {
                onThemeToggle?.invoke() ?: themeManager.toggleTheme()
            }
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                rotationZ = rotation.value
            },
        contentAlignment = Alignment.Center
    ) {
        // Background gradient effect
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(
                    if (isDarkMode) {
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(Color(0xFF667eea), Color(0xFF764ba2))
                        )
                    } else {
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(Color(0xFFFFecd2), Color(0xFFFF8a80))
                        )
                    }
                )
        )
        
        // Theme icon that changes based on mode
        if (isDarkMode) {
            // Moon icon for dark mode - keep original colors
            Image(
                painter = painterResource(id = R.drawable.moon),
                contentDescription = "Dark Mode",
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        translationX = iconOffset.value
                    }
            )
        } else {
            // Sun icon for light mode - keep original colors
            Image(
                painter = painterResource(id = R.drawable.sun),
                contentDescription = "Light Mode",
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        translationX = iconOffset.value
                    }
            )
        }
    }
}

/**
 * Simple theme toggle without animation for simpler use cases
 */
@Composable
fun SimpleThemeToggle(
    modifier: Modifier = Modifier,
    onThemeToggle: (() -> Unit)? = null
) {
    val themeManager = LocalThemeManager.current
    
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable {
                onThemeToggle?.invoke() ?: themeManager.toggleTheme()
            },
        contentAlignment = Alignment.Center
    ) {
        if (themeManager.isDarkMode) {
            Image(
                painter = painterResource(id = R.drawable.moon),
                contentDescription = "Switch to Light Mode",
                modifier = Modifier.size(20.dp)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.sun),
                contentDescription = "Switch to Dark Mode",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
