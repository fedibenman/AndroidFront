package com.example.myapplication.storyCreator.repository

import android.content.Context
import android.util.Log
import androidx.compose.ui.geometry.Offset
import com.example.myapplication.Repository.ApiClient
import com.example.myapplication.storyCreator.DTOs.CommunityProjectDto
import com.example.myapplication.storyCreator.DTOs.FlowchartDto
import com.example.myapplication.storyCreator.DTOs.ProjectDto
import com.example.myapplication.storyCreator.DTOs.PublishProjectDto
import com.example.myapplication.storyCreator.model.FlowNode
import com.example.myapplication.storyCreator.model.FlowchartState
import com.example.myapplication.storyCreator.model.NodeType
import com.example.myapplication.ui.auth.TokenDataStoreManager
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class CommunityProjectRepository(
    private val context: Context
) {
    private val client = ApiClient.client
    // Changed to use community-projects endpoint
    private val baseUrl = "${ApiClient.BASE_URL}/community-projects"
    private val tokenManager = TokenDataStoreManager(context)

    companion object {
        private const val TAG = "CommunityProjectRepo"
    }

    private suspend fun getToken(): String {
        return tokenManager.accessTokenFlow.first()
            ?: throw Exception("No access token found. Please login again.")
    }

    // Get all community projects
    suspend fun getAllCommunityProjects(): List<CommunityProjectDto> = withContext(Dispatchers.IO) {
        try {
            val token = getToken()
            Log.d(TAG, "getAllCommunityProjects - Request URL: $baseUrl")
            Log.d(TAG, "getAllCommunityProjects - Token: ${token.take(20)}...")

            val response = client.get(baseUrl) {  // Changed from baseUrl+"/community"
                headers {
                    append("Authorization", "Bearer $token")
                }
            }

            Log.d(TAG, "getAllCommunityProjects - Response Status: ${response.status}")
            val responseBody = response.bodyAsText()
            Log.d(TAG, "getAllCommunityProjects - Response Body: $responseBody")

            response.body()
        } catch (e: Exception) {
            Log.e(TAG, "getAllCommunityProjects - Error: ${e.message}", e)
            e.printStackTrace()
            emptyList()
        }
    }

    // Get filtered projects
    suspend fun getFilteredProjects(filter: String): List<CommunityProjectDto> = withContext(Dispatchers.IO) {
        try {
            val token = getToken()
            val url = "$baseUrl?filter=$filter"  // Changed from "$baseUrl/community?filter=$filter"
            Log.d(TAG, "getFilteredProjects - Request URL: $url")
            Log.d(TAG, "getFilteredProjects - Filter: $filter")

            val response = client.get(url) {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }

            Log.d(TAG, "getFilteredProjects - Response Status: ${response.status}")
            val responseBody = response.bodyAsText()
            Log.d(TAG, "getFilteredProjects - Response Body: $responseBody")

            response.body()
        } catch (e: Exception) {
            Log.e(TAG, "getFilteredProjects - Error: ${e.message}", e)
            e.printStackTrace()
            emptyList()
        }
    }

    // Toggle star on a project
    suspend fun toggleStar(projectId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = getToken()
            val url = "$baseUrl/$projectId/star"
            Log.d(TAG, "toggleStar - Request URL: $url")
            Log.d(TAG, "toggleStar - Project ID: $projectId")

            val response: HttpResponse = client.post(url) {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }

            Log.d(TAG, "toggleStar - Response Status: ${response.status}")
            val responseBody = response.bodyAsText()
            Log.d(TAG, "toggleStar - Response Body: $responseBody")

            val isSuccess = response.status.isSuccess()
            Log.d(TAG, "toggleStar - Success: $isSuccess")

            isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "toggleStar - Error: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }

    // Fork a project
    suspend fun forkProject(projectId: String): ProjectDto = withContext(Dispatchers.IO) {
        val token = getToken()
        val url = "$baseUrl/$projectId/fork"
        Log.d(TAG, "forkProject - Request URL: $url")
        Log.d(TAG, "forkProject - Project ID: $projectId")

        val response = client.post(url) {
            headers {
                append("Authorization", "Bearer $token")
            }
        }

        Log.d(TAG, "forkProject - Response Status: ${response.status}")
        val responseBody = response.bodyAsText()
        Log.d(TAG, "forkProject - Response Body: $responseBody")

        response.body()
    }

    // Publish a project to community
    suspend fun publishProject(projectId: String): CommunityProjectDto = withContext(Dispatchers.IO) {
        val token = getToken()
        val url = "$baseUrl/publish"
        Log.d(TAG, "publishProject - Request URL: $url")
        Log.d(TAG, "publishProject - Request Body: projectId=$projectId")

        val dto = PublishProjectDto(projectId = projectId)

        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $token")
            }
            setBody(dto)
        }

        Log.d(TAG, "publishProject - Response Status: ${response.status}")
        val responseBody = response.bodyAsText()
        Log.d(TAG, "publishProject - Response Body: $responseBody")

        response.body()
    }


    suspend fun getProjectFlowchart(projectId: String): FlowchartDto? = withContext(Dispatchers.IO) {
        try {
            val token = getToken()
            val url = "$baseUrl/$projectId/flowchart"
            Log.d(TAG, "getProjectFlowchart - Request URL: $url")
            Log.d(TAG, "getProjectFlowchart - Project ID: $projectId")

            val response = client.get(url) {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }

            Log.d(TAG, "getProjectFlowchart - Response Status: ${response.status}")
            val responseBody = response.bodyAsText()
            Log.d(TAG, "getProjectFlowchart - Response Body: $responseBody")

            if (response.status.value in 200..299) {
                response.body<FlowchartDto>()
            } else {
                Log.e(TAG, "getProjectFlowchart - Failed with status: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "getProjectFlowchart - Error: ${e.message}", e)
            null
        }
    }

    // Helper function to convert FlowchartDto to FlowchartState
    fun FlowchartDto.toFlowchartState(): FlowchartState {
        val nodes = this.nodes.map { nodeDto ->
            FlowNode(
                id = nodeDto.id,
                type = when (nodeDto.type) {
                    "Start" -> NodeType.Start
                    "Story" -> NodeType.Story
                    "Decision" -> NodeType.Decision
                    "End" -> NodeType.End
                    else -> NodeType.Story
                },
                text = nodeDto.text,
                position = Offset(
                    nodeDto.positionX.toFloat(),
                    nodeDto.positionY.toFloat()
                ),
                imageData = nodeDto.imageData.toString(),
            )
        }

        return FlowchartState(
            nodes = androidx.compose.runtime.mutableStateListOf(*nodes.toTypedArray())
        )
    }


}