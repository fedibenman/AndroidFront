package com.example.myapplication.storyCreator.model

sealed class NodeType {
    object Start : NodeType()
    object Story : NodeType()
    object Decision : NodeType()
    object End : NodeType()
}