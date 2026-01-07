package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.RecentActivity
import com.example.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LevelProgressBar(
    level: Int,
    currentXP: Int,
    nextLevelXP: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (nextLevelXP > 0) currentXP.toFloat() / nextLevelXP.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "xpProgress"
    )
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LEVEL $level",
                fontFamily = PressStart,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$currentXP / $nextLevelXP XP",
                fontFamily = PressStart,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .border(2.dp, PixelBlack, RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(PixelGreen, PixelGreen.copy(alpha = 0.7f))
                        ),
                        RoundedCornerShape(4.dp)
                    )
            )
            
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                fontFamily = PressStart,
                fontSize = 10.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun AchievementBadge(
    achievementId: String,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    val (icon, title, color) = when (achievementId) {
        "first_game" -> Triple(Icons.Default.Star, "First Game", PixelBlue)
        "collector" -> Triple(Icons.Default.Collections, "Collector", PixelGreen)
        "speedrunner" -> Triple(Icons.Default.Speed, "Speedrunner", PixelRed)
        "social" -> Triple(Icons.Default.People, "Social", Color(0xFFFF9800))
        "completionist" -> Triple(Icons.Default.CheckCircle, "Complete", PixelGreen)
        else -> Triple(Icons.Default.EmojiEvents, "Achievement", PixelBlue)
    }
    
    Column(
        modifier = modifier.width(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(if (isUnlocked) color else Color.Gray.copy(alpha = 0.3f))
                .border(2.dp, PixelBlack, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isUnlocked) Color.White else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = title,
            fontFamily = PressStart,
            fontSize = 8.sp,
            color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray,
            maxLines = 2
        )
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    PixelatedCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                fontFamily = PressStart,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = title,
                fontFamily = PressStart,
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}

@Composable
fun ActivityRow(
    activity: RecentActivity,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val date = remember(activity.timestamp) { Date(activity.timestamp) }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .border(1.dp, PixelBlack, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Game Cover
        CachedAsyncImage(
            url = activity.gameCover,
            contentDescription = activity.gameName,
            modifier = Modifier
                .size(50.dp, 70.dp)
                .clip(RoundedCornerShape(4.dp))
                .border(1.dp, PixelBlack, RoundedCornerShape(4.dp))
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.type.uppercase(),
                fontFamily = PressStart,
                fontSize = 8.sp,
                color = PixelBlue
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = activity.gameName,
                fontFamily = PressStart,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
            
            if (activity.details != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activity.details,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = dateFormat.format(date),
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun GamerTagRow(
    platform: String,
    username: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = platform,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = platform,
                fontFamily = PressStart,
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = username,
                fontFamily = PressStart,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun PlayStyleChip(
    style: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = style,
                fontFamily = PressStart,
                fontSize = 8.sp
            )
        },
        modifier = modifier,
        enabled = true,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PixelBlue,
            selectedLabelColor = Color.White,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = PixelBlack,
            selectedBorderColor = PixelBlack,
            borderWidth = 2.dp,
            enabled = true,
            selected = isSelected
        )
    )
}

@Composable
fun LanguageChip(
    language: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = language,
                    fontFamily = PressStart,
                    fontSize = 8.sp
                )
            }
        },
        modifier = modifier,
        enabled = true,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PixelGreen,
            selectedLabelColor = Color.White,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = PixelBlack,
            selectedBorderColor = PixelBlack,
            borderWidth = 2.dp,
            enabled = true,
            selected = isSelected
        )
    )
}
