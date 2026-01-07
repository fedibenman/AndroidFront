package com.example.myapplication.ui.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import coil.compose.AsyncImage
import com.example.myapplication.model.CollectionItem
import com.example.myapplication.model.Game
import com.example.myapplication.ui.theme.*
import java.net.URLEncoder

@Composable
fun MyCollectionScreen(
    navController: NavController,
    viewModel: CollectionViewModel
) {
    val collection = viewModel.collection
    val gamesDetails = viewModel.gamesDetails
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    LaunchedEffect(Unit) {
        viewModel.loadCollection()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Themed Background
        com.example.myapplication.ui.components.ThemedBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                text = "MY COLLECTION",
                fontFamily = PressStart,
                fontSize = 20.sp,
                color = PixelWhite,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
        } else if (collection.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No games yet!",
                    fontFamily = PressStart,
                    fontSize = 14.sp,
                    color = PixelBlack
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(collection) { item ->
                    val game = gamesDetails[item.gameId]
                    CollectionItemRow(item = item, game = game) {
                        if (game != null) {
                            if (item.status == "playing") {
                                val encodedName = URLEncoder.encode(game.name, "UTF-8")
                                navController.navigate("missions/${game.id}/$encodedName")
                            } else {
                                val encodedName = URLEncoder.encode(game.name, "UTF-8")
                                navController.navigate("gameDetails/${game.id}/$encodedName")
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
fun CollectionItemRow(
    item: CollectionItem,
    game: Game?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(0.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, PixelBlack),
        colors = CardDefaults.cardColors(containerColor = PixelWhite)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // Game Cover
            AsyncImage(
                model = game?.cover?.url,
                contentDescription = game?.name,
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.Gray)
                    .border(1.dp, PixelBlack),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = game?.name ?: "Loading...",
                    fontFamily = PressStart,
                    fontSize = 12.sp,
                    color = PixelBlack,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Status Chip
                Surface(
                    color = when (item.status) {
                        "playing" -> PixelGreen
                        "completed" -> PixelBlue
                        else -> Color.Gray
                    },
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PixelBlack)
                ) {
                    Text(
                        text = item.status.uppercase(),
                        fontFamily = PressStart,
                        fontSize = 8.sp,
                        color = PixelBlack,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                
                if (item.status == "playing" && item.missionProgress != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${item.missionProgress.completedMissions.size}/${item.missionProgress.totalMissions} Missions",
                        fontFamily = PressStart,
                        fontSize = 8.sp,
                        color = PixelBlack
                    )
                }
            }
        }
    }
}
