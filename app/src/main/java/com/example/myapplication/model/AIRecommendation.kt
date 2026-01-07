package com.example.myapplication.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AIRecommendation(
    @SerialName("game") val recommendation: Game,
    val reason: String,
    val score: Double,
    @SerialName("factors") val keyFactors: List<String>
)
