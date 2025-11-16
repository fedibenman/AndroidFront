package com.example.myapplication.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.Repository.AiConversationRepository
import com.example.myapplication.Repository.Conversation
import com.example.myapplication.Repository.CreateConversationDto
import com.example.myapplication.Repository.CreateMessageDto
import com.example.myapplication.Repository.Message
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.MutableStateFlow

class AiConversationViewModel(
    private val repository: AiConversationRepository = AiConversationRepository()
) : ViewModel() {

    val conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val messages = MutableStateFlow<List<Message>>(emptyList())
    val selectedConversation = MutableStateFlow<Conversation?>(null)
    val messageInput = MutableStateFlow("")
    val error = MutableStateFlow<String?>(null)

    fun loadConversations(userId: String) {
        viewModelScope.launch {
            try {
                val convs = repository.getConversations(userId)
                conversations.value = convs
                selectedConversation.value = convs.maxByOrNull { it.id } // simple: last updated
                selectedConversation.value?.let { loadMessages(it.id, userId) }
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

    fun selectConversation(conversation: Conversation, userId: String) {
        selectedConversation.value = conversation
        loadMessages(conversation.id, userId)
    }

    fun loadMessages(conversationId: String, userId: String) {
        viewModelScope.launch {
            try {
                messages.value = repository.getMessages(conversationId, userId)
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

    fun sendMessage(userId: String) {
        val conv = selectedConversation.value ?: return
        val content = messageInput.value
        if (content.isBlank()) return

        viewModelScope.launch {
            try {
                val newMsg = repository.createMessage(conv.id, CreateMessageDto(userId = userId, content = content))
                messages.value = messages.value + newMsg
                messageInput.value = ""
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }
}
