package com.example.myapplication.ui.mission

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.model.Mission
import com.example.myapplication.ui.theme.*

@Composable
fun MissionChecklistScreen(
    navController: NavController,
    viewModel: MissionChecklistViewModel,
    gameId: Int,
    gameName: String
) {
    val missions = viewModel.missions
    val progress = viewModel.progress
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    LaunchedEffect(gameId) {
        viewModel.loadMissions(gameId, gameName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelGray)
            .padding(16.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PixelBlue)
                .border(4.dp, PixelBlack)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {

            // Background image
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.myapplication.R.drawable.background_general),
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            Text(
                text = "MISSIONS: $gameName",
                fontFamily = PressStart,
                fontSize = 14.sp,
                color = PixelWhite,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Bar
        if (progress != null) {
            val progressPercent = if (progress.totalMissions > 0) {
                progress.completedMissions.size.toFloat() / progress.totalMissions.toFloat()
            } else 0f
            
            Column {
                Text(
                    text = "PROGRESS: ${(progressPercent * 100).toInt()}%",
                    fontFamily = PressStart,
                    fontSize = 12.sp,
                    color = PixelBlack
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progressPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .border(2.dp, PixelBlack),
                    color = PixelGreen,
                    trackColor = PixelWhite,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PixelBlack)
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = errorMessage,
                    color = PixelRed,
                    fontFamily = PressStart,
                    fontSize = 12.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(missions) { mission ->
                    MissionItemRow(mission = mission) {
                        viewModel.toggleMission(mission, gameId)
                    }
                }
            }
        }
    }
}

@Composable
fun MissionItemRow(
    mission: Mission,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(0.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, PixelBlack),
        colors = CardDefaults.cardColors(containerColor = if (mission.isCompleted) Color(0xFFE8F5E9) else PixelWhite)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox (Custom Pixel Art Style)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(if (mission.isCompleted) PixelGreen else PixelWhite)
                    .border(2.dp, PixelBlack),
                contentAlignment = Alignment.Center
            ) {
                if (mission.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = PixelBlack,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = mission.title,
                    fontFamily = PressStart,
                    fontSize = 10.sp,
                    color = PixelBlack,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
