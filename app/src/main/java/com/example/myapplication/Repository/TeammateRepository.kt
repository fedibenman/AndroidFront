package com.example.myapplication.Repository

import com.example.myapplication.DTOs.Profile
import com.example.myapplication.network.NetworkClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class RecordSwipeRequest(
    val swiperId: String,
    val targetId: String,
    val action: String // "like" or "pass"
)

@Serializable
data class SwipeResult(
    val match: Boolean,
    val matchedUser: Profile? = null
)

class TeammateRepository {
    private val client = NetworkClient.client
    private val baseUrl = NetworkClient.BASE_URL

    /**
     * Get potential teammates to swipe on
     */
    suspend fun getCandidates(userId: String, token: String, limit: Int = 10): List<Profile> {
        return client.get("$baseUrl/matches/candidates/$userId") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
            parameter("limit", limit)
        }.body()
    }

    /**
     * Record a swipe (like or pass)
     */
    suspend fun recordSwipe(swiperId: String, targetId: String, action: String, token: String): SwipeResult {
        return client.post("$baseUrl/matches/swipe") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
                append(HttpHeaders.ContentType, "application/json")
            }
            setBody(RecordSwipeRequest(swiperId, targetId, action))
        }.body()
    }

    /**
     * Get all mutual matches
     */
    suspend fun getMatches(userId: String, token: String): List<Profile> {
        return client.get("$baseUrl/matches/$userId") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }.body()
    }
}
