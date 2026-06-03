package rs.edu.raf.rma.networking.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterBody(
    @SerialName("full_name") val fullName: String,
    val username: String,
    val password: String,
)

@Serializable
data class LoginBody(
    val username: String,
    val password: String,
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Long? = null,
    val user: UserDto,
)

@Serializable
data class UserDto(
    val id: Int? = null,
    val username: String,
    @SerialName("full_name") val fullName: String,
)

@Serializable
data class QuizSubmitBody(
    val score: Float,
    val category: Int = 1,
)

@Serializable
data class QuizSubmitResponse(
    val ranking: Int? = null,
)
