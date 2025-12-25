package com.example.myapplication.calls

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import com.example.myapplication.calls.model.CallRequest
import org.json.JSONObject

object CallNotificationManager {
    private const val CALL_CHANNEL_ID = "voice_calls"
    private const val CALL_NOTIFICATION_ID = 1001
    
    fun createCallChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()
            
            val channel = NotificationChannel(
                CALL_CHANNEL_ID,
                "Voice Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming voice call notifications"
                setSound(ringtoneUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showIncomingCallNotification(context: Context, callRequest: CallRequest) {
        createCallChannel(context)
        
        // Create fullscreen intent for IncomingCallActivity
        val fullScreenIntent = Intent(context, IncomingCallActivity::class.java).apply {
            putExtra("callRequest", callRequestToJson(callRequest))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create Answer action
        val answerIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_ANSWER_CALL
            putExtra(CallActionReceiver.EXTRA_CALL_DATA, callRequestToJson(callRequest))
        }
        
        val answerPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create Decline action
        val declineIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_DECLINE_CALL
            putExtra(CallActionReceiver.EXTRA_CALL_DATA, callRequestToJson(callRequest))
        }
        
        val declinePendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val notification = NotificationCompat.Builder(context, CALL_CHANNEL_ID)
            .setContentTitle("${callRequest.callerName} is calling...")
            .setContentText("Incoming voice call")
            .setSmallIcon(R.drawable.ic_call_24)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .setColor(0xFF4CAF50.toInt())
            .addAction(
                R.drawable.ic_call_end,
                "Decline",
                declinePendingIntent
            )
            .addAction(
                R.drawable.ic_call_24,
                "Answer",
                answerPendingIntent
            )
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(CALL_NOTIFICATION_ID, notification)
        
        // Also launch fullscreen activity immediately
        context.startActivity(fullScreenIntent)
    }
    
    fun dismissCallNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(CALL_NOTIFICATION_ID)
    }
    
    private fun callRequestToJson(callRequest: CallRequest): String {
        return JSONObject().apply {
            put("callId", callRequest.callId)
            put("callerId", callRequest.callerId)
            put("callerName", callRequest.callerName)
            put("calleeId", callRequest.calleeId)
            put("calleeName", callRequest.calleeName)
            put("conversationId", callRequest.conversationId)
            put("timestamp", callRequest.timestamp)
        }.toString()
    }
}
