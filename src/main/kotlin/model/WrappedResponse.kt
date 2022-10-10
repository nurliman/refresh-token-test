package model

import kotlinx.serialization.Serializable

@Serializable
data class WrappedResponse<T>(
    var status: Boolean,
    var data: T? = null,
    var error: String? = null,
)
