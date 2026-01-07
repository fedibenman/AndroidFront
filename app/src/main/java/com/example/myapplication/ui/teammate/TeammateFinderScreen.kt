package com.example.myapplication.ui.teammate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.ui.components.PixelatedButton
import com.example.myapplication.ui.theme.PressStart
import com.example.myapplication.ui.theme.PrimaryGold

enum class FindMode {
    SELECTOR, // Show mode selector
    MATCH,    // Swipe cards
    NEARBY    // Radar view
}

@Composable
fun TeammateFinderScreen(
    navController: NavController,
    viewModel: TeammateFinderViewModel = viewModel()
) {
    var currentMode by remember { mutableStateOf(FindMode.SELECTOR) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Themed Background
        com.example.myapplication.ui.components.ThemedBackground()
        
        when (currentMode) {
            FindMode.SELECTOR -> {
                FindModeSelector(
                    onSelectMatch = { currentMode = FindMode.MATCH },
                    onSelectNearby = { currentMode = FindMode.NEARBY }
                )
            }
            FindMode.MATCH -> {
                MatchModeContent(
                    viewModel = viewModel,
                    onBack = { currentMode = FindMode.SELECTOR }
                )
            }
            FindMode.NEARBY -> {
                NearbyTeammatesScreen(
                    onBack = { currentMode = FindMode.SELECTOR },
                    onUserTap = { userId ->
                        // TODO: Navigate to user profile or chat
                    }
                )
            }
        }
    }
}

@Composable
fun FindModeSelector(
    onSelectMatch: () -> Unit,
    onSelectNearby: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "FIND TEAMMATES",
            fontFamily = PressStart,
            fontSize = 20.sp,
            color = PrimaryGold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Choose how you want to find your next teammate",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Match Mode Card
        ModeCard(
            icon = Icons.Default.People,
            title = "MATCH",
            description = "Swipe through profiles and match with compatible gamers",
            onClick = onSelectMatch
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Nearby Mode Card
        ModeCard(
            icon = Icons.Default.LocationOn,
            title = "NEARBY",
            description = "Find gamers near you who are looking for teammates right now",
            onClick = onSelectNearby
        )
    }
}

@Composable
fun ModeCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrimaryGold),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = PressStart,
                    fontSize = 14.sp,
                    color = PrimaryGold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun MatchModeContent(
    viewModel: TeammateFinderViewModel,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Back",
                    tint = PrimaryGold
                )
            }
            
            Text(
                text = "MATCH MODE",
                fontFamily = PressStart,
                fontSize = 16.sp,
                color = PrimaryGold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.width(48.dp))
        }
        
        // Card Stack Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (viewModel.isLoading && viewModel.candidates.isEmpty()) {
                CircularProgressIndicator(color = PrimaryGold)
            } else if (viewModel.candidates.isEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NO MORE PLAYERS",
                        fontFamily = PressStart,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PixelatedButton(onClick = { viewModel.loadCandidates() }) {
                        Text("REFRESH", fontFamily = PressStart, fontSize = 12.sp)
                    }
                }
            } else {
                // Draw candidates in reverse order so the first one (index 0) is on top
                viewModel.candidates.reversed().forEach { candidate ->
                    val isTopCard = candidate == viewModel.candidates.first()
                    
                    androidx.compose.runtime.key(candidate.id) {
                        SwipeableUserCard(
                            profile = candidate,
                            onSwipe = { direction ->
                                if (isTopCard) {
                                    viewModel.swipe(candidate, direction)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
        
        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pass Button
            FloatingActionButton(
                onClick = {
                    if (viewModel.candidates.isNotEmpty()) {
                        viewModel.swipe(viewModel.candidates.first(), SwipeDirection.Left)
                    }
                },
                containerColor = Color.White,
                contentColor = Color.Red,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Default.Close, "Pass")
            }

            // Like Button
            FloatingActionButton(
                onClick = {
                    if (viewModel.candidates.isNotEmpty()) {
                        viewModel.swipe(viewModel.candidates.first(), SwipeDirection.Right)
                    }
                },
                containerColor = Color.White,
                contentColor = Color.Green,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Default.Favorite, "Like")
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
    
    // Match Dialog Overlay
    if (viewModel.showMatchDialog && viewModel.matchedUser != null) {
        MatchAnimationDialog(
            matchedUser = viewModel.matchedUser!!,
            onDismiss = { viewModel.dismissMatchDialog() },
            onSendMessage = { 
                viewModel.dismissMatchDialog()
            },
            onKeepSwiping = { viewModel.dismissMatchDialog() }
        )
    }
}
