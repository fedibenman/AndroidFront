package com.example.myapplication.calls.model

data class CallRequest(
    val callId: String,
    val callerId: String,
    val callerName: String,
    val calleeId: String,
    val calleeName: String,
    val conversationId: String,
    val timestamp: Long = System.currentTimeMillis()
)
