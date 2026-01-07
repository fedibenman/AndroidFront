package com.example.myapplication.Repository

import com.example.myapplication.model.ErrorResponse
import com.example.myapplication.model.GameMissions
import com.example.myapplication.network.NetworkClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import java.net.URLEncoder

class MissionsRepository {
    private val client = NetworkClient.client

    suspend fun fetchMissions(gameId: Int, gameName: String): Result<GameMissions> = try {
        val encodedName = URLEncoder.encode(gameName, "UTF-8")
        val response: HttpResponse = client.get("games/$gameId/missions?gameName=$encodedName")
        
        if (response.status.isSuccess()) {
            val missions: GameMissions = response.body()
            Result.success(missions)
        } else {
            // Try to parse error response
            try {
                val error: ErrorResponse = response.body()
                Result.failure(Exception(error.message ?: "Failed to load missions"))
            } catch (e: Exception) {
                // If we can't parse the error, return a generic message
                Result.failure(Exception("Failed to load missions: ${response.status.value}"))
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
