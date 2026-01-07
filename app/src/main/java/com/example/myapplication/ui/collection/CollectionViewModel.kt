package com.example.myapplication.ui.collection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.AppContextHolder
import com.example.myapplication.model.CollectionItem
import com.example.myapplication.model.Game
import com.example.myapplication.Repository.CollectionRepository
import com.example.myapplication.Repository.GameDataRepository
import com.example.myapplication.ui.auth.TokenDataStoreManager
import kotlinx.coroutines.launch

class CollectionViewModel(
    private val collectionRepository: CollectionRepository = CollectionRepository(),
    private val gamesRepository: GameDataRepository = GameDataRepository()
) : ViewModel() {
    var collection by mutableStateOf<List<CollectionItem>>(emptyList())
    var gamesDetails by mutableStateOf<Map<Int, Game>>(emptyMap())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    
    private val tokenManager = TokenDataStoreManager(AppContextHolder.appContext)

    fun loadCollection() {
        isLoading = true
        viewModelScope.launch {
            val userId = tokenManager.getUserId()
            if (userId == null) {
                errorMessage = "User not logged in"
                isLoading = false
                return@launch
            }

            collectionRepository.getCollection(userId).fold(
                onSuccess = { items ->
                    collection = items
                    isLoading = false
                    // Fetch details for games in collection
                    fetchGameDetails(items)
                },
                onFailure = { e ->
                    errorMessage = "Failed to load collection: ${e.message}"
                    isLoading = false
                }
            )
        }
    }
    
    private fun fetchGameDetails(items: List<CollectionItem>) {
        viewModelScope.launch {
            items.forEach { item ->
                if (!gamesDetails.containsKey(item.gameId)) {
                    gamesRepository.getGameDetails(item.gameId).onSuccess { game ->
                        val current = gamesDetails.toMutableMap()
                        current[game.id] = game
                        gamesDetails = current
                    }
                }
            }
        }
    }
}
