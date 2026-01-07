package com.example.myapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteGame(
    val gameId: Int,
    val name: String,
    val coverUrl: String
)
