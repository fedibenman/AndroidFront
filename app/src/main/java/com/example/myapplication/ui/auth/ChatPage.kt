package com.example.myapplication.ui.auth

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.example.myapplication.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import com.example.myapplication.ui.theme.PressStart
import androidx.compose.ui.layout.ContentScale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.Repository.Conversation
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewModel.AiConversationViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.Alignment


@Composable


fun DrawerContent(
    conversations: List<Conversation>,
    onConversationClick: (Conversation) -> Unit,
    onStartAddingNewConversation: () -> Unit,
    isAddingNewConversation: Boolean,
    newConversationTitleInput: String,
    onUpdateNewConversationTitle: (String) -> Unit,
    onCreateNewConversation: (String) -> Unit,
    onCancelAddingNewConversation: () -> Unit,
    onClose: () -> Unit,
    onEditConversation: (Conversation, String) -> Unit,
    onDeleteConversation: (Conversation) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(260.dp)
            .background(Color(0xFFFEEEB0))
            .padding(16.dp)
    ) {
        // Close icon
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Icon( painter = painterResource(id = R.drawable.x_icon),
                contentDescription = "Close", tint = Color.Black,
                modifier = Modifier.size(28.dp).clickable
                { onClose() } )
        }
0
        Spacer(modifier = Modifier.height(12.dp))

        // HISTORY + ADD BUTTON
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("HISTORY", fontSize = 18.sp)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_plus),
                    contentDescription = "Add new conversation",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable {
                            // Create new conversation with empty title
                            onCreateNewConversation("")
                        }
                )
            }
        }

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            conversations.forEach { conv ->
                ConversationItem(
                    conversation = conv,
                    onClick = { onConversationClick(conv) },
                    onEdit = { newTitle -> onEditConversation(conv, newTitle) },
                    onDelete = { onDeleteConversation(conv) }
                )
            }
        }
    }
}


@Composable
fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf(conversation.title) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(8.dp)
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.container),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )

        if (isEditing) {
            // Edit mode - show TextField
            TextField(
                value = editText,
                onValueChange = { editText = it },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 12.dp, vertical = 0.dp)
                    .width(150.dp),
                textStyle = TextStyle(fontSize = 20.sp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                )
            )
            
            // Save button
            Icon(
                painter = painterResource(id = R.drawable.send),
                contentDescription = "Save",
                tint = Color.Black,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .clickable {
                        // Call the backend to update the conversation title
                        onEdit(editText)
                        isEditing = false
                    }
            )
            
            // Cancel button
            Icon(
                painter = painterResource(id = R.drawable.x_icon),
                contentDescription = "Cancel",
                tint = Color.Gray,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.Center)
                    .padding(end = 40.dp)
                    .clickable {
                        isEditing = false
                        editText = conversation.title // Reset to original
                    }
            )
        } else {
            // Normal mode - show Text with action buttons
            Text(
                text = conversation.title,
                color = Color.Black,
                fontSize = 12.sp, // Smaller font
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .width(120.dp) // Reduced width to prevent overlap with buttons
                    .clickable { onClick() }
            )
            
            // Action buttons container
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
            ) {
                // Edit button
                Icon(
                    painter = painterResource(id = R.drawable.update_ic),
                    contentDescription = "Edit Conversation",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 8.dp)
                        .clickable { isEditing = true }
                )
                
                // Delete button
                Icon(
                    painter = painterResource(id = R.drawable.delete_ic),
                    contentDescription = "Delete Conversation",
                    tint = Color.Red,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onDelete() }
                )
            }
        }
    }
}



