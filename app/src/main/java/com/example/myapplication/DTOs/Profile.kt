package com.example.myapplication.DTOs

import com.example.myapplication.model.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    @SerialName("_id") val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val gamerTags: GamerTags? = null,
    val playStyles: List<String>? = null,
    val availability: String? = null,
    val languages: List<String>? = null,
    val favoriteGame: FavoriteGame? = null,
    val stats: UserStats? = null,
    val recentActivity: List<RecentActivity>? = null,
    val achievements: List<String>? = null,
    val bio: String? = null,
    val matchScore: Int? = null,
    val matchTags: List<String>? = null
)
