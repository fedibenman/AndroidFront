package com.example.myapplication.Repository

import com.example.myapplication.model.CollectionItem
import com.example.myapplication.model.MissionProgress
import com.example.myapplication.network.NetworkClient
import com.example.myapplication.Repository.AddToCollectionRequest
import com.example.myapplication.Repository.ToggleMissionRequest
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody




class CollectionRepository {
    private val client = NetworkClient.client

    suspend fun addToCollection(userId: String, gameId: Int, status: String = "playing"): Result<Unit> = try {
        client.post("user/$userId/collection") {
            setBody(AddToCollectionRequest(gameId, status))
        }
        Result.success(Unit)
    } catch (e: Exception) {
        android.util.Log.e("CollectionRepository", "Error adding to collection", e)
        Result.failure(e)
    }

    suspend fun getCollection(userId: String): Result<List<CollectionItem>> = try {
        val response: List<CollectionItem> = client.get("user/$userId/collection").body()
        Result.success(response)
    } catch (e: Exception) {
        android.util.Log.e("CollectionRepository", "Error fetching collection", e)
        Result.failure(e)
    }

    suspend fun getMissionProgress(userId: String, gameId: Int): Result<MissionProgress> = try {
        val response: MissionProgress = client.get("user/$userId/collection/$gameId/missions/progress").body()
        Result.success(response)
    } catch (e: Exception) {
        android.util.Log.e("CollectionRepository", "Error fetching mission progress", e)
        Result.failure(e)
    }

    suspend fun toggleMission(userId: String, gameId: Int, missionNumber: Int, totalMissions: Int): Result<MissionProgress> = try {
        val response: MissionProgress = client.patch("user/$userId/collection/$gameId/missions/$missionNumber/toggle") {
            setBody(ToggleMissionRequest(totalMissions))
        }.body()
        Result.success(response)
    } catch (e: Exception) {
        android.util.Log.e("CollectionRepository", "Error toggling mission", e)
        Result.failure(e)
    }
}
