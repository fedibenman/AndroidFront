package com.example.myapplication.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
fun SignupScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onSignupSuccess: () -> Unit
) {
    // Local fields bound to viewModel to keep behavior
    var name by viewModel::name
    var email by viewModel::email
    var password by viewModel::password

    Box(
        modifier = Modifier
            .fillMaxSize()
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
                text = "Creation",
                style = TextStyle(
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Normal,
                    fontSize = 22.sp,
                    color = Color.Black
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Subtitle
            Text(
                text = "join the adventure",
                style = TextStyle(
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Name label
            Text(
                text = "NAME",
                style = TextStyle(
                    fontFamily = PressStart,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, bottom = 4.dp)
            )

            // Name input with same styled box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.input),
                    contentDescription = "Name Input Background",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds
                )

                BasicTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        viewModel.clearError()
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Normal
                    ),
                    decorationBox = { innerTextField ->
                        if (name.isEmpty()) {
                            Text(
                                text = "Enter your name",
                                style = TextStyle(
                                    color = Color.LightGray,
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
                    painter = painterResource(id = R.drawable.user),
                    contentDescription = "User Icon",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .size(30.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

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
                    value = email,
                    onValueChange = {
                        email = it
                        viewModel.clearError()
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Normal
                    ),
                    decorationBox = { innerTextField ->
                        if (email.isEmpty()) {
                            Text(
                                text = "Enter your email",
                                style = TextStyle(
                                    color = Color.LightGray,
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

            // Password input
            PasswordTextField(
                value = password,
                onValueChange = {
                    password = it
                    viewModel.clearError()
                },
                isError = false,
                modifier = Modifier.fillMaxWidth(),
                Placeholder = "Enter Password"
            )
            Spacer(Modifier.height(12.dp))
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
                value = password,
                onValueChange = {
                    password = it
                    viewModel.clearError()
                },
                isError = false,
                modifier = Modifier.fillMaxWidth(),
                Placeholder = "Confirm Password"
            )
            Spacer(Modifier.height(12.dp))

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
            }

            Spacer(Modifier.height(16.dp))

            // Signup button styled like login
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.button),
                    contentDescription = "Signup Button",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds
                )

                Text(
                    text = "CREATE ACCOUNT",
                    style = TextStyle(
                        fontFamily = PressStart,
                        fontWeight = FontWeight.Normal,
                        color = Color.White,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.height(8.dp))

            if (!viewModel.isLoading) {
                // Wrap click in TextButton style while preserving look
                TextButton(
                    onClick = {
                        viewModel.signup { success ->
                            if (success) onSignupSuccess()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { }
            } else {
                CircularProgressIndicator()
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onBack) {
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



@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    MyApplicationTheme {
        SignupScreen(
            viewModel = AuthViewModel(),
            onBack = {},
            onSignupSuccess = {}
        )
    }
}
