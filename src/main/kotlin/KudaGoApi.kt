import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.Json
import model.KudaGoResponse
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import model.News
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.exp
import mu.KotlinLogging
import java.io.IOException

private val logger = KotlinLogging.logger {}
suspend fun getNews(count: Int = 100): List<News> {
    logger.info { "Получение новостей из API KudaGo. Запрошено $count новостей." }

    return try {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        val response: KudaGoResponse = client.get("https://kudago.com/public-api/v1.4/news/") {
            parameter("page_size", count)
            parameter("order_by", "-publication_date")
            parameter("location", "spb")
            parameter("fields", "id,title,place,description,site_url,favorites_count,comments_count,publication_date")
        }.body()

        client.close()
        logger.debug { "Получен ответ от API: $response" }

        response.results.map { result ->
            News(
                result.id,
                result.title,
                result.place,
                result.description.slice(3..result.description.length - 5),
                result.site_url,
                result.favorites_count,
                result.comments_count,
                result.publication_date,
                result.rating
            )
        }
    } catch (e: Exception) {
        logger.error(e) { "Ошибка при получении новостей из API KudaGo" }
        emptyList()
    }
}

fun List<News>.getMostRatedNews(count: Int, period: String): List<News> {
    logger.info { "Фильтрация новостей за период: $period" }

    return try {
        val start: Long = dateToTimestamp(period.split("-")[0])
        val end: Long = dateToTimestamp(period.split("-")[1])

        this.asSequence()
            .filter { newsItem ->
                newsItem.publication_date in start..end
            }
            .onEach { it.rating = 1 / (1 + exp(-(it.favorites_count.toDouble() / (it.comments_count.toInt() + 1)))) }
            .sortedByDescending { it.rating }
            .take(count)
            .toList()
    } catch (e: Exception) {
        logger.error(e) { "Ошибка при фильтрации новостей" }
        emptyList()
    }
}

fun saveNews(path: String, news: Collection<News>) {
    logger.info { "Сохранение ${news.size} новостей в файл: $path" }

    try {
        val file = File(path)

        if (!file.exists()) {
            file.createNewFile()
        }

        file.printWriter().use { out ->
            out.println("id,title,place,description,siteUrl,favoritesCount,commentsCount,rating")

            news.forEach { newsItem ->
                out.println("\"${newsItem.id}\",\"${newsItem.title}\",\"${newsItem.place}\",\"${newsItem.description}\",\"${newsItem.site_url}\",\"${newsItem.favorites_count}\",\"${newsItem.comments_count}\",${newsItem.rating}")
            }
        }
    } catch (e: IOException) {
        logger.error(e) { "Ошибка при сохранении новостей в файл: $path" }
    }
}

fun dateToTimestamp(dateString: String): Long {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val localDate = LocalDate.parse(dateString, formatter)

    val instant = localDate.atStartOfDay(ZoneId.of("Europe/Moscow")).toInstant()

    return instant.epochSecond
}

object PlaceSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("place", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        return when (val jsonElement = (decoder as JsonDecoder).decodeJsonElement()) {
            is JsonObject -> jsonElement["title"].toString()
            else -> "Неизвестно"
        }
    }


    override fun serialize(encoder: Encoder, value: String?) {
        encoder.encodeString(value ?: "")
    }
}
