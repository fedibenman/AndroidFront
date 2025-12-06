package com.example.myapplication.storyCreator.DTOs

import androidx.compose.ui.geometry.Offset
import kotlinx.serialization.Serializable

@Serializable
data class NodeDto(
    val id: String,
    val type: String, // "Start", "Story", "Decision", "End"
    val text: String,
    val positionX: Float,
    val positionY: Float,
    val imageData: String?,
    val connections: List<String> // List of node IDs this node connects to
)

fun com.example.myapplication.storyCreator.model.FlowNode.toDto(): NodeDto {
    return NodeDto(
        id = this.id,
        type = when (this.type) {
            is com.example.myapplication.storyCreator.model.NodeType.Start -> "Start"
            is com.example.myapplication.storyCreator.model.NodeType.Story -> "Story"
            is com.example.myapplication.storyCreator.model.NodeType.Decision -> "Decision"
            is com.example.myapplication.storyCreator.model.NodeType.End -> "End"
        },
        text = this.text,
        positionX = this.position.x,
        positionY = this.position.y,
        imageData = this.imageData,
        connections = this.outs.toList()
    )
}

fun NodeDto.toFlowNode(): com.example.myapplication.storyCreator.model.FlowNode {
    val nodeType = when (this.type) {
        "Start" -> com.example.myapplication.storyCreator.model.NodeType.Start
        "Story" -> com.example.myapplication.storyCreator.model.NodeType.Story
        "Decision" -> com.example.myapplication.storyCreator.model.NodeType.Decision
        "End" -> com.example.myapplication.storyCreator.model.NodeType.End
        else -> com.example.myapplication.storyCreator.model.NodeType.Story
    }

    return com.example.myapplication.storyCreator.model.FlowNode(
        id = this.id,
        type = nodeType,
        text = this.text,
        position = Offset(this.positionX, this.positionY),
        imageData = this.imageData ?: ""
    ).apply {
        this.outs.addAll(connections)
    }
}