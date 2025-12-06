package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    modifier: Modifier = Modifier
) {
    val screens = listOf(
        BottomNavigationItem(
            route = "chat",
            icon = R.drawable.chat,
            label = "Chat"
        ),
        BottomNavigationItem(
            route = "my_projects",  // Changed from "projects" to "my_projects"
            icon = R.drawable.folder,
            label = "Projects"
        ),
        BottomNavigationItem(
            route = "image_analysis",
            icon = R.drawable.image_container,
            label = "Analysis"
        ),
        BottomNavigationItem(
            route = "community",
            icon = R.drawable.globe,
            label = "Community"
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
        // Handle flow_builder routes as belonging to projects
        val routeToMatch = when {
            currentRoute?.startsWith("flow_builder") == true -> "projects"
            currentRoute?.startsWith("community_project_view") == true -> "projects"
            else -> currentRoute
        }

        screens.indexOfFirst { it.route == routeToMatch }.let { index ->
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
                    Icon(
                        painter = painterResource(screen.icon),
                        contentDescription = screen.label,
                        modifier = Modifier.size(24.dp)
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
