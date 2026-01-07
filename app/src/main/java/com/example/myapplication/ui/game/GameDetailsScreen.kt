package com.example.myapplication.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
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
import com.example.myapplication.ui.theme.*
import java.net.URLEncoder

@Composable
fun GameDetailsScreen(
    navController: NavController,
    viewModel: GameDetailsViewModel,
    gameId: Int,
    gameName: String
) {
    val game = viewModel.game
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val isAdding = viewModel.isAddingToCollection

    val addSuccess = viewModel.addToCollectionSuccess
    val reviews = viewModel.reviews
    val gameRating = viewModel.gameRating
    val recommendations = viewModel.recommendations
    val showReviewDialog = viewModel.showReviewDialog
    val isPostingReview = viewModel.isPostingReview

    LaunchedEffect(gameId) {
        viewModel.loadGameDetails(gameId)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Themed Background
        com.example.myapplication.ui.components.ThemedBackground()
        
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = PixelBlack
            )
        } else if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = PixelRed,
                fontFamily = PressStart,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (game != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        // Cover Image
                        AsyncImage(
                            model = game.cover?.url,
                            contentDescription = game.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .background(Color.DarkGray),
                            contentScale = ContentScale.Crop
                        )
                    }

                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                    // Title
                    Text(
                        text = game.name,
                        fontFamily = PressStart,
                        fontSize = 20.sp,
                        color = PixelBlack,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rating & Genres
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (game.rating != null) {
                            Text(
                                text = "★ ${String.format("%.1f", game.rating)}",
                                fontFamily = PressStart,
                                fontSize = 12.sp,
                                color = PixelBlue
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        } else if (gameRating != null) {
                             Text(
                                text = "★ ${String.format("%.1f", gameRating.averageRating)} (${gameRating.totalReviews})",
                                fontFamily = PressStart,
                                fontSize = 12.sp,
                                color = PixelBlue
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                        
                        game.genres?.take(3)?.forEach { genre ->
                            Text(
                                text = genre.name,
                                fontFamily = PressStart,
                                fontSize = 10.sp,
                                color = PixelBlack,
                                modifier = Modifier
                                    .border(1.dp, PixelBlack, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons
                    var showStatusDialog by remember { mutableStateOf(false) }

                    if (showStatusDialog) {
                        AlertDialog(
                            onDismissRequest = { showStatusDialog = false },
                            title = {
                                Text(
                                    text = "SELECT STATUS",
                                    fontFamily = PressStart,
                                    fontSize = 16.sp,
                                    color = PixelBlack
                                )
                            },
                            text = {
                                Column {
                                    listOf("playing", "want to play", "played").forEach { status ->
                                        Button(
                                            onClick = {
                                                viewModel.addToCollection(game.id, status)
                                                showStatusDialog = false
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PixelWhite
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            border = androidx.compose.foundation.BorderStroke(2.dp, PixelBlack),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = status.uppercase(),
                                                fontFamily = PressStart,
                                                color = PixelBlack,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            },
                            confirmButton = {},
                            containerColor = PixelGray,
                            shape = RoundedCornerShape(0.dp)
                        )
                    }

                    if (addSuccess) {
                        Button(
                            onClick = { 
                                val encodedName = URLEncoder.encode(game.name, "UTF-8")
                                navController.navigate("missions/${game.id}/$encodedName")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PixelGreen),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, PixelBlack)
                        ) {
                            Text("PLAY NOW", fontFamily = PressStart, color = PixelBlack)
                        }
                    } else {
                        Button(
                            onClick = { showStatusDialog = true },
                            enabled = !isAdding,
                            colors = ButtonDefaults.buttonColors(containerColor = PixelBlue),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, PixelBlack)
                        ) {
                            if (isAdding) {
                                CircularProgressIndicator(color = PixelWhite, modifier = Modifier.size(24.dp))
                            } else {
                                Text("ADD TO COLLECTION", fontFamily = PressStart, color = PixelWhite)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Description
                    Text(
                        text = "DESCRIPTION",
                        fontFamily = PressStart,
                        fontSize = 14.sp,
                        color = PixelBlack,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = game.description ?: game.summary ?: "No description available.",
                        fontFamily = PressStart,
                        fontSize = 12.sp,
                        color = PixelBlack,
                        lineHeight = 18.sp
                    )
                        }
                            }


                // Reviews Section
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "REVIEWS",
                                fontFamily = PressStart,
                                fontSize = 14.sp,
                                color = PixelBlack,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Button(
                                onClick = { viewModel.showReviewDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = PixelBlue),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(30.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text("WRITE REVIEW", fontFamily = PressStart, fontSize = 8.sp, color = PixelWhite)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (reviews.isEmpty()) {
                            Text(
                                text = "No reviews yet. Be the first!",
                                fontFamily = PressStart,
                                fontSize = 10.sp,
                                color = PixelBlack.copy(alpha = 0.6f)
                            )
                        } else {
                            reviews.forEach { review ->
                                ReviewItem(review)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }

                // Similar Games Section
                if (recommendations.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "SIMILAR GAMES",
                                fontFamily = PressStart,
                                fontSize = 14.sp,
                                color = PixelBlack,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState())
                                    .fillMaxWidth()
                            ) {
                                recommendations.forEach { recGame ->
                                    RecommendationCard(
                                        game = recGame,
                                        onClick = {
                                            viewModel.trackRecommendationClick(game.id, recGame.id)
                                            val encodedName = URLEncoder.encode(recGame.name, "UTF-8")
                                            navController.navigate("game/${recGame.id}/$encodedName")
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                }
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
        
        if (showReviewDialog) {
            WriteReviewDialog(
                onDismiss = { viewModel.showReviewDialog = false },
                onSubmit = { rating: Int, text: String ->
                    viewModel.submitReview(gameId, rating, text)
                },
                isSubmitting = isPostingReview,
                isGenerating = viewModel.isGeneratingReview,
                onGenerateAI = { rating, prompt, onUpdate ->
                    viewModel.generateReview(rating, prompt, onUpdate)
                }
            )
        }
    }
}
}

@Composable
fun ReviewItem(review: com.example.myapplication.model.Review) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PixelWhite)
            .border(1.dp, PixelBlack, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = review.user?.username ?: "Unknown",
                fontFamily = PressStart,
                fontSize = 10.sp,
                color = PixelBlue
            )
            Text(
                text = "★".repeat(review.rating),
                fontSize = 10.sp,
                color = PixelBlue
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = review.text,
            fontFamily = PressStart,
            fontSize = 10.sp,
            color = PixelBlack,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun RecommendationCard(game: com.example.myapplication.model.Game, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = game.cover?.url,
            contentDescription = game.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .border(1.dp, PixelBlack, RoundedCornerShape(8.dp))
                .background(Color.Gray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = game.name,
            fontFamily = PressStart,
            fontSize = 10.sp,
            color = PixelBlack,
            maxLines = 2,
            lineHeight = 14.sp
        )
    }
}

@Composable
fun WriteReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit,
    isSubmitting: Boolean,
    isGenerating: Boolean,
    onGenerateAI: (Int, String?, (String) -> Unit) -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("WRITE REVIEW", fontFamily = PressStart, fontSize = 14.sp)
        },
        text = {
            Column {
                // Star Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    (1..5).forEach { star ->
                        Text(
                            text = if (star <= rating) "★" else "☆",
                            fontSize = 24.sp,
                            color = PixelBlue,
                            modifier = Modifier.clickable { rating = star }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Your thoughts...", fontFamily = PressStart, fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = PressStart, fontSize = 10.sp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { 
                        onGenerateAI(rating, text) { newText -> text = newText } 
                    },
                    enabled = !isGenerating && !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                     border = androidx.compose.foundation.BorderStroke(2.dp, PixelBlack)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = PixelBlack)
                    } else {
                        Text("✨ AI ASSIST", fontFamily = PressStart, color = PixelBlack, fontSize = 10.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, text) },
                enabled = !isSubmitting && text.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = PixelGreen)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = PixelBlack)
                } else {
                    Text("POST", fontFamily = PressStart, color = PixelBlack)
                }
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PixelRed)
            ) {
                Text("CANCEL", fontFamily = PressStart, color = PixelWhite)
            }
        },
        containerColor = PixelWhite,
        shape = RoundedCornerShape(8.dp)
    )
}
