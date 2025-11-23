package com.example.myapplication.ui.auth

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.DTOs.Profile
import com.example.myapplication.ui.auth.TokenDataStoreManager
import com.example.myapplication.AppContextHolder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for profile management.
 * Handles fetching and managing user profile data.
 */
class ProfileViewModel(
    private val repository: AuthRepository = KtorAuthRepository()
) : ViewModel() {

    // Profile data
    var profile by mutableStateOf<Profile?>(null)
        private set

    // UI state
    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    private val tokenManager = TokenDataStoreManager(AppContextHolder.appContext)

    /**
     * Loads the user profile from the backend
     */

    /**
     * Loads the user profile from the backend
     */
    fun loadProfile() {
        viewModelScope.launch {
            isLoading = true
            error = null

            try {
                // Get stored access token from Room
                val accessToken = tokenManager.accessTokenFlow.first()

                if (accessToken.isNullOrBlank()) {
                    error = "No access token found. Please login again."
                    isLoading = false
                    return@launch
                }

                val result = repository.getProfile(accessToken)

                result.fold(
                    onSuccess = { profileData ->
                        profile = profileData
                        Log.d("ProfileViewModel", "Profile loaded successfully")
                    },
                    onFailure = { exception ->
                        error = exception.message ?: "Failed to load profile"
                        Log.e("ProfileViewModel", "Failed to load profile", exception)
                    }
                )

            } catch (e: Exception) {
                error = "An unexpected error occurred: ${e.message}"
                Log.e("ProfileViewModel", "Unexpected error", e)
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Refreshes the profile data
     */
    fun refreshProfile() {
        loadProfile()
    }

    /**
     * Clears error state
     */
    fun clearError() {
        error = null
    }

    override fun onCleared() {
        super.onCleared()
    }
}
