package com.example.myapplication.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapplication.R
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.PressStart
import com.example.myapplication.ui.theme.ThemeManager

/**
 * Glass-textured floating bottom navigation bar - iOS style
 */
@Composable
fun MainBottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val themeManager = ThemeManager.getInstance(LocalContext.current)
    val isDarkMode by themeManager.isDarkMode.collectAsState()
    
    val screens = listOf(
        BottomNavigationItem(
            route = "home",
            iconVector = Icons.Default.Home,
            label = "Home"
        ),
        BottomNavigationItem(
            route = "chat",
            iconRes = R.drawable.chat,
            label = "Chat"
        ),
        BottomNavigationItem(
            route = "community",
            iconRes = R.drawable.globe,
            label = "Community"
        ),
        BottomNavigationItem(
            route = "finder",
            iconVector = Icons.Default.Search,
            label = "Find"
        ),
        BottomNavigationItem(
            route = "profile", 
            iconRes = R.drawable.user,
            label = "Profile"
        ),
        BottomNavigationItem(
            route = "image_analysis",
            iconVector = Icons.Default.Edit,
            label = "AI"
        ),
        BottomNavigationItem(
            route = "projects",
            iconVector = Icons.Default.Build,
            label = "Proj"
        )
    )
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Glass effect colors based on theme
    val glassBackground = if (isDarkMode) {
        Color.Black.copy(alpha = 0.6f)
    } else {
        Color.White.copy(alpha = 0.7f)
    }
    
    val glassBorder = if (isDarkMode) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.Black.copy(alpha = 0.1f)
    }
    
    val selectedColor = Color(0xFFFFD700) // Gold
    val unselectedColor = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f)
    
    // Floating container with glassmorphism effect
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Glass card with blur-like effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = Color.Black.copy(alpha = 0.3f)
                )
                .clip(RoundedCornerShape(24.dp))
                // Glass background with transparency
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDarkMode) {
                            listOf(
                                Color(0xFF1E1E1E).copy(alpha = 0.75f),
                                Color(0xFF121212).copy(alpha = 0.85f)
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.65f),
                                Color(0xFFF0F0F0).copy(alpha = 0.75f)
                            )
                        }
                    )
                )
                // Inner border for glass effect
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = if (isDarkMode) {
                            listOf(
                                Color.White.copy(alpha = 0.2f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.8f),
                                Color.White.copy(alpha = 0.3f)
                            )
                        }
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                screens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    
                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) selectedColor else unselectedColor
                    )
                    
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Icon with glow effect when selected
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .then(
                                    if (isSelected) {
                                        Modifier.background(
                                            selectedColor.copy(alpha = 0.2f),
                                            RoundedCornerShape(8.dp)
                                        )
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (screen.iconRes != null) {
                                Icon(
                                    painter = painterResource(screen.iconRes),
                                    contentDescription = screen.label,
                                    modifier = Modifier.size(20.dp),
                                    tint = iconColor
                                )
                            } else if (screen.iconVector != null) {
                                Icon(
                                    imageVector = screen.iconVector,
                                    contentDescription = screen.label,
                                    modifier = Modifier.size(20.dp),
                                    tint = iconColor
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        // Label
                        Text(
                            text = screen.label,
                            fontSize = 8.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = iconColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

data class BottomNavigationItem(
    val route: String,
    val iconRes: Int? = null,
    val iconVector: ImageVector? = null,
    val label: String
)

/**
 * Preview composable for the bottom navigation bar
 */
@Preview(showBackground = true, widthDp = 360, heightDp = 100)
@Composable
fun MainBottomNavigationBarPreview() {
    MyApplicationTheme {
        val navController = androidx.navigation.compose.rememberNavController()
        
        LaunchedEffect(Unit) {
            navController.navigate("home")
        }
        
        MainBottomNavigationBar(navController = navController)
    }
}
