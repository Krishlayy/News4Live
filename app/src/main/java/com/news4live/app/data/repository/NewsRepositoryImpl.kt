package com.news4live.app.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.news4live.app.data.api.NewsAppPluginApiService
import com.news4live.app.data.api.WordPressApiService
import com.news4live.app.data.database.NewsDatabase
import com.news4live.app.data.database.entities.RecentSearchEntity
import com.news4live.app.data.models.DtoMappers.toBookmarkEntity
import com.news4live.app.data.models.DtoMappers.toDomain
import com.news4live.app.data.models.DtoMappers.toEntity
import com.news4live.app.data.paging.NewsPagingSource
import com.news4live.app.data.paging.SearchPagingSource
import com.news4live.app.domain.model.Article
import com.news4live.app.domain.model.Bookmark
import com.news4live.app.domain.model.Category
import com.news4live.app.domain.model.HomeFeed
import com.news4live.app.domain.repository.NewsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException

class ArticleNotFoundException(message: String = "यह खबर अब उपलब्ध नहीं है") : Exception(message)

class NewsRepositoryImpl(
    private val wpApi: WordPressApiService,
    private val pluginApi: NewsAppPluginApiService,
    private val database: NewsDatabase
) : NewsRepository {

    private val articleDao = database.articleDao()
    private val bookmarkDao = database.bookmarkDao()
    private val categoryDao = database.categoryDao()
    private val searchDao = database.recentSearchDao()

    private var categoryMapCache = mutableMapOf<Long, String>()

    override suspend fun getCachedHomeFeed(): HomeFeed? {
        val cachedArticles = articleDao.getAllCachedArticles().firstOrNull()?.map { it.toDomain() } ?: emptyList()
        if (cachedArticles.isEmpty()) return null

        val cachedCategories = categoryDao.getAllCategories().firstOrNull()?.map { it.toDomain() } ?: emptyList()
        val breaking = cachedArticles.filter { it.isBreaking }.ifEmpty { cachedArticles.take(3) }
        val hero = cachedArticles.firstOrNull()
        val top = cachedArticles.drop(1).take(5)

        return HomeFeed(
            breakingNews = breaking,
            heroArticle = hero,
            topStories = top,
            latestArticles = cachedArticles,
            categories = cachedCategories,
            trendingArticles = top
        )
    }

    override suspend fun getHomeFeed(forceRefresh: Boolean): Result<HomeFeed> = coroutineScope {
        try {
            // Instant pre-seed of category map from DB or memory so posts never wait
            if (categoryMapCache.isEmpty()) {
                val dbCategories = categoryDao.getAllCategories().firstOrNull() ?: emptyList()
                dbCategories.forEach { categoryMapCache[it.id] = it.name }
            }

            // 1. Parallel Non-Blocking Dispatch: Categories + Latest + Breaking concurrently
            val categoriesDeferred = async { refreshCategories() }

            val latestDeferred = async {
                try {
                    val res = wpApi.getPosts(
                        page = 1,
                        perPage = 15,
                        embed = 1,
                        fields = WordPressApiService.FEED_FIELDS
                    )
                    if (res.isSuccessful) res.body() ?: emptyList() else emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            }

            val breakingDeferred = async {
                try {
                    val res = wpApi.getPosts(
                        page = 1,
                        perPage = 4,
                        categories = "31",
                        embed = 1,
                        fields = WordPressApiService.FEED_FIELDS
                    )
                    if (res.isSuccessful) res.body() ?: emptyList() else emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            }

            val rawLatest = latestDeferred.await()
            val rawBreaking = breakingDeferred.await()
            val categoriesResult = categoriesDeferred.await()

            val categories = categoriesResult.getOrElse {
                categoryDao.getAllCategories().firstOrNull()?.map { it.toDomain() } ?: emptyList()
            }
            val catMap = categories.associate { it.id to it.name }
            categoryMapCache.putAll(catMap)

            val latest = rawLatest.map { it.toDomain(categoryMapCache) }
            val breaking = rawBreaking.map { it.toDomain(categoryMapCache) }

            if (latest.isNotEmpty() || breaking.isNotEmpty()) {
                val allFetched = (breaking + latest).distinctBy { it.id }
                articleDao.insertArticles(allFetched.map { it.toEntity() })
                articleDao.pruneExcessCache()

                val hero = latest.firstOrNull()
                val topStories = latest.drop(1).take(5)

                val feed = HomeFeed(
                    breakingNews = breaking.ifEmpty { latest.filter { it.isBreaking }.take(4) },
                    heroArticle = hero,
                    topStories = topStories,
                    latestArticles = latest,
                    categories = categories,
                    trendingArticles = topStories
                )
                Result.success(feed)
            } else {
                val cached = getCachedHomeFeed()
                if (cached != null) Result.success(cached) else Result.failure(IOException("खबरें लोड करने में समस्या आई"))
            }
        } catch (e: Exception) {
            val cached = getCachedHomeFeed()
            if (cached != null) Result.success(cached) else Result.failure(e)
        }
    }

    override fun getPagedArticles(categoryId: Long?): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 15,
                prefetchDistance = 4,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                NewsPagingSource(
                    apiService = wpApi,
                    articleDao = articleDao,
                    categoryId = categoryId,
                    categoryMap = categoryMapCache
                )
            }
        ).flow
    }

    override suspend fun getCachedArticleById(id: Long): Article? {
        return articleDao.getArticleById(id)?.toDomain()
    }

    override suspend fun getArticleById(id: Long): Result<Article> {
        return try {
            val response = wpApi.getPostById(id = id, embed = 1)
            if (response.isSuccessful && response.body() != null) {
                val domain = response.body()!!.toDomain(categoryMapCache)
                articleDao.insertArticle(domain.toEntity())
                Result.success(domain)
            } else if (response.code() == 404) {
                // Post was deleted or unpublished from WordPress!
                // Purge from cache to prevent presenting stale deleted content
                articleDao.deleteArticle(id)
                Result.failure(ArticleNotFoundException())
            } else {
                val cached = articleDao.getArticleById(id)
                if (cached != null) {
                    Result.success(cached.toDomain())
                } else {
                    Result.failure(IOException("खबर लोड नहीं हो सकी (HTTP ${response.code()})"))
                }
            }
        } catch (e: Exception) {
            val cached = articleDao.getArticleById(id)
            if (cached != null) {
                Result.success(cached.toDomain())
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getArticleBySlug(slug: String): Result<Article> {
        return try {
            val response = wpApi.getPostBySlug(slug = slug, embed = 1)
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val domain = response.body()!!.first().toDomain(categoryMapCache)
                articleDao.insertArticle(domain.toEntity())
                Result.success(domain)
            } else {
                Result.failure(ArticleNotFoundException())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun refreshCategories(): Result<List<Category>> {
        return try {
            val response = wpApi.getCategories(perPage = 100, hideEmpty = true)
            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!
                val filtered = dtos.filter { it.id != 1L } // Exclude Uncategorized
                categoryDao.insertCategories(filtered.map { it.toEntity() })
                val domainList = filtered.map { it.toDomain() }
                domainList.forEach { categoryMapCache[it.id] = it.name }
                Result.success(domainList)
            } else {
                val cached = categoryDao.getAllCategories().firstOrNull()?.map { it.toDomain() } ?: emptyList()
                Result.success(cached)
            }
        } catch (e: Exception) {
            val cached = categoryDao.getAllCategories().firstOrNull()?.map { it.toDomain() } ?: emptyList()
            Result.success(cached)
        }
    }

    override fun searchArticles(query: String): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(pageSize = 15, enablePlaceholders = false),
            pagingSourceFactory = {
                SearchPagingSource(apiService = wpApi, query = query, categoryMap = categoryMapCache)
            }
        ).flow
    }

    override fun getBookmarks(): Flow<List<Bookmark>> {
        return bookmarkDao.getAllBookmarks().map { list -> list.map { it.toDomain() } }
    }

    override fun isBookmarked(articleId: Long): Flow<Boolean> {
        return bookmarkDao.isBookmarked(articleId)
    }

    override suspend fun toggleBookmark(article: Article): Boolean {
        val isCurrentlyBookmarked = bookmarkDao.isBookmarked(article.id).firstOrNull() ?: false
        return if (isCurrentlyBookmarked) {
            bookmarkDao.deleteBookmark(article.id)
            false
        } else {
            bookmarkDao.insertBookmark(article.toBookmarkEntity())
            articleDao.insertArticle(article.toEntity())
            true
        }
    }

    override fun getRecentSearches(): Flow<List<String>> {
        return searchDao.getRecentSearches().map { list -> list.map { it.query } }
    }

    override suspend fun saveSearchQuery(query: String) {
        if (query.isNotBlank()) {
            searchDao.insertSearch(RecentSearchEntity(query = query.trim()))
        }
    }

    override suspend fun deleteSearchQuery(query: String) {
        searchDao.deleteSearch(query)
    }

    override suspend fun clearSearchHistory() {
        searchDao.clearAll()
    }

    override fun getBreakingNews(): Flow<List<Article>> {
        return articleDao.getBreakingArticles().map { list -> list.map { it.toDomain() } }
    }
}
