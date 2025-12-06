package com.example.myapplication.storyCreator.DTOs

import androidx.compose.ui.geometry.Offset
import com.example.myapplication.storyCreator.model.FlowNode
import com.example.myapplication.storyCreator.model.FlowchartState
import com.example.myapplication.storyCreator.model.NodeType
import kotlinx.serialization.Serializable

@Serializable
data class FlowchartDto(
    val projectId: String,
    val nodes: List<NodeDto>,
    val updatedAt: Long
)


fun FlowchartDto.toFlowchartState(): FlowchartState {
    val nodes = this.nodes.map { nodeDto ->
        FlowNode(
            id = nodeDto.id,
            type = when (nodeDto.type) {
                "Start" -> NodeType.Start
                "Story" -> NodeType.Story
                "Decision" -> NodeType.Decision
                "End" -> NodeType.End
                else -> NodeType.Story
            },
            text = nodeDto.text,
            position = Offset(
                nodeDto.positionX.toFloat(),
                nodeDto.positionY.toFloat()
            ),
            imageData = nodeDto.imageData.toString(),
        )
    }

    return FlowchartState(
        nodes = androidx.compose.runtime.mutableStateListOf(*nodes.toTypedArray())
    )
}