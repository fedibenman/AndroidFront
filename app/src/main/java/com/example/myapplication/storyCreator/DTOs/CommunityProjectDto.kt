package com.example.myapplication.storyCreator.DTOs

import kotlinx.serialization.Serializable

@Serializable
data class CommunityProjectDto(
    val id: String,
    val title: String,
    val description: String,
    val authorId: String,
    val authorName: String,
    val starCount: Int,
    val forkCount: Int,
    val isStarredByUser: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)