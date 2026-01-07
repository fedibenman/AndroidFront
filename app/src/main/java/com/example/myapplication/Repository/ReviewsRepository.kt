package com.example.myapplication.Repository

import com.example.myapplication.model.ErrorResponse
import com.example.myapplication.model.Review
import com.example.myapplication.network.NetworkClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

@Serializable
data class CreateReviewRequest(
    val gameId: Int,
    val userId: String,
    val rating: Int,
    val text: String
)

@Serializable
data class GameRating(
    val averageRating: Double,
    val totalReviews: Int
)

class ReviewsRepository {
    private val client = NetworkClient.client

    suspend fun getGameReviews(gameId: Int): Result<List<Review>> = try {
        android.util.Log.d("ReviewsRepository", "=== FETCHING REVIEWS FOR GAME $gameId ===")
        val response: HttpResponse = client.get("reviews/game/$gameId")
        
        android.util.Log.d("ReviewsRepository", "Response status: ${response.status}")
        android.util.Log.d("ReviewsRepository", "Response status code: ${response.status.value}")
        
        if (response.status.isSuccess()) {
            val bodyText = response.body<String>()
            android.util.Log.d("ReviewsRepository", "Raw response body: $bodyText")
            
            try {
                val reviews: List<Review> = kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }.decodeFromString(bodyText)
                
                android.util.Log.d("ReviewsRepository", "Successfully parsed ${reviews.size} reviews")
                reviews.forEachIndexed { index, review ->
                    android.util.Log.d("ReviewsRepository", "Review $index: rating=${review.rating}, user=${review.user?.username}")
                }
                Result.success(reviews)
            } catch (e: Exception) {
                android.util.Log.e("ReviewsRepository", "Failed to parse reviews", e)
                Result.failure(Exception("Failed to parse reviews: ${e.message}"))
            }
        } else {
            try {
                val error: ErrorResponse = response.body()
                android.util.Log.e("ReviewsRepository", "Error response: ${error.message}")
                Result.failure(Exception(error.message ?: "Failed to load reviews"))
            } catch (e: Exception) {
                android.util.Log.e("ReviewsRepository", "Failed to parse error", e)
                Result.failure(Exception("Failed to load reviews: ${response.status.value}"))
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("ReviewsRepository", "Exception fetching reviews", e)
        Result.failure(e)
    }

    suspend fun getGameRating(gameId: Int): Result<GameRating> = try {
        val response: HttpResponse = client.get("reviews/game/$gameId/rating")
        
        if (response.status.isSuccess()) {
            val rating: GameRating = response.body()
            Result.success(rating)
        } else {
            try {
                val error: ErrorResponse = response.body()
                Result.failure(Exception(error.message ?: "Failed to load rating"))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to load rating: ${response.status.value}"))
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createReview(gameId: Int, userId: String, rating: Int, text: String): Result<Review> = try {
        val request = CreateReviewRequest(gameId, userId, rating, text)
        val response: HttpResponse = client.post("reviews") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        
        if (response.status.isSuccess()) {
            val review: Review = response.body()
            Result.success(review)
        } else {
            // Try to parse error response
            try {
                val error: ErrorResponse = response.body()
                Result.failure(Exception(error.message ?: "Failed to post review"))
            } catch (e: Exception) {
                // If we can't parse the error, return a generic message
                Result.failure(Exception("Failed to post review: ${response.status.value}"))
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
