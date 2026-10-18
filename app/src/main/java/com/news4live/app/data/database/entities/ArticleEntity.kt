package com.news4live.app.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "articles",
    indices = [
        Index("cachedAtTimestamp"),
        Index("isBreaking"),
        Index("categoryName")
    ]
)
data class ArticleEntity(
    @PrimaryKey
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
    val categoryIdsJson: String,
    val categoryName: String,
    val tagsJson: String,
    val isBreaking: Boolean,
    val cachedAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "bookmarks",
    indices = [
        Index("savedAtTimestamp")
    ]
)
data class BookmarkEntity(
    @PrimaryKey
    val articleId: Long,
    val title: String,
    val excerpt: String,
    val imageUrl: String?,
    val canonicalUrl: String,
    val categoryName: String,
    val date: String,
    val savedAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "categories",
    indices = [
        Index("postCount")
    ]
)
data class CategoryEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val slug: String,
    val postCount: Int,
    val parentId: Long,
    val cachedAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "recent_searches",
    indices = [
        Index("timestamp")
    ]
)
data class RecentSearchEntity(
    @PrimaryKey
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)
