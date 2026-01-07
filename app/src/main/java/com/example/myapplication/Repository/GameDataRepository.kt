package com.example.myapplication.Repository

import com.example.myapplication.model.Game
import com.example.myapplication.model.AIRecommendation
import com.example.myapplication.model.AIReviewRequest
import com.example.myapplication.model.AIReviewResponse
import com.example.myapplication.network.NetworkClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import java.net.URLEncoder

class GameDataRepository {
    private val client = NetworkClient.client

    suspend fun getPopularGames(): Result<List<Game>> = try {
        val response: List<Game> = client.get("games/popular").body()
        Result.success(response)
    } catch (e: Exception) {
        android.util.Log.e("GamesRepository", "Error fetching popular games", e)
        Result.failure(e)
    }

    suspend fun getGamesByGenre(genre: String): Result<List<Game>> = try {
        val encodedGenre = URLEncoder.encode(genre, "UTF-8")
        val response: List<Game> = client.get("games/genre/$encodedGenre").body()
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun searchGames(query: String): Result<List<Game>> = try {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val response: List<Game> = client.get("games/search?q=$encodedQuery").body()
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getGameDetails(id: Int): Result<Game> = try {
        android.util.Log.d("GameDataRepository", "=== FETCHING GAME DETAILS FOR ID: $id ===")
        val response: io.ktor.client.statement.HttpResponse = client.get("games/$id")
        android.util.Log.d("GameDataRepository", "Response status: ${response.status}")
        
        val bodyText = response.body<String>()
        android.util.Log.d("GameDataRepository", "Raw response body: $bodyText")
        
        // Now parse the actual game
        val game: Game = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }.decodeFromString(bodyText)
        
        android.util.Log.d("GameDataRepository", "Parsed game: ${game.name}")
        Result.success(game)
    } catch (e: Exception) {
        android.util.Log.e("GameDataRepository", "Error fetching game details", e)
        Result.failure(e)
    }

    suspend fun getRecommendations(gameId: Int, userId: String): Result<List<Game>> = try {
        val response: List<Game> = client.get("recommendations/$gameId/$userId").body()
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getPersonalizedRecommendations(userId: String): Result<List<AIRecommendation>> = try {
        val response: List<AIRecommendation> = client.get("games/recommendations/$userId").body()
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun generateAIReview(gameName: String, rating: Int, prompt: String? = null): Result<String> = try {
        val response = client.post("reviews/ai-generate") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(AIReviewRequest(gameName, rating, prompt ?: ""))
        }
        val responseBody = response.body<AIReviewResponse>()
        Result.success(responseBody.review)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun trackRecommendationClick(userId: String, sourceGameId: Int, clickedGameId: Int): Result<Unit> = try {
        client.post("recommendations/track") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(mapOf(
                "userId" to userId,
                "sourceGameId" to sourceGameId,
                "clickedGameId" to clickedGameId
            ))
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
