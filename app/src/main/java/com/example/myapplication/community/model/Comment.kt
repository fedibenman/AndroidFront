package com.example.myapplication.community.model

data class Comment(
    val _id: String?,
    val content: String?,
    val author: User?,      // 🔥 OBJET USER
    val post: String?,
    val likes: Int? = 0,
    val reactions: List<Reaction>? = emptyList(),
    val replies: List<Comment>? = emptyList(),
    val parentComment: String? = null
)
