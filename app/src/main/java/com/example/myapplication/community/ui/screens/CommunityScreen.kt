package com.example.myapplication.community.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.community.model.Post
import com.example.myapplication.community.viewmodel.PostViewModel
import com.example.myapplication.ui.theme.PressStart

import com.example.myapplication.ui.theme.PixelBlack
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import com.example.myapplication.community.model.Comment
import com.example.myapplication.ui.theme.PixelBlue
import com.example.myapplication.ui.theme.PixelGray
import com.example.myapplication.ui.theme.PixelGreen
import com.example.myapplication.ui.theme.PixelRed
import com.example.myapplication.ui.theme.PixelWhite

@Composable
fun CommunityScreen(
    modifier: Modifier = Modifier,
    postViewModel: PostViewModel,
    onCreatePost: () -> Unit,
    onEditPost: (Post) -> Unit,
    onDeletePost: (Post) -> Unit,
    onNavigateToChat: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    val posts by postViewModel.posts.collectAsState()
    val notifications by postViewModel.notifications.collectAsState()
    val unreadCount = notifications.count { !it.read }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PixelGray)
    ) {

        // Background image (dimmed)
        Image(
            painter = painterResource(id = R.drawable.background_general),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.5f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {

            // 👾 PIXEL HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(4.dp, PixelBlack)
                    .background(PixelBlue)
                    .padding(12.dp)
            ) {
                Text(
                    text = "COMMUNITY",
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    color = PixelWhite,
                    letterSpacing = 4.sp,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Bell Icon
                IconButton(
                    onClick = onNavigateToNotifications,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Box {
                        Icon(
                            androidx.compose.material.icons.Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = PixelWhite
                        )
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(10.dp)
                                    .background(PixelRed, androidx.compose.foundation.shape.CircleShape)
                            )
                        }
                    }
                }
            }

            val replyingToComment by postViewModel.replyingToComment.collectAsState()
            
            // LISTE DES POSTS
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(posts) { post ->
                    PostItem(
                        post = post,
                        onLike = { post._id?.let { postViewModel.likePost(it) } },
                        onDislike = { post._id?.let { postViewModel.dislikePost(it) } },
                        onAddComment = { text -> post._id?.let { postViewModel.addComment(it, text) } },
                        onEdit = { onEditPost(post) },
                        onDelete = { onDeletePost(post) },
                        onReact = { emoji -> post._id?.let { postViewModel.reactToPost(it, emoji) } },
                        onReply = { comment -> postViewModel.setReplyTarget(comment) },
                        replyingToComment = replyingToComment
                    )
                }
            }
        }

        // Bottom Navigation Bar - Reddit style
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .border(4.dp, PixelBlack)
                .background(PixelWhite)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chat Rooms Button
            Button(
                onClick = onNavigateToChat,
                colors = ButtonDefaults.buttonColors(containerColor = PixelBlue),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .border(3.dp, PixelBlack, RoundedCornerShape(4.dp))
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Message,
                    contentDescription = "Chat Rooms",
                    tint = PixelWhite,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "CHAT",
                    fontFamily = PressStart,
                    fontSize = 12.sp,
                    color = PixelWhite
                )
            }

            Spacer(Modifier.width(12.dp))

            // Create Post Button
            Button(
                onClick = onCreatePost,
                colors = ButtonDefaults.buttonColors(containerColor = PixelGreen),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .border(3.dp, PixelBlack, RoundedCornerShape(4.dp))
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Create Post",
                    tint = PixelBlack,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "POST",
                    fontFamily = PressStart,
                    fontSize = 12.sp,
                    color = PixelBlack
                )
            }
        }
    }
}

