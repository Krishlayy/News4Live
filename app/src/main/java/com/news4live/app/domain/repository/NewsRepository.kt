package com.news4live.app.domain.repository

import androidx.paging.PagingData
import com.news4live.app.domain.model.Article
import com.news4live.app.domain.model.Bookmark
import com.news4live.app.domain.model.Category
import com.news4live.app.domain.model.HomeFeed
import kotlinx.coroutines.flow.Flow

interface NewsRepository {

    suspend fun getHomeFeed(forceRefresh: Boolean = false): Result<HomeFeed>
    suspend fun getCachedHomeFeed(): HomeFeed?

    fun getPagedArticles(categoryId: Long? = null): Flow<PagingData<Article>>

    suspend fun getArticleById(id: Long): Result<Article>
    suspend fun getCachedArticleById(id: Long): Article?

    suspend fun getArticleBySlug(slug: String): Result<Article>

    fun getCategories(): Flow<List<Category>>

    suspend fun refreshCategories(): Result<List<Category>>

    fun searchArticles(query: String): Flow<PagingData<Article>>

    fun getBookmarks(): Flow<List<Bookmark>>

    fun isBookmarked(articleId: Long): Flow<Boolean>

    suspend fun toggleBookmark(article: Article): Boolean

    fun getRecentSearches(): Flow<List<String>>

    suspend fun saveSearchQuery(query: String)

    suspend fun deleteSearchQuery(query: String)

    suspend fun clearSearchHistory()

    fun getBreakingNews(): Flow<List<Article>>
}
