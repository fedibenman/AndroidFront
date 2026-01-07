package com.example.myapplication.ui.teammate

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.AppContextHolder
import com.example.myapplication.Repository.NearbyRepository
import com.example.myapplication.Repository.NearbyUser
import com.example.myapplication.ui.auth.TokenDataStoreManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NearbyTeammatesViewModel(
    private val repository: NearbyRepository = NearbyRepository()
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var isSearching by mutableStateOf(false)
        private set

    var nearbyUsers by mutableStateOf<List<NearbyUser>>(emptyList())
        private set

    var searchRange by mutableStateOf(50)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var hasLocationPermission by mutableStateOf(false)
        private set

    var currentLocation by mutableStateOf<Pair<Double, Double>?>(null)
        private set

    // Game filter
    var selectedGameId by mutableStateOf<Int?>(null)
        private set

    var selectedGameName by mutableStateOf<String?>(null)
        private set

    var userId: String? = null
        private set
    private var pollingActive = false

    init {
        loadUserId()
    }

    private fun loadUserId() {
        viewModelScope.launch {
            try {
                val context = AppContextHolder.appContext
                val tokenManager = TokenDataStoreManager(context)
                userId = tokenManager.userIdFlow.first()
            } catch (e: Exception) {
                Log.e("NearbyVM", "Error loading userId", e)
            }
        }
    }

    fun checkLocationPermission(context: Context) {
        hasLocationPermission = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun updateLocation(context: Context) {
        if (!hasLocationPermission) return

        viewModelScope.launch {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@launch
                }

                val cancellationToken = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationToken.token
                ).addOnSuccessListener { location: Location? ->
                    location?.let {
                        currentLocation = Pair(it.latitude, it.longitude)
                        sendLocationToServer(it.latitude, it.longitude)
                    }
                }
            } catch (e: Exception) {
                Log.e("NearbyVM", "Error getting location", e)
                errorMessage = "Failed to get location"
            }
        }
    }

    private fun sendLocationToServer(latitude: Double, longitude: Double) {
        val uid = userId ?: return
        
        viewModelScope.launch {
            repository.updateLocation(uid, latitude, longitude)
                .onSuccess {
                    Log.d("NearbyVM", "Location updated: $latitude, $longitude")
                }
                .onFailure { e ->
                    Log.e("NearbyVM", "Failed to update location", e)
                }
        }
    }

    fun startSearching() {
        val uid = userId ?: return
        
        viewModelScope.launch {
            isLoading = true
            repository.startNearbySearch(uid, searchRange)
                .onSuccess {
                    isSearching = true
                    startPollingNearbyUsers()
                }
                .onFailure { e ->
                    errorMessage = "Failed to start search: ${e.message}"
                }
            isLoading = false
        }
    }

    fun stopSearching() {
        val uid = userId ?: return
        
        viewModelScope.launch {
            pollingActive = false
            repository.stopNearbySearch(uid)
                .onSuccess {
                    isSearching = false
                    nearbyUsers = emptyList()
                }
                .onFailure { e ->
                    errorMessage = "Failed to stop search: ${e.message}"
                }
        }
    }

    fun updateSearchRange(range: Int) {
        searchRange = range
        if (isSearching) {
            // Re-start search with new range
            val uid = userId ?: return
            viewModelScope.launch {
                repository.startNearbySearch(uid, range)
            }
        }
    }

    var gameSearchQuery by mutableStateOf("")
        private set

    var matchingGames by mutableStateOf<List<Pair<String, Int>>>(emptyList())
        private set

    fun updateGameSearchQuery(query: String) {
        gameSearchQuery = query
        if (query.length >= 2) {
            val searchTerm = query.lowercase().trim()
            matchingGames = gameDatabase.entries
                .filter { it.key.contains(searchTerm) }
                .map { Pair(it.key.capitalize(), it.value) }
                .distinctBy { it.second } // Avoid duplicates if keys map to same ID
        } else {
            matchingGames = emptyList()
        }
    }

    // Match iOS game database
    private val gameDatabase = mapOf(
        "witcher" to 3328,
        "witcher 3" to 3328,
        "the witcher 3" to 3328,
        "fortnite" to 47137,
        "god of war" to 29179,
        "minecraft" to 22509,
        "gta v" to 3498,
        "gta 5" to 3498,
        "grand theft auto" to 3498,
        "elden ring" to 326243,
        "zelda" to 22511,
        "call of duty" to 4200,
        "cod" to 4200,
        "fifa" to 1140,
        "assassin's creed" to 4729,
        "valorant" to 326278,
        "league of legends" to 10213,
        "pubg" to 326240,
        "apex legends" to 326241
    )

    fun selectGame(name: String, id: Int) {
        gameSearchQuery = name
        matchingGames = emptyList() // Hide dropdown
        setGameFilter(id, name)
    }

    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    fun setGameFilter(gameId: Int?, gameName: String?) {
        selectedGameId = gameId
        selectedGameName = gameName
        Log.d("NearbyVM", "Game filter set: $gameName (ID: $gameId)")
    }

    fun clearGameFilter() {
        selectedGameId = null
        selectedGameName = null
    }

    private fun startPollingNearbyUsers() {
        val uid = userId ?: return
        pollingActive = true
        
        viewModelScope.launch {
            while (pollingActive && isSearching) {
                repository.getNearbyUsers(uid, searchRange, selectedGameId)
                    .onSuccess { response ->
                        nearbyUsers = response.users
                        val filterInfo = selectedGameName?.let { " for $it" } ?: ""
                        Log.d("NearbyVM", "Found ${response.users.size} nearby users$filterInfo")
                        response.users.forEach { user ->
                            Log.d("NearbyVM", " > User: ${user.name}, Game: ${user.favoriteGame?.name} (ID: ${user.favoriteGame?.gameId})")
                        }
                    }
                    .onFailure { e ->
                        Log.e("NearbyVM", "Failed to get nearby users", e)
                    }
                
                delay(3000) // Poll every 3 seconds
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun sendInvite(toUserId: String, gameId: Int, onResult: (Boolean) -> Unit) {
        val uid = userId ?: return
        viewModelScope.launch {
            repository.sendInvite(uid, toUserId, gameId)
                .onSuccess { onResult(true) }
                .onFailure { onResult(false) }
        }
    }

    suspend fun getUserProfile(userId: String): Result<com.example.myapplication.Repository.UserProfile> {
        return repository.getUserProfile(userId)
    }

    override fun onCleared() {
        super.onCleared()
        pollingActive = false
        if (isSearching) {
            userId?.let { uid ->
                viewModelScope.launch {
                    repository.stopNearbySearch(uid)
                }
            }
        }
    }
}
