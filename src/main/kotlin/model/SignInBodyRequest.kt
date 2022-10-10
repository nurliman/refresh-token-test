package model

import kotlinx.serialization.Serializable

@Serializable
data class SignInBodyRequest(
    val username: String,
    val password: String,
)
