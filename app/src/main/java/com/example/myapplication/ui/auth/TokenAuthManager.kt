package com.example.myapplication.ui.auth

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.AppContextHolder
import com.example.myapplication.DTOs.Profile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Manager for handling automatic authentication and token validation.
 * This ensures users stay logged in across app restarts.
 */
class TokenAuthManager : ViewModel() {
    
    private val tokenManager = TokenDataStoreManager(AppContextHolder.appContext)
    
    // Authentication state
    val isAuthenticated = mutableStateOf(false)
    val isLoading = mutableStateOf(false)
    val currentUser = mutableStateOf<Profile?>(null)
    
    /**
     * Checks if user has valid tokens and automatically logs them in
     */
    fun checkExistingAuth(onAuthResult: (Boolean) -> Unit) {
        if (isLoading.value) return
        
        isLoading.value = true
        
        viewModelScope.launch {
            try {
                // Get stored access token
                val accessToken = tokenManager.accessTokenFlow.first()
                
                if (accessToken.isNullOrBlank()) {
                    Log.d("TokenAuthManager", "No access token found")
                    isAuthenticated.value = false
                    isLoading.value = false
                    onAuthResult(false)
                    return@launch
                }
                
                // Validate the token by fetching user profile
                val authRepository = KtorAuthRepository()
                val profileResult = authRepository.getProfile(accessToken)
                
                profileResult.fold(
                    onSuccess = { profile ->
                        Log.d("TokenAuthManager", "Token validation successful for user: ${profile.name}")
                        currentUser.value = profile
                        isAuthenticated.value = true
                        isLoading.value = false
                        onAuthResult(true)
                    },
                    onFailure = { error ->
                        Log.d("TokenAuthManager", "Token validation failed: ${error.message}")
                        // Clear invalid tokens
                        clearStoredTokens()
                        isAuthenticated.value = false
                        isLoading.value = false
                        onAuthResult(false)
                    }
                )
                
                // Close the repository
                if (authRepository is KtorAuthRepository) {
                    authRepository.close()
                }
                
            } catch (e: Exception) {
                Log.e("TokenAuthManager", "Error checking authentication", e)
                isAuthenticated.value = false
                isLoading.value = false
                onAuthResult(false)
            }
        }
    }
    
    /**
     * Manually log out user and clear all stored tokens
     */
    fun logout() {
        clearStoredTokens()
        currentUser.value = null
        isAuthenticated.value = false
    }
    
    /**
     * Clear all stored authentication data
     */
    private fun clearStoredTokens() {
        viewModelScope.launch {
            try {
                tokenManager.clearTokens()
                Log.d("TokenAuthManager", "Tokens cleared successfully")
            } catch (e: Exception) {
                Log.e("TokenAuthManager", "Error clearing tokens", e)
            }
        }
    }
    
    /**
     * Update current user profile
     */
    fun updateCurrentUser(profile: Profile?) {
        currentUser.value = profile
    }
    
    override fun onCleared() {
        super.onCleared()
    }
}
