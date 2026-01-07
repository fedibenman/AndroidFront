package com.example.myapplication.storyCreator.Views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.AnimatedThemeToggle
import com.example.myapplication.ui.theme.LocalThemeManager

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun PopularFlows() {
    val themeManager = LocalThemeManager.current
    val isDarkMode by themeManager.isDarkMode.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkMode) PixelDarkBlue else Color(0xFFF5F5F5))
    ) {
        // Theme toggle button at top right
        Box(
            modifier = Modifier
                .padding(top = 50.dp, end = 20.dp)
                .align(Alignment.TopEnd)
        ) {
            AnimatedThemeToggle()
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🌟 POPULAR FLOWS",
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) PixelGold else Color(0xFF333333),
                modifier = Modifier.padding(top = 80.dp)
            )
            
            Text(
                text = "Coming Soon...",
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = if (isDarkMode) Color.Gray else Color(0xFF666666),
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}