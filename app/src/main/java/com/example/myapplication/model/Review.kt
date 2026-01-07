package com.example.myapplication.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val _id: String,
    val gameId: Int,
    val userId: String,
    val rating: Int,
    val text: String,
    val timestamp: String,
    val user: UserSummary? = null
)

@Serializable
data class UserSummary(
    @SerialName("id") val _id: String,
    @SerialName("name") val username: String,
    val avatar: String? = null
)
