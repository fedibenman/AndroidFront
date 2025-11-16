package com.example.myapplication.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.example.myapplication.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.myapplication.ui.theme.PressStart
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.Repository.Conversation
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewModel.AiConversationViewModel


@Composable
fun DrawerContent(
    conversations: List<Conversation>,
    onConversationClick: (Conversation) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(260.dp)
            .background(Color(0xFFFEEEB0))
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Icon(
                painter = painterResource(id = R.drawable.x_icon),
                contentDescription = "Close",
                tint = Color.Black,
                modifier = Modifier.size(28.dp).clickable { onClose() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("HISTORY", fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp))

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            conversations.forEach { conv ->
                ConversationItem(conv.title, onClick = { onConversationClick(conv) })
            }
        }
    }
}

@Composable
fun ConversationItem(title: String, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = R.drawable.container),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Text(
            title,
            color = Color.Black,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 12.dp)
        )
    }
}

@Composable
fun ConversationItem(title: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(10.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.container),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth().fillMaxHeight()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
//
            Text(text = title, color = Color.Black, fontSize = 14.sp)
        }
    }
}



@Composable
fun ChatBubbleLeft(text: String) {
    Row (Modifier.fillMaxWidth().fillMaxHeight(), horizontalArrangement = Arrangement.Start) {
Box(
    Modifier
        .fillMaxHeight()
) {
Image(
        painter = painterResource(id = R.drawable.container),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
    )
    Text(
        text,
        fontSize = 16.sp,
        modifier = Modifier.padding(8.dp)
    )
}
    }
}

@Composable
fun ChatBubbleRight(text: String) {
    Row (Modifier.fillMaxWidth().fillMaxHeight(), horizontalArrangement = Arrangement.Start) {
Box(
    Modifier
        .fillMaxHeight()
) {
    Image(
        painter = painterResource(id = R.drawable.container),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()

    )
    Text(
        text,
        fontSize = 16.sp,
        modifier = Modifier.padding(8.dp)
    )
}
    }
}



@Composable
fun ChatPage(
    viewModel: AiConversationViewModel,
    userId: String,
    onMenuClick: () -> Unit
) {
    val selectedConversation by viewModel.selectedConversation.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val messageInput by viewModel.messageInput.collectAsState()
    val conversations by viewModel.conversations.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background_general),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF282828))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.burger_icon),
                    contentDescription = "Menu",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp).clickable { onMenuClick() }
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    selectedConversation?.title ?: "NEW QUEST",
                    style = TextStyle(fontFamily = PressStart, color = Color.White, fontSize = 18.sp)
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            // Chat Messages
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                messages.forEach { msg ->
                    if (msg.senderId == userId) ChatBubbleRight(msg.content)
                    else ChatBubbleLeft(msg.content)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = messageInput,
                    onValueChange = { viewModel.messageInput.value = it },
                    placeholder = { Text("Type your message…", style = TextStyle(fontFamily = PressStart)) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.send),
                    contentDescription = "Send",
                    modifier = Modifier.size(36.dp).clickable {
                        viewModel.sendMessage(userId)
                    }
                )
            }
        }
    }
}





@Preview(showBackground = true)
@Composable
fun PreviewChatPageWithDrawer() {
    MyApplicationTheme {
        ChatPage(
            onMenuClick = {},
            viewModel = TODO(),
            userId = TODO()
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewDrawer() {
    MyApplicationTheme {
        DrawerContent(
            onClose = {},
            conversations = TODO(),
            onConversationClick = TODO()
        )
    }
}
