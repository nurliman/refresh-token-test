package model

import kotlinx.serialization.Serializable

@Serializable
data class Credential(
    val id: String,
    val username: String,
    val accessToken: String,
    val refreshToken: String,
    val roles: List<String>,
)
