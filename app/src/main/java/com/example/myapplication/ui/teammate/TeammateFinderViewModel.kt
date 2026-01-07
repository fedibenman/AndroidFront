package com.example.myapplication.ui.teammate

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.AppContextHolder
import com.example.myapplication.DTOs.Profile
import com.example.myapplication.Repository.TeammateRepository
import com.example.myapplication.ui.auth.TokenDataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TeammateFinderViewModel(
    private val repository: TeammateRepository = TeammateRepository()
) : ViewModel() {

    private val tokenManager = TokenDataStoreManager(AppContextHolder.appContext)

    var candidates by mutableStateOf<List<Profile>>(emptyList())
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    
    // For match dialog
    var matchedUser by mutableStateOf<Profile?>(null)
    var showMatchDialog by mutableStateOf(false)

    init {
        loadCandidates()
    }

    fun loadCandidates() {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val token = tokenManager.accessTokenFlow.first()
                val userId = tokenManager.userIdFlow.first()

                if (token.isNullOrBlank() || userId.isNullOrBlank()) {
                    error = "Not authenticated"
                    isLoading = false
                    return@launch
                }

                val newCandidates = repository.getCandidates(userId, token)
                // Filter out any candidates already in the list to avoid duplicates if accidentally reloaded
                val currentIds = candidates.map { it.id }.toSet()
                val uniqueNewCandidates = newCandidates.filter { it.id !in currentIds }
                
                candidates = candidates + uniqueNewCandidates
                Log.d("TeammateFinder", "Loaded ${uniqueNewCandidates.size} new candidates")
                
            } catch (e: Exception) {
                Log.e("TeammateFinder", "Error loading candidates", e)
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun swipe(candidate: Profile, direction: SwipeDirection) {
        // Optimistically remove the candidate immediately
        candidates = candidates.filter { it.id != candidate.id }
        
        // Load more if running low
        if (candidates.size < 3) {
            loadCandidates()
        }

        viewModelScope.launch {
            try {
                val token = tokenManager.accessTokenFlow.first() ?: return@launch
                val userId = tokenManager.userIdFlow.first() ?: return@launch
                val candidateId = candidate.id ?: return@launch

                val action = if (direction == SwipeDirection.Right) "like" else "pass"
                
                val result = repository.recordSwipe(userId, candidateId, action, token)
                
                if (result.match && result.matchedUser != null) {
                    matchedUser = result.matchedUser
                    showMatchDialog = true
                    Log.d("TeammateFinder", "It's a match!")
                }

            } catch (e: Exception) {
                Log.e("TeammateFinder", "Error recording swipe", e)
                // In production, we might want to undo the swipe or show an error,
                // but for now we just log it as the UI already moved on.
            }
        }
    }

    fun dismissMatchDialog() {
        showMatchDialog = false
        matchedUser = null
    }
}

enum class SwipeDirection {
    Left, Right
}
