package com.example.myapplication.directmessages.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.directmessages.model.Conversation
import com.example.myapplication.directmessages.viewmodel.DirectMessagesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectMessagesListScreen(
    viewModel: DirectMessagesViewModel,
    userId: String,
    onConversationClick: (Conversation) -> Unit,
    onNavigateBack: () -> Unit,
    onNewMessage: () -> Unit
) {
    val conversations by viewModel.conversations.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNewMessage) {
                        Icon(Icons.Default.Add, contentDescription = "New Message")
                    }
                }
            )
        }
    ) { padding ->
        if (conversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No conversations yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(conversations) { conversation ->
                    ConversationItem(
                        conversation = conversation,
                        currentUserId = userId,
                        onClick = { onConversationClick(conversation) }
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    currentUserId: String,
    onClick: () -> Unit
) {
    // Find the other participant
    val otherUser = conversation.participants.find { it._id != currentUserId }
        ?: conversation.participants.firstOrNull()

    ListItem(
        headlineContent = {
            Text(
                text = otherUser?.name ?: "Unknown User",
                fontWeight = FontWeight.Bold
            )
        },
        supportingContent = {
            Text(
                text = conversation.lastMessage?.content ?: "No messages yet",
                maxLines = 1
            )
        },
        leadingContent = {
            Icon(Icons.Default.Person, contentDescription = null)
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
    HorizontalDivider()
}
