package model

import kotlinx.serialization.Serializable

@Serializable
data class KudaGoResponse(
    val count: Long,
    val next: String?,
    val previous: String?,
    val results: List<News>
)