package com.example.myapplication.storyCreator.ViewModel

package com.example.myapplication.storyCreator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.storyCreator.data.dto.*
import com.example.myapplication.storyCreator.data.repository.StoryProjectRepository
import com.example.myapplication.storyCreator.model.FlowchartState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class StoryProjectViewModel(
    private val repository: StoryProjectRepository
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

    fun createNewProject(title: String, description: String = ""): String {
        val projectId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val project = ProjectDto(
            id = projectId,
            title = title,
            description = description,
            createdAt = now,
            updatedAt = now
        )

        viewModelScope.launch {
            repository.saveProject(project)
            loadProjects()
        }

        return projectId
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            loadProjects()
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
                loadProjects() // Refresh to update timestamps
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
}
