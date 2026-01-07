package com.example.myapplication.community.model

data class Post(
    val _id: String?,
    val title: String,
    val content: String,
    val author: User?,           // 🔥 OBJET USER
    val comments: List<Comment>, // 🔥 LISTE COMMENT AVEC USER
    val likes: List<String> = emptyList(),  // Array of user IDs who liked the post
    val dislikes: List<String> = emptyList(),
    val photo: String? = null,    // 📸 IMAGE URL
    val reactions: List<Reaction> = emptyList()
) {
    // Computed property for like count
    val likeCount: Int get() = likes.size
}

data class Reaction(
    val userId: String,
    val emoji: String
)
