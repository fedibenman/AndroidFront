package com.example.myapplication.ui.auth

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Data classes for API requests and responses
 */
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class SignupRequest(
    val name: String,
    val email: String,
    val password: String
)


@Serializable
data class TokenObject(
    val accessToken: String // This correctly models the nested accessToken string
)
@Serializable
data class AuthResponse(
//    val success: Boolean,
    val message: String? = null,
    val token: TokenObject? = null
)

/**
 * Auth repository interface. The real implementation calls your backend APIs.
 */
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun signup(name: String, email: String, password: String): Result<Unit>

    // Request a password reset code to be sent to the user's email.
    suspend fun requestPasswordReset(email: String): Result<Unit>

    // Verify the received code and change the password.
    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Unit>
}

/**
 * Real implementation using Ktor HTTP client to call the Nest API
 */
class KtorAuthRepository : AuthRepository {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        defaultRequest {
            // When running on Android emulator, use 10.0.2.2 to reach host machine's localhost.
            // Ensure your Nest API is running on the host at port 3000.
            url("http://192.168.140.182:3001/")
            contentType(ContentType.Application.Json)
        }
    }

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            // Validate input before making network call
            if (email.isBlank()) {
                return Result.failure(Exception("Email cannot be empty"))
            }
            if (password.isBlank()) {
                return Result.failure(Exception("Password cannot be empty"))
            }
            if (!email.contains("@")) {
                return Result.failure(Exception("Invalid email format"))
            }

            val response: HttpResponse = client.post("auth/login") {
                setBody(LoginRequest(email, password))
            }
        print(response)
            when (response.status.value) {
                in 200..299 -> {
                    val authResponse: AuthResponse = response.body()
                    if (authResponse!=null) {
                        Log.d("AuthRepository", "Login successful for $email")
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception(authResponse.message ?: "Login failed"))
                    }
                }
                401 -> Result.failure(Exception("Invalid credentials"))
                400 -> Result.failure(Exception("Invalid request data"))
                404 -> Result.failure(Exception("Not found"))
                500 -> Result.failure(Exception("Server error, please try again later"))
                else -> {
                    val errorMessage = response.body<AuthResponse>().message ?: "Login failed with status ${response.status}"
                    Result.failure(Exception(errorMessage))
                }
            }
        } catch (e: ConnectException) {
            Log.e("AuthRepository", "Connection failed", e)
            Result.failure(Exception("Cannot connect to server."))
        } catch (e: SocketTimeoutException) {
            Log.e("AuthRepository", "Request timeout", e)
            Result.failure(Exception("Request timeout."))
        } catch (e: UnknownHostException) {
            Log.e("AuthRepository", "Unknown host", e)
            Result.failure(Exception("Cannot reach server."))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Network error during login", e)
            Result.failure(Exception("Network error"))
        }
    }

    override suspend fun signup(name: String, email: String, password: String): Result<Unit> {
        return try {
            // Validate input before making network call
            if (name.isBlank()) {
                return Result.failure(Exception("Name cannot be empty"))
            }
            if (email.isBlank()) {
                return Result.failure(Exception("Email cannot be empty"))
            }
            if (password.isBlank()) {
                return Result.failure(Exception("Password cannot be empty"))
            }
            if (!email.contains("@")) {
                return Result.failure(Exception("Invalid email format"))
            }
            if (password.length < 6) {
                return Result.failure(Exception("Password must be at least 6 characters"))
            }

            val response: HttpResponse = client.post("auth/signup") {
                setBody(SignupRequest(name, email, password))
            }

            when (response.status.value) {
                in 200..299 -> {
                    val authResponse: AuthResponse = response.body()
                    if (authResponse != null) {
                        Log.d("AuthRepository", "Signup successful for $email")
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception(authResponse.message ?: "Signup failed"))
                    }
                }
                409 -> Result.failure(Exception("Email already exists"))
                400 -> Result.failure(Exception("Invalid request data"))
                404 -> Result.failure(Exception("Authentication service not found"))
                500 -> Result.failure(Exception("Server error, please try again later"))
                else -> {
                    val errorMessage = response.body<AuthResponse>().message ?: "Signup failed with status ${response.status}"
                    Result.failure(Exception(errorMessage))
                }
            }
        } catch (e: ConnectException) {
            Log.e("AuthRepository", "Connection failed", e)
            Result.failure(Exception("Cannot connect to server"))
        } catch (e: SocketTimeoutException) {
            Log.e("AuthRepository", "Request timeout", e)
            Result.failure(Exception("Request timeout"))
        } catch (e: UnknownHostException) {
            Log.e("AuthRepository", "Unknown host", e)
            Result.failure(Exception("Cannot reach server"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Network error during signup", e)
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    override suspend fun requestPasswordReset(email: String): Result<Unit> {
        return try {
            if (email.isBlank()) {
                return Result.failure(Exception("Email cannot be empty"))
            }
            if (!email.contains("@")) {
                return Result.failure(Exception("Invalid email format"))
            }

            val response: HttpResponse = client.post("auth/forgot-password") {
                setBody(mapOf("email" to email))
            }


// If you want to see the response body as a string, you can do this:

            Log.d("AuthRepository", "Password Reset Response Body: $response")


            when (response.status.value) {
                in 200..299 -> {
                    val authResponse: AuthResponse = response.body()
                    if (authResponse!=null) {
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception(authResponse.message ?: "Failed to send reset code"))
                    }
                }
                404 -> Result.failure(Exception("User not found"))
                else -> {
                    val authResponse: AuthResponse? = runCatching { response.body<AuthResponse>() }.getOrNull()
                    Result.failure(
                        Exception(
                            authResponse?.message
                                ?: "Failed to send reset code (status ${response.status})"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error requesting password reset", e)
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    override suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Unit> {
        return try {
            if (email.isBlank()) {
                return Result.failure(Exception("Email cannot be empty"))
            }
            if (!email.contains("@")) {
                return Result.failure(Exception("Invalid email format"))
            }
            if (code.isBlank()) {
                return Result.failure(Exception("Code cannot be empty"))
            }
            if (newPassword.length < 6) {
                return Result.failure(Exception("Password must be at least 6 characters"))
            }

            val response: HttpResponse = client.post("auth/reset-password") {
                setBody(
                    mapOf(
                        "email" to email,
                        "code" to code,
                        "newPassword" to newPassword
                    )
                )
            }

            when (response.status.value) {
                in 200..299 -> {
                    val authResponse: AuthResponse = response.body()
                    if (authResponse!=null) {
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception(authResponse.message ?: "Failed to reset password"))
                    }
                }
                400 -> Result.failure(Exception("Invalid code or password"))
                404 -> Result.failure(Exception("User or reset request not found"))
                else -> {
                    val authResponse: AuthResponse? = runCatching { response.body<AuthResponse>() }.getOrNull()
                    Result.failure(
                        Exception(
                            authResponse?.message
                                ?: "Failed to reset password (status ${response.status})"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error resetting password", e)
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    // Close the client when done
    fun close() {
        client.close()
    }
}

