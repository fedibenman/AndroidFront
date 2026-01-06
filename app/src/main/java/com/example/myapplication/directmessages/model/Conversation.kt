package com.example.myapplication.directmessages.model

data class Conversation(
    val _id: String,
    val participants: List<User>,
    val lastMessage: DirectMessage?,
    val updatedAt: String,
    val createdAt: String
)

data class User(
    val _id: String,
    val name: String,
    val email: String
)
