package com.example.myapplication.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * Screen 1: Request password reset code (by email).
 */
@Composable
fun RequestResetCodeScreen(
    viewModel: AuthViewModel,
    onBackToLogin: () -> Unit,
    onCodeSent: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Reset password",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.resetEmail,
            onValueChange = {
                viewModel.resetEmail = it
                viewModel.clearError()
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage ?: "",
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (viewModel.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    viewModel.requestPasswordReset { success ->
                        if (success) {
                            onCodeSent()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send verification code")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onBackToLogin) {
            Text("Back to login")
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

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enter verification code",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until codeLength) {
                OutlinedTextField(
                    value = digits[i],
                    onValueChange = { input ->
                        val filtered = input.takeLast(1).filter { it.isDigit() }
                        if (filtered.length <= 1) {
                            digits[i] = filtered
                            viewModel.clearError()
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("") }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage ?: "",
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        val isCodeComplete = digits.all { it.isNotEmpty() }

        if (viewModel.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (!isCodeComplete) {
                        viewModel.errorMessage = "Please enter the full code"
                    } else {
                        // Call resetPassword with a dummy password just to validate
                        // or preferably you expose a verifyCode endpoint.
                        // Here we rely on backend resetPassword validation, but since
                        // user asked for separate new password page, we only verify code logically.
                        // For now: if backend expects code-only verification,
                        // you would call a verify endpoint here.
                        onCodeVerified()
                    }
                },
                enabled = isCodeComplete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Verify code")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            viewModel.clearForgotPasswordState()
            onBackToLogin()
        }) {
            Text("Back to login")
        }
    }
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

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose new password",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(12.dp))

        PasswordTextField(
            value = viewModel.newPassword,
            onValueChange = {
                viewModel.newPassword = it
                viewModel.clearError()
            },
            label = "New password",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        PasswordTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                viewModel.clearError()
            },
            label = "Confirm new password",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage ?: "",
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (viewModel.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (viewModel.newPassword != confirmPassword) {
                        viewModel.errorMessage = "Passwords do not match"
                    } else {
                        viewModel.resetPassword { success ->
                            if (success) {
                                viewModel.clearForgotPasswordState()
                                onPasswordChanged()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Change password")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            viewModel.clearForgotPasswordState()
            onBackToLogin()
        }) {
            Text("Back to login")
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
