package com.example.myapplication.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.ui.theme.PressStart
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(navController: NavHostController) {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        val tokenManager = TokenDataStoreManager(context)
        val authRepository = KtorAuthRepository()
        
        try {
            // Get stored access token
            val accessToken = tokenManager.accessTokenFlow.first()
            
            if (!accessToken.isNullOrBlank()) {
                // Token exists, verify it's still valid
                val result = authRepository.getProfile(accessToken)
                
                if (result.isSuccess) {
                    // Token is valid, go to home
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                } else {
                    // Token is invalid, go to login
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            } else {
                // No token, go to login
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        } catch (e: Exception) {
            // Error checking token, go to login
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }
    
    // Loading UI
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "LOADING...",
                fontFamily = PressStart,
                fontSize = 10.sp
            )
        }
    }
}
