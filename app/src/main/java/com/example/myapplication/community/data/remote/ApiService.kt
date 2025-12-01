package com.example.myapplication.community.data.remote

import com.example.myapplication.community.model.Comment
import com.example.myapplication.community.model.Notification
import com.example.myapplication.community.model.Post
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface ApiService {

    @GET("community/posts")
    suspend fun getPosts(): List<Post>

    @POST("community/like/{id}")
    suspend fun likePost(@Path("id") postId: String, @Body body: Map<String, String>): Post

    @POST("community/comment")
    suspend fun addComment(@Body body: Map<String, String>): Comment

    @Multipart
    @POST("community/upload")
    suspend fun uploadImage(@Part image: MultipartBody.Part): Map<String, String>

    @POST("community/post")
    suspend fun createPost(@Body body: MutableMap<String, Any>): Post

    @PATCH("community/post/{id}")
    suspend fun updatePost(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Post

    @DELETE("community/post/{id}")
    suspend fun deletePost(@Path("id") id: String): ResponseBody
    
    @GET("notifications/{userId}")
    suspend fun getNotifications(@Path("userId") userId: String): List<Notification>
    
    @PUT("notifications/{id}/read")
    suspend fun markNotificationAsRead(@Path("id") id: String): Notification
}

