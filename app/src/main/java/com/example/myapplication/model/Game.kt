package com.example.myapplication.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Game(
    val id: Int,
    val name: String,
    val summary: String? = null,
    val rating: Double? = null,
    val cover: Cover? = null,
    val genres: List<Genre>? = null,
    @SerialName("first_release_date") val firstReleaseDate: Double? = null,
    val description: String? = null,
    val screenshots: List<String>? = null,
    val trailers: List<String>? = null
)

@Serializable
data class Cover(
    val id: Int? = null,
    val url: String? = null
)

@Serializable
data class Genre(
    val id: Int? = null,  // Made optional - backend may return null
    val name: String
)
