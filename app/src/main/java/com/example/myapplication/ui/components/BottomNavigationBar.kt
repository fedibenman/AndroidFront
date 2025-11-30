package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapplication.R
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * Bottom navigation bar for the main app screens
 */
@Composable
fun MainBottomNavigationBar(
    navController: NavController,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val screens = listOf(
        BottomNavigationItem(
            route = "chat",
            icon = R.drawable.chat,
            label = "Chat"
        ),
        BottomNavigationItem(
            route = "image_analysis", 
            icon = R.drawable.image_container,
            label = "Analysis"
        ),
        BottomNavigationItem(
            route = "profile", 
            icon = R.drawable.user,
            label = "Profile"
        )
    )
    
    var selectedItem by remember { mutableStateOf(0) }
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Update selected item when route changes
    LaunchedEffect(currentRoute) {
        screens.indexOfFirst { it.route == currentRoute }.let { index ->
            if (index != -1) selectedItem = index
        }
    }

    NavigationBar(
        modifier = modifier,
        tonalElevation = 3.dp
    ) {
        screens.forEachIndexed { index, screen ->
            NavigationBarItem(
                icon = {
                    androidx.compose.material3.Icon(
                        painter = painterResource(screen.icon),
                        contentDescription = screen.label,
                        modifier = androidx.compose.ui.Modifier.size(24.dp)
                    )
                },
                label = { Text(screen.label) },
                selected = selectedItem == index,
                alwaysShowLabel = true,
                onClick = {
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
            )
        }
    }
}

data class BottomNavigationItem(
    val route: String,
    val icon: Int,
    val label: String
)

/**
 * Preview composable for the bottom navigation bar
 */
@Preview(showBackground = true, widthDp = 360, heightDp = 80)
@Composable
fun MainBottomNavigationBarPreview() {
    MyApplicationTheme {
        // Create a mock navController for preview
        val navController = androidx.navigation.compose.rememberNavController()
        
        // Navigate to chat initially for preview
        androidx.compose.runtime.LaunchedEffect(Unit) {
            navController.navigate("chat")
        }
        
        MainBottomNavigationBar(navController = navController)
    }
}
