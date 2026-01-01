package com.example.myapplication.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.*
import com.example.myapplication.viewModel.AiConversationViewModel

@Composable
fun ChatPage(viewModel: AiConversationViewModel) {
    val messages by viewModel.messages.collectAsState()
    val selectedConversation by viewModel.selectedConversation.collectAsState()
    var inputText by remember { mutableStateOf("") }

    // Load conversations when screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadConversations()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Blue) // Fallback or PixelBlue
                    .padding(16.dp)
            ) {
                Text(
                    text = selectedConversation?.title ?: "AI CHAT",
                    fontFamily = PressStart,
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Messages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(
                        text = message.content,
                        isUser = message.sender == "user"
                    )
                }
            }

            // Input Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text("Ask AI...", fontFamily = PressStart, fontSize = 10.sp)
                    },
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = PressStart,
                        fontSize = 12.sp
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(Modifier.width(8.dp))

                // Send Button
                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessageWithImages(inputText, emptyList())
                            inputText = ""
                        }
                    },
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(text: String, isUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    if (isUser) Color.Blue else Color.Gray,
                    RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = text,
                fontFamily = PressStart,
                fontSize = 10.sp,
                color = Color.White,
                lineHeight = 14.sp
            )
        }
    }
}
