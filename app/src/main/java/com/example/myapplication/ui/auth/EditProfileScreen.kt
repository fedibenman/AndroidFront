package com.example.myapplication.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.model.FavoriteGame
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
@Composable
fun EditProfileScreen(
    viewModel: EnhancedProfileViewModel,
    onDismiss: () -> Unit
) {
    val isDarkMode = true // Default to dark for now

    Box(modifier = Modifier.fillMaxSize()) {
        // Themed Background
        com.example.myapplication.ui.components.ThemedBackground()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with glass effect
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EDIT PROFILE",
                            fontFamily = PressStart,
                            fontSize = 16.sp,
                            color = Color(0xFFFFD700)
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .background(
                                    Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Bio Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "BIO",
                            fontFamily = PressStart,
                            fontSize = 12.sp,
                            color = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        PixelatedTextField(
                            value = viewModel.editedBio,
                            onValueChange = { viewModel.updateEditedBio(it) },
                            placeholder = "Tell us about yourself...",
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4
                        )
                    }
                }
            }

            // Gamer Tags Section
            item {
                Column {
                    Text(
                        text = "GAMER TAGS",
                        fontFamily = PressStart,
                        fontSize = 12.sp,
                        color = Color(0xFFFFD700)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    PixelatedTextField(
                        value = viewModel.editedPsn,
                        onValueChange = { viewModel.updateEditedPsn(it) },
                        label = "PSN ID",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PixelatedTextField(
                        value = viewModel.editedXbox,
                        onValueChange = { viewModel.updateEditedXbox(it) },
                        label = "Xbox Gamertag",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PixelatedTextField(
                        value = viewModel.editedSteam,
                        onValueChange = { viewModel.updateEditedSteam(it) },
                        label = "Steam ID",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PixelatedTextField(
                        value = viewModel.editedDiscord,
                        onValueChange = { viewModel.updateEditedDiscord(it) },
                        label = "Discord",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PixelatedTextField(
                        value = viewModel.editedNintendo,
                        onValueChange = { viewModel.updateEditedNintendo(it) },
                        label = "Nintendo ID",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Availability Section - Glass Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "AVAILABILITY",
                            fontFamily = PressStart,
                            fontSize = 12.sp,
                            color = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        PixelatedTextField(
                            value = viewModel.editedAvailability,
                            onValueChange = { viewModel.updateEditedAvailability(it) },
                            placeholder = "e.g., Weeknights, Weekends",
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // Play Styles Section - Glass Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "PLAY STYLES",
                            fontFamily = PressStart,
                            fontSize = 12.sp,
                            color = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Grid of play style chips
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            viewModel.availablePlayStyles.forEach { style ->
                                PlayStyleChip(
                                    style = style,
                                    isSelected = viewModel.selectedPlayStyles.contains(style),
                                    onClick = { viewModel.togglePlayStyle(style) }
                                )
                            }
                        }
                    }
                }
            }

            // Languages Section - Glass Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "LANGUAGES",
                            fontFamily = PressStart,
                            fontSize = 12.sp,
                            color = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Grid of language chips
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            viewModel.availableLanguages.forEach { language ->
                                LanguageChip(
                                    language = language,
                                    isSelected = viewModel.selectedLanguages.contains(language),
                                    onClick = { viewModel.toggleLanguage(language) }
                                )
                            }
                        }
                    }
                }
            }

            // Favorite Game Section - Glass Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "FAVORITE GAME",
                            fontFamily = PressStart,
                            fontSize = 12.sp,
                            color = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Current favorite game
                        viewModel.editedFavoriteGame?.let { game ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = game.coverUrl,
                                    contentDescription = game.name,
                                    modifier = Modifier
                                        .size(60.dp, 80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = game.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    TextButton(
                                        onClick = { viewModel.setFavoriteGame(null) },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            "Remove",
                                            fontSize = 10.sp,
                                            color = Color.Red
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Search bar
                        OutlinedTextField(
                            value = viewModel.searchText,
                            onValueChange = { viewModel.searchGames(it) },
                            placeholder = { Text("Search for a game...", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                if (viewModel.searchText.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.clearSearch() }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                        )

                        // Search results
                        if (viewModel.searchResults.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                viewModel.searchResults.take(5).forEach { game ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.setFavoriteGame(
                                                    FavoriteGame(
                                                        gameId = game.id,
                                                        name = game.name,
                                                        coverUrl = game.cover?.url ?: ""
                                                    )
                                                )
                                                viewModel.clearSearch()
                                            }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = game.cover,
                                            contentDescription = game.name,
                                            modifier = Modifier
                                                .size(40.dp, 50.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = game.name,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Save Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                PixelatedButton(
                    onClick = {
                        CoroutineScope(Dispatchers.Main).launch {
                            viewModel.saveProfile()
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = PixelGreen
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SAVE CHANGES", fontFamily = PressStart, fontSize = 12.sp)
                }
            }

            // Bottom spacing for navigation bar
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    // Use fully qualified name to avoid recursion
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}
