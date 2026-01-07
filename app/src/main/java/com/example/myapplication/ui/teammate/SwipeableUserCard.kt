package com.example.myapplication.ui.teammate

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.DTOs.Profile
import com.example.myapplication.ui.components.CachedAsyncImage
import com.example.myapplication.ui.components.PixelatedCard
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.PressStart
import com.example.myapplication.ui.theme.PixelGreen
import com.example.myapplication.ui.theme.PixelatedFont
import com.example.myapplication.ui.theme.PrimaryGold
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SwipeableUserCard(
    profile: Profile,
    onSwipe: (SwipeDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    val rotation by animateFloatAsState(targetValue = offsetX / 10f)
    
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 150.dp.toPx() }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .rotate(rotation)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (abs(offsetX) > swipeThreshold) {
                            onSwipe(if (offsetX > 0) SwipeDirection.Right else SwipeDirection.Left)
                        } else {
                            offsetX = 0f
                            offsetY = 0f
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            }
            .fillMaxSize()
            .padding(16.dp)
    ) {
        PixelatedCard(
            modifier = Modifier.fillMaxSize(),
            // onClick = {} // Removed as PixelatedCard doesn't support it directly
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Image (Favorite Game Cover) or Gradient Fallback
                if (profile.favoriteGame?.coverUrl != null) {
                    CachedAsyncImage(
                        url = profile.favoriteGame.coverUrl,
                        contentDescription = "Cover Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback: Gradient with user initial
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF6B5B95),  // Purple
                                        Color(0xFF88B04B),  // Green
                                        Color(0xFF3D5A80)   // Blue
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Show user's initial as large text
                        Text(
                            text = (profile.name?.firstOrNull()?.uppercase() ?: "?"),
                            fontSize = 120.sp,
                            fontFamily = PressStart,
                            color = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }

                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                                startY = 300f
                            )
                        )
                )

                // Compatibility Badge (Top Right)
                if (profile.matchScore != null) {
                    val score = profile.matchScore
                    val badgeColor = if (score >= 80) Color(0xFF4CAF50) else if (score >= 50) Color(0xFFFFC107) else Color.Gray

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(56.dp)
                            .background(badgeColor.copy(alpha = 0.9f), androidx.compose.foundation.shape.CircleShape)
                            .border(2.dp, Color.White, androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${score}%",
                                fontFamily = PressStart,
                                fontSize = 10.sp,
                                color = Color.White
                            )
                            Text(
                                text = "MATCH",
                                fontFamily = PressStart,
                                fontSize = 6.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // User Info
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Text(
                        text = profile.name ?: "Unknown",
                        fontFamily = PressStart,
                        color = PrimaryGold,
                        fontSize = 24.sp,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black,
                                blurRadius = 4f
                            )
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (!profile.bio.isNullOrBlank()) {
                        Text(
                            text = profile.bio,
                            fontFamily = PixelatedFont,
                            color = Color.White,
                            fontSize = 14.sp,
                            maxLines = 3,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Tags Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.wrapContentWidth()
                    )   {
                         profile.playStyles?.take(3)?.forEach { style ->
                             Text(
                                 text = style,
                                 color = Color.Black,
                                 fontSize = 10.sp,
                                 fontFamily = PixelatedFont,
                                 modifier = Modifier
                                     .background(PrimaryGold, RoundedCornerShape(4.dp))
                                     .padding(horizontal = 6.dp, vertical = 2.dp)
                             )
                         }
                    }
                }
                
                // Overlay text for Like/Pass
                if (abs(offsetX) > swipeThreshold * 0.5f) {
                    val isLike = offsetX > 0
                    Text(
                        text = if (isLike) "LIKE" else "PASS",
                        color = if (isLike) Color.Green else Color.Red,
                        fontSize = 40.sp,
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Bold,
                         modifier = Modifier
                             .align(if (isLike) Alignment.TopStart else Alignment.TopEnd)
                             .padding(32.dp)
                             .rotate(if (isLike) -15f else 15f)
                             .border(4.dp, if (isLike) Color.Green else Color.Red, RoundedCornerShape(8.dp))
                             .padding(8.dp)
                    )
                }
            }
        }
    }
}
