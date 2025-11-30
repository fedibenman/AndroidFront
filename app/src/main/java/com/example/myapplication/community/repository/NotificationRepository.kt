package com.example.myapplication.community.repository

import android.util.Log
import com.example.myapplication.community.data.remote.ApiService
import com.example.myapplication.community.model.Notification
import com.example.myapplication.community.model.NotificationPost
import com.example.myapplication.community.model.NotificationUser
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NotificationRepository {
    private val BASE_URL = "http://10.0.2.2:3001/"
    private val SOCKET_URL = "http://10.0.2.2:3001/notifications"

    private val api: ApiService by lazy {
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
            .create(ApiService::class.java)
    }

    private var socket: Socket? = null

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    init {
        try {
            val options = IO.Options()
            options.path = "/socket.io"
            socket = IO.socket(SOCKET_URL, options)
            setupSocketListeners()
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Socket initialization error", e)
        }
    }

    private fun setupSocketListeners() {
        socket?.on(Socket.EVENT_CONNECT) {
            Log.d("NotificationRepo", "✅ Socket connected successfully")
        }
        
        socket?.on(Socket.EVENT_DISCONNECT) {
            Log.w("NotificationRepo", "⚠️ Socket disconnected")
        }
        
        socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
            Log.e("NotificationRepo", "❌ Socket connection error: ${args.joinToString()}")
        }
        
        socket?.on("newNotification") { args ->
            try {
                if (args.isNotEmpty()) {
                    val data = args[0] as JSONObject
                    Log.d("NotificationRepo", "📩 Received notification: ${data.toString()}")
                    val notification = parseNotification(data)
                    _notifications.value = listOf(notification) + _notifications.value
                    Log.d("NotificationRepo", "✅ Notification parsed and added")
                }
            } catch (e: Exception) {
                Log.e("NotificationRepo", "❌ Error parsing notification: ${e.message}", e)
            }
        }
    }

    private fun parseNotification(data: JSONObject): Notification {
        val fromUserObj = data.optJSONObject("fromUser")
        val fromUser = if (fromUserObj != null) {
            NotificationUser(
                _id = fromUserObj.optString("_id", ""),
                name = fromUserObj.optString("name", "Unknown"),
                email = fromUserObj.optString("email", "")
            )
        } else null
        
        val postObj = data.optJSONObject("postId")
        val post = if (postObj != null) {
            NotificationPost(
                _id = postObj.optString("_id", ""),
                title = postObj.optString("title", "Unknown Post")
            )
        } else null

        return Notification(
            _id = data.getString("_id"),
            type = data.getString("type"),
            fromUser = fromUser,
            toUser = data.getString("toUser"),
            postId = post,
            read = data.optBoolean("read", false),
            createdAt = data.getString("createdAt")
        )
    }

    private var currentUserId: String? = null

    fun connect(userId: String) {
        // If already connected with a different user, disconnect first
        if (currentUserId != null && currentUserId != userId) {
            disconnect()
        }
        
        currentUserId = userId
        
        if (socket?.connected() == false) {
            socket?.connect()
        }
        socket?.emit("joinNotificationRoom", userId)
        Log.d("NotificationRepo", "🔗 Joined notification room for user: $userId")
    }

    fun disconnect() {
        currentUserId?.let { userId ->
            socket?.emit("leaveNotificationRoom", userId)
            Log.d("NotificationRepo", "👋 Left notification room for user: $userId")
        }
        socket?.disconnect()
        currentUserId = null
    }

    suspend fun loadNotifications(userId: String) {
        try {
            val loaded = api.getNotifications(userId)
            _notifications.value = loaded
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error loading notifications", e)
        }
    }

    suspend fun markAsRead(notificationId: String) {
        try {
            api.markNotificationAsRead(notificationId)
            _notifications.value = _notifications.value.map {
                if (it._id == notificationId) it.copy(read = true) else it
            }
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error marking as read", e)
        }
    }
}
