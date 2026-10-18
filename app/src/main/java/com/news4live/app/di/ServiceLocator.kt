package com.news4live.app.di

import android.content.Context
import com.news4live.app.analytics.AnalyticsService
import com.news4live.app.analytics.AppAnalytics
import com.news4live.app.data.api.ApiClient
import com.news4live.app.data.database.NewsDatabase
import com.news4live.app.data.repository.NewsRepositoryImpl
import com.news4live.app.domain.repository.NewsRepository
import com.news4live.app.domain.usecase.*
import com.news4live.app.util.TextSizeManager

class ServiceLocator(private val context: Context) {

    private val database by lazy {
        NewsDatabase.getInstance(context)
    }

    private val wpApiService by lazy {
        ApiClient.createWordPressService(context)
    }

    private val pluginApiService by lazy {
        ApiClient.createPluginService(context)
    }

    val newsRepository: NewsRepository by lazy {
        NewsRepositoryImpl(
            wpApi = wpApiService,
            pluginApi = pluginApiService,
            database = database
        )
    }

    val textSizeManager by lazy {
        TextSizeManager(context)
    }

    val analyticsService: AnalyticsService by lazy {
        AppAnalytics(context)
    }

    val hindiAudioNewsManager by lazy {
        com.news4live.app.audio.HindiAudioNewsManager(context.applicationContext)
    }

    // Use cases
    val getHomeFeedUseCase by lazy { GetHomeFeedUseCase(newsRepository) }
    val getPagedArticlesUseCase by lazy { GetPagedArticlesUseCase(newsRepository) }
    val getArticleDetailsUseCase by lazy { GetArticleDetailsUseCase(newsRepository) }
    val searchArticlesUseCase by lazy { SearchArticlesUseCase(newsRepository) }
    val bookmarkUseCases by lazy { BookmarkUseCases(newsRepository) }
    val getCategoriesUseCase by lazy { GetCategoriesUseCase(newsRepository) }
}
