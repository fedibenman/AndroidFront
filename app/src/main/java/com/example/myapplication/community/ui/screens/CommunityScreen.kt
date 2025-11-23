package com.example.myapplication.community.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

// 🎨 PIXEL ART COLORS
val PixelBlack = Color(0xFF000000)
val PixelWhite = Color(0xFFFFFFFF)
val PixelGray = Color(0xFFC0C0C0)
val PixelBlue = Color(0xFF5D9CEC)
val PixelRed = Color(0xFFAC193D)
val PixelGreen = Color(0xFF8FCE00)

@Composable
fun CommunityScreen(
    modifier: Modifier = Modifier,
    postViewModel: PostViewModel,
    onCreatePost: () -> Unit,
    onEditPost: (Post) -> Unit,
    onDeletePost: (Post) -> Unit
) {
    val posts by postViewModel.posts.collectAsState()

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
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "COMMUNITY",
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    color = PixelWhite,
                    letterSpacing = 4.sp
                )
            }

            // LISTE DES POSTS
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(posts) { post ->
                    PostItem(
                        post = post,
                        onLike = { post._id?.let { postViewModel.likePost(it) } },
                        onAddComment = { text -> post._id?.let { postViewModel.addComment(it, text) } },
                        onEdit = { onEditPost(post) },
                        onDelete = { onDeletePost(post) }
                    )
                }
            }
        }

        // ➕ FAB PIXEL
        FloatingActionButton(
            onClick = onCreatePost,
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
                contentDescription = "Add Post",
                tint = PixelBlack,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun PostItem(
    post: Post,
    onLike: () -> Unit,
    onAddComment: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    var showComments by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .shadow(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = PixelWhite
        ),
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(4.dp, PixelBlack)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 👤 AUTHOR + ACTIONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = post.author?.name ?: "Unknown",
                    fontFamily = PressStart,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PixelBlue
                )

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PixelBlack)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = PixelRed)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // 📝 TITLE
            Text(
                text = post.title,
                fontFamily = PressStart,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = PixelBlack
            )

            Spacer(Modifier.height(8.dp))

            // 🖼 IMAGE
            if (!post.photo.isNullOrEmpty()) {
                val bitmap = remember(post._id, post.photo) {
                    try {
                        // Remove the data URI prefix if present
                        val base64String = post.photo!!.substringAfter("base64,")
                        val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                        android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    } catch (e: Exception) {
                        android.util.Log.e("IMAGE_DECODE", "Failed to decode Base64: ${e.message}")
                        null
                    }
                }
                
                if (bitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .border(2.dp, PixelBlack)
                            .background(Color.LightGray)
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Post Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            // 📄 CONTENT
            Text(
                text = post.content,
                fontFamily = PressStart,
                fontSize = 16.sp,
                color = PixelBlack,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(16.dp))

            Divider(color = PixelBlack, thickness = 2.dp)
            Spacer(Modifier.height(8.dp))

            // ❤️ LIKE / COMMENTS / SHARE
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLike) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Like", tint = PixelRed)
                    }
                    Text("${post.likes}", fontFamily = PressStart, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showComments = !showComments }) {
                        Icon(Icons.AutoMirrored.Filled.Message, contentDescription = "Comments", tint = PixelBlue)
                    }
                    Text("${post.comments.size}", fontFamily = PressStart, fontWeight = FontWeight.Bold)
                }

                Icon(Icons.Default.Share, contentDescription = null, tint = PixelBlack)
            }

            // 💬 COMMENTS SECTION
            if (showComments) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "COMMENTS",
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    post.comments.forEach { c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, PixelBlack)
                                .background(Color(0xFFEEEEEE))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "${c.author?.name ?: "User"}: ",
                                fontFamily = PressStart,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = PixelBlue
                            )
                            Text(
                                text = c.content ?: "",
                                fontFamily = PressStart,
                                fontSize = 12.sp,
                                color = PixelBlack
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ✏ INPUT COMMENT
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type...", fontFamily = PressStart) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PixelBlack,
                            unfocusedBorderColor = PixelBlack,
                            cursorColor = PixelBlack
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                onAddComment(commentText)
                                commentText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PixelBlack),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("GO", fontFamily = PressStart, color = PixelWhite)
                    }
                }
            }
        }
    }
}
