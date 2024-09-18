package model

class NewsPrinter {
    private val sb = StringBuilder()

    fun print(news: List<News>) {
        news.forEach { newsItem ->
            sb.appendLine("${newsItem.title}")
            sb.appendLine("Описание: ${newsItem.description}")
            sb.appendLine("Где все происходило: ${newsItem.place ?: "Не указано"}")
            sb.appendLine("Посмотреть полную версию: ${newsItem.site_url}")
            sb.appendLine("Людей добавивших в избранное: ${newsItem.favorites_count}")
            sb.appendLine("Комментарии: ${newsItem.comments_count}")
            sb.appendLine("Рейтинг: ${newsItem.rating}")
            sb.appendLine("--------------------")
        }

        println(sb.toString())
    }
}

fun printNews(block: NewsPrinter.() -> Unit) {
    val printer = NewsPrinter()
    printer.block()
}