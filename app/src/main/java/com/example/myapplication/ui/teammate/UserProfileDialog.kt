package com.example.myapplication.ui.teammate

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.myapplication.Repository.UserProfile
import com.example.myapplication.ui.theme.PressStart
import com.example.myapplication.ui.theme.PrimaryGold

@Composable
fun UserProfileDialog(
    userId: String,
    initialName: String,
    initialAvatar: String?,
    onDismiss: () -> Unit,
    viewModel: NearbyTeammatesViewModel
) {
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isInviting by remember { mutableStateOf(false) }
    var inviteSent by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    LaunchedEffect(userId) {
        val result = viewModel.getUserProfile(userId)
        result.onSuccess { 
            profile = it
            isLoading = false
        }.onFailure {
            isLoading = false
            Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Full width
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF1A1A26) // Dark background
        ) {
            Box(Modifier.fillMaxSize()) {
                // Close Button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .zIndex(1f)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryGold
                    )
                } else if (profile != null) {
                    val user = profile!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(40.dp))
                        
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                             AsyncImage(
                                model = user.avatar ?: initialAvatar,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = user.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        if (!user.bio.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = user.bio,
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Favorite Game
                        user.favoriteGame?.let { game ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        text = "PLAYING NOW",
                                        fontFamily = PressStart,
                                        fontSize = 10.sp,
                                        color = PrimaryGold
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AsyncImage(
                                            model = game.coverUrl,
                                            contentDescription = game.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(60.dp, 80.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Gray)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(
                                            text = game.name,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Play Styles
                        if (!user.playStyles.isNullOrEmpty()) {
                            Column(Modifier.fillMaxWidth()) {
                                Text(
                                    text = "PLAY STYLE",
                                    fontFamily = PressStart,
                                    fontSize = 10.sp,
                                    color = Color.Cyan
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    user.playStyles.forEach { style ->
                                        Surface(
                                            color = Color.Cyan.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text(
                                                text = style,
                                                color = Color.Cyan,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // Stats (Added to match iOS)
                        user.stats?.let { stats ->
                            Column(Modifier.fillMaxWidth()) {
                                Text(
                                    text = "STATS",
                                    fontFamily = PressStart,
                                    fontSize = 10.sp,
                                    color = Color.Green
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatBox(title = "Level", value = "${stats.level}")
                                    StatBox(title = "XP", value = "${stats.xp}")
                                    StatBox(title = "Games", value = "${stats.totalGamesPlayed}")
                                }
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Action Button
                        Button(
                            onClick = {
                                isInviting = true
                                viewModel.sendInvite(user.id, user.favoriteGame?.gameId ?: 0) { success ->
                                    isInviting = false
                                    if (success) {
                                        inviteSent = true
                                        Toast.makeText(context, "Invite Sent!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to send invite", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !isInviting && !inviteSent,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (inviteSent) Color.Green else PrimaryGold
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isInviting) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                            } else {
                                Text(
                                    text = if (inviteSent) "INVITE SENT!" else "INVITE TO GAME",
                                    fontFamily = PressStart,
                                    fontSize = 12.sp,
                                    color = Color.Black
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                } else {
                    Text(
                        text = "Failed to load profile",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun StatBox(title: String, value: String) {
    Surface(
        color = Color.White.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.width(100.dp).height(80.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontFamily = PressStart,
                fontSize = 14.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}
