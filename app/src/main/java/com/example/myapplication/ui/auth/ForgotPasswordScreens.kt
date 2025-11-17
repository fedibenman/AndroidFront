package com.example.myapplication.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.PressStart

/**
 * Screen 1: Request password reset code (by email).
 */
@Composable
fun RequestResetCodeScreen(
    viewModel: AuthViewModel,
    onBackToLogin: () -> Unit,
    onCodeSent: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
                    color = Color.Black
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Subtitle
            Text(
                text = "reset your password",
                style = TextStyle(
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
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

            // Email input styled like login
            val email = remember { mutableStateOf("") }

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
                    value = email.value,
                    onValueChange = {
                        email.value = it
                        viewModel.clearError()
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Normal
                    ),
                    decorationBox = { innerTextField ->
                        if (email.value.isEmpty()) {
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

            Spacer(modifier = Modifier.height(12.dp))

            if (viewModel.errorMessage != null) {
                Text(
                    text = viewModel.errorMessage ?: "",
                    color = Color.Red,
                    style = TextStyle(
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (viewModel.isLoading) {
                CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Send code button styled like login
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.button),
                    contentDescription = "Send code",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds
                )

                Text(
                    text = "SEND CODE",
                    style = TextStyle(
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Normal,
                        color = Color.White,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onBackToLogin) {
                Text(
                    text = "back to login",
                    style = TextStyle(
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    )
                )
            }
            Image(
                painter = painterResource(id = R.drawable.reset_message_2),
                contentDescription = "the reset password message",
                modifier = Modifier
                    .padding(end = 16.dp)
                    .width(300.dp).height(150.dp)
            )


        }
    }
}

/**
 * Screen 2: Enter digit code only.
 * When code is valid, navigate to a separate "New password" screen.
 */
@Composable
fun CodeInputScreen(
    viewModel: AuthViewModel,
    codeLength: Int = 6,
    onCodeVerified: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val digits = remember { mutableStateListOf(*Array(codeLength) { "" }) }

    // Keep ViewModel.resetCode in sync with local digit boxes
    LaunchedEffect(digits) {
        viewModel.resetCode = digits.joinToString("")
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
                    color = Color.Black
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Subtitle
            Text(
                text = "enter verification code",
                style = TextStyle(
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until codeLength) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.input),
                            contentDescription = "Code Input Background",
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.FillBounds
                        )

                        BasicTextField(
                            value = digits[i],
                            onValueChange = { input ->
                                val filtered = input.takeLast(1).filter { it.isDigit() }
                                if (filtered.length <= 1) {
                                    digits[i] = filtered
                                    viewModel.clearError()
                                }
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color.Black,
                                fontFamily = PressStart,
                                fontWeight = FontWeight.Normal
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (viewModel.errorMessage != null) {
                Text(
                    text = viewModel.errorMessage ?: "",
                    color = Color.Red,
                    style = TextStyle(
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            val isCodeComplete = digits.all { it.isNotEmpty() }

            if (viewModel.isLoading) {
                CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Verify button styled like login
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.button),
                    contentDescription = "Verify code",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds
                )

                Text(
                    text = "VERIFY",
                    style = TextStyle(
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Normal,
                        color = Color.White,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = {
                viewModel.clearForgotPasswordState()
                onBackToLogin()
            }) {
                Text(
                    text = "back to login",
                    style = TextStyle(
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    )
                )
        }
    }
}

/**
 * Screen 2 (continued): CodeInputScreen closing brace.
 */
}
 
/**
 * Screen 3: Enter new password after code is verified.
 * Uses viewModel.resetEmail + viewModel.resetCode.
 */
@Composable
fun NewPasswordScreen(
    viewModel: AuthViewModel,
    onPasswordChanged: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var confirmPassword by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
                    color = Color.Black
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Subtitle
            Text(
                text = "new password",
                style = TextStyle(
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // New password label
            Text(
                text = "NEW PASSWORD",
                style = TextStyle(
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, bottom = 4.dp)
            )

            PasswordTextField(
                value = viewModel.newPassword,
                onValueChange = {
                    viewModel.newPassword = it
                    viewModel.clearError()
                },
                isError = false,
                modifier = Modifier.fillMaxWidth(),
                Placeholder = "Enter Password"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Confirm password label
            Text(
                text = "CONFIRM PASSWORD",
                style = TextStyle(
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, bottom = 4.dp)
            )

            PasswordTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    viewModel.clearError()
                },
                isError = false,
                modifier = Modifier.fillMaxWidth(),
                Placeholder = "Confirm Passwrod"
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (viewModel.errorMessage != null) {
                Text(
                    text = viewModel.errorMessage ?: "",
                    color = Color.Red,
                    style = TextStyle(
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (viewModel.isLoading) {
                CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Change password button styled like login
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.button),
                    contentDescription = "Change password",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds
                )

                Text(
                    text = "CHANGE PASSWORD",
                    style = TextStyle(
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Normal,
                        color = Color.White,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = {
                viewModel.clearForgotPasswordState()
                onBackToLogin()
            }) {
                Text(
                    text = "back to login",
                    style = TextStyle(
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    )
                )
            }
        }
    }
}
/* Previews */

@Preview(showBackground = true)
@Composable
fun RequestResetCodeScreenPreview() {
    MyApplicationTheme {
        RequestResetCodeScreen(
            viewModel = AuthViewModel(),
            onBackToLogin = {},
            onCodeSent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CodeInputScreenPreview() {
    MyApplicationTheme {
        CodeInputScreen(
            viewModel = AuthViewModel(),
            onCodeVerified = {},
            onBackToLogin = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NewPasswordScreenPreview() {
    MyApplicationTheme {
        NewPasswordScreen(
            viewModel = AuthViewModel(),
            onPasswordChanged = {},
            onBackToLogin = {}
        )
    }

}
