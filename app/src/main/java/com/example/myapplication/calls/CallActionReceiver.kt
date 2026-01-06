package com.example.myapplication.calls

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.myapplication.calls.model.CallRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.json.JSONObject

class CallActionReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_ANSWER_CALL = "com.example.myapplication.ACTION_ANSWER_CALL"
        const val ACTION_DECLINE_CALL = "com.example.myapplication.ACTION_DECLINE_CALL"
        const val EXTRA_CALL_DATA = "call_data"
        
        private const val TAG = "CallActionReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val callDataJson = intent.getStringExtra(EXTRA_CALL_DATA)
        
        Log.d(TAG, "Received action: $action")
        
        if (callDataJson == null) {
            Log.e(TAG, "No call data provided")
            return
        }
        
        try {
            val callData = JSONObject(callDataJson)
            val callRequest = CallRequest(
                callId = callData.getString("callId"),
                callerId = callData.getString("callerId"),
                callerName = callData.getString("callerName"),
                calleeId = callData.getString("calleeId"),
                calleeName = callData.getString("calleeName"),
                conversationId = callData.getString("conversationId"),
                timestamp = callData.getLong("timestamp")
            )
            
            when (action) {
                ACTION_ANSWER_CALL -> handleAnswerCall(context, callRequest)
                ACTION_DECLINE_CALL -> handleDeclineCall(context, callRequest)
            }
            
            // Dismiss notification
            CallNotificationManager.dismissCallNotification(context)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing call data", e)
        }
    }
    
    private fun handleAnswerCall(context: Context, callRequest: CallRequest) {
        Log.d(TAG, "Answering call from ${callRequest.callerName}")
        
        // Update call state
        CallManager.acceptCall(callRequest)
        
        // Emit socket event for call accepted
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = com.example.myapplication.directmessages.data.DirectMessagesRepository.getInstance()
                repository.acceptCall(callRequest)
            } catch (e: Exception) {
                Log.e(TAG, "Error emitting call-accepted event", e)
            }
        }
        
        // Launch CallActivity with delay to ensure IncomingCallActivity releases audio focus
        CoroutineScope(Dispatchers.Main).launch {
            delay(1500) // Wait 1.5s for ringtone/cleanup (Increased for slow emulators)
            val callIntent = Intent(context, com.example.myapplication.chat.ui.CallActivity::class.java).apply {
                putExtra("userID", callRequest.calleeId)
                putExtra("userName", callRequest.calleeName)
                putExtra("callID", callRequest.conversationId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(callIntent)
        }
    }
    
    private fun handleDeclineCall(context: Context, callRequest: CallRequest) {
        Log.d(TAG, "Declining call from ${callRequest.callerName}")
        
        // Update call state
        CallManager.declineCall()
        
        // Emit socket event for call declined
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = com.example.myapplication.directmessages.data.DirectMessagesRepository.getInstance()
                repository.declineCall(callRequest)
            } catch (e: Exception) {
                Log.e(TAG, "Error emitting call-declined event", e)
            }
        }
    }
}
