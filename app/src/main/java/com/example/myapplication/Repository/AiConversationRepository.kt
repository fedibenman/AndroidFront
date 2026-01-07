package com.example.myapplication.Repository

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AiConversationRepository {
    private val client = ApiClient.client
    private val baseUrl = ApiClient.BASE_URL + "/ai-conversations"








    suspend fun createNewConversationOnServer(dto: CreateConversationDto, token: String): Conversation {
        // Send POST request to /ai-conversation endpoint
        return client.post("$baseUrl") {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $token")
            }
            setBody(dto)
        }.body()
    }

    // Get all conversations for a user using auth token
    suspend fun getConversations(token: String): List<Conversation> {
        Log.e("Error", "got the conversations")

        val url = "$baseUrl"

        return client.get(url) {
            headers {
                append("Authorization", "Bearer $token")
            }
        }.body()
    }


    // Create a message
    suspend fun createMessage(conversationId: String, dto: CreateMessageDto, token: String): Message {
        Log.e("sending" , dto.toString())
        return client.post("$baseUrl/$conversationId/messages") {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $token")
            }
            setBody(dto)
        }.body()
    }

    // Get all messages for a conversation
    suspend fun getMessages(conversationId: String,  token: String): List<Message> {
        return client.get("$baseUrl/$conversationId/messages") {
            headers {
                append("Authorization", "Bearer $token")
            }
        }.body()
    }

    // Edit a message
    suspend fun editMessage(messageId: String,  dto : EditMessageDto,token: String): Message {
        try {
            val url = "$baseUrl/messages/$messageId"
            Log.d("AiConversationRepository", "Making PUT request to URL: $url")

            // Create a proper data class for the request body, similar to CreateMessageDto

            Log.d("AiConversationRepository", "Request body: $dto")

            val response = client.put(url) {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $token")
                }
                setBody(dto)
            }

            // Log the response body to see what the server actually returns
            val responseText = response.bodyAsText()
            Log.d("AiConversationRepository", "Message update response body: $responseText")

            val result = response.body<Message>()
            Log.d("AiConversationRepository", "Edit message successful: ${result.id}")
            return result
        } catch (e: Exception) {
            Log.e("AiConversationRepository", "Error editing message messageId=$messageId", e)
            throw e
        }
    }


    // Delete a message
    suspend fun deleteMessage(messageId: String, token: String) {
        Log.d("AiConversationRepository", "Starting deleteMessage: messageId=$messageId")
        try {
            val url = "$baseUrl/messages/$messageId"
            Log.d("AiConversationRepository", "Making DELETE request to URL: $url")

            val response = client.delete(url) {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }

            // Log the response to confirm deletion
            val responseText = response.bodyAsText()
            Log.d("AiConversationRepository", "Message deletion response: $responseText")
            Log.d("AiConversationRepository", "Message deleted successfully: $messageId")
        } catch (e: Exception) {
            Log.e("AiConversationRepository", "Error deleting message messageId=$messageId", e)
            throw e
        }
    }

    // Edit a conversation title
    suspend fun editConversation(conversationId: String, title: String, token: String): Conversation {
        Log.d("AiConversationRepository", "Starting editConversation: conversationId=$conversationId, title=$title")
        try {
            val url = "$baseUrl/$conversationId"
            Log.d("AiConversationRepository", "Making PUT request to URL: $url")

            val requestBody = mapOf("title" to title)
            Log.d("AiConversationRepository", "Request body: $requestBody")

            val response = client.put(url) {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $token")
                }
                setBody(requestBody)
            }

            // Log the response body to see what the server actually returns
            val responseText = response.bodyAsText()
            Log.d("AiConversationRepository", "Response body: $responseText")

            val result = response.body<Conversation>()
            Log.d("AiConversationRepository", "Edit conversation successful: ${result.id}")
            return result
        } catch (e: Exception) {
            Log.e("AiConversationRepository", "Error editing conversation conversationId=$conversationId", e)
            throw e
        }
    }

    // Delete a conversation
    suspend fun deleteConversation(conversationId: String, token: String) {
        client.delete("$baseUrl/$conversationId") {
            headers {
                append("Authorization", "Bearer $token")
            }
        }
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
        install(HttpTimeout) {
            requestTimeoutMillis = 900000  // 30 seconds
            connectTimeoutMillis = 900000  // 10 seconds
            socketTimeoutMillis = 900000   // 30 seconds
        }
    }


    const val BASE_URL = "http://10.0.2.2:3001"

}





@Serializable
data class Conversation(
    @SerialName("_id") val id: String,
    val title: String,
    @SerialName("userId") val userId: String,
    val messages: List<String>? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @SerialName("__v") val v: Int? = null
)


@Serializable
data class CreateConversationDto(
    val title: String
)



@Serializable
data class Message(
    @SerialName("id") val id: String  ,
    @SerialName("conversationId") val conversationId: String,
    val sender: String = "sender",
    val content: String,
    val timestamp: String? = null,
    val images : List<ImageData> = emptyList()
)


@Serializable
data class CreateMessageDto(
    val content: String,
    val sender: String,
    val conversationId: String,
    val images: List<ImageData> = emptyList() // Additional field for images
)

@Serializable
data class EditMessageDto(
    val content: String,
    val images: List<ImageData> = emptyList() // Additional field for images
)

@Serializable
data class ImageData(
    val base64: String,
    val mimeType: String,
    val fileName: String
)
