package com.example.myapplication.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.chat.model.ChatRoom
import com.example.myapplication.chat.viewmodel.ChatViewModel
import com.example.myapplication.directmessages.viewmodel.DirectMessagesViewModel
import com.example.myapplication.directmessages.model.Conversation
import com.example.myapplication.ui.theme.PressStart

import com.example.myapplication.ui.theme.PixelBlack
import com.example.myapplication.ui.theme.PixelBlue
import com.example.myapplication.ui.theme.PixelGray
import com.example.myapplication.ui.theme.PixelGreen
import com.example.myapplication.ui.theme.PixelRed
import com.example.myapplication.ui.theme.PixelWhite

@Composable
fun ChatListScreen(
    viewModel: ChatViewModel,
    dmViewModel: DirectMessagesViewModel,
    currentUserId: String,
    onRoomClick: (ChatRoom) -> Unit,
    onConversationClick: (Conversation) -> Unit,
    onNavigateBack: () -> Unit = {},
    onNavigateToDM: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val rooms by viewModel.rooms.collectAsState()
    val conversations by dmViewModel.conversations.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 for Rooms, 1 for DMs
    var showCreateDialog by remember { mutableStateOf(false) }

    if (showCreateDialog) {
        CreateRoomDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, description ->
                viewModel.createRoom(name, description)
                showCreateDialog = false
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PixelGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Header with back button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(4.dp, PixelBlack)
                    .background(PixelBlue)
                    .padding(12.dp)
            ) {
                // Back button
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PixelWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    text = "CHAT ROOMS",
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = PixelWhite,
                    letterSpacing = 2.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Tab Switcher
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = PixelWhite,
                contentColor = PixelBlue,
                divider = { HorizontalDivider(thickness = 4.dp, color = PixelBlack) },
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = PixelBlue,
                        height = 4.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(4.dp, PixelBlack)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "ROOMS",
                            fontFamily = PressStart,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "DMs",
                            fontFamily = PressStart,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            // List Content
            if (selectedTab == 0) {
                // Room list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(rooms) { room ->
                        RoomItem(
                            room = room,
                            onClick = { onRoomClick(room) }
                        )
                    }
                }
            } else {
                // DM list
                if (conversations.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text("NO DMs YET", fontFamily = PressStart, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(conversations) { conversation ->
                            val otherUser = conversation.participants.find { it._id != currentUserId }
                                ?: conversation.participants.firstOrNull()
                                
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(4.dp, PixelBlack)
                                    .background(PixelWhite)
                                    .clickable { onConversationClick(conversation) }
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Text(
                                        text = otherUser?.name ?: "UNKNOWN",
                                        fontFamily = PressStart,
                                        fontSize = 14.sp,
                                        color = PixelBlue
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = conversation.lastMessage?.content ?: "No messages",
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        color = PixelBlack
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB for Direct Messages (left)
        FloatingActionButton(
            onClick = onNavigateToDM,
            containerColor = PixelBlue,
            contentColor = PixelWhite,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .size(64.dp)
                .border(4.dp, PixelBlack, RoundedCornerShape(4.dp))
        ) {
            Icon(
                androidx.compose.material.icons.Icons.AutoMirrored.Filled.Message,
                contentDescription = "Direct Messages",
                tint = PixelWhite,
                modifier = Modifier.size(32.dp)
            )
        }

        // FAB to create room (right)
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = PixelGreen,
            contentColor = PixelBlack,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(64.dp)
                .border(4.dp, PixelBlack, RoundedCornerShape(4.dp))
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Create Room",
                tint = PixelBlack,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun CreateRoomDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PixelWhite,
        title = {
            Text(
                "NEW ROOM",
                fontFamily = PressStart,
                fontSize = 16.sp,
                color = PixelBlack
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name", fontFamily = PressStart, fontSize = 10.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PixelBlack,
                        unfocusedBorderColor = PixelBlack,
                        cursorColor = PixelBlack,
                        focusedLabelColor = PixelBlue,
                        unfocusedLabelColor = PixelBlack
                    ),
                    textStyle = LocalTextStyle.current.copy(fontFamily = PressStart, fontSize = 12.sp)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", fontFamily = PressStart, fontSize = 10.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PixelBlack,
                        unfocusedBorderColor = PixelBlack,
                        cursorColor = PixelBlack,
                        focusedLabelColor = PixelBlue,
                        unfocusedLabelColor = PixelBlack
                    ),
                    textStyle = LocalTextStyle.current.copy(fontFamily = PressStart, fontSize = 12.sp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(name, description)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PixelGreen),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.border(2.dp, PixelBlack, RoundedCornerShape(4.dp))
            ) {
                Text("CREATE", fontFamily = PressStart, color = PixelBlack, fontSize = 12.sp)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PixelRed),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.border(2.dp, PixelBlack, RoundedCornerShape(4.dp))
            ) {
                Text("CANCEL", fontFamily = PressStart, color = PixelWhite, fontSize = 12.sp)
            }
        },
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.border(4.dp, PixelBlack)
    )
}

@Composable
fun RoomItem(
    room: ChatRoom,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = PixelWhite),
        shape = RoundedCornerShape(0.dp),
        border = androidx.compose.foundation.BorderStroke(4.dp, PixelBlack)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Chat,
                contentDescription = null,
                tint = PixelBlue,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = room.name,
                    fontFamily = PressStart,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PixelBlack
                )
                room.description?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = it,
                        fontFamily = PressStart,
                        fontSize = 12.sp,
                        color = PixelBlack.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
