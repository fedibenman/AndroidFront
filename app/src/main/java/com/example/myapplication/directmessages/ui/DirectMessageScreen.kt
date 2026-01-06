package com.example.myapplication.directmessages.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.directmessages.model.DirectMessage
import com.example.myapplication.directmessages.viewmodel.DirectMessagesViewModel
import com.example.myapplication.ui.theme.*

@Composable
fun DirectMessageScreen(
    viewModel: DirectMessagesViewModel,
    userId: String,
    currentUserName: String,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentConversation by viewModel.currentConversation.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val typingUser by viewModel.typingUser.collectAsState()
    
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    
    // Get other user
    val otherUser = currentConversation?.participants?.firstOrNull { it._id != userId }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PixelWhite)
            .padding(8.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(4.dp, PixelBlack)
                .background(PixelBlue)
                .padding(12.dp)
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PixelWhite,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                text = otherUser?.name ?: "Chat",
                modifier = Modifier.align(Alignment.Center),
                fontFamily = PressStart,
                fontSize = 14.sp,
                color = PixelWhite,
                fontWeight = FontWeight.Bold
            )
            
            // Voice call button
            Button(
                onClick = {
                    currentConversation?.let { conv ->
                        otherUser?.let { user ->
                            // Create call request
                            val callId = java.util.UUID.randomUUID().toString()
                            val callRequest = com.example.myapplication.calls.model.CallRequest(
                                callId = callId,
                                callerId = userId,
                                callerName = currentUserName,
                                calleeId = user._id,
                                calleeName = user.name,
                                conversationId = conv._id
                            )
                            
                            // Emit call request via Socket.IO
                            android.util.Log.d("DirectMessageScreen", "Initiating call to ${user.name}")
                            val repository = com.example.myapplication.directmessages.data.DirectMessagesRepository.getInstance()
                            repository.requestCall(callRequest)
                            
                            // Launch call activity immediately for caller
                            val intent = android.content.Intent(context, com.example.myapplication.chat.ui.CallActivity::class.java).apply {
                                putExtra("userID", userId)
                                putExtra("userName", currentUserName)
                                putExtra("callID", conv._id)
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PixelGreen),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .border(2.dp, PixelBlack, RoundedCornerShape(4.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Voice Call",
                    tint = PixelBlack,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "CALL",
                    fontFamily = PressStart,
                    fontSize = 10.sp,
                    color = PixelBlack
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Messages list
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState
        ) {
            items(messages) { message ->
                MessageBubble(message = message, isCurrentUser = message.sender._id == userId)
                Spacer(Modifier.height(8.dp))
            }
        }
        
        // Typing indicator
        if (typingUser != null) {
            Text(
                text = "$typingUser is typing...",
                fontFamily = PressStart,
                fontSize = 10.sp,
                color = PixelBlue,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            )
        }
        
        // Input field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { 
                    messageText = it
                    // Send typing indicator
                    if (it.isNotEmpty()) {
                        currentConversation?.let { conv ->
                            viewModel.sendTyping(conv._id, otherUser?.name ?: "User")
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                placeholder = { 
                    Text("Message...", fontFamily = PressStart, fontSize = 10.sp) 
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PixelBlack,
                    unfocusedBorderColor = PixelBlack
                ),
                textStyle = LocalTextStyle.current.copy(fontFamily = PressStart, fontSize = 12.sp),
                shape = RoundedCornerShape(4.dp)
            )
            
            Spacer(Modifier.width(8.dp))
            
            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        currentConversation?.let { conv ->
                            viewModel.sendMessage(conv._id, userId, messageText)
                            messageText = ""
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(PixelBlue, RoundedCornerShape(4.dp))
                    .border(2.dp, PixelBlack, RoundedCornerShape(4.dp))
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = PixelWhite
                )
            }
        }
    }
}

@Composable
fun MessageBubble(message: DirectMessage, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    if (isCurrentUser) PixelBlue else Color(0xFFE8F4F8),
                    RoundedCornerShape(8.dp)
                )
                .border(
                    2.dp,
                    if (isCurrentUser) PixelBlack else PixelGray.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = message.content,
                    fontFamily = PressStart,
                    fontSize = 11.sp,
                    color = if (isCurrentUser) PixelWhite else PixelBlack
                )
            }
        }
    }
}
