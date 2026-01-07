package com.example.myapplication.Repository

import kotlinx.serialization.Serializable

@Serializable
data class AddToCollectionRequest(
    val gameId: Int,
    val status: String
)

@Serializable
data class ToggleMissionRequest(
    val totalMissions: Int
)
