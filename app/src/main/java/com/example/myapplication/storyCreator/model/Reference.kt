package com.example.myapplication.storyCreator.model

data class Reference(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: ReferenceType,
    val name: String,
    val lore: String,
    val design: String,
    var imageData: String? = null,
    var modelData: String? = null
)