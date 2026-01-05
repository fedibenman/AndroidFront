package com.example.myapplication.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onSignupRequested: () -> Unit,
    onLoginSuccess: () -> Unit,
    onForgotPassword: () -> Unit
) {
    // Use ViewModel properties directly

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.background_general),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

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
                text = "CREATION",
                style = TextStyle(
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    fontSize = 24.sp
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Subtitle
            Text(
                text = "welcome back",
                style = TextStyle(
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    fontSize = 12.sp
                ),
                modifier = Modifier.padding(bottom = 24.dp)
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

            // Email input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.input),
                    contentDescription = "Email Input Background",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds
                )

                BasicTextField(
                    value = viewModel.email,
                    onValueChange = {
                        viewModel.email = it
                        viewModel.clearErrors()
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Normal
                    ),
                    decorationBox = { innerTextField ->
                        if (viewModel.email.isEmpty()) {
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
                        .padding(start = 24.dp, end = 56.dp)
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

            Spacer(modifier = Modifier.height(12.dp))

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

            // Password input
            PasswordTextField(
                value = viewModel.password,
                onValueChange = {
                    viewModel.password = it
                    viewModel.clearErrors()
                },
                isError = false,
                Placeholder = "Enter Password",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Forgot password link
            TextButton(
                onClick = onForgotPassword,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "forgot password?",
                    style = TextStyle(
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black,
                        fontSize = 10.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Error message
            if (viewModel.generalError != null) {
                Text(
                    text = viewModel.generalError ?: "",
                    color = Color.Red,
                    style = TextStyle(
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Loading indicator
            if (viewModel.isLoading) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Login button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clickable {
                        viewModel.login(rememberMeParam = false) { success ->
                            if (success) onLoginSuccess()
                        }
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.button),
                    contentDescription = "Login",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds
                )

                Text(
                    text = "LOGIN",
                    style = TextStyle(
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Normal,
                        color = Color.White,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Signup link
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "new user? ",
                    style = TextStyle(
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black,
                        fontSize = 10.sp
                    )
                )
                Text(
                    text = "sign up",
                    style = TextStyle(
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Bold,
                        color = Color.Blue,
                        fontSize = 10.sp
                    ),
                    modifier = Modifier.clickable { onSignupRequested() }
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
