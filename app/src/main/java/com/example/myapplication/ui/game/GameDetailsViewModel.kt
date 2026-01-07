package com.example.myapplication.ui.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.AppContextHolder
import com.example.myapplication.Repository.CollectionRepository
import com.example.myapplication.Repository.GameDataRepository
import com.example.myapplication.model.Game

import com.example.myapplication.ui.auth.TokenDataStoreManager
import com.example.myapplication.Repository.ReviewsRepository
import com.example.myapplication.model.Review
import com.example.myapplication.Repository.GameRating
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.launch

class GameDetailsViewModel(
    private val gamesRepository: GameDataRepository = GameDataRepository(),
    private val collectionRepository: CollectionRepository = CollectionRepository(),
    private val reviewsRepository: ReviewsRepository = ReviewsRepository()
) : ViewModel() {
    var game by mutableStateOf<Game?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isAddingToCollection by mutableStateOf(false)
    var addToCollectionSuccess by mutableStateOf(false)
    
    var reviews by mutableStateOf<List<Review>>(emptyList())
    var gameRating by mutableStateOf<GameRating?>(null)
    var recommendations by mutableStateOf<List<Game>>(emptyList())
    var isPostingReview by mutableStateOf(false)
    var showReviewDialog by mutableStateOf(false)
    
    private val tokenManager = TokenDataStoreManager(AppContextHolder.appContext)

    fun loadGameDetails(gameId: Int) {
        isLoading = true
        viewModelScope.launch {
            // Load Game Details
            gamesRepository.getGameDetails(gameId).fold(
                onSuccess = { loadedGame ->
                    game = loadedGame
                    isLoading = false
                    // Load related data after game is loaded
                    loadReviews(gameId)
                    loadGameRating(gameId)
                    loadRecommendations(gameId)
                },
                onFailure = { e ->
                    errorMessage = "Failed to load game details: ${e.message}"
                    isLoading = false
                }
            )
        }
    }

    private fun loadReviews(gameId: Int) {
        viewModelScope.launch {
            android.util.Log.d("GameDetailsViewModel", "Loading reviews for game $gameId")
            reviewsRepository.getGameReviews(gameId).fold(
                onSuccess = { loadedReviews ->
                    android.util.Log.d("GameDetailsViewModel", "Loaded ${loadedReviews.size} reviews")
                    reviews = loadedReviews
                },
                onFailure = { e ->
                    android.util.Log.e("GameDetailsViewModel", "Failed to load reviews: ${e.message}", e)
                }
            )
        }
    }

    private fun loadGameRating(gameId: Int) {
        viewModelScope.launch {
            reviewsRepository.getGameRating(gameId).onSuccess { rating ->
                gameRating = rating
            }
        }
    }

    private fun loadRecommendations(gameId: Int) {
        viewModelScope.launch {
            val userId = tokenManager.getUserId() ?: return@launch
            gamesRepository.getRecommendations(gameId, userId).onSuccess { recs ->
                recommendations = recs
            }
        }
    }

    fun submitReview(gameId: Int, rating: Int, text: String) {
        isPostingReview = true
        viewModelScope.launch {
            val userId = tokenManager.getUserId()
            if (userId == null) {
                errorMessage = "User not logged in"
                isPostingReview = false
                return@launch
            }

            reviewsRepository.createReview(gameId, userId, rating, text).fold(
                onSuccess = {
                    isPostingReview = false
                    showReviewDialog = false
                    loadReviews(gameId) // Refresh reviews
                    loadGameRating(gameId) // Refresh rating
                },
                onFailure = { e ->
                    isPostingReview = false
                    errorMessage = "Failed to post review: ${e.message}"
                }
            )
        }
    }

    var isGeneratingReview by mutableStateOf(false)

    fun generateReview(rating: Int, prompt: String?, onResult: (String) -> Unit) {
        val currentGame = game ?: return
        isGeneratingReview = true
        viewModelScope.launch {
            gamesRepository.generateAIReview(currentGame.name, rating, prompt).fold(
                onSuccess = { text ->
                    isGeneratingReview = false
                    onResult(text)
                },
                onFailure = { e ->
                    isGeneratingReview = false
                    errorMessage = "AI Generation failed: ${e.message}"
                }
            )
        }
    }

    fun trackRecommendationClick(sourceGameId: Int, clickedGameId: Int) {
        viewModelScope.launch {
            val userId = tokenManager.getUserId() ?: return@launch
            gamesRepository.trackRecommendationClick(userId, sourceGameId, clickedGameId)
        }
    }

    fun addToCollection(gameId: Int, status: String) {
        isAddingToCollection = true
        viewModelScope.launch {
            val userId = tokenManager.getUserId()
            if (userId == null) {
                errorMessage = "User not logged in"
                isAddingToCollection = false
                return@launch
            }

            collectionRepository.addToCollection(userId, gameId, status).fold(
                onSuccess = {
                    addToCollectionSuccess = true
                    isAddingToCollection = false
                },
                onFailure = { e ->
                    errorMessage = "Failed to add to collection: ${e.message}"
                    isAddingToCollection = false
                }
            )
        }
    }
    
    fun clearError() {
        errorMessage = null
    }
    
    fun resetAddToCollectionSuccess() {
        addToCollectionSuccess = false
    }
}
