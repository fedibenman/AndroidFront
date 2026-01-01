package com.example.myapplication.storyCreator.DTOs

import kotlinx.serialization.Serializable

@Serializable
data class CreateProjectDto(
    val title: String,
    val description: String
)