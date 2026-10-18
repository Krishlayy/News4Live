package com.news4live.app.data.models

import com.news4live.app.data.database.entities.ArticleEntity
import com.news4live.app.data.database.entities.BookmarkEntity
import com.news4live.app.data.database.entities.CategoryEntity
import com.news4live.app.domain.model.Article
import com.news4live.app.domain.model.Bookmark
import com.news4live.app.domain.model.Category
import com.news4live.app.util.DateUtils
import com.news4live.app.util.HtmlCompatHelper

object DtoMappers {

    fun WordPressPostDto.toDomain(categoryMap: Map<Long, String> = emptyMap(), isBookmarked: Boolean = false): Article {
        val cleanTitle = HtmlCompatHelper.decodeHtmlTitle(title.rendered)
        val cleanExcerptText = HtmlCompatHelper.cleanHtmlToPlainText(excerpt.rendered)
        val relativeDate = DateUtils.formatHindiDate(date)

        // Featured image extraction from _embedded
        var fullImageUrl: String? = null
        var thumbImageUrl: String? = null
        var mediumImageUrl: String? = null

        val mediaList = embedded?.featuredMedia
        if (!mediaList.isNullOrEmpty()) {
            val media = mediaList[0]
            fullImageUrl = media.sourceUrl.ifBlank { null }
            val sizes = media.mediaDetails?.sizes
            if (sizes != null) {
                thumbImageUrl = (sizes["thumbnail"] ?: sizes["news-portal-block-thumb"])?.sourceUrl
                mediumImageUrl = (sizes["news-portal-block-medium"] 
                    ?: sizes["news-portal-slider-medium"]
                    ?: sizes["medium"] 
                    ?: sizes["medium_large"])?.sourceUrl
            }
        }

        // Fallbacks
        if (mediumImageUrl == null) mediumImageUrl = fullImageUrl
        if (thumbImageUrl == null) thumbImageUrl = mediumImageUrl ?: fullImageUrl

        // Author Name
        val authorName = embedded?.author?.firstOrNull()?.name?.ifBlank { null } ?: "न्यूज़ 4 लाइव टीम"

        // Category Name
        val catName = if (categories.isNotEmpty()) {
            val primaryCatId = categories.first()
            categoryMap[primaryCatId] ?: embedded?.terms?.flatten()?.firstOrNull { it.id == primaryCatId }?.name ?: "समाचार"
        } else {
            "समाचार"
        }

        // Tags
        val tagNames = embedded?.terms?.flatten()?.filter { it.taxonomy == "post_tag" }?.map { it.name } ?: emptyList()

        // Breaking news check: category 31 is 'ब्रेकिंग न्यूज़' on news4live.com
        val isBreakingNews = categories.contains(31L)

        return Article(
            id = id,
            title = cleanTitle,
            contentHtml = content.rendered,
            excerptHtml = excerpt.rendered,
            cleanExcerpt = cleanExcerptText,
            date = date,
            formattedDate = relativeDate,
            slug = slug,
            canonicalUrl = link,
            featuredImageUrl = fullImageUrl,
            thumbnailImageUrl = thumbImageUrl,
            mediumImageUrl = mediumImageUrl,
            authorName = authorName,
            categoryIds = categories,
            categoryName = HtmlCompatHelper.decodeHtmlTitle(catName),
            tags = tagNames,
            isBreaking = isBreakingNews,
            isBookmarked = isBookmarked
        )
    }

    fun Article.toEntity(): ArticleEntity {
        return ArticleEntity(
            id = id,
            title = title,
            contentHtml = contentHtml,
            excerptHtml = excerptHtml,
            cleanExcerpt = cleanExcerpt,
            date = date,
            formattedDate = formattedDate,
            slug = slug,
            canonicalUrl = canonicalUrl,
            featuredImageUrl = featuredImageUrl,
            thumbnailImageUrl = thumbnailImageUrl,
            mediumImageUrl = mediumImageUrl,
            authorName = authorName,
            categoryIdsJson = categoryIds.joinToString(","),
            categoryName = categoryName,
            tagsJson = tags.joinToString(","),
            isBreaking = isBreaking
        )
    }

    fun ArticleEntity.toDomain(isBookmarked: Boolean = false): Article {
        val catIds = categoryIdsJson.split(",").mapNotNull { it.trim().toLongOrNull() }
        val tagList = if (tagsJson.isNotBlank()) tagsJson.split(",") else emptyList()

        return Article(
            id = id,
            title = title,
            contentHtml = contentHtml,
            excerptHtml = excerptHtml,
            cleanExcerpt = cleanExcerpt,
            date = date,
            formattedDate = formattedDate,
            slug = slug,
            canonicalUrl = canonicalUrl,
            featuredImageUrl = featuredImageUrl,
            thumbnailImageUrl = thumbnailImageUrl,
            mediumImageUrl = mediumImageUrl,
            authorName = authorName,
            categoryIds = catIds,
            categoryName = categoryName,
            tags = tagList,
            isBreaking = isBreaking,
            isBookmarked = isBookmarked
        )
    }

    fun Article.toBookmarkEntity(): BookmarkEntity {
        return BookmarkEntity(
            articleId = id,
            title = title,
            excerpt = cleanExcerpt,
            imageUrl = mediumImageUrl ?: featuredImageUrl,
            canonicalUrl = canonicalUrl,
            categoryName = categoryName,
            date = formattedDate
        )
    }

    fun BookmarkEntity.toDomain(): Bookmark {
        return Bookmark(
            articleId = articleId,
            title = title,
            excerpt = excerpt,
            imageUrl = imageUrl,
            canonicalUrl = canonicalUrl,
            categoryName = categoryName,
            savedAtTimestamp = savedAtTimestamp,
            formattedDate = date
        )
    }

    fun WordPressCategoryDto.toDomain(): Category {
        return Category(
            id = id,
            name = HtmlCompatHelper.decodeHtmlTitle(name),
            slug = slug,
            postCount = count,
            parentId = parent
        )
    }

    fun WordPressCategoryDto.toEntity(): CategoryEntity {
        return CategoryEntity(
            id = id,
            name = HtmlCompatHelper.decodeHtmlTitle(name),
            slug = slug,
            postCount = count,
            parentId = parent
        )
    }

    fun CategoryEntity.toDomain(): Category {
        return Category(
            id = id,
            name = name,
            slug = slug,
            postCount = postCount,
            parentId = parentId
        )
    }
}
