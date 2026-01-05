package com.example.myapplication.calls

import android.content.Context
import android.util.Log
import com.example.myapplication.calls.model.CallRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Singleton to manage call state across the application
 */
object CallManager {
    private val TAG = "CallManager"
    
    private val _activeCall = MutableStateFlow<CallRequest?>(null)
    val activeCall: StateFlow<CallRequest?> = _activeCall
    
    private val _incomingCall = MutableStateFlow<CallRequest?>(null)
    val incomingCall: StateFlow<CallRequest?> = _incomingCall
    
    private val _callStatus = MutableStateFlow<CallStatus>(CallStatus.IDLE)
    val callStatus: StateFlow<CallStatus> = _callStatus
    
    fun setIncomingCall(callRequest: CallRequest) {
        Log.d(TAG, "Incoming call from ${callRequest.callerName}")
        _incomingCall.value = callRequest
        _callStatus.value = CallStatus.INCOMING
    }
    
    fun acceptCall(callRequest: CallRequest) {
        Log.d(TAG, "Call accepted: ${callRequest.callId}")
        _activeCall.value = callRequest
        _incomingCall.value = null
        _callStatus.value = CallStatus.ACTIVE
    }
    
    fun declineCall() {
        Log.d(TAG, "Call declined")
        _incomingCall.value = null
        _callStatus.value = CallStatus.IDLE
    }
    
    fun cancelCall() {
        Log.d(TAG, "Call cancelled")
        _activeCall.value = null
        _incomingCall.value = null
        _callStatus.value = CallStatus.IDLE
    }
    
    fun endCall() {
        Log.d(TAG, "Call ended")
        _activeCall.value = null
        _callStatus.value = CallStatus.IDLE
    }
    
    fun clearIncomingCall() {
        _incomingCall.value = null
        if (_callStatus.value == CallStatus.INCOMING) {
            _callStatus.value = CallStatus.IDLE
        }
    }
    
    fun isInCall(): Boolean {
        return _callStatus.value == CallStatus.ACTIVE || _callStatus.value == CallStatus.INCOMING
    }
}

enum class CallStatus {
    IDLE,
    OUTGOING,
    INCOMING,
    ACTIVE
}
