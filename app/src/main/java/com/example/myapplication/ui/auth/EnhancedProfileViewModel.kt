package com.example.myapplication.ui.auth

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.DTOs.Profile
import com.example.myapplication.Repository.ProfileRepository
import com.example.myapplication.Repository.ProfileUpdateRequest
import com.example.myapplication.model.*
import com.example.myapplication.ui.auth.TokenDataStoreManager
import com.example.myapplication.AppContextHolder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.sqrt

class EnhancedProfileViewModel(
    private val authRepository: AuthRepository = KtorAuthRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository()
) : ViewModel() {

    // Profile data
    var profile by mutableStateOf<Profile?>(null)
        private set

    // UI state
    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set
    
    var showingEditProfile by mutableStateOf(false)
        private set

    // Edit mode state
    var editedBio by mutableStateOf("")
        private set
    var editedPsn by mutableStateOf("")
        private set
    var editedXbox by mutableStateOf("")
        private set
    var editedSteam by mutableStateOf("")
        private set
    var editedDiscord by mutableStateOf("")
        private set
    var editedNintendo by mutableStateOf("")
        private set
    var editedAvailability by mutableStateOf("")
        private set
    var selectedPlayStyles by mutableStateOf(setOf<String>())
        private set
    var selectedLanguages by mutableStateOf(setOf<String>())
        private set
    var editedFavoriteGame by mutableStateOf<FavoriteGame?>(null)
        private set
    
    // Game search
    var searchText by mutableStateOf("")
        private set
    var searchResults by mutableStateOf<List<Game>>(emptyList())
        private set
    var isSearching by mutableStateOf(false)
        private set

    private val tokenManager = TokenDataStoreManager(AppContextHolder.appContext)

    // Available options
    val availablePlayStyles = listOf(
        "Competitive", "Casual", "Speedrunner", "Completionist",
        "Story-Focused", "Multiplayer", "Solo", "Co-op"
    )
    
    val availableLanguages = listOf(
        "English", "Spanish", "French", "German", "Italian",
        "Portuguese", "Japanese", "Korean", "Chinese", "Russian"
    )

    // Computed properties
    val currentLevel: Int
        get() = calculateLevel(profile?.stats?.xp ?: 0)
    
    val currentXP: Int
        get() = profile?.stats?.xp ?: 0
    
    val nextLevelXP: Int
        get() = calculateXPForLevel(currentLevel + 1)
    
    val totalGamesPlayed: Int
        get() = profile?.stats?.totalGamesPlayed ?: 0
    
    val totalHoursPlayed: Int
        get() = profile?.stats?.totalHoursPlayed ?: 0
    
    val averageRating: Double
        get() = profile?.stats?.averageRating ?: 0.0
    
    val achievements: List<String>
        get() = profile?.achievements ?: emptyList()
    
    val hasAchievements: Boolean
        get() = achievements.isNotEmpty()
    
    val recentActivities: List<RecentActivity>
        get() = profile?.recentActivity ?: emptyList()

    /**
     * Calculate level from XP
     * Formula: level = floor(sqrt(xp / 100))
     */
    private fun calculateLevel(xp: Int): Int {
        return floor(sqrt(xp.toDouble() / 100.0)).toInt().coerceAtLeast(1)
    }
    
    /**
     * Calculate XP needed for a specific level
     * Formula: xp = level^2 * 100
     */
    private fun calculateXPForLevel(level: Int): Int {
        return level * level * 100
    }

    /**
     * Loads the user profile from the backend
     */
    fun loadProfile() {
        viewModelScope.launch {
            isLoading = true
            error = null

            try {
                val accessToken = tokenManager.accessTokenFlow.first()

                if (accessToken.isNullOrBlank()) {
                    error = "No access token found. Please login again."
                    isLoading = false
                    return@launch
                }

                Log.d("ProfileViewModel", "Loading profile with token")
                val result = authRepository.getProfile(accessToken)

                result.fold(
                    onSuccess = { profileData ->
                        profile = profileData
                        Log.d("ProfileViewModel", "Profile loaded: id=${profileData.id}, name=${profileData.name}")
                        Log.d("ProfileViewModel", "Bio: ${profileData.bio}")
                        Log.d("ProfileViewModel", "GamerTags: ${profileData.gamerTags}")
                        Log.d("ProfileViewModel", "Stats: ${profileData.stats}")
                        Log.d("ProfileViewModel", "PlayStyles: ${profileData.playStyles}")
                        initializeEditFields(profileData)
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
     * Initialize edit fields with current profile data
     */
    private fun initializeEditFields(profileData: Profile) {
        editedBio = profileData.bio ?: ""
        editedPsn = profileData.gamerTags?.psn ?: ""
        editedXbox = profileData.gamerTags?.xbox ?: ""
        editedSteam = profileData.gamerTags?.steam ?: ""
        editedDiscord = profileData.gamerTags?.discord ?: ""
        editedNintendo = profileData.gamerTags?.nintendo ?: ""
        editedAvailability = profileData.availability ?: ""
        selectedPlayStyles = profileData.playStyles?.toSet() ?: emptySet()
        selectedLanguages = profileData.languages?.toSet() ?: emptySet()
        editedFavoriteGame = profileData.favoriteGame
    }
    
    /**
     * Update edit field values
     */
    fun updateEditedBio(value: String) { editedBio = value }
    fun updateEditedPsn(value: String) { editedPsn = value }
    fun updateEditedXbox(value: String) { editedXbox = value }
    fun updateEditedSteam(value: String) { editedSteam = value }
    fun updateEditedDiscord(value: String) { editedDiscord = value }
    fun updateEditedNintendo(value: String) { editedNintendo = value }
    fun updateEditedAvailability(value: String) { editedAvailability = value }
    
    fun togglePlayStyle(style: String) {
        selectedPlayStyles = if (selectedPlayStyles.contains(style)) {
            selectedPlayStyles - style
        } else {
            selectedPlayStyles + style
        }
    }
    
    fun toggleLanguage(language: String) {
        selectedLanguages = if (selectedLanguages.contains(language)) {
            selectedLanguages - language
        } else {
            selectedLanguages + language
        }
    }
    
    fun setFavoriteGame(game: FavoriteGame?) {
        editedFavoriteGame = game
    }
    
    /**
     * Search for games
     */
    fun searchGames(query: String) {
        searchText = query
        
        if (query.length < 2) {
            searchResults = emptyList()
            return
        }
        
        viewModelScope.launch {
            isSearching = true
            try {
                val accessToken = tokenManager.accessTokenFlow.first()
                if (accessToken != null) {
                    val results = profileRepository.searchGames(query, accessToken)
                    searchResults = results
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Search failed", e)
            } finally {
                isSearching = false
            }
        }
    }
    
    fun clearSearch() {
        searchText = ""
        searchResults = emptyList()
    }

    /**
     * Save profile changes
     */
    suspend fun saveProfile() {
        isLoading = true
        error = null
        
        try {
            val accessToken = tokenManager.accessTokenFlow.first()
            val userId = profile?.id
            
            if (accessToken == null || userId == null) {
                error = "Not authenticated"
                isLoading = false
                return
            }
            
            val updates = ProfileUpdateRequest(
                bio = editedBio.takeIf { it.isNotBlank() },
                gamerTags = GamerTags(
                    psn = editedPsn.takeIf { it.isNotBlank() },
                    xbox = editedXbox.takeIf { it.isNotBlank() },
                    steam = editedSteam.takeIf { it.isNotBlank() },
                    discord = editedDiscord.takeIf { it.isNotBlank() },
                    nintendo = editedNintendo.takeIf { it.isNotBlank() }
                ),
                playStyles = selectedPlayStyles.toList().takeIf { it.isNotEmpty() },
                availability = editedAvailability.takeIf { it.isNotBlank() },
                languages = selectedLanguages.toList().takeIf { it.isNotEmpty() },
                favoriteGame = editedFavoriteGame
            )
            
            // Update profile
            profileRepository.updateProfile(userId, accessToken, updates)
            
            // Close edit screen first
            showingEditProfile = false
            
            // Reload full profile from server to get all fields including name
            val result = authRepository.getProfile(accessToken)
            result.fold(
                onSuccess = { profileData ->
                    profile = profileData
                    initializeEditFields(profileData)
                    Log.d("ProfileViewModel", "Profile reloaded after save: ${profileData.name}")
                },
                onFailure = { exception ->
                    Log.e("ProfileViewModel", "Failed to reload profile after save", exception)
                }
            )
            
        } catch (e: Exception) {
            error = "Failed to save profile: ${e.message}"
            Log.e("ProfileViewModel", "Save failed", e)
        } finally {
            isLoading = false
        }
    }
    
    /**
     * Show/hide edit profile screen
     */
    fun showEditProfile(showing: Boolean) {
        showingEditProfile = showing
        if (showing) {
            profile?.let { initializeEditFields(it) }
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
