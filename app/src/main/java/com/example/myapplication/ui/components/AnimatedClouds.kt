package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R

/**
 * Animated clouds that scroll horizontally in a seamless loop.
 * Matches the iOS AnimatedClouds implementation from RegisterScreen.swift
 * 
 * Two cloud images are positioned side by side and scroll left infinitely.
 * When one image scrolls off-screen, it seamlessly wraps around.
 */
@Composable
fun AnimatedClouds(
    modifier: Modifier = Modifier,
    durationMillis: Int = 30000 // 30 seconds for full loop (matches iOS duration: 30)
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenWidthPx = with(LocalDensity.current) { screenWidthDp.toPx() }
    
    // Infinite transition for seamless animation
    val infiniteTransition = rememberInfiniteTransition(label = "clouds")
    
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -screenWidthPx,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloudOffset"
    )
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // First cloud image
        Image(
            painter = painterResource(id = R.drawable.clouds),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.67f) // Similar to iOS height / 1.5
                .graphicsLayer {
                    translationX = offset
                }
        )
        
        // Second cloud image (seamless continuation)
        Image(
            painter = painterResource(id = R.drawable.clouds),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.33f) // Similar to iOS height / 3
                .graphicsLayer {
                    translationX = offset + screenWidthPx
                }
        )
    }
}
