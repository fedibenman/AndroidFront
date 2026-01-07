package com.example.myapplication.viewModel

import android.graphics.Bitmap
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
    fun loadConversations() {
        Log.d("AiConversationViewModel", "Starting loadConversations")

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
                val convs = repository.getConversations(accessToken)

                Log.e("the conversations", convs.toString())

                conversations.value = convs
                
                // Only create a new conversation if there are no existing conversations
                if (convs.isEmpty()) {
                    Log.d("AiConversationViewModel", "No conversations found, creating a new one")
                    createNewConversation("New Quest")
                } else {
                    // Load the last (most recent) conversation
                    val lastConversation = convs.last()
                    selectedConversation.value = lastConversation
                    
                    Log.d("AiConversationViewModel", "Loading existing conversation: ${lastConversation.id}")
                    loadMessages(lastConversation.id)
                }

                Log.d("AiConversationViewModel", "loadConversations completed successfully")

            } catch (e: Exception) {
                Log.e("AiConversationViewModel", "Error in loadConversations", e)
                error.value = e.message
            }
        }
    }


    fun selectConversation(conversation: Conversation) {
        selectedConversation.value = conversation
        loadMessages(conversation.id)
    }

    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            try {
                val accessToken = tokenManager.accessTokenFlow.first()
                if (accessToken.isNullOrBlank()) {
                    error.value = "No access token found. Please login again."
                    return@launch
                }
                messages.value = repository.getMessages(conversationId, accessToken)
                Log.d("AiConversationViewModel", "Received message from server: id=${messages.value}")
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }


    fun sendMessageWithImages(content: String, bitmaps: List<Bitmap>) {
        if (content.isBlank() && bitmaps.isEmpty()) return
        
        val conv = selectedConversation.value ?: return

        viewModelScope.launch {
            try {
                val accessToken = tokenManager.accessTokenFlow.first()
                if (accessToken.isNullOrBlank()) {
                    error.value = "No access token found. Please login again."
                    return@launch
                }

                // Convert bitmaps to ImageData
                val imageDatas = bitmaps.map { bitmap ->
                    val byteArrayOutputStream = java.io.ByteArrayOutputStream()
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                    val byteArray = byteArrayOutputStream.toByteArray()
                    val base64String = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
                    
                    com.example.myapplication.Repository.ImageData(
                        base64 = base64String,
                        mimeType = "image/jpeg",
                        fileName = "image_${System.currentTimeMillis()}.jpg"
                    )
                }
                
                val dto = com.example.myapplication.Repository.CreateMessageDto(
                    content = content,
                    sender = "user",
                    conversationId = conv.id,
                    images = imageDatas
                )
                val newMsg = repository.createMessage(conv.id, dto, accessToken)
                Log.d("AiConversationViewModel", "Received message from server: id=${newMsg.id}, content=${newMsg.content}")
                // Add the message with the backend-generated ID
                messages.value = messages.value + newMsg
                loadMessages(conv.id)
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }



    fun createNewConversation(title: String = "New Conversation") {
        viewModelScope.launch {
            try {
                val accessToken = tokenManager.accessTokenFlow.first()
                if (accessToken.isNullOrBlank()) {
                    error.value = "No access token found. Please login again."
                    return@launch
                }

                val dto = CreateConversationDto(
                    title = title
                )

                // Send to server
                val newConv = repository.createNewConversationOnServer(dto, accessToken)

                // Up date local StateFlow list
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

    fun editMessage(messageId: String, newText: String, bitmaps: List<Bitmap> = emptyList()) {
        Log.d("AiConversationViewModel", "Starting editMessage: messageId=$messageId, newText=$newText, images=${bitmaps.size}")
        viewModelScope.launch {
            try {
                val accessToken = tokenManager.accessTokenFlow.first()
                if (accessToken.isNullOrBlank()) {
                    error.value = "No access token found. Please login again."
                    return@launch
                }

                // Convert bitmaps to ImageData
                val imageDatas = bitmaps.map { bitmap ->
                    val byteArrayOutputStream = java.io.ByteArrayOutputStream()
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                    val byteArray = byteArrayOutputStream.toByteArray()
                    val base64String = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
                    
                    com.example.myapplication.Repository.ImageData(
                        base64 = base64String,
                        mimeType = "image/jpeg",
                        fileName = "image_${System.currentTimeMillis()}.jpg"
                    )
                }
                
                val dto = com.example.myapplication.Repository.EditMessageDto(
                    content = newText,
                    images = imageDatas
                )

                Log.d("AiConversationViewModel", dto.toString())
                
                val result = repository.editMessage(messageId, dto, accessToken)

                // Backend may regenerate AI response, so reload the entire message list
                selectedConversation.value?.let { conversation ->
                    loadMessages(conversation.id)
                }
                
                Log.d("AiConversationViewModel", "Edit message completed successfully")
            } catch (e: Exception) {
                Log.e("AiConversationViewModel", "Error in editMessage: messageId=$messageId", e)
                error.value = e.message
            }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            try {
                val accessToken = tokenManager.accessTokenFlow.first()
                if (accessToken.isNullOrBlank()) {
                    error.value = "No access token found. Please login again."
                    return@launch
                }

                repository.deleteMessage(messageId, accessToken)

                // Backend automatically deletes subsequent messages, so reload the entire message list
                selectedConversation.value?.let { conversation ->
                    loadMessages(conversation.id)
                }
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

    fun editConversationTitle(conversationId: String, newTitle: String) {
        Log.d("AiConversationViewModel", "Starting editConversationTitle: conversationId=$conversationId, newTitle=$newTitle")
        viewModelScope.launch {
            try {
                val accessToken = tokenManager.accessTokenFlow.first()
                if (accessToken.isNullOrBlank()) {
                    error.value = "No access token found. Please login again."
                    return@launch
                }

                Log.d("AiConversationViewModel", "Calling repository.editConversation...")
                val updatedConversation = repository.editConversation(conversationId, newTitle, accessToken)

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
                val accessToken = tokenManager.accessTokenFlow.first()
                if (accessToken.isNullOrBlank()) {
                    error.value = "No access token found. Please login again."
                    return@launch
                }

                repository.deleteConversation(conversationId, accessToken)

                // Update local state by removing the conversation
                val updatedConversations = conversations.value.filter { it.id != conversationId }
                conversations.value = updatedConversations

                // If the deleted conversation was selected, select another one
                if (selectedConversation.value?.id == conversationId) {
                    selectedConversation.value = updatedConversations.maxByOrNull { it.id }

                    if (selectedConversation.value != null) {
                        loadMessages(selectedConversation.value!!.id)
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
