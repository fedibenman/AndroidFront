package com.example.myapplication.storyCreator.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.AppContextHolder
import com.example.myapplication.storyCreator.DTOs.CommunityProjectDto
import com.example.myapplication.storyCreator.DTOs.toFlowchartState
import com.example.myapplication.storyCreator.model.FlowchartState
import com.example.myapplication.storyCreator.repository.CommunityProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommunityProjectViewModel(
    private val repository: CommunityProjectRepository = CommunityProjectRepository(context = AppContextHolder.appContext)
) : ViewModel() {

    private val _communityProjects = MutableStateFlow<List<CommunityProjectDto>>(emptyList())
    val communityProjects: StateFlow<List<CommunityProjectDto>> = _communityProjects.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    init {
        loadCommunityProjects()
    }

    fun setCurrentUserId(userId: String?) {
        _currentUserId.value = userId
    }

    fun loadCommunityProjects() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _communityProjects.value = repository.getAllCommunityProjects()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterProjects(filter: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _communityProjects.value = when (filter) {
                    "All" -> repository.getAllCommunityProjects()
                    "Popular" -> repository.getFilteredProjects("popular")
                    "Recent" -> repository.getFilteredProjects("recent")
                    "Starred" -> repository.getFilteredProjects("starred")
                    else -> repository.getAllCommunityProjects()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleStar(projectId: String) {
        viewModelScope.launch {
            try {
                val success = repository.toggleStar(projectId)
                if (success) {
                    // Update local state
                    _communityProjects.value = _communityProjects.value.map { project ->
                        if (project.id == projectId) {
                            project.copy(
                                isStarredByUser = !project.isStarredByUser,
                                starCount = if (!project.isStarredByUser)
                                    project.starCount + 1
                                else
                                    project.starCount - 1
                            )
                        } else {
                            project
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun forkProject(projectId: String) {
        viewModelScope.launch {
            try {
                repository.forkProject(projectId)
                // Update fork count locally
                _communityProjects.value = _communityProjects.value.map { project ->
                    if (project.id == projectId) {
                        project.copy(forkCount = project.forkCount + 1)
                    } else {
                        project
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    // Add to CommunityProjectViewModel
    fun loadProjectFlowchart(
        projectId: String,
        onLoaded: (FlowchartState?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val flowchartDto = repository.getProjectFlowchart(projectId)
                val flowchartState = flowchartDto?.toFlowchartState()
                onLoaded(flowchartState)
            } catch (e: Exception) {
                Log.e("CommunityProjectViewModel", "Error loading flowchart", e)
                onLoaded(null)
            }
        }
    }

}