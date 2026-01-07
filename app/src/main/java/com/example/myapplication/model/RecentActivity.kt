package com.example.myapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class RecentActivity(
    val type: String,
    val gameId: Int,
    val gameName: String,
    val gameCover: String,
    val timestamp: Long,
    val details: String? = null
)
