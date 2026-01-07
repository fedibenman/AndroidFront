package com.example.myapplication.ui.mission

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.AppContextHolder
import com.example.myapplication.model.Mission
import com.example.myapplication.model.MissionProgress
import com.example.myapplication.Repository.CollectionRepository
import com.example.myapplication.Repository.MissionsRepository
import com.example.myapplication.ui.auth.TokenDataStoreManager
import kotlinx.coroutines.launch

class MissionChecklistViewModel(
    private val missionsRepository: MissionsRepository = MissionsRepository(),
    private val collectionRepository: CollectionRepository = CollectionRepository()
) : ViewModel() {
    var missions = mutableStateListOf<Mission>()
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var progress by mutableStateOf<MissionProgress?>(null)
        private set

    private val tokenManager = TokenDataStoreManager(AppContextHolder.appContext)


    fun loadMissions(gameId: Int, gameName: String) {
        isLoading = true
        viewModelScope.launch {
            missionsRepository.fetchMissions(gameId, gameName).fold(
                onSuccess = { gameMissions ->
                    missions.clear()
                    missions.addAll(gameMissions.missions)
                    loadProgress(gameId)
                },
                onFailure = { e ->
                    errorMessage = "Failed to load missions: ${e.message}"
                    isLoading = false
                }
            )
        }
    }
    
    private fun loadProgress(gameId: Int) {
        viewModelScope.launch {
            val userId = tokenManager.getUserId() ?: return@launch
            collectionRepository.getMissionProgress(userId, gameId).fold(
                onSuccess = { p ->
                    progress = p
                    updateMissionCompletionState(p)
                    isLoading = false
                },
                onFailure = { e ->
                    // It's okay if progress fails initially (maybe no progress yet)
                    isLoading = false
                }
            )
        }
    }
    
    private fun updateMissionCompletionState(progress: MissionProgress) {
        val updatedMissions = missions.map { mission ->
            mission.copy(isCompleted = progress.completedMissions.contains(mission.number))
        }
        missions.clear()
        missions.addAll(updatedMissions)
    }

    fun toggleMission(mission: Mission, gameId: Int) {
        viewModelScope.launch {
            val userId = tokenManager.getUserId() ?: return@launch
            val total = missions.size
            
            // Optimistic update
            val index = missions.indexOfFirst { it.number == mission.number }
            if (index != -1) {
                missions[index] = mission.copy(isCompleted = !mission.isCompleted)
            }

            collectionRepository.toggleMission(userId, gameId, mission.number, total).fold(
                onSuccess = { p ->
                    progress = p
                    updateMissionCompletionState(p)
                },
                onFailure = { e ->
                    errorMessage = "Failed to update mission: ${e.message}"
                    // Revert optimistic update
                    if (index != -1) {
                        missions[index] = mission.copy(isCompleted = !mission.isCompleted) // Revert
                    }
                }
            )
        }
    }
}
