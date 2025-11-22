package com.example.myapplication.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.AppContextHolder
import com.example.myapplication.Repository.AiConversationRepository
import com.example.myapplication.Repository.Conversation
import com.example.myapplication.Repository.CreateConversationDto
import com.example.myapplication.Repository.CreateMessageDto
import com.example.myapplication.Repository.Message
import com.example.myapplication.ui.auth.TokenDataStoreManager
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import java.util.UUID

class AiConversationViewModel(
    private val repository: AiConversationRepository = AiConversationRepository()
) : ViewModel() {

    var conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val activeConversations = MutableStateFlow<List<Conversation>>(emptyList())
    val messages = MutableStateFlow<List<Message>>(emptyList())
    val selectedConversation = MutableStateFlow<Conversation?>(null)
    val messageInput = MutableStateFlow("")
    val error = MutableStateFlow<String?>(null)

    val isAddingNewConversation = MutableStateFlow(false)
    val newConversationTitleInput = MutableStateFlow("")
    private val tokenManager = TokenDataStoreManager(AppContextHolder.appContext)
    fun loadConversations(userId: String) {
        Log.d("AiConversationViewModel", "Starting loadConversations for userId: $userId")

        viewModelScope.launch {
            try {
                // ⬅️ LOAD TOKEN HERE ONLY
                val accessToken = tokenManager.accessTokenFlow.first()

                if (accessToken.isNullOrBlank()) {
                    error.value = "No access token found. Please login again."
                    return@launch
                }

                Log.d("AiConversationViewModel", "Calling repository.getConversations...")

                // ⬅️ PASS TOKEN HERE ONLY
                val convs = repository.getConversations(userId, accessToken)

                Log.e("the conversations", convs.toString())

                conversations.value = convs
                selectedConversation.value = convs.maxByOrNull { it.id }

                selectedConversation.value?.let { loadMessages(it.id, userId) }

                Log.d("AiConversationViewModel", "loadConversations completed successfully")

            } catch (e: Exception) {
                Log.e("AiConversationViewModel", "Error in loadConversations", e)
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

        val tempId = "pending-${UUID.randomUUID()}"
        val tempTimestamp = System.currentTimeMillis().toString()
        val tempMsg = Message(tempId, conv.id, "user", content, tempTimestamp)

        messages.value = messages.value + tempMsg
        messageInput.value = ""

        viewModelScope.launch {
            try {
                val dto = CreateMessageDto(userId = userId, content = content, sender = "user")
                val newMsg = repository.createMessage(conv.id, dto)
                // Replace temp with real
                val updatedMessages = messages.value.toMutableList().apply {
                    val index = indexOfFirst { it.id.startsWith("pending-") }
                    if (index != -1) {
                        this[index] = newMsg
                    }
                }
                messages.value = updatedMessages
                loadMessages(conv.id, userId)
            } catch (e: Exception) {
                error.value = e.message
                // Remove temp on error
                val updatedMessages = messages.value.filterNot { it.id.startsWith("pending-") }
                messages.value = updatedMessages
            }
        }
    }



    fun createNewConversation(title: String = "New Conversation", userId: String) {
        viewModelScope.launch {
            try {
                val dto = CreateConversationDto(
                    title = title,
                    userId = userId
                )

                // Send to server
                val newConv = repository.createNewConversationOnServer(dto)

                // Update local StateFlow list
                val updatedList = conversations.value.toMutableList()
                updatedList.add(newConv)
                conversations.value = updatedList
                selectedConversation.value = newConv
                isAddingNewConversation.value = false
            } catch (e: Exception) {
                Log.e("AiConversation", "Failed to create new conversation", e)
                error.value = e.message
                isAddingNewConversation.value = false
            }
        }
    }

    fun startAddingNewConversation() {
        isAddingNewConversation.value = true
        newConversationTitleInput.value = ""
    }

    fun cancelAddingNewConversation() {
        isAddingNewConversation.value = false
        newConversationTitleInput.value = ""
    }

    fun updateNewConversationTitle(title: String) {
        newConversationTitleInput.value = title
    }

    fun editMessage(messageId: String, newText: String, userId: String) {
        Log.d("AiConversationViewModel", "Starting editMessage: messageId=$messageId, newText=$newText, userId=$userId")
        viewModelScope.launch {
            try {
                Log.d("AiConversationViewModel", "Calling repository.editMessage...")
                repository.editMessage(messageId, newText)

                // Update local state
                val updatedMessages = messages.value.toMutableList().apply {
                    val index = indexOfFirst { it.id == messageId }
                    if (index != -1) {
                        Log.d("AiConversationViewModel", "Updating local message state: ${this[index].id}")
                        this[index] = this[index].copy(content = newText)
                    } else {
                        Log.w("AiConversationViewModel", "Message not found in local state: $messageId")
                    }
                }
                messages.value = updatedMessages
                Log.d("AiConversationViewModel", "Edit message completed successfully")
            } catch (e: Exception) {
                Log.e("AiConversationViewModel", "Error in editMessage: messageId=$messageId", e)
                error.value = e.message
            }
        }
    }

    fun deleteMessage(messageId: String, userId: String) {
        viewModelScope.launch {
            try {
                repository.deleteMessage(messageId)

                // Update local state by removing the message
                val updatedMessages = messages.value.filter { it.id != messageId }
                messages.value = updatedMessages
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

    fun editConversationTitle(conversationId: String, newTitle: String) {
        Log.d("AiConversationViewModel", "Starting editConversationTitle: conversationId=$conversationId, newTitle=$newTitle")
        viewModelScope.launch {
            try {
                Log.d("AiConversationViewModel", "Calling repository.editConversation...")
                val updatedConversation = repository.editConversation(conversationId, newTitle)

                // Update local state
                val updatedConversations = conversations.value.toMutableList().apply {
                    val index = indexOfFirst { it.id == conversationId }
                    if (index != -1) {
                        Log.d("AiConversationViewModel", "Updating local conversation state: ${this[index].id}")
                        this[index] = updatedConversation
                    } else {
                        Log.w("AiConversationViewModel", "Conversation not found in local state: $conversationId")
                    }
                }
                conversations.value = updatedConversations

                // Update selected conversation if it's the one being edited
                if (selectedConversation.value?.id == conversationId) {
                    Log.d("AiConversationViewModel", "Updating selected conversation: $conversationId")
                    selectedConversation.value = updatedConversation
                }

                Log.d("AiConversationViewModel", "Edit conversation title completed successfully")
            } catch (e: Exception) {
                Log.e("AiConversationViewModel", "Error in editConversationTitle: conversationId=$conversationId", e)
                error.value = e.message
            }
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            try {
                repository.deleteConversation(conversationId)

                // Update local state by removing the conversation
                val updatedConversations = conversations.value.filter { it.id != conversationId }
                conversations.value = updatedConversations

                // If the deleted conversation was selected, select another one
                if (selectedConversation.value?.id == conversationId) {
                    selectedConversation.value = updatedConversations.maxByOrNull { it.id }

                    if (selectedConversation.value != null) {
                        loadMessages(selectedConversation.value!!.id, selectedConversation.value!!.userId)
                    } else {
                        messages.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

}
