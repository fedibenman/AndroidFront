package com.example.myapplication.calls

import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.calls.model.CallRequest
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import org.json.JSONObject

class IncomingCallActivity : ComponentActivity() {
    
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Show on lockscreen and turn screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        
        val callDataJson = intent.getStringExtra("callRequest")
        if (callDataJson == null) {
            finish()
            return
        }
        
        val callRequest = parseCallRequest(callDataJson)
        if (callRequest == null) {
            finish()
            return
        }
        
        // Start ringtone and vibration
        startRingtone()
        startVibration()
        
        setContent {
            MyApplicationTheme {
                IncomingCallScreen(
                    callRequest = callRequest,
                    onAnswer = {
                        stopRingtone()
                        stopVibration()
                        handleAnswer(callRequest)
                    },
                    onDecline = {
                        stopRingtone()
                        stopVibration()
                        handleDecline(callRequest)
                    }
                )
            }
        }
        
        // Auto-dismiss after 30 seconds
        lifecycleScope.launch {
            delay(30000)
            if (!isFinishing) {
                stopRingtone()
                stopVibration()
                handleDecline(callRequest)
            }
        }
    }
    
    private fun startRingtone() {
        try {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneUri)
            ringtone?.play()
        } catch (e: Exception) {
            Log.e("IncomingCallActivity", "Error playing ringtone", e)
        }
    }
    
    private fun stopRingtone() {
        try {
            ringtone?.stop()
        } catch (e: Exception) {
            Log.e("IncomingCallActivity", "Error stopping ringtone", e)
        }
        ringtone = null
        
        // Reset audio mode to ensure Zego can take over properly
        try {
            val audioManager = getSystemService(AUDIO_SERVICE) as android.media.AudioManager
            audioManager.mode = android.media.AudioManager.MODE_NORMAL
        } catch (e: Exception) {
            Log.e("IncomingCallActivity", "Error resetting audio mode", e)
        }
    }
    
    private fun startVibration() {
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 1000, 500, 1000)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }
    
    private fun stopVibration() {
        vibrator?.cancel()
        vibrator = null
    }
    
    private fun handleAnswer(callRequest: CallRequest) {
        // Send answer action via broadcast
        val answerIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_ANSWER_CALL
            putExtra(CallActionReceiver.EXTRA_CALL_DATA, intent.getStringExtra("callRequest"))
        }
        sendBroadcast(answerIntent)
        
        finish()
    }
    
    private fun handleDecline(callRequest: CallRequest) {
        // Send decline action via broadcast
        val declineIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_DECLINE_CALL
            putExtra(CallActionReceiver.EXTRA_CALL_DATA, intent.getStringExtra("callRequest"))
        }
        sendBroadcast(declineIntent)
        
        finish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopRingtone()
        stopVibration()
    }
    
    private fun parseCallRequest(json: String): CallRequest? {
        return try {
            val data = JSONObject(json)
            CallRequest(
                callId = data.getString("callId"),
                callerId = data.getString("callerId"),
                callerName = data.getString("callerName"),
                calleeId = data.getString("calleeId"),
                calleeName = data.getString("calleeName"),
                conversationId = data.getString("conversationId"),
                timestamp = data.getLong("timestamp")
            )
        } catch (e: Exception) {
            Log.e("IncomingCallActivity", "Error parsing call request", e)
            null
        }
    }
}

@Composable
fun IncomingCallScreen(
    callRequest: CallRequest,
    onAnswer: () -> Unit,
    onDecline: () -> Unit
) {
    var isPulsing by remember { mutableStateOf(true) }
    val scale by animateFloatAsState(
        targetValue = if (isPulsing) 1.1f else 1.0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    )
    
    LaunchedEffect(Unit) {
        while (true) {
            isPulsing = !isPulsing
            delay(1000)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Caller Avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .background(PixelBlue, CircleShape)
                    .border(4.dp, PixelWhite, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = PixelWhite,
                    modifier = Modifier.size(64.dp)
                )
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Caller Name
            Text(
                text = callRequest.callerName,
                fontFamily = PressStart,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PixelWhite
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = "Incoming voice call...",
                fontFamily = PressStart,
                fontSize = 12.sp,
                color = PixelWhite.copy(alpha = 0.7f)
            )
            
            Spacer(Modifier.height(64.dp))
            
            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Decline Button
                FloatingActionButton(
                    onClick = onDecline,
                    containerColor = PixelRed,
                    contentColor = PixelWhite,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(72.dp)
                        .border(4.dp, PixelWhite, CircleShape)
                ) {
                    Icon(
                        Icons.Default.CallEnd,
                        contentDescription = "Decline",
                        modifier = Modifier.size(36.dp)
                    )
                }
                
                // Answer Button
                FloatingActionButton(
                    onClick = onAnswer,
                    containerColor = PixelGreen,
                    contentColor = PixelWhite,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(72.dp)
                        .border(4.dp, PixelWhite, CircleShape)
                        .scale(scale)
                ) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = "Answer",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}


