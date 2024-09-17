package model

import kotlinx.serialization.Serializable
import java.time.LocalDate


@Serializable
data class News(
    val id: Int,
    val title: String,
    val place: String?,
    val description: String,
    val siteUrl: String,
    val favoritesCount: Int,
    val commentsCount: Int,
    var rating: Double? = null){

}
