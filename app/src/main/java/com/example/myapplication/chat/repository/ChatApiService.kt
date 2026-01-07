package com.example.myapplication.chat.repository

import com.example.myapplication.chat.model.ChatMessage
import com.example.myapplication.chat.model.ChatRoom
import okhttp3.MultipartBody
import retrofit2.http.*

interface ChatApiService {
    
    @GET("chat/rooms")
    suspend fun getRooms(): List<ChatRoom>
    
    @POST("chat/rooms")
    suspend fun createRoom(@Body body: Map<String, String>): ChatRoom
    
    @GET("chat/rooms/{roomId}/messages")
    suspend fun getMessages(@Path("roomId") roomId: String): List<ChatMessage>

    @Multipart
    @POST("chat/upload-audio")
    suspend fun uploadAudio(@Part file: MultipartBody.Part): AudioUploadResponse
}

data class AudioUploadResponse(
    val audioUrl: String,
    val transcription: String
)
