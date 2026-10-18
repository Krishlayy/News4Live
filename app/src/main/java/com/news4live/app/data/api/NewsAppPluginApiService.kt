package com.news4live.app.data.api

import com.news4live.app.data.models.HomeFeedDto
import com.news4live.app.data.models.WordPressPostDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsAppPluginApiService {

    @GET("news4live/v1/home")
    suspend fun getHomeFeed(): Response<HomeFeedDto>

    @GET("news4live/v1/breaking")
    suspend fun getBreakingNews(
        @Query("limit") limit: Int = 5
    ): Response<List<WordPressPostDto>>

    @GET("news4live/v1/trending")
    suspend fun getTrendingNews(
        @Query("limit") limit: Int = 10
    ): Response<List<WordPressPostDto>>
}
