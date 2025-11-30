package com.example.myapplication.community.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.community.data.remote.RetrofitInstance
import com.example.myapplication.community.model.Comment
import com.example.myapplication.community.model.Post
import com.example.myapplication.ui.auth.KtorAuthRepository
import com.example.myapplication.ui.auth.TokenDataStoreManager
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val api = RetrofitInstance.api
    private val tokenManager = TokenDataStoreManager(context)
    private val authRepository = KtorAuthRepository()

    private val notificationRepository = com.example.myapplication.community.repository.NotificationRepository()
    val notifications = notificationRepository.notifications

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val fallbackUserId = "674a0f6bcd2c94b2d0b72f1a"

    init {
        loadPosts()
        initNotifications()
    }

    private fun initNotifications() {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            if (userId != null) {
                notificationRepository.connect(userId)
                notificationRepository.loadNotifications(userId)
            }
        }
    }

    // -------------------------------
    // Fetch current user id
    // -------------------------------
    private suspend fun getCurrentUserId(): String? {
        return try {
            val accessToken = tokenManager.accessTokenFlow.first()

            if (accessToken != null) {
                val result = authRepository.getProfile(accessToken)
                result.getOrNull()?.id
            } else null

        } catch (e: Exception) {
            Log.e("PostViewModel", "Error getting userId", e)
            null
        }
    }

    // -------------------------------
    // Load all posts
    // -------------------------------
    fun loadPosts() {
        viewModelScope.launch {
            try {
                val result = api.getPosts()
                _posts.value = result
                result.forEach { Log.d("DEBUG_POST", "Post: ${it.title}, Photo length=${it.photo?.length}") }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error loading posts", e)
            }
        }
    }

    // -------------------------------
    // Like post
    // -------------------------------
    fun likePost(postId: String) {
        viewModelScope.launch {

            // Optimistic UI update
            _posts.value = _posts.value.map { post ->
                if (post._id == postId) {
                    val currentUserId = getCurrentUserId() ?: fallbackUserId
                    val currentLikes = post.likes?.toMutableList() ?: mutableListOf()
                    if (currentLikes.contains(currentUserId)) {
                        currentLikes.remove(currentUserId)
                    } else {
                        currentLikes.add(currentUserId)
                    }
                    post.copy(likes = currentLikes)
                } else post
            }

            try {
                val userId = getCurrentUserId() ?: fallbackUserId
                val body = mapOf("userId" to userId)
                api.likePost(postId, body)
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error liking post", e)
            }

            loadPosts()
        }
    }

    // -------------------------------
    // Add comment
    // -------------------------------
    fun addComment(postId: String, text: String) {
        viewModelScope.launch {
            try {
                val userId = getCurrentUserId() ?: fallbackUserId

                val body = mapOf(
                    "content" to text,
                    "postId" to postId,
                    "photo" to "",
                    "userId" to userId
                )

                val newComment = api.addComment(body)

                _posts.value = _posts.value.map { post ->
                    if (post._id == postId)
                        post.copy(comments = post.comments + newComment)
                    else post
                }

                loadPosts()
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error adding comment", e)
            }
        }
    }

    // -------------------------------
    // Convert image to Base64
    // -------------------------------
    private fun uriToBase64(uri: Uri): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
            val bytes = inputStream.readBytes()
            inputStream.close()

            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            "data:image/jpeg;base64,$base64"

        } catch (e: Exception) {
            Log.e("DEBUG_BASE64", "Base64 conversion failed", e)
            ""
        }
    }

    // -------------------------------
    // Create Post
    // -------------------------------
    fun createPost(
        title: String,
        content: String,
        imageUri: Uri? = null,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val userId = getCurrentUserId() ?: fallbackUserId

                val photoBase64 = imageUri?.let { uriToBase64(it) } ?: ""

                val body = mapOf(
                    "title" to title,
                    "content" to content,
                    "photo" to photoBase64,
                    "tags" to emptyList<String>(),
                    "userId" to userId
                )

                val created = api.createPost(body as MutableMap<String, Any>)

                _posts.value = _posts.value + created
                _errorMessage.value = null

                onDone()
                loadPosts()

            } catch (e: HttpException) {



            } catch (e: Exception) {
                Log.e("PostViewModel", "Error creating post", e)
                _errorMessage.value = "Erreur lors de la création du post"
            }
        }
    }

    // -------------------------------
    // Update Post
    // -------------------------------
    fun updatePost(
        postId: String,
        title: String,
        content: String,
        imageUri: Uri? = null,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val body = mutableMapOf(
                    "title" to title,
                    "content" to content
                )

                val photoBase64 = imageUri?.let { uriToBase64(it) }
                if (!photoBase64.isNullOrEmpty()) {
                    body["photo"] = photoBase64
                }

                val updated = api.updatePost(postId, body)

                _posts.value = _posts.value.map { if (it._id == postId) updated else it }
                _errorMessage.value = null

                onDone()
                loadPosts()

            } catch (e: HttpException) {

            } catch (e: Exception) {
                Log.e("PostViewModel", "Error updating post", e)
                _errorMessage.value = "Erreur lors de la modification du post"
            }
        }
    }

    // -------------------------------
    // Delete Post
    // -------------------------------
    fun deletePost(postId: String, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                api.deletePost(postId)
                _posts.value = _posts.value.filter { it._id != postId }
                onDone()
                loadPosts()
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error deleting post", e)
            }
        }
    }

    // -------------------------------
    // Handle API Moderation Error
    // -------------------------------


    // -------------------------------
    // Clear error
    // -------------------------------
    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        notificationRepository.disconnect()
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
