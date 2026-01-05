package com.example.myapplication.directmessages.data

import com.example.myapplication.directmessages.model.Conversation
import com.example.myapplication.directmessages.model.DirectMessage
import com.example.myapplication.directmessages.model.User
import retrofit2.http.*

interface DirectMessagesApiService {
    
    @GET("direct-messages/conversations/{userId}")
    suspend fun getConversations(@Path("userId") userId: String): List<Conversation>
    
    @POST("direct-messages/start")
    suspend fun startConversation(@Body body: Map<String, String>): Conversation
    
    @GET("direct-messages/{conversationId}/messages")
    suspend fun getMessages(
        @Path("conversationId") conversationId: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): List<DirectMessage>
    
    @POST("direct-messages/{conversationId}/send")
    suspend fun sendMessage(
        @Path("conversationId") conversationId: String,
        @Body body: Map<String, String>
    ): DirectMessage
    
    @POST("direct-messages/messages/{messageId}/read")
    suspend fun markAsRead(@Path("messageId") messageId: String): DirectMessage
    
    // User search endpoint
    @GET("user")
    suspend fun getAllUsers(): List<User>
}
