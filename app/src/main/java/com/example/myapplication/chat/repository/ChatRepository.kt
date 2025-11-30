package com.example.myapplication.chat.repository

import android.util.Log
import com.example.myapplication.chat.model.ChatMessage
import com.example.myapplication.chat.model.ChatRoom
import com.example.myapplication.chat.model.Reaction
import com.example.myapplication.chat.model.ReplyInfo
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class ChatRepository {
    private val BASE_URL = "http://10.0.2.2:3001/"
    
    private val api: ChatApiService by lazy {
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatApiService::class.java)
    }
    
    private var socket: Socket? = null
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _typingUsers = MutableStateFlow<Set<String>>(emptySet())
    val typingUsers: StateFlow<Set<String>> = _typingUsers.asStateFlow()
    
    init {
        try {
            socket = IO.socket(BASE_URL)
            setupSocketListeners()
        } catch (e: Exception) {
            Log.e("ChatRepository", "Socket initialization error", e)
        }
    }
    
    private fun setupSocketListeners() {
        socket?.on(Socket.EVENT_CONNECT) {
            Log.d("ChatRepository", "Socket connected")
        }
        socket?.on(Socket.EVENT_DISCONNECT) {
            Log.d("ChatRepository", "Socket disconnected")
        }
        socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
            Log.e("ChatRepository", "Socket connection error: ${args.firstOrNull()}")
        }

        socket?.on("newMessage") { args ->
            try {
                if (args.isNotEmpty()) {
                    val data = args[0] as JSONObject
                    val message = parseMessage(data)
                    _messages.value = _messages.value + message
                }
            } catch (e: Exception) {
                Log.e("ChatRepository", "Error parsing message", e)
            }
        }

        socket?.on("typing") { args ->
            if (args.isNotEmpty()) {
                val userName = args[0] as String
                _typingUsers.value = _typingUsers.value + userName
            }
        }

        socket?.on("stopTyping") {
             _typingUsers.value = emptySet()
        }
        
        socket?.on("messageReactionUpdated") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                val updatedMessage = parseMessage(data)
                val currentMessages = _messages.value.toMutableList()
                val index = currentMessages.indexOfFirst { it._id == updatedMessage._id }
                if (index != -1) {
                    currentMessages[index] = updatedMessage
                    _messages.value = currentMessages
                }
            }
        }
    }
    
    private fun parseMessage(data: JSONObject): ChatMessage {
        val reactionsArray = data.optJSONArray("reactions")
        val reactions = if (reactionsArray != null) {
            (0 until reactionsArray.length()).map { i ->
                val r = reactionsArray.getJSONObject(i)
                Reaction(
                    emoji = r.getString("emoji"),
                    userId = r.getString("userId"),
                    userName = r.getString("userName")
                )
            }
        } else null
        
        val replyToObject = data.optJSONObject("replyTo")
        val replyTo = if (replyToObject != null) {
            ReplyInfo(
                messageId = replyToObject.optString("messageId", ""),
                content = replyToObject.optString("content", ""),
                senderName = replyToObject.optString("senderName", "")
            )
        } else null

        return ChatMessage(
            _id = data.optString("_id"),
            senderId = data.getString("senderId"),
            senderName = data.getString("senderName"),
            content = data.getString("content"),
            roomId = data.getString("roomId"),
            createdAt = data.optString("createdAt"),
            audioUrl = if (data.has("audioUrl")) data.getString("audioUrl") else null,
            transcription = if (data.has("transcription")) data.getString("transcription") else null,
            duration = if (data.has("duration")) data.getString("duration") else null,
            reactions = reactions,
            replyTo = replyTo
        )
    }
    
    fun connect() {
        socket?.connect()
    }
    
    fun joinRoom(roomId: String) {
        socket?.emit("joinRoom", roomId)
    }
    
    fun leaveRoom(roomId: String) {
        socket?.emit("leaveRoom", roomId)
        _messages.value = emptyList()
        _typingUsers.value = emptySet()
    }
    
    fun sendMessage(senderId: String, senderName: String, content: String, roomId: String, audioUrl: String? = null, transcription: String? = null, duration: String? = null, replyTo: ReplyInfo? = null) {
        val data = JSONObject().apply {
            put("senderId", senderId)
            put("senderName", senderName)
            put("content", content)
            put("roomId", roomId)
            if (audioUrl != null) put("audioUrl", audioUrl)
            if (transcription != null) put("transcription", transcription)
            if (duration != null) put("duration", duration)
            if (replyTo != null) {
                val replyObj = JSONObject().apply {
                    put("messageId", replyTo.messageId)
                    put("content", replyTo.content)
                    put("senderName", replyTo.senderName)
                }
                put("replyTo", replyObj)
            }
        }
        socket?.emit("sendMessage", data)
    }

    suspend fun uploadAudio(file: File): AudioUploadResponse? {
        return try {
            val requestFile = RequestBody.create("audio/*".toMediaTypeOrNull(), file)
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            api.uploadAudio(body)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error uploading audio", e)
            null
        }
    }

    fun sendTyping(roomId: String, userName: String) {
        val data = JSONObject().apply {
            put("roomId", roomId)
            put("userName", userName)
        }
        socket?.emit("typing", data)
    }

    fun sendStopTyping(roomId: String) {
        val data = JSONObject().apply {
            put("roomId", roomId)
        }
        socket?.emit("stopTyping", data)
    }
    
    fun addReaction(messageId: String, emoji: String, userId: String, userName: String, roomId: String) {
        val data = JSONObject().apply {
            put("messageId", messageId)
            put("emoji", emoji)
            put("userId", userId)
            put("userName", userName)
            put("roomId", roomId)
        }
        socket?.emit("addReaction", data)
    }
    
    fun removeReaction(messageId: String, emoji: String, userId: String, roomId: String) {
        val data = JSONObject().apply {
            put("messageId", messageId)
            put("emoji", emoji)
            put("userId", userId)
            put("roomId", roomId)
        }
        socket?.emit("removeReaction", data)
    }
    
    suspend fun getRooms(): List<ChatRoom> {
        return try {
            api.getRooms()
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching rooms", e)
            emptyList()
        }
    }
    
    suspend fun createRoom(name: String, description: String): ChatRoom? {
        return try {
            api.createRoom(mapOf("name" to name, "description" to description))
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error creating room", e)
            null
        }
    }
    
    suspend fun loadMessages(roomId: String) {
        try {
            val loadedMessages = api.getMessages(roomId)
            _messages.value = loadedMessages
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error loading messages", e)
        }
    }
    
    fun disconnect() {
        socket?.disconnect()
    }
}