// Recursive comment display with indentation
@Composable
fun CommentWithReplies(
    comment: Comment,
    onReply: (Comment) -> Unit,
    depth: Int = 0
) {
    val indentPadding = (depth * 16).dp
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indentPadding)
    ) {
        // Comment card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (depth > 0) Color(0xFFE8F4F8) else Color(0xFFF9F9F9),
                    RoundedCornerShape(4.dp)
                )
                .border(
                    width = if (depth > 0) 2.dp else 0.dp,
                    color = if (depth > 0) PixelBlue.copy(alpha = 0.3f) else Color.Transparent,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Show reply indicator for nested comments
                if (depth > 0) {
                    Text(
                        text = "↳ ",
                        fontSize = 12.sp,
                        color = PixelBlue
                    )
                }
                
                Text(
                    text = "${comment.author?.name ?: "User"}: ",
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = PixelBlue
                )
                Spacer(Modifier.weight(1f))
                // Small Reply Button
                Text(
                    text = "Reply",
                    fontFamily = PressStart,
                    fontSize = 8.sp,
                    color = PixelGray,
                    modifier = Modifier.clickable { 
                        onReply(comment)
                    }
                )
            }
            
            Text(
                text = comment.content ?: "",
                fontFamily = PressStart,
                fontSize = 10.sp,
                color = PixelBlack
            )
        }
        
        // Display replies recursively
        if (!comment.replies.isNullOrEmpty()) {
            Spacer(Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                comment.replies.forEach { reply ->
                    CommentWithReplies(
                        comment = reply,
                        onReply = onReply,
                        depth = depth + 1
                    )
                }
            }
        }
    }
}



