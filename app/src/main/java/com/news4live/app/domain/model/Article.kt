package com.news4live.app.domain.model

data class Article(
    val id: Long,
    val title: String,
    val contentHtml: String,
    val excerptHtml: String,
    val cleanExcerpt: String,
    val date: String,
    val formattedDate: String,
    val slug: String,
    val canonicalUrl: String,
    val featuredImageUrl: String?,
    val thumbnailImageUrl: String?,
    val mediumImageUrl: String?,
    val authorName: String,
    val categoryIds: List<Long>,
    val categoryName: String,
    val tags: List<String>,
    val isBreaking: Boolean = false,
    val isBookmarked: Boolean = false
)

data class Category(
    val id: Long,
    val name: String,
    val slug: String,
    val postCount: Int,
    val parentId: Long = 0
)

data class HomeFeed(
    val breakingNews: List<Article>,
    val heroArticle: Article?,
    val topStories: List<Article>,
    val latestArticles: List<Article>,
    val categories: List<Category>,
    val trendingArticles: List<Article>
)

data class Bookmark(
    val articleId: Long,
    val title: String,
    val excerpt: String,
    val imageUrl: String?,
    val canonicalUrl: String,
    val categoryName: String,
    val savedAtTimestamp: Long,
    val formattedDate: String
)

sealed class ParsedHtmlBlock {
    data class Paragraph(val text: String, val isItalic: Boolean = false, val isBold: Boolean = false) : ParsedHtmlBlock()
    data class Heading(val text: String, val level: Int) : ParsedHtmlBlock()
    data class Image(val url: String, val caption: String? = null, val alt: String? = null) : ParsedHtmlBlock()
    data class Blockquote(val text: String, val author: String? = null) : ParsedHtmlBlock()
    data class BulletList(val items: List<String>) : ParsedHtmlBlock()
    data class Link(val text: String, val url: String) : ParsedHtmlBlock()
    data class VideoEmbed(val videoUrl: String, val youtubeVideoId: String? = null) : ParsedHtmlBlock()
    data class RawHtml(val html: String) : ParsedHtmlBlock()
}
