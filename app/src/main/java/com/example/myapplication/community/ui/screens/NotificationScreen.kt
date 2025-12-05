package com.example.myapplication.community.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.community.model.Notification
import com.example.myapplication.community.viewmodel.PostViewModel
import com.example.myapplication.ui.theme.PixelBlack
import com.example.myapplication.ui.theme.PixelBlue
import com.example.myapplication.ui.theme.PixelGray
import com.example.myapplication.ui.theme.PixelWhite
import com.example.myapplication.ui.theme.PressStart
import com.example.myapplication.ui.theme.PixelRed

@Composable
fun NotificationScreen(
    postViewModel: PostViewModel,
    onBack: () -> Unit
) {
    val notifications by postViewModel.notifications.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelGray)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .border(4.dp, PixelBlack)
                .background(PixelBlue)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PixelWhite
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "NOTIFICATIONS",
                fontFamily = PressStart,
                color = PixelWhite,
                fontSize = 18.sp
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notifications) { notification ->
                NotificationItem(
                    notification = notification,
                    onClick = {
                        if (!notification.read) {
                            postViewModel.markNotificationAsRead(notification._id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    val bgColor = if (notification.read) PixelWhite else Color(0xFFFFF9C4) // Light yellow for unread

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, PixelBlack)
            .background(bgColor)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = when (notification.type) {
            "LIKE" -> "❤️"
            "COMMENT" -> "💬"
            "REACT" -> notification.emoji ?: "😊"
            else -> "🔔"
        }
        
        Text(
            text = icon,
            fontSize = 24.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            val message = when (notification.type) {
                "LIKE" -> "${notification.fromUser?.name ?: "Unknown"} liked your post"
                "COMMENT" -> "${notification.fromUser?.name ?: "Unknown"} commented on your post"
                "REACT" -> "${notification.fromUser?.name ?: "Unknown"} reacted ${notification.emoji ?: "❤️"} to your post"
                else -> "${notification.fromUser?.name ?: "Unknown"} interacted with your post"
            }
            
            Text(
                text = message,
                fontFamily = PressStart,
                fontSize = 12.sp,
                color = PixelBlack,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "\"${notification.postId?.title ?: "Unknown Post"}\"",
                fontFamily = PressStart,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
        
        if (!notification.read) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(PixelRed, androidx.compose.foundation.shape.CircleShape)
            )
        }
    }
}
