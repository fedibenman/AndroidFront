package com.example.myapplication.community.model

data class Notification(
    val _id: String,
    val type: String, // "LIKE" or "COMMENT"
    val fromUser: NotificationUser?,
    val toUser: String,
    val postId: NotificationPost?,
    val read: Boolean,
    val createdAt: String,
    val emoji: String? = null // For REACT notifications
)

data class NotificationUser(
    val _id: String,
    val name: String,
    val email: String
)

data class NotificationPost(
    val _id: String,
    val title: String
)
