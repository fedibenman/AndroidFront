package com.example.myapplication.ui.auth

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.PressStart
import android.graphics.BitmapFactory
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.viewModel.ImageAnalysisViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import com.example.myapplication.ui.theme.AnimatedThemeToggle
import com.example.myapplication.ui.theme.LocalThemeManager
import com.example.myapplication.ui.theme.MyApplicationTheme


@Composable
fun PixelArtButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clickable(enabled = enabled) { onClick() }
    ) {
        // Shadow layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(color = Color(0xFF4A4A4A))
                .border(width = 2.dp, color = Color.Black)
        )
        
        // Main button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = if (enabled) Color(0xFFE8F4F8) else Color.Gray)
                .border(width = 2.dp, color = Color.Black),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}


@Composable
fun ImageAnalysisScreen(
    onBack: () -> Unit
) {
    // ViewModel for handling image analysis
    val viewModel: ImageAnalysisViewModel = viewModel()
    val themeManager = LocalThemeManager.current
    val isDarkMode by themeManager.isDarkMode.collectAsState()
    
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    val isLoading by viewModel.isLoading
    val analysisResult by viewModel.analysisResult
    val error by viewModel.error
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                selectedImageUri = uri
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    selectedImageBitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    viewModel.clearResult()
                } catch (e: Exception) {
                    viewModel.error.value = "Failed to load image: ${e.message}"
                }
            }
        }
    )

    // Function to send image to backend
    fun sendImageForAnalysis() {
        if (selectedImageBitmap == null) {
            viewModel.error.value = "Please select an image first"
            return
        }
        
        scope.launch {
            try {
                viewModel.analyzeImage(selectedImageBitmap!!)
            } catch (e: Exception) {
                viewModel.error.value = "Analysis failed: ${e.message}"
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background based on theme
        if (isDarkMode) {
            // Dark theme background using background_dark image
            Image(
                painter = painterResource(id = R.drawable.background_dark),
                contentDescription = "Dark Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Light theme background
            Image(
                painter = painterResource(id = R.drawable.background_general),
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.x_icon),
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onBack() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Image Analysis",
                style = TextStyle(
                    fontFamily = PressStart,
                    color = Color.Black,
                    fontSize = 20.sp
                )
            )
        }

        // Image Selection Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.White)
                .border(width = 2.dp, color = Color.Black)
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageBitmap != null) {
                Image(
                    bitmap = selectedImageBitmap!!.asImageBitmap(),
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center), // centers Column content inside Box
                    horizontalAlignment = Alignment.CenterHorizontally

                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.add_image_button),
                        contentDescription = "Add image",
                        tint = Color.Black,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tap to select an image",
                        color = Color.Black,
                        fontSize = 15.sp,
                        style = TextStyle(fontFamily = PressStart)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Analyze Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            PixelArtButton(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(50.dp),
                onClick = { sendImageForAnalysis() },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "ANALYZE IMAGE",
                        style = TextStyle(
                            fontFamily = PressStart,
                            color = Color.Black,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        // Error Message
        error?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFD32F2F))
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = it,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }

        // Loading State
        if (isLoading) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Analyzing image...",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }

        // Analysis Results
        analysisResult?.let {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2A2A2A))
                    .clip(RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = it,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
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

@Preview(showBackground = true)
@Composable
fun PreviewImageAnalysisScreen() {
    MyApplicationTheme {
        // Simple preview without theme manager for now
        Box(modifier = Modifier.fillMaxSize()) {
            // Background
            Image(
                painter = painterResource(id = R.drawable.background_general),
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Theme toggle button at top right
            Box(
                modifier = Modifier
                    .padding(top = 50.dp, end = 20.dp)
                    .align(Alignment.TopEnd)
            ) {
                AnimatedThemeToggle()
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.x_icon),
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Image Analysis",
                        style = TextStyle(
                            fontFamily = com.example.myapplication.ui.theme.PressStart,
                            color = Color.Black,
                            fontSize = 20.sp
                        )
                    )
                }
                
                // Image Selection Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.White)
                        .border(width = 2.dp, color = Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.add_image_button),
                            contentDescription = "Add image",
                            tint = Color.Black,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tap to select an image",
                            color = Color.Black,
                            fontSize = 15.sp,
                            style = TextStyle(fontFamily = com.example.myapplication.ui.theme.PressStart)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                // Analyze Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Mock PixelArtButton
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(50.dp)
                    ) {
                        // Shadow layer
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(x = 4.dp, y = 4.dp)
                                .background(color = Color(0xFF4A4A4A))
                                .border(width = 2.dp, color = Color.Black)
                        )
                        
                        // Main button
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = Color(0xFFE8F4F8))
                                .border(width = 2.dp, color = Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ANALYZE IMAGE",
                                style = TextStyle(
                                    fontFamily = com.example.myapplication.ui.theme.PressStart,
                                    color = Color.Black,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