@Composable
fun ChatBubbleLeft(
    text: String,
    messageId: String,
    onEditMessage: (String, String) -> Unit,
    onDeleteMessage: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf(text) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start // align to left
    ) {
        Column {
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .fillMaxWidth(0.8f) // max 80% of screen width
            ) {
                Image(
                    painter = painterResource(id = R.drawable.container),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                
                if (isEditing) {
                    TextField(
                        value = editText,
                        onValueChange = { editText = it },
                        modifier = Modifier.padding(10.dp),
                        textStyle = TextStyle(fontSize = 16.sp),
                        singleLine = false,
                        maxLines = 5
                    )
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp, bottom = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.x_icon),
                            contentDescription = "Cancel",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(2.dp)
                                .clickable {
                                    isEditing = false
                                    editText = text // Reset to original text
                                }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.send),
                            contentDescription = "Save",
                            modifier = Modifier
                                .size(20.dp)
                                .padding(2.dp)
                                .clickable {
                                    onEditMessage(messageId, editText)
                                    isEditing = false
                                }
                        )
                    }
                } else {
                    Text(
                        text,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // 3-dots menu button - positioned below the bubble
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.drop_down_icon),
                    contentDescription = "Menu",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(4.dp)
                        .clickable { dropdownExpanded = true }
                )
            }


        }
    }
}



@Composable
fun ChatBubbleRight(
    text: String,
    messageId: String,
    onEditMessage: (String, String) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onEditMessageFromDropdown: (String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf(text) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Column(horizontalAlignment = Alignment.End) {

            // ---------------- BUBBLE ----------------
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .wrapContentHeight()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.container),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )

                if (isEditing) {
                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {
                        TextField(
                            value = editText,
                            onValueChange = { editText = it },
                            singleLine = false,
                            maxLines = 5,
                            textStyle = TextStyle(fontSize = 16.sp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.x_icon),
                                contentDescription = "Cancel",
                                tint = Color.Gray,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(4.dp)
                                    .clickable {
                                        isEditing = false
                                        editText = text
                                    }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                painter = painterResource(id = R.drawable.send),
                                contentDescription = "Save",
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(4.dp)
                                    .clickable {
                                        onEditMessage(messageId, editText)
                                        isEditing = false
                                    }
                            )
                        }
                    }
                } else {
                    Text(
                        text,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ---------------- DROPDOWN ANCHOR ----------------
            Box(
                modifier = Modifier.wrapContentSize(Alignment.TopEnd)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.drop_down_icon),
                    contentDescription = "Menu",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(4.dp)
                        .clickable { dropdownExpanded = true }
                )

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            dropdownExpanded = false
                            // Trigger edit mode from parent composable
                            onEditMessageFromDropdown(messageId)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            dropdownExpanded = false
                            onDeleteMessage(messageId)
                        }
                    )
                }
            }
        }
    }
}




