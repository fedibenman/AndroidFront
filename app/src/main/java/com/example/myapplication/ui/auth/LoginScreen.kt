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
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.PressStart
import com.example.myapplication.ui.theme.PressStart

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onSignupRequested: () -> Unit,
    onLoginSuccess: () -> Unit,
    onForgotPassword: () -> Unit
) {
    
    
    
    

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 🖼️ Background image
        Image(
            painter = painterResource(id = R.drawable.background_general),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var emailError by remember { mutableStateOf<String?>(null) }
        var passwordError by remember { mutableStateOf<String?>(null) }

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
                text = "Creation",
                style = TextStyle(
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    fontSize = 22.sp,
                ),
                modifier = Modifier
                    .padding(bottom = 4.dp)
            )

            // Subtitle
            Text(
                text = "join the adventure",
                style = TextStyle(
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                ),
                modifier = Modifier
                    .padding(bottom = 24.dp)
            )

            // Email label
            Text(
                text = "EMAIL",
                style = TextStyle(
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, bottom = 4.dp)
            )

            // Email input with background image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Image(
                    painter = painterResource(
                        id = if (emailError != null) R.drawable.input_error else R.drawable.input
                    ),
                    contentDescription = "Email Input Background",
                    modifier = Modifier
                        .matchParentSize(),
                    contentScale = ContentScale.FillBounds
                )

                BasicTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Normal
                    ),
                    decorationBox = { innerTextField ->
                        if (email.isEmpty()) {
                            Text(
                                text = "Enter your email",
                                style = TextStyle(
                                    color = Color.DarkGray,
                                    fontFamily = PressStart,
                                    fontWeight = FontWeight.Normal
                                )
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 24.dp, end = 24.dp)
                )

                Image(
                    painter = painterResource(id = R.drawable.globe),
                    contentDescription = "Email Icon",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .size(30.dp)
                )
            }

            if (emailError != null) {
                Text(
                    text = emailError ?: "",
                    color = Color.Red,
                    style = TextStyle(
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Password label
            Text(
                text = "PASSWORD",
                style = TextStyle(
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, bottom = 4.dp)
            )

            // Password input with background image + eye toggle
            PasswordTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                Placeholder = "Enter your password",                isError = passwordError != null,
                modifier = Modifier
                    .fillMaxWidth()
            )

            if (passwordError != null) {
                Text(
                    text = passwordError ?: "",
                    color = Color.Red,
                    style = TextStyle(
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp)
                )
            }

            // Forgot password
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onForgotPassword) {
                    Text(
                        "Forgot password?",
                        style = TextStyle(
                            fontFamily = PressStart,
                            fontWeight = FontWeight.Normal,
                            color = Color.Red
                        )
                    )
                }
            }
            // Login button with centered text inside image
Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(70.dp)
        .clickable {
            viewModel.login { success ->
                if (success) onLoginSuccess()
            }
        }
) {
    Image(
        painter = painterResource(id = R.drawable.button),
        contentDescription = "Login Button",
        modifier = Modifier
            .matchParentSize(),
        contentScale = ContentScale.FillBounds
    )

    Text(
        text = "LOGIN",
        style = TextStyle(
            fontFamily = PressStart,
            fontWeight = FontWeight.Normal,
            color = Color.White
        ),
        modifier = Modifier.align(Alignment.Center)
    )
}

            Spacer(Modifier.height(10.dp))



            // Signup prompt
            Text(
                text = "new adventure?",
                style = TextStyle(
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                ),
                modifier = Modifier
                    .padding(top = 8.dp)
            )

            TextButton(onClick = onSignupRequested) {
                Text(
                    text = "create account",
                    style = TextStyle(
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        color = Color.Yellow
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MyApplicationTheme {
        LoginScreen(
            viewModel = AuthViewModel(),
            onSignupRequested = {},
            onLoginSuccess = {},
            onForgotPassword = {}
        )
    }
}
