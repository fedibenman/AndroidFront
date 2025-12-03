package com.example.myapplication.chat.ui

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.chat.model.ChatMessage
import com.example.myapplication.chat.viewmodel.ChatViewModel
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/* -------------------------------------------------------------
   CHAT SCREEN
------------------------------------------------------------- */

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val currentRoom by viewModel.currentRoom.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val typingUsers by viewModel.typingUsers.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val replyingTo by viewModel.replyingToMessage.collectAsState()

    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val context = LocalContext.current

    val permissionLauncher =
        rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
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

        /* ---------- HEADER ---------- */
        /* ---------- HEADER ---------- */
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Spacer to balance the row if we want the title centered, or just put title on left/center
            // Let's use a Box to center the title and put the button on the right
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = currentRoom?.name ?: "Room",
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center,
                    fontFamily = PressStart,
                    fontSize = 12.sp,
                    color = PixelBlack
                )

                IconButton(
                    onClick = {
                        val userId = viewModel.currentUserIdFlow.value
                        val userName = viewModel.currentUserName.value
                        val roomId = currentRoom?._id

                        if (userId != null && roomId != null) {
                            val intent = android.content.Intent(context, com.example.myapplication.chat.ui.CallActivity::class.java).apply {
                                putExtra("userID", userId)
                                putExtra("userName", userName)
                                putExtra("callID", roomId)
                            }
                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Start Call",
                        tint = PixelBlack
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        /* ---------- MESSAGES LIST ---------- */
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState
        ) {
            items(messages) { msg ->
                MessageItem(message = msg, viewModel = viewModel)
                Spacer(Modifier.height(6.dp))
            }
        }

        /* ---------- TYPING ---------- */
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

        /* ---------- REPLYING UI ---------- */
        replyingTo?.let { reply ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PixelGray.copy(alpha = 0.2f))
                    .border(1.dp, PixelBlack)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Replying to ${reply.senderName}",
                        fontFamily = PressStart,
                        fontSize = 10.sp,
                        color = PixelBlue
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (reply.audioUrl != null) "Voice Message" else reply.content,
                        fontFamily = PressStart,
                        fontSize = 10.sp,
                        color = PixelBlack,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = { viewModel.cancelReply() }) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = PixelBlack)
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        /* ---------- INPUT BAR ---------- */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PixelWhite)
                .border(3.dp, PixelBlack)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            /* ---- Record button ---- */
            IconButton(
                onClick = {
                    if (isRecording) {
                        viewModel.stopRecording()
                    } else {
                        val perm = android.Manifest.permission.RECORD_AUDIO
                        val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                            context, perm
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                        if (granted) viewModel.startRecording()
                        else permissionLauncher.launch(perm)
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
                    contentDescription = null,
                    tint = PixelBlack
                )
            }

            Spacer(Modifier.width(8.dp))

            /* ---- Recording Timer OR Text Input ---- */
            if (isRecording) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(PixelGray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .border(1.dp, PixelBlack, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Recording... $recordingDuration",
                        fontFamily = PressStart,
                        fontSize = 12.sp,
                        color = Color.Red
                    )
                }
            } else {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = {
                        val wasEmpty = messageText.isEmpty()
                        messageText = it
                        if (wasEmpty && it.isNotEmpty()) viewModel.onTyping(true)
                        else if (it.isEmpty()) viewModel.onTyping(false)
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

            /* ---- SEND / DELETE ---- */
            when {
                recordedAudioFile != null -> {
                    IconButton(
                        onClick = { viewModel.discardAudio() },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Red.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                            .border(2.dp, PixelBlack, RoundedCornerShape(6.dp))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = PixelBlack)
                    }

                    Spacer(Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.sendAudio() },
                        modifier = Modifier
                            .size(48.dp)
                            .background(PixelGreen, RoundedCornerShape(6.dp))
                            .border(2.dp, PixelBlack, RoundedCornerShape(6.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = PixelBlack)
                    }
                }

                !isRecording -> {
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
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = PixelBlack)
                    }
                }
            }
        }
    }
}


/* -------------------------------------------------------------
   MESSAGE ITEM
------------------------------------------------------------- */

