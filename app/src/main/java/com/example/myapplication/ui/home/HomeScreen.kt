package com.example.myapplication.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.model.Game
import com.example.myapplication.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import java.net.URLEncoder

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
    val popularGames = viewModel.popularGames
    val genreGames = viewModel.genreGames
    val searchResults = viewModel.searchResults
    val searchQuery = viewModel.searchQuery
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    Box(modifier = Modifier.fillMaxSize()) {
        // Themed Background (Light: background_general + clouds, Dark: night + stars)
        com.example.myapplication.ui.components.ThemedBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PixelWhite, RoundedCornerShape(8.dp)),
                placeholder = {
                    Text(
                        "Search Games...",
                        fontFamily = PressStart,
                        fontSize = 12.sp
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PixelBlack,
                    unfocusedBorderColor = PixelBlack,
                    cursorColor = PixelBlack
                ),
                shape = RoundedCornerShape(8.dp)
            )

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
            } else if (searchQuery.isNotEmpty()) {
                // Search Results
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "SEARCH RESULTS",
                            fontFamily = PressStart,
                            fontSize = 16.sp,
                            color = PixelBlack,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(searchResults) { game ->
                        GameRowItem(game = game) {
                            navigateToGameDetails(navController, game)
                        }
                    }
                }
            } else {
                // Home Content
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // AI Recommendations
                    if (viewModel.aiRecommendations.isNotEmpty()) {
                        item {
                            SectionHeader("PERSONALIZED PICKS ✨")
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp), // More space for wider cards
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                items(viewModel.aiRecommendations) { rec ->
                                    Card(
                                        modifier = Modifier
                                            .width(200.dp)
                                            .clickable { navigateToGameDetails(navController, rec.recommendation) },
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(2.dp, PrimaryGold),
                                        colors = CardDefaults.cardColors(containerColor = PixelBlack)
                                    ) {
                                        Column {
                                            AsyncImage(
                                                model = rec.recommendation.cover?.url,
                                                contentDescription = rec.recommendation.name,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(110.dp)
                                                    .background(Color.Gray),
                                                contentScale = ContentScale.Crop
                                            )
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                    text = rec.recommendation.name,
                                                    fontFamily = PressStart,
                                                    fontSize = 12.sp,
                                                    color = PrimaryGold,
                                                    maxLines = 1
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "∵ " + rec.reason,
                                                    fontFamily = PixelatedFont,
                                                    fontSize = 10.sp,
                                                    color = Color.White,
                                                    lineHeight = 14.sp,
                                                    maxLines = 2
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = PrimaryGold,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "${rec.score.toInt()}% Match",
                                                        fontFamily = PressStart,
                                                        fontSize = 10.sp,
                                                        color = Color.Green
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Popular Games
                    item {
                        SectionHeader("POPULAR GAMES")
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = popularGames,
                                key = { it.id }
                            ) { game ->
                                GameCardItem(game = game) {
                                    navigateToGameDetails(navController, game)
                                }
                            }
                        }
                    }

                    // Genre Sections
                    genreGames.forEach { (genre, games) ->
                        item {
                            SectionHeader("$genre GAMES")
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = games,
                                    key = { it.id }
                                ) { game ->
                                    GameCardItem(game = game) {
                                        navigateToGameDetails(navController, game)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Floating Collection Button (bottom-right)
        androidx.compose.material3.FloatingActionButton(
            onClick = { navController.navigate("collection") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 100.dp),
            containerColor = PrimaryGold,
            contentColor = Color.Black
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "My Collection"
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PixelBlue)
            .border(2.dp, PixelBlack)
            .padding(8.dp)
    ) {
        Text(
            text = title,
            fontFamily = PressStart,
            fontSize = 14.sp,
            color = PixelWhite,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GameCardItem(game: Game, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(0.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, PixelBlack),
        colors = CardDefaults.cardColors(containerColor = PixelWhite)
    ) {
        Column {
            AsyncImage(
                model = game.cover?.url,
                contentDescription = game.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop,
                fallback = androidx.compose.ui.graphics.painter.ColorPainter(Color.Magenta),
                error = androidx.compose.ui.graphics.painter.ColorPainter(Color.Red)
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = game.name,
                    fontFamily = PressStart,
                    fontSize = 10.sp,
                    color = PixelBlack,
                    maxLines = 2,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (game.rating != null) {
                    Text(
                        text = "★ ${String.format("%.1f", game.rating)}",
                        fontFamily = PressStart,
                        fontSize = 8.sp,
                        color = PixelBlue
                    )
                }
            }
        }
    }
}

@Composable
fun GameRowItem(game: Game, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(0.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, PixelBlack),
        colors = CardDefaults.cardColors(containerColor = PixelWhite)
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = game.cover?.url,
                contentDescription = game.name,
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.Gray)
                    .border(1.dp, PixelBlack),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = game.name,
                    fontFamily = PressStart,
                    fontSize = 12.sp,
                    color = PixelBlack
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (game.rating != null) {
                    Text(
                        text = "RATING: ${String.format("%.1f", game.rating)}",
                        fontFamily = PressStart,
                        fontSize = 10.sp,
                        color = PixelBlue
                    )
                }
            }
        }
    }
}

fun navigateToGameDetails(navController: NavController, game: Game) {
    val encodedName = URLEncoder.encode(game.name, "UTF-8")
    navController.navigate("gameDetails/${game.id}/$encodedName")
}
