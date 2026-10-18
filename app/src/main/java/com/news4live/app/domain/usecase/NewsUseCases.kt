package com.news4live.app.domain.usecase

import androidx.paging.PagingData
import com.news4live.app.domain.model.Article
import com.news4live.app.domain.model.Bookmark
import com.news4live.app.domain.model.Category
import com.news4live.app.domain.model.HomeFeed
import com.news4live.app.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow

class GetHomeFeedUseCase(private val repository: NewsRepository) {
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<HomeFeed> {
        return repository.getHomeFeed(forceRefresh)
    }

    suspend fun getCached(): HomeFeed? {
        return repository.getCachedHomeFeed()
    }
}

class GetPagedArticlesUseCase(private val repository: NewsRepository) {
    operator fun invoke(categoryId: Long? = null): Flow<PagingData<Article>> {
        return repository.getPagedArticles(categoryId)
    }
}

class GetArticleDetailsUseCase(private val repository: NewsRepository) {
    suspend fun getById(id: Long): Result<Article> {
        return repository.getArticleById(id)
    }

    suspend fun getCached(id: Long): Article? {
        return repository.getCachedArticleById(id)
    }

    suspend fun getBySlug(slug: String): Result<Article> {
        return repository.getArticleBySlug(slug)
    }
}

class SearchArticlesUseCase(private val repository: NewsRepository) {
    operator fun invoke(query: String): Flow<PagingData<Article>> {
        return repository.searchArticles(query)
    }

    fun getRecentSearches(): Flow<List<String>> = repository.getRecentSearches()
    suspend fun saveQuery(query: String) = repository.saveSearchQuery(query)
    suspend fun deleteQuery(query: String) = repository.deleteSearchQuery(query)
    suspend fun clearHistory() = repository.clearSearchHistory()
}

class BookmarkUseCases(private val repository: NewsRepository) {
    fun getBookmarks(): Flow<List<Bookmark>> = repository.getBookmarks()
    fun isBookmarked(articleId: Long): Flow<Boolean> = repository.isBookmarked(articleId)
    suspend fun toggleBookmark(article: Article): Boolean = repository.toggleBookmark(article)
}

class GetCategoriesUseCase(private val repository: NewsRepository) {
    fun getCategories(): Flow<List<Category>> = repository.getCategories()
    suspend fun refresh(): Result<List<Category>> = repository.refreshCategories()
}
