import kotlinx.coroutines.runBlocking
import model.printNews


fun main() = runBlocking {
    val newsList = getNews(count = 1000)

    val filteredNews = newsList.getMostRatedNews(count = 1000, period = "17.07.2024-18.09.2024")

    saveNews("newsr.csv", filteredNews)

    printNews {
        print(filteredNews)
    }
} 