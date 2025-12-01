package com.example.myapplication.community.model

data class Post(
    val _id: String?,
    val title: String,
    val content: String,
    val author: User?,           // 🔥 OBJET USER
    val comments: List<Comment>, // 🔥 LISTE COMMENT AVEC USER
    val likes: List<String>? = emptyList(),     // 🔥 LISTE DES IDs DES USERS QUI ONT LIKÉ
    val photo: String? = null // 📸 IMAGE URL
)
