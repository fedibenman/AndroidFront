package com.example.myapplication.directmessages.model

data class DirectMessage(
    val _id: String,
    val conversationId: String,
    val sender: User,
    val content: String,
    val type: String = "text", // text, audio, image
    val audioUrl: String? = null,
    val imageUrl: String? = null,
    val read: Boolean = false,
    val createdAt: String
)
