package com.example.myapplication.DTOs

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val name: String? = null,
    val email: String
)
