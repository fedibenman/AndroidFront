package com.example.myapplication.storyCreator.model

data class FlowchartState(
    val nodes: MutableList<FlowNode> = mutableListOf()
) {
    fun findNode(id: String) = nodes.firstOrNull { it.id == id }
}