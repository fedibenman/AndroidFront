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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.storyCreator.ViewModel.CommunityProjectViewModel
import com.example.myapplication.storyCreator.ViewModel.StoryProjectViewModel

@Composable
fun ProjectsMainScreen(
    storyProjectViewModel: StoryProjectViewModel,
    communityProjectViewModel: CommunityProjectViewModel,
    onProjectClick: (String) -> Unit,
    onCommunityProjectClick: (String) -> Unit,
    onForkSuccess: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("📚 My Projects", "🌍 Community")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelDarkBlue)
    ) {
        // Header with tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF1A1A1A),
            contentColor = PixelGold,
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, Color(0xFF000000)),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = PixelHighlight,
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
                        color = if (selectedTab == index) PixelGold else Color.Gray,
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

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun ProjectsMainScreenPreview() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("📚 My Projects", "🌍 Community")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelDarkBlue)
    ) {
        // Header with tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF1A1A1A),
            contentColor = PixelGold,
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, Color(0xFF000000)),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = PixelHighlight,
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
                        color = if (selectedTab == index) PixelGold else Color.Gray,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        // Preview content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PixelDarkBlue)
                .padding(16.dp)
        ) {
            Text(
                text = if (selectedTab == 0) "My Projects Content" else "Community Projects Content",
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                color = PixelGold
            )
        }
    }
}