@Composable
fun MessageItem(message: ChatMessage, viewModel: ChatViewModel) {
    var showReactionPicker by remember { mutableStateOf(false) }
    val currentUserId by viewModel.currentUserIdFlow.collectAsState(initial = null)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = { showReactionPicker = true }
            ),
        colors = CardDefaults.cardColors(containerColor = PixelWhite),
        border = BorderStroke(2.dp, PixelBlack),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            /* ---------- QUOTED MESSAGE ---------- */
            message.replyTo?.let { reply ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .border(1.dp, PixelGray, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            reply.senderName,
                            fontFamily = PressStart,
                            fontSize = 10.sp,
                            color = PixelBlue
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            reply.content,
                            fontFamily = PressStart,
                            fontSize = 10.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = PixelBlack
                        )
                    }
                }
            }

            /* ---------- NAME ---------- */
            Text(
                message.senderName,
                fontFamily = PressStart,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PixelBlue
            )

            Spacer(Modifier.height(4.dp))

            /* ---------- AUDIO OR TEXT ---------- */
            if (message.audioUrl != null) {
                Spacer(Modifier.height(8.dp))
                AudioPlayer(message.audioUrl, message.duration)
            } else {
                Text(
                    message.content,
                    fontFamily = PressStart,
                    fontSize = 14.sp,
                    color = PixelBlack,
                    lineHeight = 18.sp
                )
            }

            /* ---------- TRANSCRIPTION ---------- */
            message.transcription?.let { transcription ->
                if (transcription != "Transcription disabled") {
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

            /* ---------- REACTIONS ---------- */
            if (!message.reactions.isNullOrEmpty()) {
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val grouped = message.reactions.groupBy { it.emoji }

                    grouped.forEach { (emoji, list) ->
                        val reacted = currentUserId != null &&
                                list.any { it.userId == currentUserId }

                        Button(
                            onClick = {
                                message._id?.let { id ->
                                    if (reacted) viewModel.removeReaction(id, emoji)
                                    else viewModel.addReaction(id, emoji)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (reacted) PixelGreen.copy(alpha = 0.3f)
                                else PixelGray.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .height(32.dp)
                                .border(1.dp, PixelBlack, RoundedCornerShape(16.dp)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "$emoji ${list.size}",
                                fontFamily = PressStart,
                                fontSize = 10.sp,
                                color = PixelBlack
                            )
                        }
                    }
                }
            }
        }
    }

    /* ---------- REACTION PICKER ---------- */
    if (showReactionPicker) {
        AlertDialog(
            onDismissRequest = { showReactionPicker = false },
            containerColor = PixelWhite,
            title = {
                Text(
                    "Add Reaction",
                    fontFamily = PressStart,
                    fontSize = 12.sp,
                    color = PixelBlack,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("❤️", "👍", "😂", "🔥", "😮")
                            .forEach { emoji ->
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(PixelGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .border(2.dp, PixelBlack, RoundedCornerShape(8.dp))
                                        .clickable {
                                            message._id?.let { id ->
                                                viewModel.addReaction(id, emoji)
                                            }
                                            showReactionPicker = false
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(emoji, fontSize = 32.sp)
                                }
                            }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.startReplying(message)
                            showReactionPicker = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PixelBlue),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.border(2.dp, PixelBlack, RoundedCornerShape(4.dp))
                    ) {
                        Text("Reply", fontFamily = PressStart, fontSize = 12.sp, color = PixelWhite)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showReactionPicker = false },
                    modifier = Modifier
                        .border(2.dp, PixelBlack, RoundedCornerShape(4.dp))
                        .background(PixelGray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                ) {
                    Text("Cancel", fontFamily = PressStart, fontSize = 10.sp, color = PixelBlack)
                }
            },
            shape = RoundedCornerShape(0.dp),
            tonalElevation = 0.dp
        )
    }
}


/* -------------------------------------------------------------
   AUDIO PLAYER
------------------------------------------------------------- */

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
                mp.setVolume(1f, 1f)
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
            delay(100)
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

        /* ---------- Play / Pause ---------- */
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
                .background(PixelGreen, CircleShape)
                .border(2.dp, PixelBlack, CircleShape)
        ) {
            Icon(
                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = PixelBlack
            )
        }

        Spacer(Modifier.width(8.dp))

        /* ---------- Slider ---------- */
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

        /* ---------- Remaining time ---------- */
        val remainingMs = if (duration > 0) duration - currentPosition else 0f
        val minutes = (remainingMs / 1000 / 60).toInt()
        val seconds = (remainingMs / 1000 % 60).toInt()

        Text(
            text = "%02d:%02d".format(minutes, seconds),
            fontFamily = PressStart,
            fontSize = 10.sp,
            color = PixelBlack
        )
    }
}
