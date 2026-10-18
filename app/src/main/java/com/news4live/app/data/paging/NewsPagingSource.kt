package com.news4live.app.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.news4live.app.data.api.WordPressApiService
import com.news4live.app.data.database.dao.ArticleDao
import com.news4live.app.data.models.DtoMappers.toDomain
import com.news4live.app.data.models.DtoMappers.toEntity
import com.news4live.app.domain.model.Article
import java.io.IOException

class NewsPagingSource(
    private val apiService: WordPressApiService,
    private val articleDao: ArticleDao,
    private val categoryId: Long? = null,
    private val categoryMap: Map<Long, String> = emptyMap()
) : PagingSource<Int, Article>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val page = params.key ?: 1
        val perPage = params.loadSize.coerceAtMost(15)

        return try {
            val response = apiService.getPosts(
                page = page,
                perPage = perPage,
                categories = if (categoryId != null && categoryId > 0) categoryId.toString() else null,
                embed = 1,
                fields = WordPressApiService.FEED_FIELDS
            )

            if (response.isSuccessful) {
                val dtos = response.body() ?: emptyList()
                val articles = dtos.map { it.toDomain(categoryMap) }

                // Cache to Room for offline reading
                if (articles.isNotEmpty()) {
                    try {
                        articleDao.insertArticles(articles.map { it.toEntity() })
                        // Enforce cache size limits (prune older non-bookmarked cache)
                        articleDao.pruneExcessCache()
                    } catch (_: Exception) {}
                }

                val totalPagesHeader = response.headers()["X-WP-TotalPages"]?.toIntOrNull()
                val nextKey = if (articles.isEmpty() || (totalPagesHeader != null && page >= totalPagesHeader)) {
                    null
                } else {
                    page + 1
                }
                val prevKey = if (page == 1) null else page - 1

                LoadResult.Page(
                    data = articles,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            } else {
                // WordPress returns HTTP 400 with 'rest_post_invalid_page_number' when beyond total pages
                if (response.code() == 400) {
                    LoadResult.Page(
                        data = emptyList(),
                        prevKey = if (page == 1) null else page - 1,
                        nextKey = null
                    )
                } else {
                    LoadResult.Error(IOException("सर्वर त्रुटि: HTTP ${response.code()}"))
                }
            }
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
