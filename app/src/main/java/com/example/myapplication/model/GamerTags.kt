package com.example.myapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class GamerTags(
    val psn: String? = null,
    val xbox: String? = null,
    val steam: String? = null,
    val discord: String? = null,
    val nintendo: String? = null
)
