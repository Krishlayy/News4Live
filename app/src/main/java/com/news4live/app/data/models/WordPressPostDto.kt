package com.news4live.app.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WordPressPostDto(
    @SerialName("id")
    val id: Long,
    @SerialName("date")
    val date: String = "",
    @SerialName("modified")
    val modified: String = "",
    @SerialName("slug")
    val slug: String = "",
    @SerialName("link")
    val link: String = "",
    @SerialName("title")
    val title: RenderedTextDto = RenderedTextDto(),
    @SerialName("content")
    val content: RenderedTextDto = RenderedTextDto(),
    @SerialName("excerpt")
    val excerpt: RenderedTextDto = RenderedTextDto(),
    @SerialName("featured_media")
    val featuredMediaId: Long = 0,
    @SerialName("categories")
    val categories: List<Long> = emptyList(),
    @SerialName("tags")
    val tags: List<Long> = emptyList(),
    @SerialName("_embedded")
    val embedded: EmbeddedDto? = null
)

@Serializable
data class RenderedTextDto(
    @SerialName("rendered")
    val rendered: String = "",
    @SerialName("protected")
    val isProtected: Boolean = false
)

@Serializable
data class EmbeddedDto(
    @SerialName("author")
    val author: List<AuthorDto>? = null,
    @SerialName("wp:featuredmedia")
    val featuredMedia: List<FeaturedMediaDto>? = null,
    @SerialName("wp:term")
    val terms: List<List<TermDto>>? = null
)

@Serializable
data class AuthorDto(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("url")
    val url: String = "",
    @SerialName("description")
    val description: String = "",
    @SerialName("link")
    val link: String = "",
    @SerialName("avatar_urls")
    val avatarUrls: Map<String, String>? = null
)

@Serializable
data class FeaturedMediaDto(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("date")
    val date: String = "",
    @SerialName("slug")
    val slug: String = "",
    @SerialName("link")
    val link: String = "",
    @SerialName("title")
    val title: RenderedTextDto = RenderedTextDto(),
    @SerialName("source_url")
    val sourceUrl: String = "",
    @SerialName("alt_text")
    val altText: String = "",
    @SerialName("media_details")
    val mediaDetails: MediaDetailsDto? = null
)

@Serializable
data class MediaDetailsDto(
    @SerialName("width")
    val width: Int = 0,
    @SerialName("height")
    val height: Int = 0,
    @SerialName("file")
    val file: String = "",
    @SerialName("sizes")
    val sizes: Map<String, ImageSizeDto>? = null
)

@Serializable
data class ImageSizeDto(
    @SerialName("file")
    val file: String = "",
    @SerialName("width")
    val width: Int = 0,
    @SerialName("height")
    val height: Int = 0,
    @SerialName("mime_type")
    val mimeType: String = "",
    @SerialName("source_url")
    val sourceUrl: String = ""
)

@Serializable
data class TermDto(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("link")
    val link: String = "",
    @SerialName("name")
    val name: String = "",
    @SerialName("slug")
    val slug: String = "",
    @SerialName("taxonomy")
    val taxonomy: String = ""
)
