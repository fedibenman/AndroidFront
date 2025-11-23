package com.example.myapplication.community.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.community.viewmodel.PostViewModel
import com.example.myapplication.ui.theme.PressStart

@Composable
fun EditPostScreen(
    postId: String,
    postViewModel: PostViewModel,
    onBack: () -> Unit,
    onUpdated: () -> Unit
) {
    val posts by postViewModel.posts.collectAsState()
    val found = posts.find { it._id == postId }

    var title by remember { mutableStateOf(found?.title ?: "") }
    var content by remember { mutableStateOf(found?.content ?: "") }

    LaunchedEffect(found) {
        found?.let {
            title = it.title ?: ""
            content = it.content ?: ""
        }
    }

    val errorMessage by postViewModel.errorMessage.collectAsState()

    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
    }

    Box(
        modifier = Modifier
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .border(2.dp, PixelBlack, RoundedCornerShape(0.dp))
                            .background(PixelWhite)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = PixelBlack
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "EDIT POST",
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = PixelWhite,
                        letterSpacing = 3.sp
                    )
                }
            }

            // FORM CONTAINER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(4.dp, PixelBlack)
                    .background(PixelWhite)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // TITLE
                Text(
                    "TITLE",
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PixelBlack
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, PixelBlack, RoundedCornerShape(0.dp)),
                    shape = RoundedCornerShape(0.dp),
                    placeholder = {
                        Text(
                            "Enter title...",
                            fontFamily = PressStart,
                            color = Color.Gray
                        )
                    },
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = PressStart,
                        fontSize = 16.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PixelBlack,
                        unfocusedBorderColor = PixelBlack,
                        cursorColor = PixelBlack
                    )
                )

                // CONTENT
                Text(
                    "CONTENT",
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PixelBlack
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .border(2.dp, PixelBlack, RoundedCornerShape(0.dp)),
                    shape = RoundedCornerShape(0.dp),
                    placeholder = {
                        Text(
                            "Edit content...",
                            fontFamily = PressStart,
                            color = Color.Gray
                        )
                    },
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = PressStart,
                        fontSize = 16.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PixelBlack,
                        unfocusedBorderColor = PixelBlack,
                        cursorColor = PixelBlack
                    )
                )

                // IMAGE PICKER
                Button(
                    onClick = {
                        launcher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(4.dp, PixelBlack, RoundedCornerShape(0.dp)),
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PixelGray,
                        contentColor = PixelBlack
                    )
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = PixelBlack,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (selectedImageUri != null) "NEW IMAGE SELECTED ✓" else "CHANGE IMAGE",
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(Modifier.height(8.dp))

                // SAVE BUTTON
                Button(
                    onClick = {
                        if (found != null && title.isNotBlank() && content.isNotBlank()) {
                            postViewModel.updatePost(found._id!!, title, content, selectedImageUri) { onUpdated() }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(4.dp, PixelBlack, RoundedCornerShape(0.dp)),
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PixelGreen,
                        contentColor = PixelBlack
                    )
                ) {
                    Text(
                        "SAVE",
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 4.sp
                    )
                }

                // CANCEL BUTTON
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(4.dp, PixelBlack, RoundedCornerShape(0.dp)),
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PixelRed,
                        contentColor = PixelWhite
                    )
                ) {
                    Text(
                        "CANCEL",
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 4.sp
                    )
                }

                // ERROR MESSAGE
                if (errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(4.dp, PixelBlack, RoundedCornerShape(0.dp))
                            .background(PixelRed)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            fontFamily = PressStart,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PixelWhite
                        )
                    }
                }
            }
        }
    }
}
