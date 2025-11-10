package com.example.myapplication.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * Simple ViewModel to manage auth UI state and call the repository.
 * Replace FakeAuthRepository with the real implementation that calls backend APIs.
 */
class AuthViewModel(
    private val repository: AuthRepository = KtorAuthRepository()
) : ViewModel() {

    override fun onCleared() {
        if (repository is KtorAuthRepository) {
            repository.close()
        }
        super.onCleared()
    }
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var name by mutableStateOf("")

    // Forgot password flow state
    var resetEmail by mutableStateOf("")
    var resetCode by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var forgotStep by mutableStateOf(ForgotPasswordStep.RequestCode)

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
    var success by mutableStateOf(false)
        private set

    fun clearError() {
        errorMessage = null
    }

    fun clearForgotPasswordState() {
        resetEmail = ""
        resetCode = ""
        newPassword = ""
        forgotStep = ForgotPasswordStep.RequestCode
        errorMessage = null
    }

    fun login(onComplete: (Boolean) -> Unit = {}) {
        errorMessage = null
        isLoading = true
        viewModelScope.launch {
            val result = repository.login(email.trim(), password)
            isLoading = false
            result.fold(onSuccess = {
                success = true
                onComplete(true)
            }, onFailure = {
                errorMessage = it.message ?: "Login failed"
                onComplete(false)
            })
        }
    }

    fun signup(onComplete: (Boolean) -> Unit = {}) {
        errorMessage = null
        isLoading = true
        viewModelScope.launch {
            val result = repository.signup(name.trim(), email.trim(), password)
            isLoading = false
            result.fold(onSuccess = {
                success = true
                onComplete(true)
            }, onFailure = {
                errorMessage = it.message ?: "Signup failed"
                onComplete(false)
            })
        }
    }

    fun requestPasswordReset(onComplete: (Boolean) -> Unit = {}) {
        errorMessage = null
        isLoading = true
        viewModelScope.launch {
            val result = repository.requestPasswordReset(resetEmail.trim())
            isLoading = false
            result.fold(onSuccess = {
                forgotStep = ForgotPasswordStep.VerifyCode
                onComplete(true)
            }, onFailure = {
                errorMessage = it.message ?: "Failed to send reset code"
                onComplete(false)
            })
        }
    }

    fun resetPassword(onComplete: (Boolean) -> Unit = {}) {
        errorMessage = null
        isLoading = true
        viewModelScope.launch {
            val result = repository.resetPassword(resetEmail.trim(), resetCode.trim(), newPassword)
            isLoading = false
            result.fold(onSuccess = {
                success = true
                onComplete(true)
            }, onFailure = {
                errorMessage = it.message ?: "Failed to reset password"
                onComplete(false)
            })
        }
    }
}

/**
 * Steps in the forgot password flow.
 */
enum class ForgotPasswordStep {
    RequestCode,
    VerifyCode
}
