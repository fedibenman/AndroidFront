package com.example.myapplication.ui.auth

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

import com.example.myapplication.ui.auth.TokenDataStoreManager
import com.example.myapplication.AppContextHolder

/**
 * ViewModel for authentication.
 * Handles login, signup, and forgot password flows with proper error handling and UI state.
 */
class AuthViewModel(
    private val repository: AuthRepository = KtorAuthRepository()
) : ViewModel() {

    // Basic input fields
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var name by mutableStateOf("")

    // Forgot password state
    var resetEmail by mutableStateOf("")
    var resetCode by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var forgotStep by mutableStateOf(ForgotPasswordStep.RequestCode)

    // UI state
    var isLoading by mutableStateOf(false)
    var success by mutableStateOf(false)

    var rememberMe by mutableStateOf(false)

    private val tokenManager = TokenDataStoreManager(AppContextHolder.appContext)

    // Error state
    var generalError by mutableStateOf<String?>(null)

    var emailError by mutableStateOf<String?>(null)

    var passwordError by mutableStateOf<String?>(null)

    /** Clears all error messages */
    fun clearErrors() {
        generalError = null
        emailError = null
        passwordError = null
    }

    /** Clears forgot password flow */
    fun clearForgotPasswordState() {
        resetEmail = ""
        resetCode = ""
        newPassword = ""
        forgotStep = ForgotPasswordStep.RequestCode
        clearErrors()
    }

    /** Logs in the user */
    fun login(rememberMeParam: Boolean, onComplete: (Boolean) -> Unit = {}) {
        clearErrors()

        // Input validation
        if (email.isBlank()) { emailError = "Email cannot be empty"; onComplete(false); return }
        if (!email.contains("@")) { emailError = "Invalid email format"; onComplete(false); return }
        if (password.isBlank()) { passwordError = "Password cannot be empty"; onComplete(false); return }

        isLoading = true
        viewModelScope.launch {
            val result = repository.login(email.trim(), password)
            isLoading = false
            result.fold(
                onSuccess = { loginResponse ->
                    // Always save the access token
                    tokenManager.saveAccessTokenOnly(loginResponse.token.accessToken)
                    
                    // Only save refresh token if "remember me" is checked
                    if (rememberMeParam) {
                        // Update the stored token to include the refresh token
                        tokenManager.saveTokens(loginResponse.token.accessToken, loginResponse.token.refreshToken)
                    }
                    
                    success = true
                    onComplete(true)
                },
                onFailure = {
                    generalError = it.message ?: "Login failed"
                    onComplete(false)
                }
            )
        }
    }

    /** Signs up a new user */
    fun signup(onComplete: (Boolean) -> Unit = {}) {
        clearErrors()

        // Input validation
        if (name.isBlank()) { generalError = "Name cannot be empty"; onComplete(false); return }
        if (email.isBlank()) { emailError = "Email cannot be empty"; onComplete(false); return }
        if (!email.contains("@")) { emailError = "Invalid email format"; onComplete(false); return }
        if (password.isBlank()) { passwordError = "Password cannot be empty"; onComplete(false); return }

        isLoading = true
        viewModelScope.launch {
            val result = repository.signup(name.trim(), email.trim(), password)
            isLoading = false
            result.fold(
                onSuccess = {
                    success = true
                    onComplete(true)
                },
                onFailure = {
                    generalError = it.message ?: "Signup failed"
                    onComplete(false)
                }
            )
        }
    }

    /** Requests a password reset code */
    fun requestPasswordReset(onComplete: (Boolean) -> Unit = {}) {
        clearErrors()

        if (resetEmail.isBlank()) { emailError = "Email cannot be empty"; onComplete(false); return }

        isLoading = true
        viewModelScope.launch {
            val result = repository.requestPasswordReset(resetEmail.trim())
            isLoading = false
            result.fold(
                onSuccess = {
                    forgotStep = ForgotPasswordStep.VerifyCode
                    onComplete(true)
                },
                onFailure = {
                    generalError = it.message ?: "Failed to send reset code"
                    onComplete(false)
                }
            )
        }
    }

    /** Resets password using code */
    fun resetPassword(onComplete: (Boolean) -> Unit = {}) {
        clearErrors()

        if (resetEmail.isBlank()) { emailError = "Email cannot be empty"; onComplete(false); return }
        if (resetCode.isBlank()) { generalError = "Code cannot be empty"; onComplete(false); return }
        if (newPassword.isBlank()) { passwordError = "Password cannot be empty"; onComplete(false); return }

        isLoading = true
        viewModelScope.launch {
            val result = repository.resetPassword(resetEmail.trim(), resetCode.trim(), newPassword)
            isLoading = false
            result.fold(
                onSuccess = {
                    success = true
                    onComplete(true)
                },
                onFailure = {
                    generalError = it.message ?: "Failed to reset password"
                    onComplete(false)
                }
            )
        }
    }

    /** Logs out / clears auth state */
    fun logout() {
        email = ""
        password = ""
        name = ""
        resetEmail = ""
        resetCode = ""
        newPassword = ""
        forgotStep = ForgotPasswordStep.RequestCode
        success = false
        clearErrors()
    }

    override fun onCleared() {
        if (repository is KtorAuthRepository) {
            repository.close()
        }
        super.onCleared()
    }
}

/** Steps in the forgot password flow */
enum class ForgotPasswordStep {
    RequestCode,
    VerifyCode
}
