package com.example.myapplication.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AIReviewResponse(
    @SerialName("generatedReview") val review: String
)
