package com.example.myapplication.directmessages.data

import android.util.Log
import com.example.myapplication.directmessages.model.Conversation
import com.example.myapplication.directmessages.model.DirectMessage
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DirectMessagesRepository private constructor() {
    companion object {
        @Volatile
        private var instance: DirectMessagesRepository? = null

        fun getInstance(): DirectMessagesRepository {
            return instance ?: synchronized(this) {
                instance ?: DirectMessagesRepository().also { instance = it }
            }
        }
    }

    private val BASE_URL = "http://10.0.2.2:3001/"
    
    private val api: DirectMessagesApiService by lazy {
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
            .create(DirectMessagesApiService::class.java)
    }
    
    // Socket.IO
    private var socket: Socket? = null
    
    private val _newMessage = MutableStateFlow<DirectMessage?>(null)
    val newMessage: StateFlow<DirectMessage?> = _newMessage
    
    private val _typingUser = MutableStateFlow<String?>(null)
    val typingUser: StateFlow<String?> = _typingUser
    
    // Call events
    private val _incomingCall = MutableStateFlow<com.example.myapplication.calls.model.CallRequest?>(null)
    val incomingCall: StateFlow<com.example.myapplication.calls.model.CallRequest?> = _incomingCall
    
    private val _callAccepted = MutableStateFlow<String?>(null)
    val callAccepted: StateFlow<String?> = _callAccepted
    
    private val _callDeclined = MutableStateFlow<String?>(null)
    val callDeclined: StateFlow<String?> = _callDeclined
    
    init {
        connectSocket()
    }
    
    private fun connectSocket() {
        try {
            socket = IO.socket(BASE_URL)
            socket?.connect()
            
            socket?.on("new-direct-message") { args ->
                try {
                    if (args.isNotEmpty()) {
                        val data = args[0] as JSONObject
                        Log.d("DirectMessagesRepo", "RAW SOCKET DATA: $data")
                        
                        try {
                            // Parse Sender
                            val senderJson = data.optJSONObject("sender")
                            val sender = if (senderJson != null) {
                                com.example.myapplication.directmessages.model.User(
                                    _id = senderJson.optString("_id", ""),
                                    name = senderJson.optString("name", "Unknown"),
                                    email = senderJson.optString("email", "")
                                )
                            } else {
                                // Fallback if sender is just an ID or missing
                                val senderId = data.optString("sender", "unknown")
                                com.example.myapplication.directmessages.model.User(senderId, "Unknown", "")
                            }
                            
                            // Parse Message
                            val message = DirectMessage(
                                _id = data.optString("_id"),
                                conversationId = data.optString("conversationId"),
                                sender = sender,
                                content = data.optString("content", ""),
                                type = data.optString("type", "text"),
                                audioUrl = if (data.has("audioUrl")) data.optString("audioUrl") else null,
                                imageUrl = if (data.has("imageUrl")) data.optString("imageUrl") else null,
                                read = data.optBoolean("read", false),
                                createdAt = data.optString("createdAt")
                            )
                            
                            Log.d("DirectMessagesRepo", "Parsed Message: ${message.content}, ID: ${message._id}")
                            _newMessage.value = message
                            
                        } catch (e: Exception) {
                            Log.e("DirectMessagesRepo", "Inner Parsing Error", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DirectMessagesRepo", "Critical Socket Error", e)
                }
            }
            
            socket?.on("user-typing-direct") { args ->
                if (args.isNotEmpty()) {
                    val userName = args[0] as String
                    _typingUser.value = userName
                }
            }
            
            // Listen for incoming calls (user-specific event)
            // This will be set up via listenForIncomingCalls(userId) method
            Log.d("DirectMessagesRepo", "Socket connected")
        } catch (e: Exception) {
            Log.e("DirectMessagesRepo", "Socket error", e)
        }
    }
    
    fun joinConversation(conversationId: String) {
        socket?.emit("join-direct-conversation", JSONObject().apply {
            put("conversationId", conversationId)
        })
    }
    
    fun sendTyping(conversationId: String, userName: String) {
        socket?.emit("typing-direct", JSONObject().apply {
            put("conversationId", conversationId)
            put("userName", userName)
        })
    }
    
    suspend fun getConversations(userId: String): List<Conversation> {
        return try {
            api.getConversations(userId)
        } catch (e: Exception) {
            Log.e("DirectMessagesRepo", "Error getting conversations", e)
            emptyList()
        }
    }

    suspend fun getAllUsers(): List<com.example.myapplication.directmessages.model.User> {
        return try {
            api.getAllUsers()
        } catch (e: Exception) {
            Log.e("DirectMessagesRepo", "Error getting all users", e)
            emptyList()
        }
    }
    
    suspend fun startConversation(user1Id: String, user2Id: String): Conversation? {
        return try {
            Log.d("DirectMessagesRepo", "Starting conversation: user1=$user1Id, user2=$user2Id")
            val result = api.startConversation(mapOf("user1Id" to user1Id, "user2Id" to user2Id))
            Log.d("DirectMessagesRepo", "Conversation started successfully: ${result._id}")
            result
        } catch (e: Exception) {
            Log.e("DirectMessagesRepo", "Error starting conversation: ${e.message}", e)
            Log.e("DirectMessagesRepo", "Exception type: ${e.javaClass.simpleName}")
            if (e is retrofit2.HttpException) {
                Log.e("DirectMessagesRepo", "HTTP Error code: ${e.code()}")
                Log.e("DirectMessagesRepo", "HTTP Error body: ${e.response()?.errorBody()?.string()}")
            }
            null
        }
    }
    
    suspend fun getMessages(conversationId: String): List<DirectMessage> {
        return try {
            api.getMessages(conversationId)
        } catch (e: Exception) {
            Log.e("DirectMessagesRepo", "Error getting messages", e)
            emptyList()
        }
    }
    
    fun sendMessage(conversationId: String, senderId: String, content: String) {
        val data = JSONObject().apply {
            put("conversationId", conversationId)
            put("senderId", senderId)
            put("content", content)
            put("type", "text")
        }
        socket?.emit("send-direct-message", data)
    }
    

    
    // ========== VOICE CALL METHODS ==========
    
    fun requestCall(callRequest: com.example.myapplication.calls.model.CallRequest) {
        Log.d("DirectMessagesRepo", "Requesting call: ${callRequest.callId}")
        val data = JSONObject().apply {
            put("callId", callRequest.callId)
            put("callerId", callRequest.callerId)
            put("callerName", callRequest.callerName)
            put("calleeId", callRequest.calleeId)
            put("calleeName", callRequest.calleeName)
            put("conversationId", callRequest.conversationId)
        }
        socket?.emit("call-request", data)
    }
    
    fun acceptCall(callRequest: com.example.myapplication.calls.model.CallRequest) {
        Log.d("DirectMessagesRepo", "Accepting call: ${callRequest.callId}")
        val data = JSONObject().apply {
            put("callId", callRequest.callId)
            put("callerId", callRequest.callerId)
            put("calleeId", callRequest.calleeId)
            put("calleeName", callRequest.calleeName)
        }
        socket?.emit("call-accepted", data)
    }
    
    fun declineCall(callRequest: com.example.myapplication.calls.model.CallRequest) {
        Log.d("DirectMessagesRepo", "Declining call: ${callRequest.callId}")
        val data = JSONObject().apply {
            put("callId", callRequest.callId)
            put("callerId", callRequest.callerId)
            put("calleeId", callRequest.calleeId)
            put("calleeName", callRequest.calleeName)
        }
        socket?.emit("call-declined", data)
    }
    
    fun cancelCall(callId: String, callerId: String, calleeId: String) {
        Log.d("DirectMessagesRepo", "Cancelling call: $callId")
        val data = JSONObject().apply {
            put("callId", callId)
            put("callerId", callerId)
            put("calleeId", calleeId)
        }
        socket?.emit("call-cancelled", data)
    }
    
    fun listenForIncomingCalls(userId: String) {
        Log.d("DirectMessagesRepo", "Setting up call listeners for user: $userId")
        
        // Listen for incoming calls (user-specific event)
        socket?.on("incoming-call-$userId") { args ->
            if (args.isNotEmpty()) {
                try {
                    val data = args[0] as JSONObject
                    val callRequest = com.example.myapplication.calls.model.CallRequest(
                        callId = data.optString("callId"),
                        callerId = data.optString("callerId"),
                        callerName = data.optString("callerName"),
                        calleeId = data.optString("calleeId"),
                        calleeName = data.optString("calleeName"),
                        conversationId = data.optString("conversationId"),
                        timestamp = data.optLong("timestamp", System.currentTimeMillis())
                    )
                    Log.d("DirectMessagesRepo", "Incoming call from ${callRequest.callerName}")
                    _incomingCall.value = callRequest
                } catch (e: Exception) {
                    Log.e("DirectMessagesRepo", "Error parsing incoming call", e)
                }
            }
        }
        
        // Listen for call accepted (user-specific)
        socket?.on("call-accepted-$userId") { args ->
            if (args.isNotEmpty()) {
                try {
                    val data = args[0] as JSONObject
                    Log.d("DirectMessagesRepo", "Call accepted: ${data.optString("callId")}")
                    _callAccepted.value = data.optString("callId")
                } catch (e: Exception) {
                    Log.e("DirectMessagesRepo", "Error parsing call-accepted", e)
                }
            }
        }
        
        // Listen for call declined (user-specific)
        socket?.on("call-declined-$userId") { args ->
            if (args.isNotEmpty()) {
                try {
                    val data = args[0] as JSONObject
                    Log.d("DirectMessagesRepo", "Call declined: ${data.optString("callId")}")
                    _callDeclined.value = data.optString("callId")
                } catch (e: Exception) {
                    Log.e("DirectMessagesRepo", "Error parsing call-declined", e)
                }
            }
        }
        // Listen for call cancelled (user-specific)
        socket?.on("call-cancelled-$userId") { args ->
            if (args.isNotEmpty()) {
                try {
                    val data = args[0] as JSONObject
                    Log.d("DirectMessagesRepo", "Call cancelled: ${data.optString("callId")}")
                    if (_incomingCall.value?.callId == data.optString("callId")) {
                        _incomingCall.value = null
                    }
                } catch (e: Exception) {
                    Log.e("DirectMessagesRepo", "Error parsing call-cancelled", e)
                }
            }
        }
    }
    
    fun disconnect() {
        socket?.disconnect()
    }
}
