package com.example.myapplication.chat.model

data class ChatMessage(
    val _id: String?,
    val senderId: String,
    val senderName: String,
    val content: String,
    val roomId: String,
    val createdAt: String?,
    val audioUrl: String? = null,
    val transcription: String? = null,
    val duration: String? = null
)
