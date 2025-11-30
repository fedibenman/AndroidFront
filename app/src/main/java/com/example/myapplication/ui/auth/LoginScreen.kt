package com.example.myapplication.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.AnimatedThemeToggle
import com.example.myapplication.ui.theme.LocalThemeManager
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.PressStart

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onSignupRequested: () -> Unit,
    onLoginSuccess: () -> Unit,
    onForgotPassword: () -> Unit
) {
    val themeManager = LocalThemeManager.current
    val isDarkMode = themeManager.isDarkMode
    
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
                modifier = Modifier.matchParentSize()
            )
        } else {
            // Light theme background
            Image(
                painter = painterResource(id = R.drawable.background_general),
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
        
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
                .padding(16.dp)
                .fillMaxWidth()
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Title
            Text(
                text = "Login",
                style = TextStyle(
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Normal,
                    color = if (isDarkMode) Color.White else Color.Black,
                    fontSize = 22.sp
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Subtitle
            Text(
                text = "enter the realm",
                style = TextStyle(
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Normal,
                    color = if (isDarkMode) Color.Gray else Color.Black
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Display general error
            viewModel.generalError?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    style = TextStyle(fontFamily = PressStart),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Email label
            Text(
                text = "EMAIL",
                style = TextStyle(fontFamily = PressStart, fontWeight = FontWeight.Normal, color = if (isDarkMode) Color.White else Color.Black),
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 4.dp)
            )

            // Email input with background
            Box(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                Image(
                    painter = painterResource(
                        id = if (viewModel.emailError != null) R.drawable.input_error else R.drawable.input
                    ),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds
                )

                BasicTextField(
                    value = viewModel.email,
                    onValueChange = { viewModel.email = it; viewModel.emailError = null },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.Black, fontFamily = PressStart),
                    decorationBox = { innerTextField ->
                        if (viewModel.email.isEmpty()) {
                            Text(
                                text = "Enter your email",
                                style = TextStyle(color = if (isDarkMode) Color.Gray else Color.DarkGray, fontFamily = PressStart)
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp, end = 24.dp)
                )

                Image(
                    painter = painterResource(id = R.drawable.globe),
                    contentDescription = "Email Icon",
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp).size(30.dp)
                )
            }

            viewModel.emailError?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    style = TextStyle(fontFamily = PressStart),
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Password label
            Text(
                text = "PASSWORD",
                style = TextStyle(fontFamily = PressStart, fontWeight = FontWeight.Normal, color = if (isDarkMode) Color.White else Color.Black),
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 4.dp)
            )

            PasswordTextField(
                value = viewModel.password,
                onValueChange = { viewModel.password = it; viewModel.passwordError = null },
                Placeholder = "Enter your password",
                isError = viewModel.passwordError != null,
                modifier = Modifier.fillMaxWidth()
            )

            viewModel.passwordError?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    style = TextStyle(fontFamily = PressStart),
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp)
                )
            }

            // Forgot password
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onForgotPassword) {
                    Text(
                        "forgot password?",
                        style = TextStyle(fontFamily = PressStart, color = Color.Red)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Remember me checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Checkbox(
                    checked = viewModel.rememberMe,
                    onCheckedChange = { viewModel.rememberMe = it }
                )
                Text(
                    text = "Remember me",
                    style = TextStyle(
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Normal,
                        color = if (isDarkMode) Color.White else Color.Black,
                        fontSize = 16.sp
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Login button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clickable(enabled = !viewModel.isLoading) {
                        viewModel.login(viewModel.rememberMe) { success ->
                            if (success) onLoginSuccess()
                        }
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.button),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds
                )

                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "LOGIN",
                        style = TextStyle(fontFamily = PressStart, color = Color.White),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            Spacer(Modifier.height(24.dp))

            // Signup prompt
            Column(
                modifier = Modifier.padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "New adventurer?",
                    style = TextStyle(fontFamily = PressStart, color = if (isDarkMode) Color.White else Color.Black)
                )
                
                TextButton(onClick = onSignupRequested) {
                    Text(
                        text = "CREATE ACCOUNT",
                        style = TextStyle(fontFamily = PressStart, fontSize = 18.sp, color = Color.Yellow)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    // Mock ThemeManager for preview
    val mockThemeManager = object {
        val isDarkMode = false
        fun toggleTheme() {}
    }
    
    androidx.compose.runtime.CompositionLocalProvider(
        LocalThemeManager provides mockThemeManager as com.example.myapplication.ui.theme.ThemeManager
    ) {
        MyApplicationTheme {
            LoginScreen(
                viewModel = AuthViewModel(),
                onSignupRequested = {},
                onLoginSuccess = {},
                onForgotPassword = {}
            )
        }
    }
}
