package com.example.myapplication.storyCreator.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.AppContextHolder
import com.example.myapplication.storyCreator.DTOs.CreateProjectDto
import com.example.myapplication.storyCreator.DTOs.FlowchartDto
import com.example.myapplication.storyCreator.DTOs.ProjectDto
import com.example.myapplication.storyCreator.DTOs.toAddDto
import com.example.myapplication.storyCreator.DTOs.toDto
import com.example.myapplication.storyCreator.DTOs.toFlowNode
import com.example.myapplication.storyCreator.DTOs.toProjectArtStyle
import com.example.myapplication.storyCreator.DTOs.toReference
import com.example.myapplication.storyCreator.DTOs.toUpdateDto
import com.example.myapplication.storyCreator.model.FlowchartState
import com.example.myapplication.storyCreator.model.ProjectArtStyle
import com.example.myapplication.storyCreator.model.Reference
import com.example.myapplication.storyCreator.repository.StoryProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StoryProjectViewModel(
    private val repository: StoryProjectRepository = StoryProjectRepository(context = AppContextHolder.appContext)
) : ViewModel() {

    private val _projects = MutableStateFlow<List<ProjectDto>>(emptyList())
    val projects: StateFlow<List<ProjectDto>> = _projects.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentProject = MutableStateFlow<ProjectDto?>(null)
    val currentProject: StateFlow<ProjectDto?> = _currentProject.asStateFlow()

    // References state
    private val _references = MutableStateFlow<List<Reference>>(emptyList())
    val references: StateFlow<List<Reference>> = _references.asStateFlow()

    private val _projectArtStyle = MutableStateFlow<ProjectArtStyle?>(null)
    val projectArtStyle: StateFlow<ProjectArtStyle?> = _projectArtStyle.asStateFlow()

    private val _referencesLoading = MutableStateFlow(false)
    val referencesLoading: StateFlow<Boolean> = _referencesLoading.asStateFlow()

    // init {
    //    loadProjects()
    // }

    // ==================== PROJECT METHODS ====================

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
                onSuccess(project.id)
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

    fun setCurrentProject(projectId: String) {
        viewModelScope.launch {
            val project = _projects.value.find { it.id == projectId }
            _currentProject.value = project
        }
    }

    // ==================== FLOWCHART METHODS ====================

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

    // ==================== REFERENCES METHODS ====================

    fun loadReferences(projectId: String) {
        viewModelScope.launch {
            _referencesLoading.value = true
            try {
                val referencesDto = repository.getProjectReferences(projectId)
                if (referencesDto != null) {
                    _projectArtStyle.value = referencesDto.toProjectArtStyle()
                    _references.value = referencesDto.references.map { it.toReference() }
                } else {
                    _projectArtStyle.value = null
                    _references.value = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _projectArtStyle.value = null
                _references.value = emptyList()
            } finally {
                _referencesLoading.value = false
            }
        }
    }

    fun updateArtStyle(projectId: String, artStyle: ProjectArtStyle) {
        viewModelScope.launch {
            try {
                val dto = artStyle.toUpdateDto()
                repository.updateArtStyle(projectId, dto)
                _projectArtStyle.value = artStyle
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addReference(projectId: String, reference: Reference, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val dto = reference.toAddDto()
                val addedReference = repository.addReference(projectId, dto)
                _references.value = _references.value + addedReference.toReference()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateReference(projectId: String, reference: Reference, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val dto = reference.toDto()
                repository.updateReference(projectId, reference.id, dto)
                _references.value = _references.value.map {
                    if (it.id == reference.id) reference else it
                }
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteReference(projectId: String, referenceId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.deleteReference(projectId, referenceId)
                _references.value = _references.value.filter { it.id != referenceId }
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ==================== AI GENERATION METHODS ====================


    fun generateReferenceAssets(
        projectId: String,
        referenceId: String,
        onSuccess: (imageData: String, modelData: String?) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val generatedAssets = repository.generateReferenceAssets(
                    projectId = projectId,
                    referenceId = referenceId
                )
                onSuccess(generatedAssets.imageData, generatedAssets.modelData)
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Failed to generate assets")
            }
        }
    }

    // ==================== PUBLISH METHODS ====================

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