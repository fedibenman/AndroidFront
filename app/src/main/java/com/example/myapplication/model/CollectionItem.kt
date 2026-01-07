package com.example.myapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class CollectionItem(
    val gameId: Int,
    val status: String,
    val addedAt: String? = null,
    val missionProgress: MissionProgress? = null
)
