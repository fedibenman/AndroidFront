package com.example.myapplication.Repository

import com.example.myapplication.network.NetworkClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class NearbyUser(
    val id: String,
    val name: String,
    val avatar: String? = null,
    val favoriteGame: FavoriteGameInfo? = null,
    val playStyles: List<String>? = null,
    val bio: String? = null,
    val distance: Int // in km
)

@Serializable
data class FavoriteGameInfo(
    val gameId: Int,
    val name: String,
    val coverUrl: String
)

@Serializable
data class NearbyUsersResponse(
    val users: List<NearbyUser>,
    val range: Int? = null,
    val message: String? = null
)

@Serializable
data class LocationUpdateResponse(
    val success: Boolean
)

@Serializable
data class SearchStatusResponse(
    val success: Boolean,
    val searching: Boolean? = null
)

// Request body classes for proper serialization
@Serializable
data class LocationUpdateRequest(
    val userId: String,
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class StartSearchRequest(
    val userId: String,
    val range: Int
)

@Serializable
data class StopSearchRequest(
    val userId: String
)

class NearbyRepository {
    private val client = NetworkClient.client
    private val baseUrl = NetworkClient.BASE_URL

    /**
     * Update user's location for nearby discovery
     */
    suspend fun updateLocation(userId: String, latitude: Double, longitude: Double): Result<LocationUpdateResponse> {
        return try {
            val response = client.post("$baseUrl/matches/nearby/update-location") {
                contentType(ContentType.Application.Json)
                setBody(LocationUpdateRequest(userId, latitude, longitude))
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Start searching for nearby teammates
     */
    suspend fun startNearbySearch(userId: String, range: Int = 50): Result<SearchStatusResponse> {
        return try {
            val response = client.post("$baseUrl/matches/nearby/start-search") {
                contentType(ContentType.Application.Json)
                setBody(StartSearchRequest(userId, range))
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Stop searching for nearby teammates
     */
    suspend fun stopNearbySearch(userId: String): Result<SearchStatusResponse> {
        return try {
            val response = client.post("$baseUrl/matches/nearby/stop-search") {
                contentType(ContentType.Application.Json)
                setBody(StopSearchRequest(userId))
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get nearby users that are actively searching
     * @param gameId Optional filter to show only users playing a specific game
     */
    suspend fun getNearbyUsers(userId: String, range: Int? = null, gameId: Int? = null): Result<NearbyUsersResponse> {
        return try {
            val params = mutableListOf<String>()
            if (range != null) params.add("range=$range")
            if (gameId != null) params.add("gameId=$gameId")
            
            val url = if (params.isNotEmpty()) {
                "$baseUrl/matches/nearby/users/$userId?${params.joinToString("&")}"
            } else {
                "$baseUrl/matches/nearby/users/$userId"
            }
            android.util.Log.d("NearbyRepo", "GET Request: $url (gameId=$gameId)")
            val response = client.get(url)
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    /**
     * Send a game invite
     */
    suspend fun sendInvite(fromUserId: String, toUserId: String, gameId: Int): Result<Boolean> {
        android.util.Log.d("NearbyRepo", "Sending invite: $fromUserId -> $toUserId (Game: $gameId)")
        return try {
            val response = client.post("$baseUrl/notifications/invite") {
                contentType(ContentType.Application.Json)
                setBody(InviteRequest(fromUserId, toUserId, gameId))
            }
            android.util.Log.d("NearbyRepo", "Invite response: ${response.status}")
            if (response.status.value in 200..299) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to send invite"))
            }
        } catch (e: Exception) {
            android.util.Log.e("NearbyRepo", "Error sending invite", e)
            Result.failure(e)
        }
    }

    /**
     * Get user profile
     */
    suspend fun getUserProfile(userId: String): Result<UserProfile> {
        return try {
            val response = client.get("$baseUrl/user/$userId")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Serializable
data class InviteRequest(
    val fromUserId: String,
    val toUserId: String,
    val gameId: Int
)

@Serializable
data class UserProfile(
    @kotlinx.serialization.SerialName("_id") val id: String,
    val name: String,
    val avatar: String? = null,
    val bio: String? = null,
    val favoriteGame: FavoriteGameInfo? = null,
    val playStyles: List<String>? = null,
    val stats: UserStats? = null
)

@Serializable
data class UserStats(
    val level: Int = 1,
    val xp: Int = 0,
    val totalGamesPlayed: Int = 0
)