@Composable
fun ChatPage(
    viewModel: AiConversationViewModel,
    userId: String
) {
    val selectedConversation by viewModel.selectedConversation.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val messageInput by viewModel.messageInput.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val error by viewModel.error.collectAsState()

    val isAddingNewConversation by viewModel.isAddingNewConversation.collectAsState()
    val newConversationTitleInput by viewModel.newConversationTitleInput.collectAsState()

    // State for tracking which message is being edited
    var editingMessageId by remember { mutableStateOf<String?>(null) }
    var isEditingMode by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(userId) {
        viewModel.loadConversations(userId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(conversations.size) {
        if (conversations.isEmpty()) {
            viewModel.createNewConversation("New quest", userId)
        }
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                conversations = conversations,
                onConversationClick = { conv ->
                    viewModel.selectConversation(conv, userId)
                    scope.launch { drawerState.close() }
                },
                onStartAddingNewConversation = {},
                isAddingNewConversation = false,
                newConversationTitleInput = "",
                onUpdateNewConversationTitle = {},
                onCreateNewConversation = { title -> viewModel.createNewConversation(title, userId) },
                onCancelAddingNewConversation = {},
                onClose = { scope.launch { drawerState.close() } },
                onEditConversation = { conversation, newTitle -> 
                    // Call the ViewModel to update the conversation title
                    viewModel.editConversationTitle(conversation.id, newTitle)
                },
                onDeleteConversation = { conv -> 
                    // Implement delete conversation logic
                    viewModel.deleteConversation(conv.id)
                }
            )
        }
    ) {
        Image(
            painter = painterResource(id = R.drawable.background_general),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()

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
                    modifier = Modifier.size(28.dp).clickable { 
                        scope.launch { drawerState.open() }
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    selectedConversation?.title ?: "NEW QUEST",
                    style = TextStyle(fontFamily = PressStart, color = Color.White, fontSize = 18.sp)
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            if (error != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = error!!,
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Chat Messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = messages,
                    key = { it.id }
                ) { msg ->
                    if (msg.sender == "user") {
                        // User messages should appear on the right side
                        ChatBubbleRight(
                            text = msg.content,
                            messageId = msg.id,
                            onEditMessage = { messageId, newText ->
                                viewModel.editMessage(messageId, newText, userId)
                            },
                            onDeleteMessage = { messageId ->
                                viewModel.deleteMessage(messageId, userId)
                            },
                            onEditMessageFromDropdown = { messageId ->
                                // Find the message and set up editing mode
                                val messageToEdit = messages.find { it.id == messageId }
                                if (messageToEdit != null) {
                                    viewModel.messageInput.value = messageToEdit.content
                                    editingMessageId = messageId
                                    isEditingMode = true
                                }
                            }
                        )
                    } else {
                        // AI/other messages should appear on the left side
                        ChatBubbleLeft(
                            text = msg.content,
                            messageId = msg.id,
                            onEditMessage = { messageId, newText ->
                                viewModel.editMessage(messageId, newText, userId)
                            },
                            onDeleteMessage = { messageId ->
                                viewModel.deleteMessage(messageId, userId)
                            }
                        )
                    }
                }
            }

            // Input Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(8.dp)
            ) {
                // Background image
                Image(
                    painter = painterResource(id = R.drawable.container),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )

                // Foreground content: text field + send button
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageInput,
                        onValueChange = { newValue ->
                            viewModel.messageInput.value = newValue
                            // Reset edit mode if text field is cleared
                            if (newValue.isEmpty() && isEditingMode) {
                                editingMessageId = null
                                isEditingMode = false
                            }
                        },
                        placeholder = { 
                            Text(
                                if (isEditingMode) "Edit your message..." else "Type your message…", 
                                style = TextStyle(fontFamily = PressStart)
                            ) 
                        },
                        textStyle = TextStyle(
                            color = if (isEditingMode) Color(0xFFFF6600) else Color.Black
                        ),
                        modifier = Modifier.weight(1f).background(color = Color.White)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.send),
                        contentDescription = if (isEditingMode) "Update" else "Send",
                        modifier = Modifier.size(36.dp).clickable {
                            if (isEditingMode && editingMessageId != null) {
                                // Update the existing message
                                val messageId = editingMessageId ?: return@clickable
                                viewModel.editMessage(messageId, messageInput, userId)
                                // Reset editing mode
                                editingMessageId = null
                                isEditingMode = false
                                viewModel.messageInput.value = ""
                            } else {
                                // Send new message
                                viewModel.sendMessage(userId)
                            }
                        }
                    )
                }
            }

        }
    }
}





@Preview(showBackground = true)
@Composable
fun PreviewChatPageWithDrawer() {
    val mockViewModel = AiConversationViewModel()
    MyApplicationTheme {
        ChatPage(
            viewModel = mockViewModel,
            userId = "user123"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDrawer() {
    // Create mock conversations for preview - using correct constructor
    val mockConversations = listOf(
        Conversation(id = "conv1", title = "How to use Android Compose", userId = "user123"),
        Conversation(id = "conv2", title = "Building Chat Applications",  userId = "user123"),
        Conversation(id = "conv3", title = "MVVM Architecture Patterns", userId = "user123"),
        Conversation(id = "conv4", title = "Database Management in Android", userId = "user123"),
        Conversation(id = "conv5", title = "API Integration Best Practices",  userId = "user123")
    )
    
    MyApplicationTheme {
        DrawerContent(
            conversations = mockConversations,
            onConversationClick = { conversation ->
                println("Selected: ${conversation.title}")
            },
            onStartAddingNewConversation = {},
            isAddingNewConversation = false,
            newConversationTitleInput = "",
            onUpdateNewConversationTitle = {},
            onCreateNewConversation = { title ->
                println("Creating new conversation: $title")
            },
            onCancelAddingNewConversation = {},
            onClose = {},
            onEditConversation = { conversation, newTitle ->
                println("Edit conversation: ${conversation.title} -> $newTitle")
            },
            onDeleteConversation = { conversation ->
                println("Delete conversation: ${conversation.title}")
            }
        )
    }
}
