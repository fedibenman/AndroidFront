package com.example.myapplication.chat.ui

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.chat.model.ChatMessage
import com.example.myapplication.chat.viewmodel.ChatViewModel
import com.example.myapplication.ui.theme.PixelBlack
import com.example.myapplication.ui.theme.PixelBlue
import com.example.myapplication.ui.theme.PixelGray
import com.example.myapplication.ui.theme.PixelGreen
import com.example.myapplication.ui.theme.PixelWhite
import com.example.myapplication.ui.theme.PressStart
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val currentRoom by viewModel.currentRoom.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val typingUsers by viewModel.typingUsers.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()

    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startRecording()
        }
    }

    val recordingDuration by viewModel.recordingDuration.collectAsState()
    val recordedAudioFile by viewModel.recordedAudioFile.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelWhite)
            .padding(8.dp)
    ) {

        // HEADER
        Text(
            text = currentRoom?.name ?: "Room",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontFamily = PressStart,
            fontSize = 12.sp,
            color = PixelBlack
        )

        Spacer(Modifier.height(8.dp))

        // MESSAGES LIST
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState
        ) {
            items(messages) { msg ->
                MessageItem(msg)
                Spacer(Modifier.height(6.dp))
            }
        }

        if (typingUsers.isNotEmpty()) {
            Text(
                text = "${typingUsers.joinToString()} typing...",
                fontFamily = PressStart,
                fontSize = 10.sp,
                color = PixelBlue,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        // INPUT BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PixelWhite)
                .border(3.dp, PixelBlack)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (isRecording) {
                        viewModel.stopRecording()
                    } else {
                        if (androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.RECORD_AUDIO
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            viewModel.startRecording()
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isRecording) Color.Red else PixelGray,
                        RoundedCornerShape(6.dp)
                    )
                    .border(2.dp, PixelBlack, RoundedCornerShape(6.dp))
            ) {
                Icon(
                    if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = "Record",
                    tint = PixelBlack
                )
            }

            Spacer(Modifier.width(8.dp))

            if (isRecording) {
                // RECORDING TIMER UI
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(PixelGray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .border(1.dp, PixelBlack, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Recording... $recordingDuration",
                        fontFamily = PressStart,
                        fontSize = 12.sp,
                        color = Color.Red
                    )
                }
            } else {
                // TEXT INPUT
                OutlinedTextField(
                    value = messageText,
                    onValueChange = {
                        val wasEmpty = messageText.isEmpty()
                        messageText = it
                        val isEmpty = it.isEmpty()

                        if (wasEmpty && !isEmpty) viewModel.onTyping(true)
                        else if (!wasEmpty && isEmpty) viewModel.onTyping(false)
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text("Type...", fontFamily = PressStart, fontSize = 12.sp)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PixelBlack,
                        unfocusedBorderColor = PixelBlack,
                        cursorColor = PixelBlack
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = PressStart,
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(Modifier.width(8.dp))

            if (recordedAudioFile != null) {
                // PREVIEW MODE: Show Delete and Send buttons
                IconButton(
                    onClick = { viewModel.discardAudio() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Red.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                        .border(2.dp, PixelBlack, RoundedCornerShape(6.dp))
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = PixelBlack
                    )
                }
                
                Spacer(Modifier.width(8.dp))
                
                IconButton(
                    onClick = { viewModel.sendAudio() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(PixelGreen, RoundedCornerShape(6.dp))
                        .border(2.dp, PixelBlack, RoundedCornerShape(6.dp))
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = PixelBlack
                    )
                }
            } else if (!isRecording) {
                // NORMAL MODE: Show Send button for text
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(messageText)
                            messageText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(PixelGreen, RoundedCornerShape(6.dp))
                        .border(2.dp, PixelBlack, RoundedCornerShape(6.dp))
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = PixelBlack
                    )
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: ChatMessage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PixelWhite),
        border = BorderStroke(2.dp, PixelBlack),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Text(
                text = message.senderName,
                fontFamily = PressStart,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PixelBlue
            )

            Spacer(Modifier.height(4.dp))

            if (message.audioUrl != null) {
                // AUDIO MESSAGE
                Spacer(Modifier.height(8.dp))
                AudioPlayer(message.audioUrl, message.duration)
            } else {
                // TEXT MESSAGE
                Text(
                    text = message.content,
                    fontFamily = PressStart,
                    fontSize = 14.sp,
                    color = PixelBlack,
                    lineHeight = 18.sp
                )
            }

            // TRANSCRIPTION (Optional)
            message.transcription?.let { transcription ->
                if (transcription != "Transcription disabled") {
                    Spacer(Modifier.height(4.dp))
                    var show by remember { mutableStateOf(false) }

                    TextButton(onClick = { show = !show }) {
                        Text(
                            if (show) "Hide Text" else "Show Text",
                            fontFamily = PressStart,
                            fontSize = 10.sp,
                            color = PixelBlue
                        )
                    }

                    if (show) {
                        Text(
                            transcription,
                            fontFamily = PressStart,
                            fontSize = 12.sp,
                            color = PixelBlack,
                            modifier = Modifier
                                .background(Color.LightGray.copy(alpha = 0.3f))
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AudioPlayer(url: String, messageDuration: String? = null) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableFloatStateOf(0f) }

    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(url) {
        try {
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener { mp ->
                duration = mp.duration.toFloat()
                mp.setVolume(1.0f, 1.0f)
            }
            mediaPlayer.setOnCompletionListener { 
                isPlaying = false 
                currentPosition = 0f
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        onDispose {
            mediaPlayer.release()
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = mediaPlayer.currentPosition.toFloat()
            kotlinx.coroutines.delay(100)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PixelGray.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .border(2.dp, PixelBlack, RoundedCornerShape(16.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                if (isPlaying) {
                    mediaPlayer.pause()
                    isPlaying = false
                } else {
                    mediaPlayer.start()
                    isPlaying = true
                }
            },
            modifier = Modifier
                .size(40.dp)
                .background(PixelGreen, androidx.compose.foundation.shape.CircleShape)
                .border(2.dp, PixelBlack, androidx.compose.foundation.shape.CircleShape)
        ) {
            Icon(
                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Play/Pause",
                tint = PixelBlack
            )
        }
        
        Spacer(Modifier.width(8.dp))
        
        Slider(
            value = currentPosition,
            onValueChange = { 
                currentPosition = it
                mediaPlayer.seekTo(it.toInt())
            },
            valueRange = 0f..if (duration > 0) duration else 1f,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = PixelBlack,
                activeTrackColor = PixelGreen,
                inactiveTrackColor = PixelGray
            )
        )
        
        Spacer(Modifier.width(8.dp))
        
        // Display countdown: total duration - current position
        val remainingMs = if (duration > 0) duration - currentPosition else 0f
        val minutes = (remainingMs / 1000 / 60).toInt()
        val seconds = (remainingMs / 1000 % 60).toInt()
        Text(
            text = String.format("%02d:%02d", minutes, seconds),
            fontFamily = PressStart,
            fontSize = 10.sp,
            color = PixelBlack
        )
    }
}
