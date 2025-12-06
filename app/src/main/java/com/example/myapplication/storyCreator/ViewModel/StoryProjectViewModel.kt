package com.example.myapplication.storyCreator.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.AppContextHolder
import com.example.myapplication.storyCreator.DTOs.CreateProjectDto
import com.example.myapplication.storyCreator.DTOs.FlowchartDto
import com.example.myapplication.storyCreator.DTOs.ProjectDto
import com.example.myapplication.storyCreator.DTOs.toDto
import com.example.myapplication.storyCreator.DTOs.toFlowNode
import com.example.myapplication.storyCreator.model.FlowchartState
import com.example.myapplication.storyCreator.repository.StoryProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StoryProjectViewModel(
    private val repository: StoryProjectRepository  = StoryProjectRepository(context = AppContextHolder.appContext)
) : ViewModel() {

    private val _projects = MutableStateFlow<List<ProjectDto>>(emptyList())
    val projects: StateFlow<List<ProjectDto>> = _projects.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentProject = MutableStateFlow<ProjectDto?>(null)
    val currentProject: StateFlow<ProjectDto?> = _currentProject.asStateFlow()

    init {
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _projects.value = repository.getAllProjects()
                    .sortedByDescending { it.updatedAt }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createNewProject(title: String, description: String = "", onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val dto = CreateProjectDto(title = title, description = description)
                val project = repository.createProject(dto)
                loadProjects()
                onSuccess(project.id)  // Call the callback with the new project ID
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            try {
                repository.deleteProject(projectId)
                loadProjects()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadFlowchart(projectId: String, callback: (FlowchartState?) -> Unit) {
        viewModelScope.launch {
            try {
                val flowchartDto = repository.getFlowchart(projectId)
                if (flowchartDto != null) {
                    val nodes = flowchartDto.nodes.map { it.toFlowNode() }
                    val state = FlowchartState(nodes.toMutableList())
                    callback(state)
                } else {
                    callback(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback(null)
            }
        }
    }




    fun saveFlowchart(projectId: String, state: FlowchartState) {
        viewModelScope.launch {
            try {
                val nodeDtos = state.nodes.map { it.toDto() }
                val flowchartDto = FlowchartDto(
                    projectId = projectId,
                    nodes = nodeDtos,
                    updatedAt = System.currentTimeMillis()
                )
                repository.saveFlowchart(flowchartDto)
                loadProjects()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setCurrentProject(projectId: String) {
        viewModelScope.launch {
            val project = _projects.value.find { it.id == projectId }
            _currentProject.value = project
        }
    }




    private val _publishState = MutableStateFlow<PublishState>(PublishState.Idle)
    val publishState = _publishState.asStateFlow()

    fun publishProject(projectId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _publishState.value = PublishState.Loading
            try {
                repository.publishProject(projectId)
                _publishState.value = PublishState.Success
                onSuccess()
            } catch (e: Exception) {
                _publishState.value = PublishState.Error(e.message ?: "Failed to publish project")
            }
        }
    }

    fun resetPublishState() {
        _publishState.value = PublishState.Idle
    }
}

sealed class PublishState {
    object Idle : PublishState()
    object Loading : PublishState()
    object Success : PublishState()
    data class Error(val message: String) : PublishState()
}
