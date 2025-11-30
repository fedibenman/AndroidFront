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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.viewModel.ImageAnalysisViewModel
import kotlinx.coroutines.launch

@Composable
fun ImageAnalysisScreen(
    onBack: () -> Unit
) {
    // ViewModel for handling image analysis
    val viewModel: ImageAnalysisViewModel = viewModel()
    
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
            viewModel.analyzeImage(selectedImageBitmap!!)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
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
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onBack() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Image Analysis",
                style = TextStyle(
                    fontFamily = PressStart,
                    color = Color.White,
                    fontSize = 20.sp
                )
            )
        }

        // Image Selection Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color(0xFF2A2A2A))
                .clip(RoundedCornerShape(12.dp))
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageBitmap != null) {
                Image(
                    bitmap = selectedImageBitmap!!.asImageBitmap(),
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp)),
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
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tap to select an image",
                        color = Color.Gray,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Analyze Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color(0xFF4CAF50))
                .clip(RoundedCornerShape(25.dp))
                .clickable { sendImageForAnalysis() },
            contentAlignment = Alignment.Center
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
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
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
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Analysis Results",
                        style = TextStyle(
                            color = Color(0xFF4CAF50),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Text(
                        text = it,
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewImageAnalysisScreen() {
    MyApplicationTheme {
        ImageAnalysisScreen(onBack = {})
    }
}
