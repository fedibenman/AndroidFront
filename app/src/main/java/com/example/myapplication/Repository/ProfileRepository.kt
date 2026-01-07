package com.example.myapplication.Repository

import com.example.myapplication.DTOs.Profile
import com.example.myapplication.model.FavoriteGame
import com.example.myapplication.model.Game
import com.example.myapplication.model.GamerTags
import com.example.myapplication.network.NetworkClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class ProfileUpdateRequest(
    val bio: String? = null,
    val gamerTags: GamerTags? = null,
    val playStyles: List<String>? = null,
    val availability: String? = null,
    val languages: List<String>? = null,
    val favoriteGame: FavoriteGame? = null
)

class ProfileRepository {
    private val client = NetworkClient.client
    private val baseUrl = NetworkClient.BASE_URL
    
    suspend fun getFullProfile(userId: String, token: String): Profile {
        return client.get("$baseUrl/profile/$userId") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }.body()
    }
    
    suspend fun updateProfile(userId: String, token: String, updates: ProfileUpdateRequest): Profile {
        return client.put("$baseUrl/auth/profile") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
                append(HttpHeaders.ContentType, "application/json")
            }
            setBody(updates)
        }.body()
    }
    
    suspend fun searchGames(query: String, token: String): List<Game> {
        return client.get("$baseUrl/games/search") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
            parameter("q", query)
        }.body()
    }
    
    suspend fun updateGamerTags(userId: String, token: String, tags: GamerTags): Profile {
        return client.put("$baseUrl/profile/$userId/gamer-tags") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
                append(HttpHeaders.ContentType, "application/json")
            }
            setBody(tags)
        }.body()
    }
    
    suspend fun updatePlayStyles(userId: String, token: String, styles: List<String>): Profile {
        return client.put("$baseUrl/profile/$userId/play-styles") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
                append(HttpHeaders.ContentType, "application/json")
            }
            setBody(mapOf("playStyles" to styles))
        }.body()
    }
}
