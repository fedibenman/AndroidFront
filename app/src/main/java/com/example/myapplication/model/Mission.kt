package com.example.myapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class Mission(
    val number: Int,
    val title: String,
    val description: String? = null,
    val objectives: List<String>? = null,
    var isCompleted: Boolean = false
)

@Serializable
data class GameMissions(
    val gameName: String,
    val sourceUrl: String? = null,
    val missions: List<Mission>
)

@Serializable
data class MissionProgress(
    val completedMissions: List<Int>,
    val totalMissions: Int,
    val lastUpdated: String
)
