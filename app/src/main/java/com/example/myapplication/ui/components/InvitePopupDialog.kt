package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.Repository.InviteSender
import com.example.myapplication.Repository.NotificationRepository
import com.example.myapplication.Repository.SwipeResponse
import kotlinx.coroutines.launch

// Colors matching the app theme
private val PrimaryGold = Color(0xFFFFD700)
private val DarkBackground = Color(0xFF1A1A2E)
private val AccentGreen = Color(0xFF4CAF50)
private val AccentRed = Color(0xFFE53935)

@Composable
fun InvitePopupDialog(
    fromUserId: String,
    fromUserName: String,
    gameId: Int?,
    notificationId: String?,
    myUserId: String,
    onDismiss: () -> Unit,
    onMatchSuccess: (matchedUserName: String) -> Unit,
    onStartChat: (otherUserId: String, otherUserName: String) -> Unit
) {
    val repository = remember { NotificationRepository() }
    val coroutineScope = rememberCoroutineScope()
    
    var isLoading by remember { mutableStateOf(false) }
    var userProfile by remember { mutableStateOf<InviteSender?>(null) }
    var showMatchSuccess by remember { mutableStateOf(false) }
    var matchedUser by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Fetch full profile on dialog open
    LaunchedEffect(fromUserId) {
        try {
            val result = repository.getUserProfile(fromUserId)
            result.onSuccess { profile ->
                userProfile = profile
            }
        } catch (e: Exception) {
            android.util.Log.e("InvitePopup", "Failed to fetch profile", e)
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showMatchSuccess) {
                    // Match Success View
                    MatchSuccessContent(
                        matchedUserName = matchedUser ?: fromUserName,
                        onMessageClick = {
                            onStartChat(fromUserId, matchedUser ?: fromUserName)
                            onDismiss()
                        },
                        onClose = onDismiss
                    )
                } else {
                    // Profile View
                    ProfileContent(
                        userName = fromUserName,
                        profile = userProfile,
                        gameId = gameId,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onAccept = {
                            coroutineScope.launch {
                                isLoading = true
                                errorMessage = null
                                
                                val result = repository.respondToInvite(myUserId, fromUserId, "like")
                                result.onSuccess { response ->
                                    if (response.match) {
                                        matchedUser = response.matchedUser?.name ?: fromUserName
                                        showMatchSuccess = true
                                    } else {
                                        // Invite accepted but not a match yet (other user hasn't liked back)
                                        onMatchSuccess(fromUserName)
                                        onDismiss()
                                    }
                                }.onFailure { e ->
                                    errorMessage = "Failed to accept invite. Please try again."
                                }
                                isLoading = false
                            }
                        },
                        onDecline = {
                            coroutineScope.launch {
                                isLoading = true
                                repository.respondToInvite(myUserId, fromUserId, "pass")
                                onDismiss()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    userName: String,
    profile: InviteSender?,
    gameId: Int?,
    isLoading: Boolean,
    errorMessage: String?,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    // Avatar
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = userName.take(2).uppercase(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Name
    Text(
        text = userName,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )
    
    // Bio
    profile?.bio?.let { bio ->
        if (bio.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = bio,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
    
    // Favorite Game
    profile?.favoriteGame?.name?.let { gameName ->
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "⭐",
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = gameName,
                fontSize = 14.sp,
                color = PrimaryGold,
                fontWeight = FontWeight.Medium
            )
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    // Invite message
    Text(
        text = "wants to play with you!",
        fontSize = 16.sp,
        color = Color.White.copy(alpha = 0.8f)
    )
    
    // Error message
    errorMessage?.let {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = it,
            fontSize = 12.sp,
            color = AccentRed
        )
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    // Buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Decline Button
        OutlinedButton(
            onClick = onDecline,
            modifier = Modifier.weight(1f),
            enabled = !isLoading,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = AccentRed
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed)
        ) {
            Text("Decline", fontWeight = FontWeight.Bold)
        }
        
        // Accept Button
        Button(
            onClick = onAccept,
            modifier = Modifier.weight(1f),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentGreen
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Accept", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun MatchSuccessContent(
    matchedUserName: String,
    onMessageClick: () -> Unit,
    onClose: () -> Unit
) {
    // Match animation placeholder
    Text(
        text = "🎮",
        fontSize = 48.sp
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
        text = "It's a Match!",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = PrimaryGold
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Text(
        text = "You and $matchedUserName can now message each other!",
        fontSize = 14.sp,
        color = Color.White.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    // Message Button
    Button(
        onClick = onMessageClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentGreen
        )
    ) {
        Text("💬 Send Message", fontWeight = FontWeight.Bold, color = Color.White)
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    // Close Button
    TextButton(onClick = onClose) {
        Text("Maybe Later", color = Color.White.copy(alpha = 0.6f))
    }
}
