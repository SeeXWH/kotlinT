package model

import PlaceSerializer
import kotlinx.serialization.Serializable


@Serializable
data class News(
    val id: Long,
    val title: String,
    @Serializable(with = PlaceSerializer::class)
    val place: String?,
    val description: String,
    val site_url: String?,
    val favorites_count: Long,
    val comments_count: Long,
    val publication_date: Long,
    var rating: Double? = null
) {

}
