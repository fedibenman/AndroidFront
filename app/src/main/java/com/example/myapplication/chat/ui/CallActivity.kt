package com.example.myapplication.chat.ui

import android.Manifest
import android.content.AttributionSource
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment
import com.example.myapplication.R
import java.util.Collections
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.myapplication.directmessages.data.DirectMessagesRepository

class CallActivity : AppCompatActivity() {

    private var pendingUserID: String? = null
    private var pendingUserName: String? = null
    private var pendingCallID: String? = null

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        // Keep screen on during call to prevent audio issues
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val userID = intent.getStringExtra("userID")
        val userName = intent.getStringExtra("userName")
        val callID = intent.getStringExtra("callID")

        Log.d("CallActivity", "onCreate - Received params: userID=$userID, userName=$userName, callID=$callID")

        if (userID == null || userName == null || callID == null) {
            Log.e("CallActivity", "Missing required parameters")
            Toast.makeText(this, "Missing call parameters", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Store the call details
        pendingUserID = userID
        pendingUserName = userName
        pendingCallID = callID

        // Check and request permissions
        if (savedInstanceState == null) {
            if (hasRequiredPermissions()) {
                addCallFragment(userID, userName, callID)
            } else {
                requestPermissions()
            }
        }

        lifecycleScope.launch {
            DirectMessagesRepository.getInstance().callDeclined.collect { declinedCallId ->
                if (declinedCallId != null && declinedCallId == pendingCallID) {
                    Log.d("CallActivity", "Call declined: $declinedCallId")
                    Toast.makeText(this@CallActivity, "Call declined", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    // ... (rest of methods)

    private fun createAttributedContext(): Context {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            createAttributionContext("voice_call")
        } else {
            this
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            REQUIRED_PERMISSIONS,
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // All permissions granted, proceed with call
                pendingUserID?.let { userID ->
                    pendingUserName?.let { userName ->
                        pendingCallID?.let { callID ->
                            addCallFragment(userID, userName, callID)
                        }
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "Microphone and camera permissions are required for voice calls",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    private fun addCallFragment(userID: String, userName: String, callID: String) {
        Log.d("CallActivity", "addCallFragment - userID=$userID, userName=$userName, callID=$callID")
        
        val appID: Long = 1060413186 
        val appSign = "96dfce239aee51e875821e2117adaeb76d48d2d610587167d0be4f9d711ad8eb"

        val config = ZegoUIKitPrebuiltCallConfig.groupVoiceCall()
        config.turnOnCameraWhenJoining = false
        config.turnOnMicrophoneWhenJoining = true
        config.audioVideoViewConfig.useVideoViewAspectFill = true

        val fragment = ZegoUIKitPrebuiltCallFragment.newInstance(
            appID, appSign, userID, userName, callID, config
        )

        try {
            Log.d("CallActivity", "Adding ZegoCloud fragment to view")
           supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit() // Changed from commitNow() to avoid lifecycle race conditions
            Log.d("CallActivity", "ZegoCloud fragment added successfully")
        } catch (e: Exception) {
            Log.e("CallActivity", "Error adding fragment", e)
            Toast.makeText(this, "Failed to start call: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
