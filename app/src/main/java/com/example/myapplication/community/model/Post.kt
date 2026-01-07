package com.example.myapplication.community.model

data class Post(
    val _id: String?,
    val title: String,
    val content: String,
    val author: User?,           // 🔥 OBJET USER
    val comments: List<Comment>, // 🔥 LISTE COMMENT AVEC USER
    val likes: List<String> = emptyList(),  // Array of user IDs who liked the post
    val photo: String? = null    // 📸 IMAGE URL
) {
    // Computed property for like count
    val likeCount: Int get() = likes.size
}
