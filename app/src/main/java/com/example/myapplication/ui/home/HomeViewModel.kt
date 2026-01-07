package com.example.myapplication.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Game
import com.example.myapplication.Repository.GameDataRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.example.myapplication.ui.auth.TokenDataStoreManager
import com.example.myapplication.AppContextHolder
import kotlinx.coroutines.flow.first

class HomeViewModel(
    private val repository: GameDataRepository = GameDataRepository()
) : ViewModel() {
    private val tokenManager = TokenDataStoreManager(AppContextHolder.appContext)

    var popularGames by mutableStateOf<List<Game>>(emptyList())
    var genreGames by mutableStateOf<Map<String, List<Game>>>(emptyMap())
    var searchResults by mutableStateOf<List<Game>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    
    // Search state
    var searchQuery by mutableStateOf("")
    private var searchJob: Job? = null
    
    // AI Recommendations
    var aiRecommendations by mutableStateOf<List<com.example.myapplication.model.AIRecommendation>>(emptyList())
    
    init {
        loadPopularGames()
        // Load some default genres
        loadGamesByGenre("Action")
        loadGamesByGenre("RPG")
        loadGamesByGenre("Adventure")
        loadUserAndRecommendations()
    }
    
    private fun loadUserAndRecommendations() {
        viewModelScope.launch {
            try {
                val userId = tokenManager.userIdFlow.first()
                if (!userId.isNullOrBlank()) {
                    loadAIRecommendations(userId)
                }
            } catch (e: Exception) {
                println("Failed to get userId for recommendations: ${e.message}")
            }
        }
    }
    
    fun loadAIRecommendations(userId: String) {
        viewModelScope.launch {
            repository.getPersonalizedRecommendations(userId).fold(
                onSuccess = { recommendations ->
                    aiRecommendations = recommendations
                },
                onFailure = { e ->
                    println("Failed to load AI recommendations: ${e.message}")
                }
            )
        }
    }

    fun loadPopularGames() {
        isLoading = true
        viewModelScope.launch {
            repository.getPopularGames().fold(
                onSuccess = { games ->
                    popularGames = games
                    isLoading = false
                },
                onFailure = { e ->
                    errorMessage = "Failed to load popular games: ${e.toString()}"
                    isLoading = false
                }
            )
        }
    }

    fun loadGamesByGenre(genre: String) {
        viewModelScope.launch {
            repository.getGamesByGenre(genre).fold(
                onSuccess = { games ->
                    val currentMap = genreGames.toMutableMap()
                    currentMap[genre] = games
                    genreGames = currentMap
                },
                onFailure = { e ->
                    // Log error but don't show full screen error for genre sections
                    println("Failed to load genre $genre: ${e.message}")
                }
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery = query
        searchJob?.cancel()
        
        if (query.isBlank()) {
            searchResults = emptyList()
            return
        }
        
        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            isLoading = true
            repository.searchGames(query).fold(
                onSuccess = { games ->
                    searchResults = games
                    isLoading = false
                },
                onFailure = { e ->
                    errorMessage = "Search failed: ${e.message}"
                    isLoading = false
                }
            )
        }
    }
    
    fun clearError() {
        errorMessage = null
    }
}
