package com.example.myapplication.chat.ui

import android.Manifest
import android.content.AttributionSource
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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

        val userID = intent.getStringExtra("userID") ?: return
        val userName = intent.getStringExtra("userName") ?: return
        val callID = intent.getStringExtra("callID") ?: return

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
    }

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
                // Permissions denied
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
        val appID: Long = 1081952728 
        val appSign = "c416f40482159b1d4ed83f0de382354ae00ddbb2f611ee9d2c641b135855369f"

        val config = ZegoUIKitPrebuiltCallConfig.groupVoiceCall()

        val fragment = ZegoUIKitPrebuiltCallFragment.newInstance(
            appID, appSign, userID, userName, callID, config
        )

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commitNow()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear the keep screen on flag
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
