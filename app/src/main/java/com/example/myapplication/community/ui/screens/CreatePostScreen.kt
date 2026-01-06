package com.example.myapplication.community.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.community.viewmodel.PostViewModel
import com.example.myapplication.ui.theme.PressStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    postViewModel: PostViewModel,
    onBack: () -> Unit,
    onPostCreated: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "CREATE POST", 
                        style = TextStyle(fontFamily = PressStart, fontSize = 16.sp)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title", fontFamily = PressStart, fontSize = 10.sp) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontFamily = PressStart, fontSize = 12.sp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Body Input
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content", fontFamily = PressStart, fontSize = 10.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = TextStyle(fontFamily = PressStart, fontSize = 12.sp),
                maxLines = 10
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Image Selection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Add, contentDescription = "Add Image")
                        Text("Add Image", fontFamily = PressStart, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = {
                    isLoading = true
                    postViewModel.createPost(title, content, imageUri) {
                        isLoading = false
                        onPostCreated()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotEmpty() && content.isNotEmpty() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("POST", fontFamily = PressStart)
                }
            }
        }
    }
}
