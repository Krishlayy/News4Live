package com.news4live.app.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WordPressCategoryDto(
    @SerialName("id")
    val id: Long,
    @SerialName("count")
    val count: Int = 0,
    @SerialName("description")
    val description: String = "",
    @SerialName("link")
    val link: String = "",
    @SerialName("name")
    val name: String = "",
    @SerialName("slug")
    val slug: String = "",
    @SerialName("taxonomy")
    val taxonomy: String = "category",
    @SerialName("parent")
    val parent: Long = 0
)

@Serializable
data class WordPressTagDto(
    @SerialName("id")
    val id: Long,
    @SerialName("count")
    val count: Int = 0,
    @SerialName("description")
    val description: String = "",
    @SerialName("link")
    val link: String = "",
    @SerialName("name")
    val name: String = "",
    @SerialName("slug")
    val slug: String = "",
    @SerialName("taxonomy")
    val taxonomy: String = "post_tag"
)

@Serializable
data class HomeFeedDto(
    @SerialName("breaking")
    val breaking: List<WordPressPostDto> = emptyList(),
    @SerialName("featured")
    val featured: List<WordPressPostDto> = emptyList(),
    @SerialName("latest")
    val latest: List<WordPressPostDto> = emptyList(),
    @SerialName("categories")
    val categories: List<WordPressCategoryDto> = emptyList(),
    @SerialName("trending")
    val trending: List<WordPressPostDto> = emptyList()
)
