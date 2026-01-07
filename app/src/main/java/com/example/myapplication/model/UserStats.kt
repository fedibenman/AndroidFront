package com.example.myapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class UserStats(
    val level: Int = 1,
    val xp: Int = 0,
    val totalGamesPlayed: Int = 0,
    val totalHoursPlayed: Int = 0,
    val averageRating: Double = 0.0
)
