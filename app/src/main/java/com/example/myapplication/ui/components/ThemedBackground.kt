package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.myapplication.R
import com.example.myapplication.ui.theme.ThemeManager

/**
 * ThemedBackground - Shared background component matching iOS style
 * 
 * Light Mode: background_general.png with animated clouds overlay
 * Dark Mode: night_background.png with pulsing stars overlay (glowing effect)
 * 
 * Use this component as the background for all screens to maintain consistency
 * with the iOS app's visual style.
 */
@Composable
fun ThemedBackground(
    modifier: Modifier = Modifier,
    showClouds: Boolean = true  // Can disable clouds for performance if needed
) {
    val themeManager = ThemeManager.getInstance(LocalContext.current)
    val isDarkMode by themeManager.isDarkMode.collectAsState()
    
    Box(modifier = modifier.fillMaxSize()) {
        if (isDarkMode) {
            DarkModeBackground()
        } else {
            LightModeBackground(showClouds = showClouds)
        }
    }
}

/**
 * Dark mode background - matches iOS DarkThemeBackground
 * Uses night image with optional pulsing stars for glow effect
 */
@Composable
private fun DarkModeBackground() {
    // Pulsing animation for stars (glow effect)
    val infiniteTransition = rememberInfiniteTransition(label = "starsGlow")
    val starsAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starsAlpha"
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Base night background
        Image(
            painter = painterResource(id = R.drawable.night_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        // Stars overlay with pulsing glow effect
        Image(
            painter = painterResource(id = R.drawable.stars),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(starsAlpha)
        )
    }
}

/**
 * Light mode background - matches iOS style
 * Uses background_general with seamless animated clouds
 */
@Composable
private fun LightModeBackground(showClouds: Boolean) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.background_general),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        // Animated clouds overlay
        if (showClouds) {
            AnimatedClouds()
        }
    }
}
