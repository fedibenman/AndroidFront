package com.example.myapplication.ui.auth

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.Repository.Conversation
import com.example.myapplication.Repository.ImageData
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.PressStart
import com.example.myapplication.viewModel.AiConversationViewModel
import com.example.myapplication.ui.theme.LocalThemeManager
import com.example.myapplication.ui.theme.AnimatedThemeToggle
import kotlinx.coroutines.launch


@Composable
fun StyledContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
    ) {
        // Shadow layer (offset to bottom-right for pixel art effect)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(color = Color(0xFF4A4A4A))
                .border(width = 2.dp, color = Color.Black)
        )
        
        // Main container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
                .border(width = 2.dp, color = Color.Black)
        ) {
            content()
        }
    }
}


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

    StyledContainer(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(8.dp)
    ) {

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
    images: List<ImageData>,
    onEditMessage: (String, String, List<Bitmap>) -> Unit,
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
            // Images displayed on top of the message bubble (outside container)
            if (images.isNotEmpty() && !isEditing) {
                LazyRow(
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    items(images.size) { idx ->
                        val imageData = images[idx]
                        val bitmap = imageData.base64.decodeToBitmap()
                        
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = imageData.fileName,
                            modifier = Modifier
                                .size(40.dp) // Smaller size
                                .padding(end = 4.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            StyledContainer(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text,
                        fontSize = 16.sp
                    )
                }
            }

        }
    }
}



