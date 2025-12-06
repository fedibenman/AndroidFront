package com.example.myapplication.storyCreator.DTOs

import kotlinx.serialization.Serializable

@Serializable
data class ProjectDto(
    val id: String,
    val title: String,
    val description: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isFork: Boolean = false,
    val originalProjectId: String? = null,
    val originalAuthorName: String? = null
)