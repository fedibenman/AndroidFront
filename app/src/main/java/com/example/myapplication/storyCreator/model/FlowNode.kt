package com.example.myapplication.storyCreator.model

import androidx.compose.ui.geometry.Offset
import java.util.UUID


data class FlowNode(
    val id: String = UUID.randomUUID().toString(),
    val type: NodeType,
    var text: String = "",
    var position: Offset = Offset(100f, 100f),
    // outgoing edges -> list of target node ids
    val outs: MutableList<String> = mutableListOf(),
    var imageData : String
)