@Composable
fun ChatBubbleRight(
    text: String,
    messageId: String,
    images: List<ImageData>,   // ← MESSAGE IMAGES ARE PASSED HERE
    onEditMessage: (String, String, List<ImageData>) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onEditMessageFromDropdown: (String, List<ImageData>) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf(text) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Local copy for editing
    var localImages by remember { mutableStateOf(images.toMutableList()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Column(horizontalAlignment = Alignment.End) {
            // Images displayed on top of the message bubble (outside container)
            if (localImages.isNotEmpty() && !isEditing) {
                LazyRow(
                    modifier = Modifier
                        .padding(bottom = 7.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    items(localImages.size) { idx ->
                        val imageData = localImages[idx]
                        val bitmap = imageData.base64.decodeToBitmap()
                        
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = imageData.fileName,
                            modifier = Modifier
                                .size(70.dp) // Smaller size
                                .padding(start = 4.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            StyledContainer(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .wrapContentHeight()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = text,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ------------------ DROPDOWN ------------------
            Box(
                modifier = Modifier.wrapContentSize(Alignment.TopEnd)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.drop_down_icon),
                    contentDescription = "Menu",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(24.dp)
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
                            isEditing = true
                            editText = text
                            localImages = images.toMutableList()
                            onEditMessageFromDropdown(messageId, localImages)
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

// Simple Base64 to Bitmap extension
fun String.decodeToBitmap(): Bitmap {
    val decodedBytes = Base64.decode(this, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
}






@Composable
fun ChatPage(
    viewModel: AiConversationViewModel
) {
    val themeManager = LocalThemeManager.current
    val isDarkMode = themeManager.isDarkMode
    
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
    
    // Image upload state
    var selectedImageUris by remember { mutableStateOf<MutableList<Uri>>(mutableStateListOf()) }
    var selectedImageBitmaps by remember { mutableStateOf<MutableList<Bitmap>>(mutableStateListOf()) }
    var showImagePreview by remember { mutableStateOf(false) }
    
    // Edit-specific image state
    var editingMessageImages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    
    // Context for file operations
    val context = LocalContext.current
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                selectedImageUris.add(uri)
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    selectedImageBitmaps.add(bitmap) // Store Android Bitmap directly
                    showImagePreview = selectedImageBitmaps.isNotEmpty()
                    inputStream?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    )

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.loadConversations()
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Removed auto-creation of conversations here since it's now handled in loadConversations()
    // Only create conversations when explicitly requested by user

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                conversations = conversations,
                onConversationClick = { conv ->
                    viewModel.selectConversation(conv)
                    scope.launch { drawerState.close() }
                },
                onStartAddingNewConversation = {},
                isAddingNewConversation = false,
                newConversationTitleInput = "",
                onUpdateNewConversationTitle = {},
                onCreateNewConversation = { title -> viewModel.createNewConversation(title) },
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
        Box(modifier = Modifier.fillMaxSize()) {
            // Background based on theme
            if (isDarkMode) {
                Image(
                    painter = painterResource(id = R.drawable.background_dark),
                    contentDescription = "Dark Background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.background_general),
                    contentDescription = "Background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

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
                            images = msg.images,
                            onEditMessage = { messageId, newText, images ->
                                // Convert the ImageData of this message to Bitmaps before sending to ViewModel
                                val bitmaps = images.map { imageData ->
                                    val decodedBytes = Base64.decode(imageData.base64, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                }

                                viewModel.editMessage(messageId, newText, bitmaps)
                            },
                            onDeleteMessage = { messageId ->
                                viewModel.deleteMessage(messageId)
                            },
                            onEditMessageFromDropdown = { messageId, images ->
                                // Find the message and set up editing mode
                                val messageToEdit = messages.find { it.id == messageId }
                                if (messageToEdit != null) {
                                    viewModel.messageInput.value = messageToEdit.content

                                    editingMessageId = messageId
                                    isEditingMode = true
                                    val bitmaps = images.map { imageData ->
                                        val decodedBytes = Base64.decode(imageData.base64, Base64.DEFAULT)
                                        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                    }
                                    // Store this message's images so they appear in edit mode
                                    editingMessageImages = bitmaps
                                    selectedImageBitmaps.clear()
                                    selectedImageBitmaps.addAll(bitmaps)
                                    showImagePreview = selectedImageBitmaps.isNotEmpty()
                                }
                            }
                        )
                    } else {
                        // AI/other messages should appear on the left side
                        ChatBubbleLeft(
                            text = msg.content,
                            messageId = msg.id,
                            images = msg.images,
                            onEditMessage = { messageId, newText, bitmaps ->
                                viewModel.editMessage(messageId, newText, bitmaps)
                            },
                            onDeleteMessage = { messageId ->
                                viewModel.deleteMessage(messageId)
                            }
                        )
                    }
                }
            }

            // Input Bar (Dynamic Height Container - expands to contain images)
            val inputBarHeight = if (showImagePreview && selectedImageBitmaps.isNotEmpty()) {
                200.dp// 1fwef + 60dp
            } else {
                110.dp
            }

            StyledContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(inputBarHeight)
                    .padding(8.dp)
            ) {
                // Foreground content: image preview + input controls
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Image Preview (top section, only when images exist)
                        if (showImagePreview && selectedImageBitmaps.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .height(60.dp)
                                    .padding(top = 12.dp)
                                    .fillMaxWidth()
                            ) {
                                LazyRow(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    items(selectedImageBitmaps.size) { index ->
                                        Row(
                                            modifier = Modifier
                                                .padding(end = 8.dp)
                                                .height(60.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Image preview
                                            Image(
                                                bitmap = selectedImageBitmaps[index].asImageBitmap(),
                                                contentDescription = "Selected image preview $index",
                                                modifier = Modifier
                                                    .height(60.dp)
                                                    .width(60.dp)
                                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                                    .background(Color.LightGray)
                                                    .padding(end = 8.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                            
                                            // Remove image button
                                            Icon(
                                                painter = painterResource(id = R.drawable.x_icon),
                                                contentDescription = "Remove image",
                                                tint = Color.Red,
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clickable {
                                                        selectedImageUris.removeAt(index)
                                                        selectedImageBitmaps.removeAt(index)
                                                        showImagePreview = selectedImageBitmaps.isNotEmpty()
                                                    }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Input controls (bottom section, always present)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Add Image Button
                            Icon(
                                painter = painterResource(id = R.drawable.add_image_button),
                                contentDescription = "Add image",
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(end = 8.dp)
                                    .clickable {
                                        imagePickerLauncher.launch("image/*")
                                    }
                            )
                            
                            // Text field
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
                            
                            // Send button
                            Icon(
                                painter = painterResource(id = R.drawable.send),
                                contentDescription = if (isEditingMode) "Update" else "Send",
                                modifier = Modifier.size(36.dp).clickable {
                                    if (isEditingMode && editingMessageId != null) {
                                        // Update the existing message with images
                                        val messageId = editingMessageId ?: return@clickable
                                        val bitmaps = if (selectedImageBitmaps.isNotEmpty()) {
                                            selectedImageBitmaps.toList() as List<Bitmap>
                                        } else {
                                            editingMessageImages // Use original images if none selected
                                        }
                                        viewModel.editMessage(messageId, messageInput, bitmaps)
                                        // Reset editing mode
                                        editingMessageId = null
                                        isEditingMode = false
                                        viewModel.messageInput.value = ""
                                        // Clear images after editing
                                        selectedImageUris.clear()
                                        selectedImageBitmaps.clear()
                                        showImagePreview = false
                                    } else {
                                        // Send new message with images
                                        val bitmaps = if (selectedImageBitmaps.isNotEmpty()) {
                                            selectedImageBitmaps.toList() as List<Bitmap>
                                        } else {
                                            emptyList()
                                        }
                                        viewModel.sendMessageWithImages(messageInput, bitmaps)
                                        
                                        // Clear images after sending
                                        selectedImageUris.clear()
                                        selectedImageBitmaps.clear()
                                        showImagePreview = false
                                        viewModel.messageInput.value = ""
                                    }
                                }
                            )
                        }
                    }
                }
            }
            }

            // Theme toggle button at top right - positioned last to be on top
            Box(
                modifier = Modifier
                    .padding(top = 50.dp, end = 20.dp)
                    .align(Alignment.TopEnd)
            ) {
                AnimatedThemeToggle()
            }
        }
    }
}





@Preview(showBackground = true)
@Composable
fun PreviewChatPageWithDrawer() {
    MyApplicationTheme {
        ChatPage(
            viewModel = remember { AiConversationViewModel() }
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
