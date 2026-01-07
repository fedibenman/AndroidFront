package com.example.myapplication.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AnimatedThemeToggle(
    modifier: Modifier = Modifier,
    themeManager: ThemeManager = ThemeManager.getInstance(LocalContext.current)
) {
    val isDarkMode by themeManager.isDarkMode.collectAsState()
    val scope = rememberCoroutineScope()
    
    // Animation states
    var sunOffset by remember { mutableStateOf(if (isDarkMode) 150f else 0f) }
    var moonOffset by remember { mutableStateOf(if (isDarkMode) 0f else -150f) }
    
    val sunOffsetAnim by animateFloatAsState(
        targetValue = sunOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "sunOffset"
    )
    
    val moonOffsetAnim by animateFloatAsState(
        targetValue = moonOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "moonOffset"
    )
    
    LaunchedEffect(isDarkMode) {
        if (isDarkMode) {
            sunOffset = 150f
            moonOffset = 0f
        } else {
            sunOffset = 0f
            moonOffset = -150f
        }
    }
    
    Box(
        modifier = modifier
            .size(60.dp, 120.dp)
            .clickable {
                scope.launch {
                    themeManager.toggleTheme()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Sun Icon
        Icon(
            imageVector = Icons.Default.LightMode,
            contentDescription = "Light Mode",
            tint = Color(0xFFFFD700),
            modifier = Modifier
                .size(40.dp)
                .graphicsLayer {
                    translationY = sunOffsetAnim
                }
                .alpha(if (isDarkMode) 0f else 1f)
        )
        
        // Moon Icon
        Icon(
            imageVector = Icons.Default.DarkMode,
            contentDescription = "Dark Mode",
            tint = Color.White,
            modifier = Modifier
                .size(30.dp)
                .graphicsLayer {
                    translationY = moonOffsetAnim
                }
                .alpha(if (isDarkMode) 1f else 0f)
        )
    }
}
