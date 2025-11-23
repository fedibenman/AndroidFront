import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: Token
)

@Serializable
data class Token(
    val accessToken: String,
    val refreshToken: String
)
