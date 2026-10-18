package com.news4live.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.news4live.app.data.database.entities.ArticleEntity
import com.news4live.app.data.database.entities.BookmarkEntity
import com.news4live.app.data.database.entities.CategoryEntity
import com.news4live.app.data.database.entities.RecentSearchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY cachedAtTimestamp DESC")
    fun getAllCachedArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    suspend fun getArticleById(id: Long): ArticleEntity?

    @Query("SELECT * FROM articles WHERE isBreaking = 1 ORDER BY cachedAtTimestamp DESC LIMIT 10")
    fun getBreakingArticles(): Flow<List<ArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: ArticleEntity)

    @Query("DELETE FROM articles WHERE id = :id")
    suspend fun deleteArticle(id: Long)

    @Query("DELETE FROM articles WHERE id NOT IN (SELECT articleId FROM bookmarks) AND cachedAtTimestamp < :timestamp")
    suspend fun deleteOldCache(timestamp: Long)

    /**
     * Cache Eviction Policy: Keeps the top 100 most recently cached articles that are not bookmarked.
     * Deletes older articles to ensure Room database stays fast and lightweight.
     */
    @Query("""
        DELETE FROM articles 
        WHERE id NOT IN (SELECT articleId FROM bookmarks) 
        AND id NOT IN (
            SELECT id FROM articles 
            ORDER BY cachedAtTimestamp DESC 
            LIMIT 100
        )
    """)
    suspend fun pruneExcessCache()

    @Query("SELECT * FROM articles WHERE title LIKE '%' || :query || '%' OR cleanExcerpt LIKE '%' || :query || '%'")
    fun searchCachedArticles(query: String): Flow<List<ArticleEntity>>
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY savedAtTimestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE articleId = :articleId)")
    fun isBookmarked(articleId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE articleId = :articleId")
    suspend fun deleteBookmark(articleId: Long)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE postCount > 0 ORDER BY postCount DESC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories")
    suspend fun clearCategories()
}

@Dao
interface RecentSearchDao {
    @Query("SELECT * FROM recent_searches ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<RecentSearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: RecentSearchEntity)

    @Query("DELETE FROM recent_searches WHERE query = :query")
    suspend fun deleteSearch(query: String)

    @Query("DELETE FROM recent_searches")
    suspend fun clearAll()
}
