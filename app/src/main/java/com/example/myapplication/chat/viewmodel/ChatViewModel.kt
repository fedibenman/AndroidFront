package com.example.myapplication.chat.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.chat.model.ChatMessage
import com.example.myapplication.chat.model.ChatRoom
import com.example.myapplication.chat.repository.ChatRepository
import com.example.myapplication.ui.auth.KtorAuthRepository
import com.example.myapplication.ui.auth.TokenDataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context = application.applicationContext
    private val repository = ChatRepository()
    private val tokenManager = TokenDataStoreManager(context)
    private val authRepository = KtorAuthRepository()
    
    private val _rooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val rooms: StateFlow<List<ChatRoom>> = _rooms.asStateFlow()
    
    private val _currentRoom = MutableStateFlow<ChatRoom?>(null)
    val currentRoom: StateFlow<ChatRoom?> = _currentRoom.asStateFlow()
    
    val messages: StateFlow<List<ChatMessage>> = repository.messages
    val typingUsers: StateFlow<Set<String>> = repository.typingUsers
    
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserIdFlow: StateFlow<String?> = _currentUserId.asStateFlow()
    private val _currentUserName = MutableStateFlow<String>("User")
    val currentUserName: StateFlow<String> = _currentUserName.asStateFlow()

    private val _replyingToMessage = MutableStateFlow<ChatMessage?>(null)
    val replyingToMessage: StateFlow<ChatMessage?> = _replyingToMessage.asStateFlow()
    
    init {
        loadRooms()
        repository.connect()
        loadCurrentUser()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            tokenManager.accessTokenFlow.collect { accessToken ->
                try {
                    Log.d("ChatViewModel", "Token flow emission: $accessToken")
                    if (accessToken != null) {
                        val result = authRepository.getProfile(accessToken)
                        val profile = result.getOrNull()
                        if (profile != null) {
                            Log.d("ChatViewModel", "Profile loaded: ${profile.name}, ID: ${profile.id}")
                            _currentUserId.value = profile.id
                            // Use email as fallback if name is null or empty
                            _currentUserName.value = profile.name?.ifEmpty { profile.email } ?: profile.email
                        } else {
                            Log.e("ChatViewModel", "Failed to load profile: ${result.exceptionOrNull()}")
                        }
                    } else {
                        Log.d("ChatViewModel", "No access token in flow")
                    }
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Error loading user", e)
                }
            }
        }
    }
    
    fun loadRooms() {
        viewModelScope.launch {
            val loadedRooms = repository.getRooms()
            _rooms.value = loadedRooms
        }
    }
    
    fun selectRoom(room: ChatRoom) {
        viewModelScope.launch {
            _currentRoom.value?.let { repository.leaveRoom(it._id) }
            _currentRoom.value = room
            repository.joinRoom(room._id)
            repository.loadMessages(room._id)
        }
    }
    
    fun sendMessage(content: String) {
        val room = _currentRoom.value ?: return
        val userId = _currentUserId.value ?: "unknown"
        val userName = _currentUserName.value
        
        val replyTo = _replyingToMessage.value?.let {
            com.example.myapplication.chat.model.ReplyInfo(
                messageId = it._id ?: "",
                content = if (it.audioUrl != null) "Voice Message" else it.content,
                senderName = it.senderName
            )
        }

        repository.sendMessage(userId, userName, content, room._id, replyTo = replyTo)
        repository.sendStopTyping(room._id)
        _replyingToMessage.value = null
    }
    
    fun onTyping(isTyping: Boolean) {
        val room = _currentRoom.value ?: return
        val userName = _currentUserName.value
        
        if (isTyping) {
            repository.sendTyping(room._id, userName)
        } else {
            repository.sendStopTyping(room._id)
        }
    }
    
    fun addReaction(messageId: String, emoji: String) {
        val room = _currentRoom.value ?: return
        val userId = _currentUserId.value ?: return
        val userName = _currentUserName.value
        
        repository.addReaction(messageId, emoji, userId, userName, room._id)
    }
    
    fun removeReaction(messageId: String, emoji: String) {
        val room = _currentRoom.value ?: return
        val userId = _currentUserId.value ?: return
        
        repository.removeReaction(messageId, emoji, userId, room._id)
    }

    fun startReplying(message: ChatMessage) {
        _replyingToMessage.value = message
    }

    fun cancelReply() {
        _replyingToMessage.value = null
    }
    
    private val audioRecorder = com.example.myapplication.chat.utils.AudioRecorder(context)
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    private val _recordingDuration = MutableStateFlow("00:00")
    val recordingDuration: StateFlow<String> = _recordingDuration.asStateFlow()
    private var timerJob: kotlinx.coroutines.Job? = null
    
    private var audioFile: java.io.File? = null
    
    private val _recordedAudioFile = MutableStateFlow<java.io.File?>(null)
    val recordedAudioFile: StateFlow<java.io.File?> = _recordedAudioFile.asStateFlow()

    fun startRecording() {
        val file = java.io.File(context.cacheDir, "voice_message.mp4")
        audioRecorder.startRecording(file)
        audioFile = file
        _isRecording.value = true
        
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var seconds = 0L
            while (true) {
                val min = seconds / 60
                val sec = seconds % 60
                _recordingDuration.value = String.format("%02d:%02d", min, sec)
                kotlinx.coroutines.delay(1000)
                seconds++
            }
        }
    }

    fun stopRecording() {
        audioRecorder.stopRecording()
        _isRecording.value = false
        timerJob?.cancel()
        _recordingDuration.value = "00:00"
        _recordedAudioFile.value = audioFile
    }
    
    fun discardAudio() {
        _recordedAudioFile.value = null
        audioFile = null
    }
    
    fun sendAudio() {
        val file = _recordedAudioFile.value ?: return
        val room = _currentRoom.value ?: return
        val userId = _currentUserId.value ?: "unknown"
        val userName = _currentUserName.value
        
        viewModelScope.launch {
            // Calculate duration using MediaMetadataRetriever
            val duration = try {
                val retriever = android.media.MediaMetadataRetriever()
                retriever.setDataSource(file.absolutePath)
                val durationMs = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                retriever.release()
                val minutes = (durationMs / 1000 / 60).toInt()
                val seconds = (durationMs / 1000 % 60).toInt()
                String.format("%02d:%02d", minutes, seconds)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error calculating duration", e)
                "00:00"
            }
            
            val response = repository.uploadAudio(file)
            if (response != null) {
                val replyTo = _replyingToMessage.value?.let {
                    com.example.myapplication.chat.model.ReplyInfo(
                        messageId = it._id ?: "",
                        content = if (it.audioUrl != null) "Voice Message" else it.content,
                        senderName = it.senderName
                    )
                }
                repository.sendMessage(userId, userName, "Voice Message", room._id, response.audioUrl, response.transcription, duration, replyTo)
            }
            _recordedAudioFile.value = null
            audioFile = null
            _replyingToMessage.value = null
        }
    }

    private fun sendVoiceMessage() {
        val file = audioFile ?: return
        val room = _currentRoom.value ?: return
        val userId = _currentUserId.value ?: "unknown"
        val userName = _currentUserName.value

        viewModelScope.launch {
            val response = repository.uploadAudio(file)
            if (response != null) {
                repository.sendMessage(userId, userName, "Voice Message", room._id, response.audioUrl, response.transcription)
            }
        }
    }

    fun createRoom(name: String, description: String) {
        viewModelScope.launch {
            repository.createRoom(name, description)
            loadRooms()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        repository.disconnect()
    }
}
