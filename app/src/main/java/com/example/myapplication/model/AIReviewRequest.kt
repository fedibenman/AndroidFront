package com.example.myapplication.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AIReviewRequest(
    val gameName: String,
    val rating: Int,
    @SerialName("quickThoughts") val prompt: String
)
