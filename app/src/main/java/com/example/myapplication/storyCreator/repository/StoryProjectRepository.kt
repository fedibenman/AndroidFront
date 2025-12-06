package com.example.myapplication.storyCreator.repository


import android.content.Context
import com.example.myapplication.storyCreator.DTOs.FlowchartDto
import com.example.myapplication.storyCreator.DTOs.ProjectDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

open class StoryProjectRepository(private val context: Context) {
    private val gson = Gson()
    private val projectsDir = File(context.filesDir, "story_projects")

    init {
        if (!projectsDir.exists()) {
            projectsDir.mkdirs()
        }
    }

    // Projects Management
    open suspend fun getAllProjects(): List<ProjectDto> = withContext(Dispatchers.IO) {
        val projectsFile = File(projectsDir, "projects.json")
        if (!projectsFile.exists()) return@withContext emptyList()

        try {
            val json = projectsFile.readText()
            val type = object : TypeToken<List<ProjectDto>>() {}.type
            gson.fromJson<List<ProjectDto>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    open suspend fun saveProject(project: ProjectDto) = withContext(Dispatchers.IO) {
        val projects = getAllProjects().toMutableList()
        val existingIndex = projects.indexOfFirst { it.id == project.id }

        if (existingIndex != -1) {
            projects[existingIndex] = project.copy(updatedAt = System.currentTimeMillis())
        } else {
            projects.add(project)
        }

        val projectsFile = File(projectsDir, "projects.json")
        projectsFile.writeText(gson.toJson(projects))
    }

    open suspend fun deleteProject(projectId: String) = withContext(Dispatchers.IO) {
        // Delete project metadata
        val projects = getAllProjects().toMutableList()
        projects.removeAll { it.id == projectId }

        val projectsFile = File(projectsDir, "projects.json")
        projectsFile.writeText(gson.toJson(projects))

        // Delete flowchart data
        val flowchartFile = File(projectsDir, "${projectId}_flowchart.json")
        if (flowchartFile.exists()) {
            flowchartFile.delete()
        }
    }

    // Flowchart Management
    open suspend fun getFlowchart(projectId: String): FlowchartDto? = withContext(Dispatchers.IO) {
        val flowchartFile = File(projectsDir, "${projectId}_flowchart.json")
        if (!flowchartFile.exists()) return@withContext null

        try {
            val json = flowchartFile.readText()
            gson.fromJson(json, FlowchartDto::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    open suspend fun saveFlowchart(flowchart: FlowchartDto) = withContext(Dispatchers.IO) {
        val flowchartFile = File(projectsDir, "${flowchart.projectId}_flowchart.json")
        flowchartFile.writeText(gson.toJson(flowchart))

        // Update project's updatedAt timestamp
        val projects = getAllProjects().toMutableList()
        val projectIndex = projects.indexOfFirst { it.id == flowchart.projectId }
        if (projectIndex != -1) {
            projects[projectIndex] = projects[projectIndex].copy(
                updatedAt = System.currentTimeMillis()
            )
            val projectsFile = File(projectsDir, "projects.json")
            projectsFile.writeText(gson.toJson(projects))
        }
    }
}
