package com.example.myapplication.directmessages.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.directmessages.data.DirectMessagesRepository
import com.example.myapplication.directmessages.model.Conversation
import com.example.myapplication.directmessages.model.DirectMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DirectMessagesViewModel : ViewModel() {
    private val repository = DirectMessagesRepository.getInstance()
    
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations
    
    private val _messages = MutableStateFlow<List<DirectMessage>>(emptyList())
    val messages: StateFlow<List<DirectMessage>> = _messages
    
    private val _currentConversation = MutableStateFlow<Conversation?>(null)
    val currentConversation: StateFlow<Conversation?> = _currentConversation
    
    private val _users = MutableStateFlow<List<com.example.myapplication.directmessages.model.User>>(emptyList())
    val users: StateFlow<List<com.example.myapplication.directmessages.model.User>> = _users
    
    val typingUser = repository.typingUser
    
    val incomingCall = repository.incomingCall
    val callAccepted = repository.callAccepted
    val callDeclined = repository.callDeclined
    
    fun listenForIncomingCalls(userId: String) {
        repository.listenForIncomingCalls(userId)
    }
    fun loadConversations(userId: String) {
        viewModelScope.launch {
            val result = repository.getConversations(userId)
            _conversations.value = result
            
            // Join all conversation rooms for real-time updates
            result.forEach { conversation ->
                if (conversation._id != null && conversation._id.isNotEmpty()) {
                    repository.joinConversation(conversation._id)
                    android.util.Log.d("DirectMessagesVM", "Joined conversation room: ${conversation._id}")
                } else {
                    android.util.Log.e("DirectMessagesVM", "Skipping conversation with null/empty ID")
                }
            }
        }
    }
    
    fun startConversation(user1Id: String, user2Id: String, onSuccess: (Conversation) -> Unit) {
        android.util.Log.d("DirectMessagesVM", "startConversation called: user1=$user1Id, user2=$user2Id")
        viewModelScope.launch {
            android.util.Log.d("DirectMessagesVM", "Making API call to start conversation")
            val conversation = repository.startConversation(user1Id, user2Id)
            
            if (conversation != null) {
                android.util.Log.d("DirectMessagesVM", "Conversation created successfully: ${conversation._id}")
                _currentConversation.value = conversation
                android.util.Log.d("DirectMessagesVM", "Calling onSuccess callback")
                onSuccess(conversation)
            } else {
                android.util.Log.e("DirectMessagesVM", "Failed to start conversation - repository returned null")
            }
        }
    }
    
    fun selectConversation(conversation: Conversation) {
        _currentConversation.value = conversation
        repository.joinConversation(conversation._id)
        loadMessages(conversation._id)
    }
    
    init {
        viewModelScope.launch {
            repository.newMessage.collect { message ->
                if (message != null) {
                    android.util.Log.d("DirectMessagesVM", "New message received via socket: ${message.content}")
                    
                    // Update messages if we're in this conversation
                    val currentConv = _currentConversation.value
                    if (currentConv != null && message.conversationId == currentConv._id) {
                        // Check if message already exists to avoid duplicates
                        val exists = _messages.value.any { it._id == message._id }
                        if (!exists) {
                            android.util.Log.d("DirectMessagesVM", "Adding new message from socket: ${message.content}, sender: ${message.sender.name}")
                            _messages.value = _messages.value + message
                        }
                    }
                    
                    // Update conversation list in real-time
                    val conversations = _conversations.value.toMutableList()
                    val conversationIndex = conversations.indexOfFirst { it._id == message.conversationId }
                    
                    if (conversationIndex != -1) {
                        // Update existing conversation with new lastMessage
                        val updatedConversation = conversations[conversationIndex].copy(
                            lastMessage = message,
                            updatedAt = message.createdAt
                        )
                        conversations.removeAt(conversationIndex)
                        // Move to top of list
                        conversations.add(0, updatedConversation)
                        _conversations.value = conversations
                        android.util.Log.d("DirectMessagesVM", "Updated conversation list - moved ${message.conversationId} to top")
                    } else {
                        // Conversation not in list yet - might be a new conversation
                        // Reload conversations to get the new one
                        android.util.Log.d("DirectMessagesVM", "New conversation detected - reloading list")
                    }
                }
            }
        }
    }
    
    fun loadConversationById(conversationId: String, userId: String) {
        android.util.Log.d("DirectMessagesVM", "loadConversationById: $conversationId")
        viewModelScope.launch {
            val conversations = repository.getConversations(userId)
            val conversation = conversations.firstOrNull { it._id == conversationId }
            conversation?.let {
                _currentConversation.value = it
                repository.joinConversation(it._id)
                loadMessages(it._id)
                android.util.Log.d("DirectMessagesVM", "Conversation loaded: ${it._id}, participants: ${it.participants.size}")
            } ?: android.util.Log.e("DirectMessagesVM", "Conversation not found: $conversationId")
        }
    }
    
    fun loadMessages(conversationId: String) {

        viewModelScope.launch {
            val result = repository.getMessages(conversationId)
            _messages.value = result.reversed() // Show oldest first
        }
    }
    
    fun sendMessage(conversationId: String, senderId: String, content: String) {
        android.util.Log.d("DirectMessagesVM", "sendMessage called: convId=$conversationId, senderId=$senderId, content=$content")
        repository.sendMessage(conversationId, senderId, content)
        // Message will be added via socket listener (newMessage flow)
    }
    
    fun sendTyping(conversationId: String, userName: String) {
        repository.sendTyping(conversationId, userName)
    }
    fun loadUsers() {
        viewModelScope.launch {
            _users.value = repository.getAllUsers()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        repository.disconnect()
    }
}
