package com.news4live.app.data.api

import com.news4live.app.data.models.WordPressCategoryDto
import com.news4live.app.data.models.WordPressPostDto
import com.news4live.app.data.models.WordPressTagDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WordPressApiService {

    companion object {
        const val FEED_FIELDS = "id,date,slug,title,excerpt,link,categories,_links,_embedded"
    }

    @GET("wp/v2/posts")
    suspend fun getPosts(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("categories") categories: String? = null,
        @Query("tags") tags: String? = null,
        @Query("search") search: String? = null,
        @Query("_embed") embed: Int = 1,
        @Query("_fields") fields: String? = null
    ): Response<List<WordPressPostDto>>

    @GET("wp/v2/posts/{id}")
    suspend fun getPostById(
        @Path("id") id: Long,
        @Query("_embed") embed: Int = 1
    ): Response<WordPressPostDto>

    @GET("wp/v2/posts")
    suspend fun getPostBySlug(
        @Query("slug") slug: String,
        @Query("_embed") embed: Int = 1
    ): Response<List<WordPressPostDto>>

    @GET("wp/v2/categories")
    suspend fun getCategories(
        @Query("per_page") perPage: Int = 100,
        @Query("hide_empty") hideEmpty: Boolean = true,
        @Query("orderby") orderby: String = "count",
        @Query("order") order: String = "desc"
    ): Response<List<WordPressCategoryDto>>

    @GET("wp/v2/tags")
    suspend fun getTags(
        @Query("per_page") perPage: Int = 100,
        @Query("hide_empty") hideEmpty: Boolean = true
    ): Response<List<WordPressTagDto>>
}
