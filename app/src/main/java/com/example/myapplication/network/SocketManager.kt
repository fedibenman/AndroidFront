package com.example.myapplication.network

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.json.JSONObject
import java.net.URISyntaxException
import com.example.myapplication.network.NetworkClient

object SocketManager {
    private var socket: Socket? = null
    private const val TAG = "SocketManager"

    // Add replay = 1 buffer so late collectors get the last notification
    private val _notificationFlow = MutableSharedFlow<JSONObject>(replay = 1, extraBufferCapacity = 10)
    val notificationFlow: SharedFlow<JSONObject> = _notificationFlow

    fun connect(userId: String) {
        try {
            if (socket?.connected() == true) return

            val opts = IO.Options()
            opts.path = "/socket.io"
            opts.transports = arrayOf("websocket")
            // Namespace must match backend: /notifications
            socket = IO.socket("${NetworkClient.BASE_URL}/notifications", opts)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Connected to Socket.IO")
                // Join room event - matching Gateway logic: client.join(`user_${userId}`)
                // The gateway handles 'joinNotificationRoom'
                socket?.emit("joinNotificationRoom", userId)
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e(TAG, "Connection Error: ${args.firstOrNull()}")
            }

            socket?.on("newNotification") { args ->
                Log.d(TAG, "Received notification event: ${args.firstOrNull()}")
                val data = args.firstOrNull() as? JSONObject
                data?.let {
                    val emitted = _notificationFlow.tryEmit(it)
                    Log.d(TAG, "Notification emitted to flow: $emitted")
                }
            }

            socket?.connect()

        } catch (e: URISyntaxException) {
            Log.e(TAG, "Socket URI Error", e)
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
    }
}
