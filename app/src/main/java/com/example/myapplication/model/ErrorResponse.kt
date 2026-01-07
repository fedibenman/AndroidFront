package com.example.myapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val statusCode: Int? = null,
    val message: String? = null,
    val error: String? = null
)
