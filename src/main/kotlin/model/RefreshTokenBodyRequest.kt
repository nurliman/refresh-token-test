package model

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenBodyRequest(
    val refreshToken: String,
)
