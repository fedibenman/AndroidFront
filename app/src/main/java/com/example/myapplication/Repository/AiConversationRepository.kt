package com.example.myapplication.Repository

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

class AiConversationRepository {

    private val client = ApiClient.client
    private val baseUrl = ApiClient.BASE_URL

    // Create a conversation
    suspend fun createConversation(dto: CreateConversationDto): Conversation {
        return client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()
    }

    // Get all conversations for a user
    suspend fun getConversations(userId: String, status: String? = null): List<Conversation> {
        val url = "$baseUrl?userId=$userId" + (status?.let { "&status=$it" } ?: "")
        return client.get(url).body()
    }

    // Create a message
    suspend fun createMessage(conversationId: String, dto: CreateMessageDto): Message {
        return client.post("$baseUrl/$conversationId/messages") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()
    }

    // Get all messages
    suspend fun getMessages(conversationId: String, userId: String): List<Message> {
        return client.get("$baseUrl/$conversationId/messages?userId=$userId").body()
    }
}


object ApiClient {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    const val BASE_URL = "http://192.168.140.182:3001/ai-conversations"
}





@Serializable
data class Conversation(
    val id: String,
    val userId: String,
    val title: String,
    val status: String
)

@Serializable
data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val timestamp: String
)

@Serializable
data class CreateConversationDto(
    val userId: String,
    val title: String
)

@Serializable
data class CreateMessageDto(
    var conversationId: String = "",
    val userId: String,
    val content: String
)
