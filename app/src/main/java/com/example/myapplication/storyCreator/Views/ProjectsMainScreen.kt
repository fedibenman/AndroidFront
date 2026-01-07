package com.example.myapplication.storyCreator.Views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.storyCreator.ViewModel.CommunityProjectViewModel
import com.example.myapplication.storyCreator.ViewModel.StoryProjectViewModel
import com.example.myapplication.ui.theme.AnimatedThemeToggle
import com.example.myapplication.ui.theme.LocalThemeManager

@Composable
fun ProjectsMainScreen(
    storyProjectViewModel: StoryProjectViewModel,
    communityProjectViewModel: CommunityProjectViewModel,
    onProjectClick: (String) -> Unit,
    onCommunityProjectClick: (String) -> Unit,
    onForkSuccess: () -> Unit
) {
    val themeManager = LocalThemeManager.current
    val isDarkMode by themeManager.isDarkMode.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("📚 My Projects", "🌍 Community")

    Box(modifier = Modifier.fillMaxSize()) {
        // Theme toggle button at top right
        Box(
            modifier = Modifier
                .padding(top = 50.dp, end = 20.dp)
                .align(Alignment.TopEnd)
        ) {
            AnimatedThemeToggle()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDarkMode) PixelDarkBlue else Color(0xFFF5F5F5))
        ) {
            // Header with tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFE0E0E0),
                contentColor = if (isDarkMode) PixelGold else Color(0xFF333333),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(3.dp, if (isDarkMode) Color(0xFF000000) else Color(0xFF999999)),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = if (isDarkMode) PixelHighlight else Color(0xFF2196F3),
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = title,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 16.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) {
                                if (isDarkMode) PixelGold else Color(0xFF1976D2)
                            } else {
                                if (isDarkMode) Color.Gray else Color(0xFF666666)
                            },
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            // Content based on selected tab
            when (selectedTab) {
                0 -> {
                    // My Projects Tab
                    ProjectsListScreen(
                        viewModel = storyProjectViewModel,
                        onProjectClick = onProjectClick
                    )
                }
                1 -> {
                    // Community Projects Tab
                    CommunityProjectsScreen(
                        viewModel = communityProjectViewModel,
                        storyProjectViewModel = storyProjectViewModel,
                        onProjectClick = onCommunityProjectClick,
                        onForkSuccess = {
                            // Switch to My Projects tab after forking
                            selectedTab = 0
                            onForkSuccess()
                        }
                    )
                }
            }
        }
    }
}

//@Preview(showBackground = true, widthDp = 400, heightDp = 800)
//@Composable
//fun ProjectsMainScreenPreview() {
//    // Create a mock ThemeManager
//    val context = LocalContext.current
//    val mockThemeManager = remember {
//        object : com.example.myapplication.ui.theme.ThemeManager(context) {
//            override val isDarkMode: Boolean = false
//        }
//    }
//
//    androidx.compose.runtime.CompositionLocalProvider(
//        LocalThemeManager provides mockThemeManager
//    ) {
//        var selectedTab by remember { mutableStateOf(0) }
//        val tabs = listOf("📚 My Projects", "🌍 Community")
//
//        Box(modifier = Modifier.fillMaxSize()) {
//            // Theme toggle button at top right
//            Box(
//                modifier = Modifier
//                    .padding(top = 50.dp, end = 20.dp)
//                    .align(Alignment.TopEnd)
//            ) {
//                AnimatedThemeToggle()
//            }
//
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color(0xFFF5F5F5))
//            ) {
//                // Header with tabs
//                TabRow(
//                    selectedTabIndex = selectedTab,
//                    containerColor = Color(0xFFE0E0E0),
//                    contentColor = Color(0xFF333333),
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .border(3.dp, Color(0xFF999999)),
//                    indicator = { tabPositions ->
//                        TabRowDefaults.SecondaryIndicator(
//                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
//                            color = Color(0xFF2196F3),
//                            height = 3.dp
//                        )
//                    }
//                ) {
//                    tabs.forEachIndexed { index, title ->
//                        Tab(
//                            selected = selectedTab == index,
//                            onClick = { selectedTab = index },
//                            modifier = Modifier.padding(vertical = 12.dp)
//                        ) {
//                            Text(
//                                text = title,
//                                fontFamily = FontFamily.Monospace,
//                                fontSize = 16.sp,
//                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
//                                color = if (selectedTab == index) Color(0xFF1976D2) else Color(0xFF666666),
//                                letterSpacing = 1.sp,
//                                modifier = Modifier.padding(vertical = 8.dp)
//                            )
//                        }
//                    }
//                }
//
//                // Preview content
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .background(Color(0xFFF5F5F5))
//                        .padding(16.dp)
//                ) {
//                    Text(
//                        text = if (selectedTab == 0) "My Projects Content" else "Community Projects Content",
//                        fontFamily = FontFamily.Monospace,
//                        fontSize = 18.sp,
//                        color = Color(0xFF333333)
//                    )
//                }
//            }
//        }
//    }
//}