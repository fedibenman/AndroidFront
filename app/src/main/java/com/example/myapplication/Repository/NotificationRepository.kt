package com.example.myapplication.Repository

import com.example.myapplication.network.NetworkClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import io.ktor.http.ContentType
import kotlinx.serialization.Serializable

@Serializable
data class SwipeResponse(
    val match: Boolean,
    val matchedUser: MatchedUser? = null
)

@Serializable
data class MatchedUser(
    val _id: String,
    val name: String,
    val email: String? = null
)

@Serializable
data class InviteSender(
    val _id: String,
    val name: String,
    val email: String? = null,
    val avatar: String? = null,
    val bio: String? = null,
    val favoriteGame: FavoriteGame? = null
)

@Serializable
data class FavoriteGame(
    val gameId: Int? = null,
    val name: String? = null,
    val cover: GameCover? = null
)

@Serializable
data class GameCover(
    val url: String? = null
)

class NotificationRepository {
    private val client = NetworkClient.client

    /**
     * Accept or decline an invite by recording a swipe
     * @param myUserId The current user's ID
     * @param senderId The user who sent the invite
     * @param action "like" to accept, "pass" to decline
     */
    suspend fun respondToInvite(myUserId: String, senderId: String, action: String): Result<SwipeResponse> = try {
        android.util.Log.d("NotificationRepo", "Responding to invite: $myUserId -> $senderId ($action)")
        val response: SwipeResponse = client.post("matches/swipe") {
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "swiperId" to myUserId,
                "targetId" to senderId,
                "action" to action
            ))
        }.body()
        android.util.Log.d("NotificationRepo", "Response: match=${response.match}")
        Result.success(response)
    } catch (e: Exception) {
        android.util.Log.e("NotificationRepo", "Error responding to invite", e)
        Result.failure(e)
    }

    /**
     * Fetch sender's full profile to display in popup
     */
    suspend fun getUserProfile(userId: String): Result<InviteSender> = try {
        android.util.Log.d("NotificationRepo", "Fetching profile for: $userId")
        val response: InviteSender = client.get("user/$userId").body()
        Result.success(response)
    } catch (e: Exception) {
        android.util.Log.e("NotificationRepo", "Error fetching profile", e)
        Result.failure(e)
    }

    /**
     * Mark notification as read
     */
    suspend fun markNotificationRead(notificationId: String): Result<Unit> = try {
        client.post("notifications/$notificationId/read") {
            contentType(ContentType.Application.Json)
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