@Composable
fun PostItem(
    post: Post,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onAddComment: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReact: (String) -> Unit,
    onReply: (Comment) -> Unit,
    replyingToComment: Comment?
) {
    var commentText by remember { mutableStateOf("") }
    var showComments by remember { mutableStateOf(false) }
    var showReactionPicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(4.dp),
                spotColor = PixelBlack.copy(alpha = 0.25f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = PixelWhite
        ),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(3.dp, PixelBlack)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            
            // ⬅ LEFT COLUMN: VOTES (Reddit Style)
            Column(
                modifier = Modifier
                    .width(56.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF8F9FA),
                                Color(0xFFEEEFF0)
                            )
                        )
                    )
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                val currentUserId = "69258d95573be880ade99495" // TODO: Get from auth
                val hasUpvoted = post.likes?.contains(currentUserId) == true
                val hasDownvoted = post.dislikes?.contains(currentUserId) == true
                val voteCount = (post.likes?.size ?: 0) - (post.dislikes?.size ?: 0)
                
                IconButton(onClick = onLike, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp, 
                        contentDescription = "Upvote",
                        tint = if (hasUpvoted) Color(0xFFFF4500) else Color(0xFF878A8C),
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Text(
                    text = when {
                        voteCount > 0 -> "+$voteCount"
                        voteCount < 0 -> "$voteCount"
                        else -> "0"
                    },
                    fontFamily = PressStart,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        hasUpvoted -> Color(0xFFFF4500)
                        hasDownvoted -> Color(0xFF7193FF)
                        else -> PixelBlack
                    }
                )

                IconButton(onClick = onDislike, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown, 
                        contentDescription = "Downvote",
                        tint = if (hasDownvoted) Color(0xFF7193FF) else Color(0xFF878A8C),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // ➡ RIGHT COLUMN: CONTENT
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                
                // 👤 HEADER: User • Time • Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Avatar placeholder
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(PixelBlue, androidx.compose.foundation.shape.CircleShape)
                                .border(1.dp, PixelBlack, androidx.compose.foundation.shape.CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "u/${post.author?.name ?: "Unknown"}",
                            fontFamily = PressStart,
                            fontSize = 10.sp,
                            color = PixelGray
                        )
                        Text(
                            text = " • 2h", // Mock time
                            fontFamily = PressStart,
                            fontSize = 10.sp,
                            color = PixelGray
                        )
                    }

                    // Edit/Delete Menu (simplified as icons for now)
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PixelGray, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = PixelGray, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 📝 TITLE
                Text(
                    text = post.title,
                    fontFamily = PressStart,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = PixelBlack,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(8.dp))

                // 📄 CONTENT (Text Body)
                if (post.content.isNotBlank()) {
                    Text(
                        text = post.content,
                        fontFamily = PressStart,
                        fontSize = 12.sp,
                        color = PixelBlack,
                        lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // 🖼 IMAGE
                if (!post.photo.isNullOrEmpty()) {
                    val bitmap = remember(post._id, post.photo) {
                        try {
                            val base64String = post.photo!!.substringAfter("base64,")
                            val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                            android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    
                    if (bitmap != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .border(2.dp, PixelBlack, RoundedCornerShape(4.dp))
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.LightGray)
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Post Image",
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 🎭 REACTIONS DISPLAY - Larger emojis
                if (!post.reactions.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Group reactions by emoji
                        post.reactions.groupBy { it.emoji }.forEach { (emoji, reactions) ->
                            Row(
                                modifier = Modifier
                                    .background(Color(0xFFF0F0F0), RoundedCornerShape(16.dp))
                                    .border(2.dp, PixelGray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .clickable { onReact(emoji) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = emoji,
                                    fontSize = 18.sp // Larger emoji
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "${reactions.size}",
                                    fontFamily = PressStart,
                                    fontSize = 10.sp,
                                    color = PixelBlack,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // 🦶 FOOTER: Actions (Comments, Share, React)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Comments Button
                    Button(
                        onClick = { showComments = !showComments },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = PixelGray
                        ),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Message,
                            contentDescription = "Comments",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "${post.comments.size}",
                            fontFamily = PressStart,
                            fontSize = 10.sp
                        )
                    }

                    // Share Button
                    Button(
                        onClick = { /* Share logic */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = PixelGray
                        ),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Share",
                            fontFamily = PressStart,
                            fontSize = 10.sp
                        )
                    }
                    
                    // React Button
                     Button(
                        onClick = { showReactionPicker = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = PixelGray
                        ),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "😊+",
                            fontSize = 14.sp
                        )
                    }
                }

                // 💬 COMMENTS SECTION (Expanded)
                if (showComments) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = PixelGray, thickness = 1.dp)
                    Spacer(Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        post.comments.forEach { c ->
                            // Display comment and its replies recursively
                            CommentWithReplies(
                                comment = c,
                                onReply = onReply,
                                depth = 0
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Reply Indicator
                    replyingToComment?.let { replyTarget ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE3F2FD), RoundedCornerShape(4.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Replying to ${replyTarget.author?.name ?: "User"}",
                                fontFamily = PressStart,
                                fontSize = 9.sp,
                                color = PixelBlue,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel reply",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onReply(replyTarget) }, // Toggle off
                                tint = PixelGray
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    // ✏ INPUT COMMENT
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Add a comment...", fontFamily = PressStart, fontSize = 10.sp) },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = PressStart,
                                fontSize = 10.sp
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PixelBlack,
                                unfocusedBorderColor = PixelGray,
                                cursorColor = PixelBlack
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    onAddComment(commentText)
                                    commentText = ""
                                }
                            },
                            modifier = Modifier
                                .background(PixelBlack, RoundedCornerShape(4.dp))
                                .size(48.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Message, contentDescription = "Send", tint = PixelWhite)
                        }
                    }
                }
            }
        }
    }
    
    // Reaction Picker Dialog (Simplified)
    if (showReactionPicker) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showReactionPicker = false },
            confirmButton = {},
            title = { Text("React", fontFamily = PressStart) },
            text = {
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    listOf("❤️", "👍", "😂", "🔥").forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 24.sp,
                            modifier = Modifier.clickable {
                                onReact(emoji)
                                showReactionPicker = false
                            }
                        )
                    }
                }
            }
        )
    }
}
