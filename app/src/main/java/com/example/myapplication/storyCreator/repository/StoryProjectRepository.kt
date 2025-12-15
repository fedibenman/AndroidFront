package com.example.myapplication.storyCreator.repository

import android.content.Context
import android.util.Log
import com.example.myapplication.Repository.ApiClient
import com.example.myapplication.storyCreator.DTOs.AddReferenceDto
import com.example.myapplication.storyCreator.DTOs.CommunityProjectDto
import com.example.myapplication.storyCreator.DTOs.CreateProjectDto
import com.example.myapplication.storyCreator.DTOs.FlowchartDto
import com.example.myapplication.storyCreator.DTOs.ProjectDto
import com.example.myapplication.storyCreator.DTOs.ProjectReferencesDto
import com.example.myapplication.storyCreator.DTOs.PublishProjectDto
import com.example.myapplication.storyCreator.DTOs.ReferenceDto
import com.example.myapplication.storyCreator.DTOs.UpdateArtStyleDto
import com.example.myapplication.ui.auth.TokenDataStoreManager
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

class StoryProjectRepository(
    private val context: Context
) {
    private val client = ApiClient.client
    private val baseUrl = "${ApiClient.BASE_URL}/projects"
    private val communityBaseUrl = "${ApiClient.BASE_URL}/community-projects"
    private val tokenManager = TokenDataStoreManager(context)

    companion object {
        private const val TAG = "StoryProjectRepository"
    }

    private suspend fun getToken(): String {
        return tokenManager.accessTokenFlow.first()
            ?: throw Exception("No access token found. Please login again.")
    }

    // ==================== PROJECT METHODS ====================

    suspend fun getAllProjects(): List<ProjectDto> = withContext(Dispatchers.IO) {
        try {
            val token = getToken()
            Log.d(TAG, "getAllProjects - Request URL: $baseUrl")
            Log.d(TAG, "getAllProjects - Token: ${token.take(20)}...")

            val response = client.get(baseUrl) {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }

            Log.d(TAG, "getAllProjects - Response Status: ${response.status}")
            val responseBody = response.bodyAsText()
            Log.d(TAG, "getAllProjects - Response Body: $responseBody")

            response.body()
        } catch (e: Exception) {
            Log.e(TAG, "getAllProjects - Error: ${e.message}", e)
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getProject(id: String): ProjectDto? = withContext(Dispatchers.IO) {
        try {
            val token = getToken()
            val url = "$baseUrl/$id"
            Log.d(TAG, "getProject - Request URL: $url")
            Log.d(TAG, "getProject - Project ID: $id")

            val response = client.get(url) {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }

            Log.d(TAG, "getProject - Response Status: ${response.status}")
            val responseBody = response.bodyAsText()
            Log.d(TAG, "getProject - Response Body: $responseBody")

            response.body()
        } catch (e: Exception) {
            Log.e(TAG, "getProject - Error: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }

    suspend fun createProject(dto: CreateProjectDto): ProjectDto = withContext(Dispatchers.IO) {
        val token = getToken()
        Log.d(TAG, "createProject - Request URL: $baseUrl")
        Log.d(TAG, "createProject - Request Body: title=${dto.title}, description=${dto.description}")

        val response = client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $token")
            }
            setBody(dto)
        }

        Log.d(TAG, "createProject - Response Status: ${response.status}")
        val responseBody = response.bodyAsText()
        Log.d(TAG, "createProject - Response Body: $responseBody")

        response.body()
    }

    suspend fun saveProject(project: ProjectDto) = withContext(Dispatchers.IO) {
        val token = getToken()
        val url = "$baseUrl/${project.id}"
        Log.d(TAG, "saveProject - Request URL: $url")
        Log.d(TAG, "saveProject - Request Body: $project")

        val response = client.put(url) {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $token")
            }
            setBody(project)
        }

        Log.d(TAG, "saveProject - Response Status: ${response.status}")
        val responseBody = response.bodyAsText()
        Log.d(TAG, "saveProject - Response Body: $responseBody")
    }

    suspend fun deleteProject(projectId: String) = withContext(Dispatchers.IO) {
        val token = getToken()
        val url = "$baseUrl/$projectId"
        Log.d(TAG, "deleteProject - Request URL: $url")
        Log.d(TAG, "deleteProject - Project ID: $projectId")

        val response = client.delete(url) {
            headers {
                append("Authorization", "Bearer $token")
            }
        }

        Log.d(TAG, "deleteProject - Response Status: ${response.status}")
        val responseBody = response.bodyAsText()
        Log.d(TAG, "deleteProject - Response Body: $responseBody")
    }

    // ==================== FLOWCHART METHODS ====================

    suspend fun getFlowchart(projectId: String): FlowchartDto? = withContext(Dispatchers.IO) {
        try {
            val token = getToken()
            val url = "$baseUrl/$projectId/flowchart"
            Log.d(TAG, "getFlowchart - Request URL: $url")
            Log.d(TAG, "getFlowchart - Project ID: $projectId")

            val response = client.get(url) {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }

            Log.d(TAG, "getFlowchart - Response Status: ${response.status}")
            val responseBody = response.bodyAsText()
            Log.d(TAG, "getFlowchart - Response Body: $responseBody")

            response.body()
        } catch (e: Exception) {
            Log.e(TAG, "getFlowchart - Error: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }

    suspend fun saveFlowchart(flowchart: FlowchartDto) = withContext(Dispatchers.IO) {
        val token = getToken()
        val url = "$baseUrl/${flowchart.projectId}/flowchart"
        Log.d(TAG, "saveFlowchart - Request URL: $url")
        Log.d(TAG, "saveFlowchart - Project ID: ${flowchart.projectId}")
        Log.d(TAG, "saveFlowchart - Nodes Count: ${flowchart.nodes.size}")
        Log.d(TAG, "saveFlowchart - Full Data: ${flowchart.toString()}")

        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $token")
            }
            setBody(flowchart)
        }

        Log.d(TAG, "saveFlowchart - Response Status: ${response.status}")
        val responseBody = response.bodyAsText()
        Log.d(TAG, "saveFlowchart - Response Body: $responseBody")
    }

    // ==================== REFERENCES METHODS ====================

    // Get project references (includes art style and all references)
    suspend fun getReferences(projectId: String): ProjectReferencesDto? = withContext(Dispatchers.IO) {
        try {
            val token = getToken()
            val url = "$baseUrl/$projectId/references"
            Log.d(TAG, "getReferences - Request URL: $url")
            Log.d(TAG, "getReferences - Project ID: $projectId")

            val response = client.get(url) {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }

            Log.d(TAG, "getReferences - Response Status: ${response.status}")
            val responseBody = response.bodyAsText()
            Log.d(TAG, "getReferences - Response Body: $responseBody")

            response.body()
        } catch (e: Exception) {
            Log.e(TAG, "getReferences - Error: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }


    // Add a new reference
    suspend fun addReference(projectId: String, referenceDto: AddReferenceDto): ReferenceDto = withContext(Dispatchers.IO) {
        val token = getToken()
        val url = "$baseUrl/$projectId/references"
        Log.d(TAG, "addReference - Request URL: $url")
        Log.d(TAG, "addReference - Project ID: $projectId")
        Log.d(TAG, "addReference - Reference: name=${referenceDto.name}, type=${referenceDto.type}")

        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $token")
            }
            setBody(referenceDto)
        }

        Log.d(TAG, "addReference - Response Status: ${response.status}")
        val responseBody = response.bodyAsText()
        Log.d(TAG, "addReference - Response Body: $responseBody")

        response.body()
    }

    // Update an existing reference
    suspend fun updateReference(projectId: String, referenceId: String, referenceDto: ReferenceDto) = withContext(Dispatchers.IO) {
        val token = getToken()
        val url = "$baseUrl/$projectId/references/$referenceId"
        Log.d(TAG, "updateReference - Request URL: $url")
        Log.d(TAG, "updateReference - Project ID: $projectId, Reference ID: $referenceId")
        Log.d(TAG, "updateReference - Reference: name=${referenceDto.name}, type=${referenceDto.type}")

        val response = client.put(url) {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $token")
            }
            setBody(referenceDto)
        }

        Log.d(TAG, "updateReference - Response Status: ${response.status}")
        val responseBody = response.bodyAsText()
        Log.d(TAG, "updateReference - Response Body: $responseBody")
    }

    // Delete a reference
    suspend fun deleteReference(projectId: String, referenceId: String) = withContext(Dispatchers.IO) {
        val token = getToken()
        val url = "$baseUrl/$projectId/references/$referenceId"
        Log.d(TAG, "deleteReference - Request URL: $url")
        Log.d(TAG, "deleteReference - Project ID: $projectId, Reference ID: $referenceId")

        val response = client.delete(url) {
            headers {
                append("Authorization", "Bearer $token")
            }
        }

        Log.d(TAG, "deleteReference - Response Status: ${response.status}")
        val responseBody = response.bodyAsText()
        Log.d(TAG, "deleteReference - Response Body: $responseBody")
    }

    // ==================== COMMUNITY METHODS ====================

    suspend fun publishProject(projectId: String): CommunityProjectDto = withContext(Dispatchers.IO) {
        val token = getToken()
        val url = "$communityBaseUrl/publish"
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



    // Add this to StoryProjectRepository class in the REFERENCES METHODS section

    // Update art style for a project
    suspend fun updateArtStyle(projectId: String, dto: UpdateArtStyleDto) = withContext(Dispatchers.IO) {
        val token = getToken()
        val url = "$baseUrl/$projectId/art-style"
        Log.d(TAG, "updateArtStyle - Request URL: $url")
        Log.d(TAG, "updateArtStyle - Project ID: $projectId")
        Log.d(TAG, "updateArtStyle - Art Style: dimension=${dto.artDimension}, style=${dto.artStyle}")

        val response = client.put(url) {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $token")
            }
            setBody(dto)
        }

        Log.d(TAG, "updateArtStyle - Response Status: ${response.status}")
        val responseBody = response.bodyAsText()
        Log.d(TAG, "updateArtStyle - Response Body: $responseBody")
    }



    // Update the function name in the repository to match the ViewModel call
    suspend fun getProjectReferences(projectId: String): ProjectReferencesDto? = withContext(Dispatchers.IO) {
        try {
            val token = getToken()
            val url = "$baseUrl/$projectId/references"
            Log.d(TAG, "getProjectReferences - Request URL: $url")
            Log.d(TAG, "getProjectReferences - Project ID: $projectId")

            val response = client.get(url) {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }

            Log.d(TAG, "getProjectReferences - Response Status: ${response.status}")
            val responseBody = response.bodyAsText()
            Log.d(TAG, "getProjectReferences - Response Body: $responseBody")

            response.body()
        } catch (e: Exception) {
            Log.e(TAG, "getProjectReferences - Error: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }



    @Serializable
    data class GeneratedAssetsDto(
        val imageData: String,
        val modelData: String? = null
    )

    suspend fun generateReferenceAssets(
        projectId: String,
        referenceId: String
    ): GeneratedAssetsDto = withContext(Dispatchers.IO) {
        val token = getToken()
        val url = "$baseUrl/$projectId/references/$referenceId/generate-assets"
        Log.d(TAG, "generateReferenceAssets - Request URL: $url")
        Log.d(TAG, "generateReferenceAssets - Project ID: $projectId, Reference ID: $referenceId")

        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $token")
            }
        }

        Log.d(TAG, "generateReferenceAssets - Response Status: ${response.status}")
        val responseBody = response.bodyAsText()
        Log.d(TAG, "generateReferenceAssets - Response Body (truncated): ${responseBody.take(200)}...")

        response.body()
    }
}