package com.news4live.app.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.news4live.app.data.api.WordPressApiService
import com.news4live.app.data.models.DtoMappers.toDomain
import com.news4live.app.domain.model.Article
import java.io.IOException

class SearchPagingSource(
    private val apiService: WordPressApiService,
    private val query: String,
    private val categoryMap: Map<Long, String> = emptyMap()
) : PagingSource<Int, Article>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val page = params.key ?: 1
        val perPage = params.loadSize.coerceAtMost(15)

        if (query.isBlank() || query.length < 2) {
            return LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
        }

        return try {
            val response = apiService.getPosts(
                page = page,
                perPage = perPage,
                search = query,
                embed = 1
            )

            if (response.isSuccessful) {
                val dtos = response.body() ?: emptyList()
                val articles = dtos.map { it.toDomain(categoryMap) }

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
                if (response.code() == 400) {
                    LoadResult.Page(
                        data = emptyList(),
                        prevKey = if (page == 1) null else page - 1,
                        nextKey = null
                    )
                } else {
                    LoadResult.Error(IOException("खोज परिणाम लोड नहीं हो सके: HTTP ${response.code()}"))
                }
            }
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
