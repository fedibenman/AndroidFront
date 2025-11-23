package com.example.myapplication.community.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
fun CreatePostScreen(
    postViewModel: PostViewModel,
    onBack: () -> Unit,
    onPostCreated: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val errorMessage by postViewModel.errorMessage.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
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
                        text = "CREATE POST",
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
                            "Write something...",
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
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
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
                        if (selectedImageUri != null) "IMAGE SELECTED ✓" else "PICK IMAGE",
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(Modifier.height(8.dp))

                // POST BUTTON
                Button(
                    onClick = {
                        if (title.isNotBlank() && content.isNotBlank()) {
                            postViewModel.createPost(
                                title,
                                content,
                                selectedImageUri
                            ) { onPostCreated() }
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
                        "POST",
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
